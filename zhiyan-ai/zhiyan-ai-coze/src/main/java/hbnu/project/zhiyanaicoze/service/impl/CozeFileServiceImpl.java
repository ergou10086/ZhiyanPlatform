package hbnu.project.zhiyanaicoze.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyanaicoze.client.KnowledgeServiceClient;
import hbnu.project.zhiyanaicoze.config.properties.CozeProperties;
import hbnu.project.zhiyanaicoze.exception.CozeApiException;
import hbnu.project.zhiyanaicoze.model.response.CozeFileDetailResponse;
import hbnu.project.zhiyanaicoze.model.response.CozeFileUploadResponse;
import hbnu.project.zhiyanaicoze.service.CozeFileService;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Coze 文件服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CozeFileServiceImpl implements CozeFileService {

    private final CozeProperties cozeProperties;
    private final WebClient cozeWebClient;
    private final KnowledgeServiceClient knowledgeServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * 上传单个文件到 Coze
     * 消息中无法直接使用本地文件，创建消息或对话前，需要先调用此接口上传本地文件到扣子。上传文件后，你可以在消息中通过指定 file_id 的方式在多模态内容中直接使用此文件。此接口上传的文件可用于发起对话等 API 中传入文件等多模态内容。使用方式可参考 object_string object 。
     *
     * @param file   文件
     * @param userId 用户ID
     * @return 上传响应
     */
    @Override
    public CozeFileUploadResponse uploadFile(MultipartFile file, Long userId){
        log.info("[Coze 文件上传] 开始上传文件: fileName={}, size={}, userId={}",
                file.getOriginalFilename(), file.getSize(), userId);

        try{
            // 构建 multipart 请求
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            }, MediaType.parseMediaType(file.getContentType()));

            // 发送上传请求 - 先获取原始 JSON 字符串
            String rawResponse = cozeWebClient.post()
                    .uri("/v1/files/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(json -> log.info("[Coze 文件上传] 原始响应 JSON: {}", json))
                    .doOnError(error -> log.error("[Coze 文件上传] 上传失败", error))
                    .block();

            // 手动解析 JSON
            CozeFileUploadResponse response = null;
            if (rawResponse != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    response = objectMapper.readValue(rawResponse, CozeFileUploadResponse.class);
                    log.info("[Coze 文件上传] 解析后响应: code={}, msg={}, data={}",
                            response.getCode(), response.getMsg(), response.getData());
                    if (response.getData() != null) {
                        log.info("[Coze 文件上传] 文件数据详情: fileId={}, fileName={}, fileSize={}",
                                response.getData().getFileId(), response.getData().getFileName(), response.getData().getFileSize());
                    }
                } catch (Exception e) {
                    log.error("[Coze 文件上传] JSON 解析失败", e);
                }
            }

            if (response == null || response.getCode() != 0) {
                throw new CozeApiException("文件上传失败: " + (response != null ? response.getMsg() : "响应为空"));
            }
            
            // 验证 fileId
            if (response.getData() == null || response.getData().getFileId() == null) {
                log.error("[Coze 文件上传] 响应中缺少 fileId: response={}", response);
                throw new CozeApiException("文件上传成功但未返回 fileId");
            }

            return response;
        }catch (ServiceException | CozeApiException | IOException e){
            log.error("[Coze 文件上传] 读取文件失败/文件上传失败", e);
            throw new ServiceException("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 批量上传文件到 Coze
     *
     * @param files  文件列表
     * @param userId 用户ID
     * @return 上传响应列表
     */
    @Override
    public List<CozeFileUploadResponse> uploadFiles(List<MultipartFile> files, Long userId) {
        log.info("[Coze 批量上传] 开始上传 {} 个文件, userId={}", files.size(), userId);

        List<CozeFileUploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                CozeFileUploadResponse response = uploadFile(file, userId);
                responses.add(response);
            } catch (Exception e) {
                log.error("[Coze 批量上传] 上传文件失败: {}", file.getOriginalFilename(), e);
                // 继续上传其他文件
            }
        }

        log.info("[Coze 批量上传] 成功上传 {}/{} 个文件", responses.size(), files.size());
        return responses;
    }

    /**
     * 从知识库上传文件到 Coze
     *
     * @param fileIds 知识库文件ID列表
     * @param userId  用户ID
     * @return Coze 文件ID列表
     */
    @Override
    public List<String> uploadKnowledgeFiles(List<Long> fileIds, Long userId) {
        log.info("[Coze 知识库文件上传] 开始上传 {} 个知识库文件, userId={}", fileIds.size(), userId);

        List<String> cozeFileIds = new ArrayList<>();

        for(Long fileId: fileIds){
            try{
                // 从知识库服务获取文件
                log.info("[Coze 知识库文件上传] 从知识库下载文件: fileId={}", fileId);
                byte[] fileBytes = knowledgeServiceClient.downloadFile(fileId);

                if (fileBytes == null || fileBytes.length == 0) {
                    log.warn("[Coze 知识库文件上传] 文件内容为空: fileId={}", fileId);
                    continue;
                }

                // 获取文件信息
                String fileName = knowledgeServiceClient.getFileName(fileId);
                String contentType = knowledgeServiceClient.getFileContentType(fileId);

                log.info("[Coze 知识库文件上传] 上传到 Coze: fileName={}, size={}", fileName, fileBytes.length);

                //构建 multipart 请求
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("file", new  ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                }, MediaType.parseMediaType(contentType));


                // 上传到 Coze
                CozeFileUploadResponse response = cozeWebClient.post()
                        .uri("/v1/files/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(builder.build()))
                        .retrieve()
                        .bodyToMono(CozeFileUploadResponse.class)
                        .doOnSuccess(res -> {
                            log.info("[Coze 知识库文件上传] 收到响应: code={}, msg={}, data={}",
                                    res.getCode(), res.getMsg(), res.getData());
                            if (res.getData() != null) {
                                log.info("[Coze 知识库文件上传] 文件数据详情: fileId={}, fileName={}, fileSize={}",
                                        res.getData().getFileId(), res.getData().getFileName(), res.getData().getFileSize());
                            }
                        })
                        .block();

                if (response != null && response.getCode() == 0 && response.getData() != null) {
                    String cozeFileId = response.getData().getFileId();
                    if (cozeFileId != null) {
                        cozeFileIds.add(cozeFileId);
                        log.info("[Coze 知识库文件上传] 上传成功: cozeFileId={}", cozeFileId);
                    } else {
                        log.warn("[Coze 知识库文件上传] 响应中缺少 fileId: knowledgeFileId={}, response={}", fileId, response);
                    }
                } else {
                    log.warn("[Coze 知识库文件上传] 上传失败或响应无效: knowledgeFileId={}, response={}", fileId, response);
                }
            }catch (ServiceException | CozeApiException e){
                log.error("[Coze 知识库文件上传] 上传失败: fileId={}", fileId, e);
                // 抛出异常不终止，继续处理其他文件
            }
        }
        log.info("[Coze 知识库文件上传] 成功上传 {}/{} 个文件", cozeFileIds.size(), fileIds.size());
        return cozeFileIds;
    }

    /**
     * 从知识库上传文件到 Coze（返回详细信息）
     *
     * @param fileIds 知识库文件ID列表
     * @param userId 用户ID
     * @return 上传响应列表（包含文件详细信息）
     */
    @Override
    public List<CozeFileUploadResponse> uploadKnowledgeFilesWithDetails(List<Long> fileIds, Long userId) {
        log.info("[Coze 知识库文件上传详情] 开始上传 {} 个知识库文件, userId={}", fileIds.size(), userId);

        List<CozeFileUploadResponse> responses = new ArrayList<>();

        for(Long fileId: fileIds){
            try{
                // 从知识库服务获取文件
                log.info("[Coze 知识库文件上传详情] 从知识库下载文件: fileId={}", fileId);
                byte[] fileBytes = knowledgeServiceClient.downloadFile(fileId);

                if (fileBytes == null || fileBytes.length == 0) {
                    log.warn("[Coze 知识库文件上传详情] 文件内容为空: fileId={}", fileId);
                    continue;
                }

                // 获取文件信息
                String fileName = knowledgeServiceClient.getFileName(fileId);
                String contentType = knowledgeServiceClient.getFileContentType(fileId);

                log.info("[Coze 知识库文件上传详情] 上传到 Coze: fileName={}, size={}", fileName, fileBytes.length);

                //构建 multipart 请求
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("file", new  ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return fileName;
                    }
                }, MediaType.parseMediaType(contentType));


                // 上传到 Coze
                String rawResponse = cozeWebClient.post()
                        .uri("/v1/files/upload")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .body(BodyInserters.fromMultipartData(builder.build()))
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                log.info("[Coze 知识库文件上传详情] 原始响应 JSON: {}", rawResponse);
                CozeFileUploadResponse response = objectMapper.readValue(rawResponse, CozeFileUploadResponse.class);

                if (response != null && response.getCode() == 0 && response.getData() != null) {
                    responses.add(response);
                    log.info("[Coze 知识库文件上传详情] 上传成功: cozeFileId={}", response.getData().getFileId());
                } else {
                    log.warn("[Coze 知识库文件上传详情] 上传失败或响应无效: knowledgeFileId={}, response={}", fileId, response);
                }
            }catch (ServiceException | CozeApiException | JsonProcessingException e){
                log.error("[Coze 知识库文件上传详情] 上传失败: fileId={}", fileId, e);
                // 抛出异常不终止，继续处理其他文件
            }
        }
        log.info("[Coze 知识库文件上传详情] 成功上传 {}/{} 个文件", responses.size(), fileIds.size());
        return responses;
    }

    /**
     * 查询文件详情
     *
     * @param fileId Coze 文件ID
     * @return 文件详情
     */
    @Override
    public CozeFileDetailResponse getFileDetail(String fileId) {
        log.info("[Coze 文件详情] 查询文件详情: fileId={}", fileId);
        try {
            CozeFileDetailResponse response = cozeWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/files/retrieve")
                            .queryParam("file_id", fileId)
                            .build())
                    .retrieve()
                    .bodyToMono(CozeFileDetailResponse.class)
                    .block();

            if (response == null || response.getCode() != 0) {
                throw new CozeApiException("查询文件详情失败: " + (response != null ? response.getMsg() : "响应为空"));
            }

            return response;

        } catch (Exception e) {
            log.error("[Coze 文件详情] 查询失败", e);
            throw new CozeApiException("查询文件详情失败: " + e.getMessage());
        }
    }
}
