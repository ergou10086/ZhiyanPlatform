package hbnu.project.zhiyanaicoze.service.impl;

import hbnu.project.zhiyanaicoze.client.KnowledgeServiceClient;
import hbnu.project.zhiyanaicoze.service.CozeFileProxyService;
import hbnu.project.zhiyancommonbasic.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Coze 文件代理服务实现
 *
 * @author ErgouTree
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CozeFileProxyServiceImpl implements CozeFileProxyService {

    private final KnowledgeServiceClient knowledgeServiceClient;
    
    @Value("${server.port:8094}")
    private String serverPort;
    
    @Value("${server.address:localhost}")
    private String serverAddress;

    /**
     * 文件缓存：token -> FileInfo
     * 使用ConcurrentHashMap存储临时文件信息
     */
    private final Map<String, FileCacheEntry> fileCache = new ConcurrentHashMap<>();

    /**
     * 定时清理过期文件
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    {
        // 每分钟清理一次过期文件
        scheduler.scheduleAtFixedRate(this::cleanExpiredFiles, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public String generateFileUrl(MultipartFile file, Long userId) {
        try {
            String token = UUID.randomUUID().toString().replace("-", "");
            byte[] fileBytes = file.getBytes();
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();

            FileInfo fileInfo = new FileInfo(fileBytes, fileName, contentType);
            
            // 缓存文件信息（30分钟过期）
            long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
            fileCache.put(token, new FileCacheEntry(fileInfo, expireTime));

            String url = buildFileUrl(token);
            log.info("[文件代理] 生成文件URL: fileName={}, token={}, url={}", fileName, token, url);
            
            return url;
        } catch (IOException e) {
            log.error("[文件代理] 读取文件失败", e);
            throw new ServiceException("文件读取失败");
        }
    }

    @Override
    public String generateKnowledgeFileUrl(Long fileId, Long userId) {
        try {
            String token = UUID.randomUUID().toString().replace("-", "");
            
            // 从知识库获取文件
            byte[] fileBytes = knowledgeServiceClient.downloadFile(fileId);
            String fileName = knowledgeServiceClient.getFileName(fileId);
            String contentType = knowledgeServiceClient.getFileContentType(fileId);

            if (fileBytes == null || fileBytes.length == 0) {
                throw new ServiceException("知识库文件不存在或为空");
            }

            FileInfo fileInfo = new FileInfo(fileBytes, fileName, contentType);
            
            // 缓存文件信息（30分钟过期）
            long expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
            fileCache.put(token, new FileCacheEntry(fileInfo, expireTime));

            String url = buildFileUrl(token);
            log.info("[文件代理] 生成知识库文件URL: fileId={}, fileName={}, token={}, url={}", 
                    fileId, fileName, token, url);
            
            return url;
        } catch (Exception e) {
            log.error("[文件代理] 获取知识库文件失败: fileId={}", fileId, e);
            throw new ServiceException("获取知识库文件失败");
        }
    }

    @Override
    public List<String> generateFileUrls(List<MultipartFile> files, Long userId) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(generateFileUrl(file, userId));
        }
        return urls;
    }

    @Override
    public List<String> generateKnowledgeFileUrls(List<Long> fileIds, Long userId) {
        List<String> urls = new ArrayList<>();
        for (Long fileId : fileIds) {
            urls.add(generateKnowledgeFileUrl(fileId, userId));
        }
        return urls;
    }

    @Override
    public FileInfo getFileByToken(String token) {
        FileCacheEntry entry = fileCache.get(token);
        if (entry == null) {
            log.warn("[文件代理] Token不存在: token={}", token);
            return null;
        }

        // 检查是否过期
        if (System.currentTimeMillis() > entry.getExpireTime()) {
            log.warn("[文件代理] Token已过期: token={}", token);
            fileCache.remove(token);
            return null;
        }

        return entry.getFileInfo();
    }

    /**
     * 构建文件访问URL
     *
     * @param token 访问token
     * @return 完整的文件访问URL
     */
    private String buildFileUrl(String token) {
        // 生成公开访问URL
        // 格式：http://localhost:8094/api/coze/files/proxy/{token}
        return String.format("http://%s:%s/api/coze/files/proxy/%s", 
                serverAddress, serverPort, token);
    }

    /**
     * 清理过期文件
     */
    private void cleanExpiredFiles() {
        long now = System.currentTimeMillis();
        int removedCount = 0;
        
        for (Map.Entry<String, FileCacheEntry> entry : fileCache.entrySet()) {
            if (now > entry.getValue().getExpireTime()) {
                fileCache.remove(entry.getKey());
                removedCount++;
            }
        }
        
        if (removedCount > 0) {
            log.info("[文件代理] 清理了 {} 个过期文件", removedCount);
        }
    }

    /**
     * 文件缓存项
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class FileCacheEntry {
        private FileInfo fileInfo;
        private long expireTime;
    }
}

