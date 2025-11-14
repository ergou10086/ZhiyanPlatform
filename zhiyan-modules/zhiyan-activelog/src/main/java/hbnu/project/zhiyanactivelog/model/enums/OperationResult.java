package hbnu.project.zhiyanactivelog.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作结果枚举
 *
 * @author ErgouTree
 */
@Getter
@AllArgsConstructor
public enum OperationResult {

    /**
     * 成功
     */
    SUCCESS("SUCCESS", "成功"),

    /**
     * 失败
     */
    FAILED("FAILED", "失败");

    private final String code;
    private final String desc;
}
