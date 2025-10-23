package hbnu.project.zhiyancommonswagger.util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Swagger 工具类
 * 提供 Swagger 相关的辅助方法
 * 
 * @author ErgouTree
 */
public class SwaggerUtil {

    /**
     * 检查方法是否有 Swagger 文档注解
     */
    public static boolean hasSwaggerAnnotation(Method method) {
        return method.isAnnotationPresent(Operation.class) ||
               method.isAnnotationPresent(Tag.class);
    }

    /**
     * 检查类是否有 Swagger 文档注解
     */
    public static boolean hasSwaggerAnnotation(Class<?> clazz) {
        return clazz.isAnnotationPresent(Tag.class);
    }

    /**
     * 获取方法的 Operation 注解
     */
    public static Operation getOperation(Method method) {
        return method.getAnnotation(Operation.class);
    }

    /**
     * 获取类的 Tag 注解
     */
    public static Tag getTag(Class<?> clazz) {
        return clazz.getAnnotation(Tag.class);
    }

    /**
     * 构建标准响应示例
     */
    public static String buildResponseExample(String dataExample) {
        return """
                {
                  "code": 200,
                  "message": "操作成功",
                  "data": %s,
                  "timestamp": 1234567890
                }
                """.formatted(dataExample);
    }

    /**
     * 构建错误响应示例
     */
    public static String buildErrorResponseExample(int code, String message) {
        return """
                {
                  "code": %d,
                  "message": "%s",
                  "data": null,
                  "timestamp": 1234567890
                }
                """.formatted(code, message);
    }

    /**
     * 获取分页参数描述
     */
    public static String getPageParamDescription(String paramName) {
        return switch (paramName.toLowerCase()) {
            case "page", "pagenum" -> "页码（从1开始）";
            case "size", "pagesize" -> "每页数量";
            case "sort" -> "排序字段（格式：字段名,asc/desc）";
            default -> "分页参数";
        };
    }

    /**
     * 生成通用的成功响应注解
     */
    public static ApiResponse successResponse(String description) {
        return createApiResponse("200", description, null);
    }

    /**
     * 生成通用的错误响应注解
     */
    public static ApiResponse errorResponse(String code, String description) {
        return createApiResponse(code, description, null);
    }

    /**
     * 创建 ApiResponse 注解实例（辅助方法）
     */
    private static ApiResponse createApiResponse(String responseCode, String description, Class<?> schemaClass) {
        return new ApiResponse() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ApiResponse.class;
            }

            @Override
            public String responseCode() {
                return responseCode;
            }

            @Override
            public String description() {
                return description;
            }

            @Override
            public Content[] content() {
                return new Content[0];
            }

            @Override
            public io.swagger.v3.oas.annotations.headers.Header[] headers() {
                return new io.swagger.v3.oas.annotations.headers.Header[0];
            }

            @Override
            public io.swagger.v3.oas.annotations.links.Link[] links() {
                return new io.swagger.v3.oas.annotations.links.Link[0];
            }

            @Override
            public io.swagger.v3.oas.annotations.extensions.Extension[] extensions() {
                return new io.swagger.v3.oas.annotations.extensions.Extension[0];
            }

            @Override
            public String ref() {
                return "";
            }

            @Override
            public boolean useReturnTypeSchema() {
                return false;
            }
        };
    }
}

