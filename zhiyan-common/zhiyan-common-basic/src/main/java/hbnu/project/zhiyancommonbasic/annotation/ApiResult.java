package hbnu.project.zhiyancommonbasic.annotation;

import hbnu.project.zhiyancommonbasic.domain.R;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统一响应结果注解
 * 用于标记返回统一响应格式的接口
 *
 * @author ErgouTree
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "请求成功",
                content = @Content(schema = @Schema(implementation = R.class))
        ),
        @ApiResponse(
                responseCode = "400",
                description = "请求参数错误",
                content = @Content(schema = @Schema(implementation = R.class))
        ),
        @ApiResponse(
                responseCode = "401",
                description = "未授权/登录失效",
                content = @Content(schema = @Schema(implementation = R.class))
        ),
        @ApiResponse(
                responseCode = "403",
                description = "权限不足",
                content = @Content(schema = @Schema(implementation = R.class))
        ),
        @ApiResponse(
                responseCode = "500",
                description = "服务器内部错误",
                content = @Content(schema = @Schema(implementation = R.class))
        )
})
public @interface ApiResult {
    /**
     * 成功响应描述
     */
    String value() default "操作成功";
}
