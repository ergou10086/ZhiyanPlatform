package hbnu.project.zhiyancommonseata.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Seata Feign 请求拦截器
 * 在 Feign 调用时传递 XID
 *
 * @author ErgouTree
 */
@Slf4j
public class SeataFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String xid = RootContext.getXID();
        if (StringUtils.hasLength(xid)) {
            requestTemplate.header(RootContext.KEY_XID, xid);
            log.debug("Feign 请求添加 Seata XID: {}", xid);
        }
    }
}
