package hbnu.project.zhiyancommonsecurity.filter;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * XSS过滤处理
 *
 * @author akoiv
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    /**
     * @param request
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }
        return HtmlUtil.cleanHtmlTag(value).trim();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> rawMap = super.getParameterMap();
        if (MapUtil.isEmpty(rawMap)) {
            return rawMap;
        }
        // 创建新Map避免修改原始参数
        Map<String, String[]> cleanedMap = new HashMap<>(rawMap.size());
        for (Map.Entry<String, String[]> entry : rawMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                String[] cleanedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    cleanedValues[i] = cleanXss(values[i]);
                }
                cleanedMap.put(entry.getKey(), cleanedValues);
            }
        }
        return cleanedMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (ArrayUtil.isEmpty(values)) {
            return values;
        }
        int length = values.length;
        String[] escapseValues = new String[length];
        for (int i = 0; i < length; i++) {
            // 防xss攻击和过滤前后空格
            escapseValues[i] = HtmlUtil.cleanHtmlTag(values[i]).trim();
        }
        return escapseValues;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 非json类型，直接返回
        if (!isJsonRequest()) {
            return super.getInputStream();
        }

        // 为空，直接返回
        String json = StrUtil.str(IoUtil.readBytes(super.getInputStream(), false), StandardCharsets.UTF_8);
        if (StringUtils.isEmpty(json)) {
            return super.getInputStream();
        }

        // 读取原始请求体
        String body = StrUtil.str(IoUtil.readBytes(super.getInputStream(), false), StandardCharsets.UTF_8);
        if (StrUtil.isBlank(body)) {
            return super.getInputStream();
        }

        // xss过滤,清理XSS并返回新的流
        String cleanedBody = cleanXss(body);
        byte[] bodyBytes = cleanedBody.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bodyBytes);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不需要实现
            }

            @Override
            public int read() {
                return inputStream.read();
            }
        };
    }

    /**
     * 清理XSS攻击内容
     */
    private String cleanXss(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtil.cleanHtmlTag(value).trim();
    }

    /**
     * 是否是Json请求
     */
    public boolean isJsonRequest() {
        String header = super.getHeader(HttpHeaders.CONTENT_TYPE);
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }
}
