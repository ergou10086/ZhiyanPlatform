package hbnu.project.zhiyanauth.model.enums;

/**
 * 用户状态枚举
 *
 * @author ErgouTree
 */
public enum UserStatus {
    
    /**
     * 正常状态
     */
    ACTIVE("正常", "用户账号正常可用"),
    
    /**
     * 锁定状态
     */
    LOCKED("锁定", "用户账号被锁定，无法登录"),
    
    /**
     * 禁用状态
     */
    DISABLED("禁用", "用户账号被禁用"),
    
    /**
     * 已删除状态
     */
    DELETED("已删除", "用户账号已删除（软删除）");

    private final String statusName;
    private final String description;

    UserStatus(String statusName, String description) {
        this.statusName = statusName;
        this.description = description;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getDescription() {
        return description;
    }
}
