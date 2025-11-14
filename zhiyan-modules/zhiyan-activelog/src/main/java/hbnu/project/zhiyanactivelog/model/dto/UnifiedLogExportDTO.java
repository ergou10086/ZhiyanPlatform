package hbnu.project.zhiyanactivelog.model.dto;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一日志导出DTO
 * 用于导出聚合后的操作日志
 *
 * @author ErgouTree
 */
@Data
public class UnifiedLogExportDTO {

    @ExcelProperty(value = "日志ID", index = 0)
    private String id;

    @ExcelProperty(value = "项目ID", index = 1)
    private String projectId;

    @ExcelProperty(value = "用户ID", index = 2)
    private String userId;

    @ExcelProperty(value = "用户名", index = 3)
    private String username;

    @ExcelProperty(value = "业务模块", index = 4)
    private String operationModule;

    @ExcelProperty(value = "操作类型", index = 5)
    private String operationType;

    @ExcelProperty(value = "标题", index = 6)
    private String title;

    @ExcelProperty(value = "描述", index = 7)
    private String description;

    @ExcelProperty(value = "IP地址", index = 8)
    private String ip;

    @ExcelProperty(value = "用户代理", index = 9)
    private String userAgent;

    @ExcelProperty(value = "操作时间", index = 10)
    private String time;

    @ExcelProperty(value = "来源", index = 11)
    private String source;

    @ExcelProperty(value = "关联ID", index = 12)
    private String relatedId;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化时间为字符串
     */
    public void setTime(LocalDateTime time) {
        this.time = time != null ? time.format(DATE_FORMATTER) : "";
    }
}
