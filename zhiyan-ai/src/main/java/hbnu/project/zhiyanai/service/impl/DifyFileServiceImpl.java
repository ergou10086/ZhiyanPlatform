package hbnu.project.zhiyanai.service.impl;

import hbnu.project.zhiyanai.client.KnowledgeServiceClient;
import hbnu.project.zhiyanai.config.properties.DifyProperties;
import hbnu.project.zhiyanai.exception.DifyApiException;
import hbnu.project.zhiyanai.model.dto.DifyFileUploadRequest;
import hbnu.project.zhiyanai.model.dto.FileContext;
import hbnu.project.zhiyanai.model.response.DifyFileUploadResponse;
import hbnu.project.zhiyanai.service.DifyFileService;
import hbnu.project.zhiyancommonbasic.domain.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        R<List<FileContext>> result = knowledgeServiceClient.getFilesByIds(fileIds);

        if (result == null || result.getData() == null || result.getCode() != 200) {
            log.error("[Dify 文件上传] 获取知识库文件失败");
            throw new DifyApiException("获取知识库文件失败");
        }

        List<FileContext> fileContexts = result.getData();
        List<String> difyFileIds = new ArrayList<>();

        for (FileContext fileContext : fileContexts) {
            try {
                // 如果文件有 URL，下载并上传到 Dify
                if (fileContext.getFileUrl() != null) {
                    String difyFileId = uploadFileFromUrl(fileContext.getFileUrl(), fileContext.getFileName(), userId);
                    difyFileIds.add(difyFileId);
                } else {
                    log.warn("[Dify 文件上传] 文件无 URL: fileId={}", fileContext.getFileId());
                }
            } catch (DifyApiException e) {
                log.error("[Dify 文件上传] 上传知识库文件失败: fileId={}", fileContext.getFileId(), e);
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

    /**
     * 从 URL 下载文件并上传到 Dify
     */
    private String uploadFileFromUrl(String fileUrl, String fileName, Long userId) {
        try {
            log.info("[Dify 文件上传] 从 URL 下载文件: url={}", fileUrl);

            // 下载文件
            URL url = new URL(fileUrl);
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
        }
    }
}

