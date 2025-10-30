package hbnu.project.zhiyanaicoze.service.impl;

import hbnu.project.zhiyanaicoze.service.CozeFileService;
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

            // 发送上传请求
            CozeFileUploadResponse response = cozeWebClient.post()
                    .uri("/v1/files/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(CozeFileUploadResponse.class)
                    .doOnSuccess(res -> log.info("[Coze 文件上传] 上传成功: fileId={}",
                            res.getData() != null ? res.getData().getFileId() : "null"))
                    .doOnError(error -> log.error("[Coze 文件上传] 上传失败", error))
                    .block();

            if (response == null || response.getCode() != 0) {
                throw new CozeApiException("文件上传失败: " + (response != null ? response.getMsg() : "响应为空"));
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
                        .block();

                if (response != null && response.getCode() == 0 && response.getData() != null) {
                    cozeFileIds.add(response.getData().getFileId());
                    log.info("[Coze 知识库文件上传] 上传成功: cozeFileId={}", response.getData().getFileId());
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
