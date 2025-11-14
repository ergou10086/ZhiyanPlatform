package hbnu.project.zhiyanactivelog.model.dto;

import cn.idev.excel.annotation.ExcelProperty;
import hbnu.project.zhiyanactivelog.model.enums.OperationResult;
import hbnu.project.zhiyanactivelog.model.enums.ProjectOperationType;
import hbnu.project.zhiyancommonexcel.annotation.ExcelEnumFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 项目日志导出DTO
 *
 * @author ErgouTree
 */
@Data
public class ProjectLogExportDTO {

    @ExcelProperty(value = "日志ID", index = 0)
    private String id;

    @ExcelProperty(value = "项目ID", index = 1)
    private String projectId;

    @ExcelProperty(value = "项目名称", index = 2)
    private String projectName;

    @ExcelProperty(value = "用户ID", index = 3)
    private String userId;

    @ExcelProperty(value = "用户名", index = 4)
    private String username;

    @ExcelProperty(value = "操作类型", index = 5)
    @ExcelEnumFormat(enumClass = ProjectOperationType.class, codeField = "code", textField = "desc")
    private String operationType;

    @ExcelProperty(value = "操作模块", index = 6)
    private String operationModule;

    @ExcelProperty(value = "操作描述", index = 7)
    private String operationDesc;

    @ExcelProperty(value = "操作结果", index = 8)
    @ExcelEnumFormat(enumClass = OperationResult.class, codeField = "code", textField = "desc")
    private String operationResult;

    @ExcelProperty(value = "错误信息", index = 9)
    private String errorMessage;

    @ExcelProperty(value = "IP地址", index = 10)
    private String ipAddress;

    @ExcelProperty(value = "用户代理", index = 11)
    private String userAgent;

    @ExcelProperty(value = "操作时间", index = 12)
    private String operationTime;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化时间为字符串
     */
    public void setOperationTime(LocalDateTime time) {
        this.operationTime = time != null ? time.format(DATE_FORMATTER) : "";
    }
}