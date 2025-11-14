package hbnu.project.zhiyancommonoss.service;

import hbnu.project.zhiyancommonbasic.exception.FileException;
import hbnu.project.zhiyancommonbasic.exception.file.*;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonbasic.utils.file.FileTypeUtils;
import hbnu.project.zhiyancommonbasic.utils.file.FileUtils;
import hbnu.project.zhiyancommonoss.entity.FileUploadRequest;
import hbnu.project.zhiyancommonoss.enums.BucketType;
import hbnu.project.zhiyancommonoss.exception.OssException;
import hbnu.project.zhiyancommonoss.util.MinioUtils;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import hbnu.project.zhiyancommonoss.properties.MinioProperties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO核心服务
 * 提供文件上传、下载、删除等基础操作
 *
 * @author ErgouTree
 * @participate akoiv
 * @rewrite ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    @Autowired
    private final MinioClient minioClient;

    @Autowired
    private final MinioUtils minioUtils;

    @Autowired
    private final MinioProperties minioProperties;

//    // 注入 S3Client Bean
//    @Autowired
//    private final S3Client s3Client;

    /**
     * 构建兼容 MinIO 的 S3Client（用于分片上传相关API）
     */
    private S3Client buildS3Client() {
        String endpoint = minioProperties.getEndpoint();
        String accessKey = minioProperties.getAccessKey();
        String secretKey = minioProperties.getSecretKey();

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                // MinIO 不强制 Region，但 AWS SDK 需要一个值，使用常见的 us-east-1
                .region(Region.US_EAST_1)
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    /**
     * 检查桶是否存在
     */
    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        try{
            return minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
        }catch (OssException e) {
            log.error("检查桶是否存在失败: {}", bucketName, e);
            throw new OssException("检查存储桶失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建桶
     */
    @SneakyThrows
    public void createBucket(String bucketName) {
        try{
            if(!bucketExists(bucketName)){
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建桶成功: {}", bucketName);
            }
        }catch (OssException e) {
            log.error("创建桶失败: {}", bucketName, e);
            throw new OssException("创建存储桶失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取所有桶
     */
    @SneakyThrows
    public List<String> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<String> bucketNames = new ArrayList<>();
            for (Bucket bucket : buckets) {
                bucketNames.add(bucket.name());
            }
            return bucketNames;
        } catch (OssException e) {
            log.error("获取桶列表失败", e);
            throw new OssException("获取存储桶列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件
     *
     * @param file       文件
     * @param bucketType 桶类型
     * @param objectKey  对象键（路径）
     * @return 上传结果
     */
    @SneakyThrows
    public FileUploadRequest uploadFile(MultipartFile file, BucketType bucketType, String objectKey) {
        String bucketName = minioUtils.getBucketName(bucketType);

        try {
            // 确保桶存在
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }

            // 获取文件信息
            String originalFilename = file.getOriginalFilename();
            String fileExtension = FileTypeUtils.getExtension(file);
            long fileSize = file.getSize();

            // 检查文件大小
            minioUtils.validateFile(file, fileExtension, fileSize);

            // 获取文件内容类型
            String contentType = file.getContentType();
            if (StringUtils.isEmpty(contentType)) {
                contentType = "application/octet-stream";
            }

            // 上传文件
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .stream(inputStream, fileSize, -1)
                        .contentType(contentType)
                        .build();

                ObjectWriteResponse response = minioClient.putObject(putObjectArgs);

                // 构建访问URL
                String fileUrl = minioUtils.getFileUrl(bucketName, objectKey);

                log.info("文件上传成功: bucket={}, object={}, size={}", bucketName, objectKey, fileSize);

                return FileUploadRequest.builder()
                        .url(fileUrl)
                        .filename(originalFilename)
                        .eTag(response.etag())
                        .build();
            }
        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, object={}", bucketName, objectKey, e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }


    /**
     * 上传字节数组
     *
     * @param bytes      字节数组
     * @param bucketType 桶类型
     * @param objectKey  对象键
     * @param contentType 内容类型
     * @return 上传结果
     */
    @SneakyThrows
    public FileUploadRequest uploadBytes(byte[] bytes, BucketType bucketType, String objectKey, String contentType) {
        String bucketName = minioUtils.getBucketName(bucketType);

        try {
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }

            if (StringUtils.isEmpty(contentType)) {
                contentType = "application/octet-stream";
            }

            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectKey)
                        .stream(inputStream, bytes.length, -1)
                        .contentType(contentType)
                        .build();

                ObjectWriteResponse response = minioClient.putObject(putObjectArgs);
                String fileUrl = minioUtils.getFileUrl(bucketName, objectKey);

                log.info("字节数组上传成功: bucket={}, object={}, size={}", bucketName, objectKey, bytes.length);

                return FileUploadRequest.builder()
                        .url(fileUrl)
                        .filename(FileUtils.getName(objectKey))
                        .eTag(response.etag())
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error("字节数组上传失败: bucket={}, object={}", bucketName, objectKey, e);
            throw new FileUploadException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     *
     * @param bucketType 桶类型
     * @param objectKey  对象键
     * @return 文件流
     */
    @SneakyThrows
    public InputStream downloadFile(BucketType bucketType, String objectKey) {
        String bucketName = minioUtils.getBucketName(bucketType);

        try{
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            InputStream stream = minioClient.getObject(getObjectArgs);
            log.info("文件下载成功: bucket={}, object={}", bucketName, objectKey);
            return stream;
        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, object={}", bucketName, objectKey, e);
            throw new FileDownloadException("文件下载失败: " + e.getMessage(), e);
        }
    }


    /**
     * 获取文件预签名URL（用于临时访问）
     *
     * @param bucketType 桶类型
     * @param objectKey  对象键
     * @param expiry     过期时间（秒）
     * @return 预签名URL
     */
    @SneakyThrows
    public String getPresignedUrl(BucketType bucketType, String objectKey, int expiry) {
        String bucketName = minioUtils.getBucketName(bucketType);

        try{
            // 提取文件名（从objectKey中获取最后一部分）
            String fileName = objectKey;
            if (objectKey.contains("/")) {
                fileName = objectKey.substring(objectKey.lastIndexOf("/") + 1);
            }
            
            // 对文件名进行URL编码，支持中文文件名
            String encodedFileName;
            try {
                encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            } catch (java.io.UnsupportedEncodingException e) {
                encodedFileName = fileName;
            }
            
            // 构建响应头参数，强制浏览器下载文件而不是显示
            java.util.Map<String, String> extraQueryParams = new java.util.HashMap<>();
            // 使用RFC 5987格式，同时支持ASCII和UTF-8编码的文件名
            extraQueryParams.put("response-content-disposition", 
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);
            
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .extraQueryParams(extraQueryParams)  // 添加响应头参数
                    .build();

            String url = minioClient.getPresignedObjectUrl(args);
            
            // 详细日志：输出生成的URL和参数
            log.info("✅ [下载URL] 生成成功");
            log.info("✅ [下载URL] bucket: {}", bucketName);
            log.info("✅ [下载URL] objectKey: {}", objectKey);
            log.info("✅ [下载URL] fileName: {}", fileName);
            log.info("✅ [下载URL] encodedFileName: {}", encodedFileName);
            log.info("✅ [下载URL] extraQueryParams: {}", extraQueryParams);
            log.info("✅ [下载URL] 生成的URL: {}", url);
            log.info("✅ [下载URL] URL是否包含response-content-disposition: {}", url.contains("response-content-disposition"));
            
            return url;
        }catch (Exception e) {
            log.error("生成预签名URL失败: bucket={}, object={}", bucketName, objectKey, e);
            throw new FileDownloadException("生成访问链接失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     *
     * @param bucketType 桶类型
     * @param objectKey  对象键
     */
    @SneakyThrows
    public void deleteFile(BucketType bucketType, String objectKey) {
        String bucketName = minioUtils.getBucketName(bucketType);

        try{
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            minioClient.removeObject(removeObjectArgs);
            log.info("文件删除成功: bucket={}, object={}", bucketName, objectKey);
        }catch (Exception e) {
            log.error("文件删除失败: bucket={}, object={}", bucketName, objectKey, e);
            throw new FileDeleteException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量删除文件
     *
     * @param bucketType 桶类型
     * @param objectKeys 对象键列表
     */
    public void deleteFiles(BucketType bucketType, List<String> objectKeys) {
        if (StringUtils.isEmpty(objectKeys)) {
            return;
        }

        String bucketName = minioUtils.getBucketName(bucketType);

        for (String objectKey : objectKeys) {
            try {
                deleteFile(bucketType, objectKey);
            } catch (Exception e) {
                log.error("批量删除文件时发生错误: bucket={}, object={}", bucketName, objectKey, e);
            }
        }

        log.info("批量删除文件完成: bucket={}, count={}", bucketName, objectKeys.size());
    }


    /**
     * 检查文件是否存在
     *
     * @param bucketType 桶类型
     * @param objectKey  对象键
     * @return 是否存在
     */
    public boolean fileExists(BucketType bucketType, String objectKey) {
        String bucketName = minioUtils.getBucketName(bucketType);

        try {
            StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build();

            minioClient.statObject(statObjectArgs);
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 列出指定前缀的所有文件
     *
     * @param bucketType 桶类型
     * @param prefix     前缀
     * @return 文件列表
     */
    public List<String> listFiles(BucketType bucketType, String prefix) {
        String bucketName = minioUtils.getBucketName(bucketType);
        List<String> fileList = new ArrayList<>();

        try {
            ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .recursive(true)
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);

            for (Result<Item> result : results) {
                Item item = result.get();
                fileList.add(item.objectName());
            }

            log.debug("列出文件成功: bucket={}, prefix={}, count={}", bucketName, prefix, fileList.size());
            return fileList;

        } catch (Exception e) {
            log.error("列出文件失败: bucket={}, prefix={}", bucketName, prefix, e);
            throw new FileException("获取文件列表失败: " + e.getMessage(), e);
        }
    }


    /**
     * 桶之间迁移复制文件
     *
     * @param sourceBucketType 源桶类型
     * @param sourceObjectKey  源对象键
     * @param targetBucketType 目标桶类型
     * @param targetObjectKey  目标对象键
     */
    public void copyFile(BucketType sourceBucketType, String sourceObjectKey,
                         BucketType targetBucketType, String targetObjectKey) {
        String sourceBucketName = minioUtils.getBucketName(sourceBucketType);
        String targetBucketName = minioUtils.getBucketName(targetBucketType);

        try {
            CopySource copySource = CopySource.builder()
                    .bucket(sourceBucketName)
                    .object(sourceObjectKey)
                    .build();

            CopyObjectArgs copyObjectArgs = CopyObjectArgs.builder()
                    .bucket(targetBucketName)
                    .object(targetObjectKey)
                    .source(copySource)
                    .build();

            minioClient.copyObject(copyObjectArgs);
            log.info("文件复制成功: from {}/{} to {}/{}",
                    sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);

        } catch (Exception e) {
            log.error("文件复制失败", e);
            throw new FileException("文件复制失败: " + e.getMessage(), e);
        }
    }

    /**
     * 初始化分片上传
     *
     * @param bucketName 桶名称
     * @param objectKey 对象键
     * @return uploadId
     */
    public String initiateMultipartUpload(String bucketName, String objectKey) {
        try{
            if(!bucketExists(bucketName)){
                createBucket(bucketName);
            }
            // 使用 try-with-resources 自动关闭 S3Client
            try (S3Client s3 = buildS3Client()) {
                CreateMultipartUploadResponse resp = s3.createMultipartUpload(
                        CreateMultipartUploadRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .build()
                );
                String uploadId = resp.uploadId();
                log.info("初始化分片上传成功: bucket={}, object={}, uploadId={}", bucketName, objectKey, uploadId);
                return uploadId;
            }
        }catch (FileException e) {
            log.error("初始化分片上传失败: bucket={}, object={}", bucketName, objectKey, e);
            throw new OssException("初始化分片上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 上传分片
     *
     * @param bucketName 桶名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     * @param partNumber 分片号(从1开始)
     * @param data 分片数据
     * @param size 分片大小
     * @return 分片ETag
     */
    public String uploadPart(String bucketName, String objectKey, String uploadId, int partNumber, InputStream data, long size) {
        try{
            try (S3Client s3 = buildS3Client()) {
                UploadPartResponse resp = s3.uploadPart(
                        UploadPartRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .uploadId(uploadId)
                                .partNumber(partNumber)
                                .contentLength(size)
                                .build(),
                        software.amazon.awssdk.core.sync.RequestBody.fromInputStream(data, size)
                );
                String etag = resp.eTag();
                log.info("上传分片成功: bucket={}, object={}, uploadId={}, partNumber={}, etag={}",
                        bucketName, objectKey, uploadId, partNumber, etag);

                return etag;
            }
        }catch (FileException e) {
            log.error("上传分片失败: bucket={}, object={}, uploadId={}, partNumber={}",
                    bucketName, objectKey, uploadId, partNumber, e);
            throw new OssException("上传分片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 完成分片上传
     */
    public String completeMultipartUpload(String bucketName, String objectKey, String uploadId, io.minio.messages.Part[] parts) {
        try{
            try (S3Client s3 = buildS3Client()) {
                List<CompletedPart> completed = new ArrayList<>();
                if (parts != null) {
                    for (io.minio.messages.Part p : parts) {
                        completed.add(CompletedPart.builder()
                                .partNumber(p.partNumber())
                                .eTag(p.etag())
                                .build());
                    }
                } else {
                    // 若未提供parts，则自动列举已上传分片
                    ListPartsResponse listResp = s3.listParts(
                            ListPartsRequest.builder()
                                    .bucket(bucketName)
                                    .key(objectKey)
                                    .uploadId(uploadId)
                                    .build()
                    );
                    listResp.parts().forEach(ap -> completed.add(
                            CompletedPart.builder().partNumber(ap.partNumber()).eTag(ap.eTag()).build()
                    ));
                }

                CompletedMultipartUpload cmp = CompletedMultipartUpload.builder()
                        .parts(completed)
                        .build();
                var resp = s3.completeMultipartUpload(
                        CompleteMultipartUploadRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .uploadId(uploadId)
                                .multipartUpload(cmp)
                                .build()
                );
                String etag = resp.eTag();
                log.info("完成分片上传: bucket={}, object={}, uploadId={}, etag={}", bucketName, objectKey, uploadId, etag);
                return etag;
            }
        }catch (FileException e) {
            log.error("完成分片上传失败: bucket={}, object={}, uploadId={}",
                    bucketName, objectKey, uploadId, e);
            throw new OssException("完成分片上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 自动列举分片并完成上传（简化上层调用）
     */
    public String completeMultipartUploadAuto(String bucketName, String objectKey, String uploadId) {
        return completeMultipartUpload(bucketName, objectKey, uploadId, null);
    }

    /**
     * 取消分片上传
     *
     * @param bucketName 桶名称
     * @param objectKey 对象键
     * @param uploadId 上传ID
     */
    public void abortMultipartUpload(String bucketName, String objectKey, String uploadId) {
        try {
            try(S3Client s3 = buildS3Client()) {
                s3.abortMultipartUpload(
                        AbortMultipartUploadRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .uploadId(uploadId)
                                .build()
                );

                log.info("取消分片上传成功: bucket={}, object={}, uploadId={}",
                        bucketName, objectKey, uploadId);
            }
        } catch (Exception e) {
            log.error("取消分片上传失败: bucket={}, object={}, uploadId={}",
                    bucketName, objectKey, uploadId, e);
            throw new OssException("取消分片上传失败: " + e.getMessage(), e);
        }
    }


    /**
     * 列出已上传的分片（返回 AWS SDK 的 ListPartsResponse）
     */
    public ListPartsResponse listParts(String bucketName, String objectKey, String uploadId) {
        try {
            S3Client s3 = buildS3Client();
            return s3.listParts(
                    ListPartsRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .uploadId(uploadId)
                            .build()
            );
        } catch (Exception e) {
            log.error("列出分片失败: bucket={}, object={}, uploadId={}",
                    bucketName, objectKey, uploadId, e);
            throw new OssException("列出分片失败: " + e.getMessage(), e);
        }
    }
}
