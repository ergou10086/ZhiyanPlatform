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

package hbnu.project.zhiyancommonseata.rest;

import io.seata.core.context.RootContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Seata 分布式事务框架中用于REST 请求拦截的组件
 *
 * @author xiaojing
 */
public class SeataRestTemplateInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);

        // 获取当前全局事务 ID
		String xid = RootContext.getXID();

        // 将 XID 添加到请求头
		if (StringUtils.hasLength(xid)) {
			requestWrapper.getHeaders().add(RootContext.KEY_XID, xid);
		}
        // 继续
		return clientHttpRequestExecution.execute(requestWrapper, bytes);
	}

}
