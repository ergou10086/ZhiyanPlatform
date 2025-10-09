package hbnu.project.zhiyanauth.response;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

/**
 * 批量权限校验响应体
 *
 * @author Tokito
 */
@Data
@Builder
public class BatchPermissionCheckResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 权限检查结果映射
     * Key: 权限标识
     * Value: 是否拥有该权限
     */
    private Map<String, Boolean> permissionResults;

    /**
     * 消息说明
     */
    private String message;
}