package hbnu.project.zhiyanknowledge.client;

import org.springframework.cloud.openfeign.FeignClient;
import hbnu.project.zhiyancommonbasic.domain.R;
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
        name = "zhiyan-message-service",
        path = "/zhiyan/message/internal"
)
public interface MessageServiceClient {
}
