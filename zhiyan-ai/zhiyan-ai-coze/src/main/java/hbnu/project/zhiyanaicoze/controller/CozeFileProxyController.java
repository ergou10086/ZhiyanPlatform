package hbnu.project.zhiyanaicoze.controller;

import hbnu.project.zhiyanaicoze.service.CozeFileProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Coze 文件代理控制器
 * 提供公开的文件访问URL，用于Coze插件访问
 *
 * @author ErgouTree
 */
@Slf4j
@RestController
@RequestMapping("/api/coze/files/proxy")
@RequiredArgsConstructor
public class CozeFileProxyController {

    private final CozeFileProxyService cozeFileProxyService;

    /**
     * 通过临时token访问文件
     * 生成的URL格式：http://your-domain/api/coze/files/proxy/{token}
     *
     * @param token 临时访问token
     * @return 文件内容
     */
    @GetMapping("/{token}")
    public ResponseEntity<Resource> getFile(@PathVariable String token) {
        log.info("[文件代理] 访问文件: token={}", token);
        
        try {
            // 从token获取文件信息
            CozeFileProxyService.FileInfo fileInfo = cozeFileProxyService.getFileByToken(token);
            
            if (fileInfo == null) {
                log.warn("[文件代理] Token无效或已过期: token={}", token);
                return ResponseEntity.notFound().build();
            }
            
            ByteArrayResource resource = new ByteArrayResource(fileInfo.getFileBytes());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + fileInfo.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .contentLength(fileInfo.getFileBytes().length)
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("[文件代理] 获取文件失败: token={}", token, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

