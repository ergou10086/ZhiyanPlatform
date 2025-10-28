/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hbnu.project.zhiyancommonseata.web;

import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import io.seata.core.context.RootContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author xiaojing
 *
 * Seata HandlerInterceptor, Convert Seata information into
 * @see io.seata.core.context.RootContext from http request's header in
 * {@link HandlerInterceptor#preHandle(HttpServletRequest, HttpServletResponse, Object)},
 * And clean up Seata information after servlet method invocation in
 * {@link HandlerInterceptor#afterCompletion(HttpServletRequest, HttpServletResponse, Object, Exception)}
 */
public class SeataHandlerInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory
			.getLogger(SeataHandlerInterceptor.class);

    /**
     * Spring MVC 拦截器的「请求处理前」回调方法，返回 true 表示继续执行后续拦截器/控制器，false 表示中断
     * 核心逻辑：将 HTTP 请求头中的 XID 绑定到 Seata 上下文，确保当前请求能加入全局事务
     *
     * @param request  HTTP 请求对象，用于从请求头中获取 XID（Key：RootContext.KEY_XID）
     * @param response HTTP 响应对象，当前方法暂未使用
     * @param handler  当前请求对应的处理器（如 Controller 方法），当前方法暂未使用
     * @return boolean 固定返回 true，确保请求能正常进入后续处理流程
     */
	@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 1. 从 Seata 上下文（RootContext）中获取当前线程已有的 XID（可能为空，如首次进入事务）
        String xid = RootContext.getXID();
        // 2. 从 HTTP 请求头中提取上游服务传递的 XID（请求头 Key 由 RootContext.KEY_XID 定义，默认是 "TX_XID"）
        String rpcXid = request.getHeader(RootContext.KEY_XID);

        // 3. 调试日志：打印当前上下文的 XID 和请求头中的 XID，便于问题排查
        if (log.isDebugEnabled()) {
            log.debug("xid in RootContext {} xid in RpcContext {}", xid, rpcXid);
        }

        // 4. 核心逻辑：若当前上下文无 XID，但请求头中有 XID，则将请求头的 XID 绑定到上下文
        // （避免重复绑定：只有上下文为空时才绑定，防止覆盖已有事务）
        if (StringUtils.isBlank(xid) && rpcXid != null) {
            RootContext.bind(rpcXid);
            if (log.isDebugEnabled()) {
                log.debug("bind {} to RootContext", rpcXid); // 打印绑定日志
            }
        }

        // 5. 必须返回 true，否则请求会被拦截器阻断，无法到达 Controller
        return true;
    }


    /**
     * Spring MVC 拦截器的「请求处理完成后」回调方法（无论请求成功/失败都会执行）
     * 核心逻辑：清理 Seata 上下文，解绑 XID，避免 XID 残留导致后续请求误加入错误的事务
     *
     * @param request  HTTP 请求对象，用于再次获取请求头中的 XID，与解绑的 XID 做一致性校验
     * @param response HTTP 响应对象，当前方法暂未使用
     * @param handler  当前请求对应的处理器，当前方法暂未使用
     * @param e        请求处理过程中抛出的异常（若有），当前方法暂未使用
     */
	@Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception e) {
        // 1. 先判断当前上下文是否有 XID：若无 XID，说明当前请求未参与事务，直接返回（无需清理）
        if (StringUtils.isNotBlank(RootContext.getXID())) {
            // 2. 再次从请求头中获取 XID（用于后续校验：解绑的 XID 是否与请求头的 XID 一致）
            String rpcXid = request.getHeader(RootContext.KEY_XID);

            // 3. 若请求头中无 XID，说明当前请求未传递事务信息，直接返回（无需清理）
            if (StringUtils.isEmpty(rpcXid)) {
                return;
            }

            // 4. 核心操作：从 Seata 上下文解绑 XID，并返回解绑的 XID（unbindXid）
            String unbindXid = RootContext.unbind();
            if (log.isDebugEnabled()) {
                log.debug("unbind {} from RootContext", unbindXid); // 打印解绑日志
            }

            // 5. 一致性校验：若解绑的 XID 与请求头的 XID 不一致，可能是事务上下文被篡改，打印警告日志
            if (!rpcXid.equalsIgnoreCase(unbindXid)) {
                log.warn("xid in change during RPC from {} to {}", rpcXid, unbindXid);
                // 补偿逻辑：若解绑的 XID 不为空，重新绑定回上下文，避免事务信息丢失
                if (unbindXid != null) {
                    RootContext.bind(unbindXid);
                    log.warn("bind {} back to RootContext", unbindXid);
                }
            }
        }
	}

}
