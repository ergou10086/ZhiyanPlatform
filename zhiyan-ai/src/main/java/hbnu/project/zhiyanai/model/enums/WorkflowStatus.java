package hbnu.project.zhiyanai.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 工作流执行状态
 *
 * @author ErgouTree
 */
@Getter
@RequiredArgsConstructor
public enum WorkflowStatus {
    /**
     * 等待中
     */
    PENDING("pending", "等待中"),

    /**
     * 运行中
     */
    RUNNING("running", "运行中"),

    /**
     * 成功
     */
    SUCCESS("success", "成功"),

    /**
     * 失败
     */
    FAILED("failed", "失败"),

    /**
     * 超时
     */
    TIMEOUT("timeout", "超时");

    private final String code;
    private final String description;
}