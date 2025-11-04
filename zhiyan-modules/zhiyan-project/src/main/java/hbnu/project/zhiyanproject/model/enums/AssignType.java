package hbnu.project.zhiyanproject.model.enums;

import lombok.Getter;

/**
 * 任务分配类型枚举
 *
 * @author System
 */
@Getter
public enum AssignType {
    
    /**
     * 被管理员分配
     */
    ASSIGNED("被分配", "管理员将任务分配给成员"),
    
    /**
     * 用户主动接取
     */
    CLAIMED("主动接取", "成员主动接取任务");
    
    private final String name;
    private final String description;
    
    AssignType(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

