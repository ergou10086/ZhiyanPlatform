package hbnu.project.zhiyanknowledge.client;

import hbnu.project.zhiyancommonbasic.domain.R;
import hbnu.project.zhiyanmessage.model.pojo.SendMessageRequestPOJO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息服务Feign客户端
 * 用于项目模块调用消息模块的接口
 *
 * @author ErgouTree
 */
@FeignClient(
        name = "zhiyan-message",
        url = "http://localhost:8088",
        path = "/zhiyan/message/internal"
)
public interface MessageServiceClient {

    /**
     * 发送个人消息(单收件人)
     */
    @PostMapping("/send/personal")
    R<Void> sendPersonalMessage(@RequestBody SendMessageRequestPOJO request);

    /**
     * 发送批量个人消息(多收件人)
     */
    @PostMapping("/send/batch")
    R<Void> sendBatchPersonalMessage(@RequestBody SendMessageRequestPOJO request);

    /**
     * 发送广播消息(全体用户)
     */
    @PostMapping("/send/broadcast")
    R<Void> sendBroadcastMessage(@RequestBody SendMessageRequestPOJO request);

    /**
     * 批量发送个人消息
     */
    @PostMapping("/send/batch-personal")
    R<Void> sendBroadcastPersonalMessage(@RequestBody SendMessageRequestPOJO request);
}
