package hbnu.project.zhiyancommonsse.controller;

import cn.dev33.satoken.stp.StpUtil;
import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyancommonbasic.exception.ControllerException;
import hbnu.project.zhiyancommonsecurity.utils.SecurityUtils;
import hbnu.project.zhiyancommonsse.core.SseEmitterManager;
import hbnu.project.zhiyancommonsse.dto.SseMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * SSE 控制器
 *
 * @author Lion Li
 */
@Slf4j
@RestController
@ConditionalOnProperty(value = "sse.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SseController implements DisposableBean {

    private final SseEmitterManager sseEmitterManager;

    /**
     * 建立 SSE 连接
     * 客户端通过此接口建立长连接，接收服务器推送的消息
     *
     * @param token JWT Token (从请求头获取)
     * @return SseEmitter 实例
     */
    @GetMapping(value = "${sse.path:/sse/connect}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        try {
            // 从 Spring Security 上下文获取用户 ID
            Long userId = SecurityUtils.getUserId();

            // 使用 token 或生成唯一标识符
            String sessionToken = token != null ? token : java.util.UUID.randomUUID().toString();

            log.info("[SSE] 用户建立连接: userId={}, token={}", userId, sessionToken);

            return sseEmitterManager.connect(userId, sessionToken);
        } catch (ControllerException e) {
            log.error("[SSE] 建立连接失败", e);
            throw new RuntimeException("建立 SSE 连接失败: " + e.getMessage());
        }
    }


    /**
     * 关闭 SSE 连接
     */
    @GetMapping(value = "${sse.path:/sse/connect}/close")
    public R<Void> close(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        try{
            Long userId = SecurityUtils.getUserId();
            String sessionToken = token != null ? token : "";

            log.info("[SSE] 用户关闭连接: userId={}, token={}", userId, sessionToken);

            sseEmitterManager.disconnect(userId, sessionToken);
            return R.ok(null, "连接已关闭");
        }catch (ControllerException e){
            log.error("[SSE] 关闭连接失败", e);
            return R.fail("关闭连接失败: " + e.getMessage());
        }
    }


    /**
     * 向特定用户发送消息（测试接口）
     *
     * @param userId 目标用户的 ID
     * @param msg    要发送的消息内容
     */
    @GetMapping(value = "${sse.path:/sse/connect}/send")
    public R<Void> send(Long userId, String msg) {
        log.info("[SSE] 发送消息给用户: userId={}, message={}", userId, msg);

        SseMessageDto dto = new SseMessageDto();
        dto.setUserIds(List.of(userId));
        dto.setMessage(msg);
        sseEmitterManager.publishMessage(dto);

        return R.ok();
    }


    /**
     * 向所有用户发送消息（广播）
     *
     * @param msg 要发送的消息内容
     */
    @GetMapping(value = "${sse.path:/sse/connect}/sendAll")
    public R<Void> sendAll(String msg) {
        log.info("[SSE] 广播消息: message={}", msg);

        sseEmitterManager.publishAll(msg);

        return R.ok(null, "消息已广播");
    }


    /**
     * 清理资源。此方法目前不执行任何操作，但避免因未实现而导致错误
     */
    @Override
    public void destroy() throws Exception {
        // 销毁时不需要做什么 此方法避免无用操作报错
    }
}
