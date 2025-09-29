package hbnu.project.zhiyancommonbasic.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hbnu.project.zhiyancommonbasic.utils.text.ConvertUtils;
import hbnu.project.zhiyancommonbasic.constants.GeneralConstants;
import hbnu.project.zhiyancommonbasic.domain.R;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端工具类
 *
 * @author yui
 */
public class ServletUtils {
    // Jackson 实例：静态常量全局复用，避免重复创建
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 获取String参数
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取String参数（带默认值）
     */
    public static String getParameter(String name, String defaultValue) {
        return ConvertUtils.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name) {
        return ConvertUtils.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取Integer参数（带默认值）
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return ConvertUtils.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取Boolean参数
     */
    public static Boolean getParameterToBool(String name) {
        return ConvertUtils.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取Boolean参数（带默认值）
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        return ConvertUtils.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获得所有请求参数（原始数组形式）
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 不可修改的参数Map
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map); // 防止外部修改，保证线程安全
    }

    /**
     * 获得所有请求参数（字符串形式，数组用逗号拼接）
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 参数Map
     */
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            // 注意：确保 StringUtils 存在 join 方法（如 org.apache.commons.lang3.StringUtils）
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    /**
     * 从 RequestContextHolder 获取当前 HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            // 捕获异常返回null，避免上游因NPE崩溃（实际使用时建议加日志）
            return null;
        }
    }

    /**
     * 从 RequestContextHolder 获取当前 HttpServletResponse
     */
    public static HttpServletResponse getResponse() {
        try {
            return getRequestAttributes().getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前请求的 HttpSession
     */
    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession() : null; // 避免NPE
    }

    /**
     * 从 RequestContextHolder 获取 ServletRequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取请求头（自动URL解码）
     *
     * @param request 请求对象
     * @param name    头名称
     * @return 解码后的头值（空串表示无此头）
     */
    public static String getHeader(HttpServletRequest request, String name) {
        if (request == null || name == null) {
            return StringUtils.EMPTY; // 防御性判断，避免NPE
        }
        String value = request.getHeader(name);
        return StringUtils.isEmpty(value) ? StringUtils.EMPTY : urlDecode(value);
    }

    /**
     * 获取所有请求头（不区分大小写的Map）
     *
     * @param request 请求对象
     * @return 头信息Map
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedCaseInsensitiveMap<>();
        if (request == null) {
            return map; // 空对象安全返回
        }
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 向客户端渲染JSON字符串（固定UTF-8编码）
     *
     * @param response 响应对象
     * @param string   JSON字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        if (response == null) {
            return; // 防御性判断
        }
        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE); // 复用MediaType常量，避免硬编码
            response.setCharacterEncoding(GeneralConstants.UTF8); // 复用全局UTF-8常量
            response.getWriter().print(string);
            response.getWriter().flush(); // 强制刷新，避免数据残留
        } catch (IOException e) {
            // 建议替换为日志框架（如SLF4J），避免System.out打印
            e.printStackTrace();
        }
    }

    /**
     * 判断是否为Ajax异步请求
     *
     * @param request 请求对象
     * @return true=Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        // 1. 检查 Accept 头是否包含 JSON
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        // 2. 检查 X-Requested-With 头（Ajax框架默认携带）
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            return true;
        }
        // 3. 检查请求URI后缀（如 .json/.xml）
        String uri = request.getRequestURI();
        if (uri != null && StringUtils.inStringIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }
        // 4. 检查自定义 __ajax 参数
        String ajax = request.getParameter("__ajax");
        return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
    }

    /**
     * URL编码（使用全局UTF-8常量）
     *
     * @param str 待编码字符串
     * @return 编码后的字符串（空串表示异常）
     */
    public static String urlEncode(String str) {
        if (StringUtils.isEmpty(str)) {
            return StringUtils.EMPTY; // 提前判断，避免无效编码
        }
        try {
            return URLEncoder.encode(str, GeneralConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    /**
     * URL解码（使用全局UTF-8常量）
     *
     * @param str 待解码字符串
     * @return 解码后的字符串（空串表示异常）
     */
    public static String urlDecode(String str) {
        if (StringUtils.isEmpty(str)) {
            return StringUtils.EMPTY;
        }
        try {
            return URLDecoder.decode(str, GeneralConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    /**
     * WebFlux 响应写入（默认失败状态码）
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value) {
        return webFluxResponseWriter(response, HttpStatus.OK, value, R.FAIL);
    }

    /**
     * WebFlux 响应写入（自定义业务状态码）
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value, int code) {
        return webFluxResponseWriter(response, HttpStatus.OK, value, code);
    }

    /**
     * WebFlux 响应写入（自定义HTTP状态码和业务状态码）
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, HttpStatus status, Object value, int code) {
        return webFluxResponseWriter(response, MediaType.APPLICATION_JSON_VALUE, status, value, code);
    }

    /**
     * WebFlux 响应写入核心方法（Jackson序列化）
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String contentType, HttpStatus status, Object value, int code) {
        if (response == null) {
            return Mono.empty(); // WebFlux中返回空Mono，避免异常
        }
        // 设置响应头和状态码
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);

        // 构建统一响应对象 R
        R<?> result = R.fail(code, value != null ? value.toString() : "");

        try {
            // Jackson 序列化：直接转字节数组（比先转String更高效）
            byte[] jsonBytes = OBJECT_MAPPER.writeValueAsBytes(result);
            DataBuffer dataBuffer = response.bufferFactory().wrap(jsonBytes);
            return response.writeWith(Mono.just(dataBuffer));
        } catch (JsonProcessingException e) {
            // 序列化失败时，返回友好错误信息
            String errorMsg = "JSON serialize failed: " + e.getMessage();
            DataBuffer errorBuffer = response.bufferFactory().wrap(errorMsg.getBytes());
            return response.writeWith(Mono.just(errorBuffer));
        }
    }
}