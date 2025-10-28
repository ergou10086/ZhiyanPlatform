package hbnu.project.zhiyancommonseata.exception;

import io.seata.core.model.GlobalStatus;
import lombok.Getter;

/**
 * Seata 分布式事务专用异常类
 * 用于封装分布式事务过程中出现的各类异常（如事务提交/回滚失败、状态异常等）
 *
 * @author ErgouTree
 */
@Getter
public class SeataException extends RuntimeException {
    /**
     * 全局事务ID（XID），便于追踪具体事务
     */
    private final String xid;

    /**
     * 全局事务状态（可选，记录异常发生时的事务状态）
     */
    private final GlobalStatus globalStatus;

    /**
     * 分支事务ID（可选，若异常由分支事务引发）
     */
    private final Long branchId;

    /**
     * 基础构造方法（仅包含异常信息）
     *
     * @param message 异常描述信息
     */
    public SeataException(String message) {
        this(message, null, null, null, null);
    }

    /**
     * 包含XID的构造方法（适用于全局事务相关异常）
     *
     * @param message 异常描述信息
     * @param xid     全局事务ID
     */
    public SeataException(String message, String xid) {
        this(message, xid, null, null, null);
    }

    /**
     * 包含XID和事务状态的构造方法（适用于事务状态异常场景）
     *
     * @param message       异常描述信息
     * @param xid           全局事务ID
     * @param globalStatus  异常发生时的全局事务状态
     */
    public SeataException(String message, String xid, GlobalStatus globalStatus) {
        this(message, xid, globalStatus, null, null);
    }

    /**
     * 包含分支事务信息的构造方法（适用于分支事务失败场景）
     *
     * @param message       异常描述信息
     * @param xid           全局事务ID
     * @param branchId      分支事务ID
     * @param cause         原始异常（根因）
     */
    public SeataException(String message, String xid, Long branchId, Throwable cause) {
        this(message, xid, null, branchId, cause);
    }

    /**
     * 完整构造方法（包含所有可能的上下文信息）
     *
     * @param message       异常描述信息
     * @param xid           全局事务ID
     * @param globalStatus  全局事务状态
     * @param branchId      分支事务ID
     * @param cause         原始异常（根因）
     */
    public SeataException(String message, String xid, GlobalStatus globalStatus, Long branchId, Throwable cause) {
        super(buildErrorMessage(message, xid, globalStatus, branchId), cause);
        this.xid = xid;
        this.globalStatus = globalStatus;
        this.branchId = branchId;
    }

    /**
     * 构建格式化的异常信息，包含事务上下文（便于问题排查）
     */
    private static String buildErrorMessage(String message, String xid, GlobalStatus status, Long branchId) {
        StringBuilder sb = new StringBuilder(message);
        if (xid != null) {
            sb.append(" [XID: ").append(xid).append("]");
        }
        if (status != null) {
            sb.append(" [Status: ").append(status.name()).append("]");
        }
        if (branchId != null) {
            sb.append(" [BranchID: ").append(branchId).append("]");
        }
        return sb.toString();
    }
}
