package hbnu.project.zhiyanactivelog.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Wiki操作类型枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum WikiOperationType {

    /**
     * 创建Wiki页面
     */
    CREATE("CREATE", "创建Wiki页面"),

    /**
     * 更新Wiki页面
     */
    UPDATE("UPDATE", "更新Wiki页面"),

    /**
     * 删除Wiki页面
     */
    DELETE("DELETE", "删除Wiki页面"),

    /**
     * 移动Wiki页面
     */
    MOVE("MOVE", "移动Wiki页面"),

    /**
     * 复制Wiki页面
     */
    COPY("COPY", "复制Wiki页面"),

    /**
     * 恢复Wiki页面
     */
    RESTORE("RESTORE", "恢复Wiki页面");

    private final String code;
    private final String desc;
}
