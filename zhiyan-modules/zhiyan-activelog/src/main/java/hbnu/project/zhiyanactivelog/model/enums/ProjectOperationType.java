package hbnu.project.zhiyanactivelog.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目操作类型枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum ProjectOperationType {

    /**
     * 创建项目
     */
    CREATE("CREATE", "创建项目"),

    /**
     * 更新项目
     */
    UPDATE("UPDATE", "更新项目"),

    /**
     * 删除项目
     */
    DELETE("DELETE", "删除项目"),

    /**
     * 添加成员
     */
    MEMBER_ADD("MEMBER_ADD", "添加成员"),

    /**
     * 移除成员
     */
    MEMBER_REMOVE("MEMBER_REMOVE", "移除成员"),

    /**
     * 角色变更
     */
    ROLE_CHANGE("ROLE_CHANGE", "角色变更"),

    /**
     * 状态变更
     */
    STATUS_CHANGE("STATUS_CHANGE", "状态变更");

    private final String code;
    private final String desc;
}
