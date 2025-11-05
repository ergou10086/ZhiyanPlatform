package hbnu.project.zhiyanaidify.service.impl;

import hbnu.project.zhiyanaidify.client.DifyApiClient;
import hbnu.project.zhiyanaidify.client.KnowledgeServiceClient;
import hbnu.project.zhiyanaidify.config.properties.DifyProperties;
import hbnu.project.zhiyanaidify.exception.DifyApiException;
import hbnu.project.zhiyanaidify.model.dto.FileContextDTO;
import hbnu.project.zhiyanaidify.model.response.DifyFileUploadResponse;
import hbnu.project.zhiyanaidify.service.DifyFileService;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Dify 文件服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyFileServiceImpl implements DifyFileService {

    private final DifyProperties difyProperties;
    private final KnowledgeServiceClient knowledgeServiceClient;
    private final RestTemplate restTemplate;
    private final DifyApiClient difyApiClient;

    @Override
    public DifyFileUploadResponse uploadFile(MultipartFile file, Long userId) {
        try {
            log.info("[Dify 文件上传] 开始上传文件: fileName={}, size={}, userId={}",
                    file.getOriginalFilename(), file.getSize(), userId);

            // 构建请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
            body.add("user", String.valueOf(userId));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(difyProperties.getApiKey());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求
            String uploadUrl = difyProperties.getApiUrl() + "/files/upload";
            ResponseEntity<DifyFileUploadResponse> response = restTemplate.postForEntity(
                    uploadUrl,
                    requestEntity,
                    DifyFileUploadResponse.class
            );

            DifyFileUploadResponse uploadResponse = response.getBody();
            if (uploadResponse != null) {
                log.info("[Dify 文件上传] 上传成功: fileId={}", uploadResponse.getFileId());
                return uploadResponse;
            } else {
                throw new DifyApiException("文件上传失败：响应为空");
            }

        } catch (IOException e) {
            log.error("[Dify 文件上传] 文件读取失败", e);
            throw new DifyApiException("文件读取失败: " + e.getMessage());
        } catch (DifyApiException e) {
            log.error("[Dify 文件上传] 上传失败", e);
            throw new DifyApiException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public List<DifyFileUploadResponse> uploadFiles(List<MultipartFile> files, Long userId) {
        List<DifyFileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                DifyFileUploadResponse response = uploadFile(file, userId);
                responses.add(response);
            } catch (DifyApiException e) {
                log.error("[Dify 批量上传] 文件上传失败: fileName={}", file.getOriginalFilename(), e);
                // 继续上传其他文件
            }
        }

        log.info("[Dify 批量上传] 完成: 成功={}, 总数={}", responses.size(), files.size());
        return responses;
    }

    @Override
    public List<String> uploadKnowledgeFiles(List<Long> fileIds, Long userId) {
        log.info("[Dify 文件上传] 从知识库获取文件: fileIds={}", fileIds);

        // 从知识库服务获取文件信息
        R<List<FileContextDTO>> result = knowledgeServiceClient.getFilesByIds(fileIds);

        if (result == null || result.getData() == null || result.getCode() != 200) {
            log.error("[Dify 文件上传] 获取知识库文件失败");
            throw new DifyApiException("获取知识库文件失败");
        }

        List<FileContextDTO> fileContextDTOS = result.getData();
        List<String> difyFileIds = new ArrayList<>();

        for (FileContextDTO fileContextDTO : fileContextDTOS) {
            try {
                // 如果文件有 URL，下载并上传到 Dify
                if (fileContextDTO.getFileUrl() != null) {
                    String difyFileId = uploadFileFromUrl(fileContextDTO.getFileUrl(), fileContextDTO.getFileName(), userId);
                    difyFileIds.add(difyFileId);
                } else {
                    log.warn("[Dify 文件上传] 文件无 URL: fileId={}", fileContextDTO.getFileId());
                }
            } catch (DifyApiException e) {
                log.error("[Dify 文件上传] 上传知识库文件失败: fileId={}", fileContextDTO.getFileId(), e);
            }
        }

        log.info("[Dify 文件上传] 知识库文件上传完成: 成功={}, 总数={}", difyFileIds.size(), fileIds.size());
        return difyFileIds;
    }

    @Override
    public void deleteFile(String fileId, Long userId) {
        try {
            log.info("[Dify 文件删除] 删除文件: fileId={}, userId={}", fileId, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(difyProperties.getApiKey());

            String deleteUrl = difyProperties.getApiUrl() + "/files/" + fileId;
            restTemplate.delete(deleteUrl);

            log.info("[Dify 文件删除] 删除成功: fileId={}", fileId);

        } catch (DifyApiException e) {
            log.error("[Dify 文件删除] 删除失败: fileId={}", fileId, e);
            throw new DifyApiException("删除文件失败: " + e.getMessage());
        }
    }


    @Override
    public Resource previewFile(String fileId, Long userId) {
        try{
            String apiKey = difyProperties.getApiKey();

            log.info("[预览文件] fileId={}, userId={}", fileId, userId);

            ResponseEntity<byte[]> response = difyApiClient.previewFile(apiKey, fileId);

            if(response.getStatusCode().is2xxSuccessful()){
                byte[] fileBytes = response.getBody();
                log.info("[预览文件] 成功获取文件，大小={}bytes", Objects.requireNonNull(fileBytes).length);

                // 转换为 Resource
                return new ByteArrayResource(fileBytes);
            }else{
                throw new DifyApiException("文件预览失败：未能获取文件内容");
            }
        }catch (DifyApiException | ServiceException e){
            log.error("[预览文件] 失败: fileId={}, error={}", fileId, e.getMessage(), e);
            throw new DifyApiException("文件预览失败: " + e.getMessage(), e);
        }
    }


    @Override
    public byte[] getFileBytes(String fileId, Long userId) {
        try{
            String apiKey = "Bearer " + difyProperties.getApiKey();

            log.info("[获取文件字节] fileId={}, userId={}", fileId, userId);

            ResponseEntity<byte[]> response = difyApiClient.previewFile(apiKey, fileId);

            if(response.getStatusCode().is2xxSuccessful()){
                byte[] fileBytes = response.getBody();
                log.info("[获取文件字节] 成功，大小={}bytes", Objects.requireNonNull(fileBytes).length);
                return fileBytes;
            }else {
                throw new DifyApiException("获取文件失败：未能获取文件内容");
            }
        }catch (DifyApiException | ServiceException e){
            log.error("[获取文件字节] 失败: fileId={}, error={}", fileId, e.getMessage(), e);
            throw new DifyApiException("获取文件失败: " + e.getMessage(), e);
        }
    }


    /**
     * 从 URL 下载文件并上传到 Dify
     */
    private String uploadFileFromUrl(String fileUrl, String fileName, Long userId) {
        try {
            log.info("[Dify 文件上传] 从 URL 下载文件: url={}", fileUrl);

            // 下载文件
            URI uri = new URI(fileUrl);
            URL url = uri.toURL();
            InputStream inputStream = url.openStream();
            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();

            // 构建请求
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            body.add("user", String.valueOf(userId));

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(difyProperties.getApiKey());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 发送请求
            String uploadUrl = difyProperties.getApiUrl() + "/files/upload";
            ResponseEntity<DifyFileUploadResponse> response = restTemplate.postForEntity(
                    uploadUrl,
                    requestEntity,
                    DifyFileUploadResponse.class
            );

            DifyFileUploadResponse uploadResponse = response.getBody();
            if (uploadResponse != null && uploadResponse.getFileId() != null) {
                log.info("[Dify 文件上传] 从 URL 上传成功: fileId={}", uploadResponse.getFileId());
                return uploadResponse.getFileId();
            } else {
                throw new DifyApiException("从 URL 上传文件失败：响应为空");
            }

        } catch (DifyApiException | IOException e) {
            log.error("[Dify 文件上传] 从 URL 上传失败: url={}", fileUrl, e);
            throw new DifyApiException("从 URL 上传文件失败: " + e.getMessage());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}

