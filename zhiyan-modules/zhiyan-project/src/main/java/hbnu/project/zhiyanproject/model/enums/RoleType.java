package hbnu.project.zhiyanproject.model.enums;

import lombok.Getter;

/**
 * 任务角色类型枚举
 *
 * @author System
 */
@Getter
public enum RoleType {
    
    /**
     * 执行者
     */
    EXECUTOR("执行者", "负责执行任务的成员"),
    
    /**
     * 关注者（预留）
     */
    FOLLOWER("关注者", "关注任务进展但不执行"),
    
    /**
     * 审核者（预留）
     */
    REVIEWER("审核者", "负责审核验收任务成果");
    
    private final String name;
    private final String description;
    
    RoleType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

