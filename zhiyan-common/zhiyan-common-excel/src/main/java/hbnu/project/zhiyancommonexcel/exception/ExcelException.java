package hbnu.project.zhiyancommonexcel.exception;

import lombok.Getter;

/**
 * Excel处理异常（导入/导出通用）
 * 细化异常类型，携带关键上下文信息，便于问题定位
 *
 * @author ErgouTree
 * @Re_rewrite worst galgame player
 */
@Getter
public class ExcelException extends RuntimeException {

    /**
     * 异常类型（区分导入/导出/模板生成等场景）
     */
    private final ExcelErrorType errorType;

    /**
     * 关联的行号（导入/导出时的具体行，便于定位错误数据）
     * 非必传，无行号场景可设为null
     */
    private final Integer rowNum;

    /**
     * 关联的字段名（导入时的字段校验错误，便于定位字段）
     * 非必传，无字段关联场景可设为null
     */
    private final String fieldName;

    /**
     * 构造器：完整参数（推荐用于精确异常定位）
     *
     * @param errorType  异常类型（必填）
     * @param message    异常描述（必填）
     * @param rowNum     关联行号（可选）
     * @param fieldName  关联字段名（可选）
     * @param cause      原始异常（可选，用于异常链追踪）
     */
    public ExcelException(ExcelErrorType errorType, String message, Integer rowNum, String fieldName, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.rowNum = rowNum;
        this.fieldName = fieldName;
    }

    /**
     * 简化构造器：仅异常类型+描述（不关注行号/字段）
     */
    public ExcelException(ExcelErrorType errorType, String message) {
        this(errorType, message, null, null, null);
    }

    /**
     * 简化构造器：异常类型+描述+行号（用于行级错误，如导入行数据格式错误）
     */
    public ExcelException(ExcelErrorType errorType, String message, Integer rowNum) {
        this(errorType, message, rowNum, null, null);
    }

    /**
     * 简化构造器：异常类型+描述+行号+字段（用于字段级错误，如单元格数据校验失败）
     */
    public ExcelException(ExcelErrorType errorType, String message, Integer rowNum, String fieldName) {
        this(errorType, message, rowNum, fieldName, null);
    }

    /**
     * 简化构造器：携带原始异常（用于包装底层异常，如IO异常、POI库异常）
     */
    public ExcelException(ExcelErrorType errorType, String message, Throwable cause) {
        this(errorType, message, null, null, cause);
    }

    /**
     * Excel异常类型枚举（细化场景，便于前端/日志区分处理）
     */
    public enum ExcelErrorType {
        /** 导出相关异常 */
        EXPORT_ERROR("导出失败"),
        /** 导入相关异常 */
        IMPORT_ERROR("导入失败"),
        /** 模板生成异常（如动态生成导出模板时出错） */
        TEMPLATE_GENERATE_ERROR("模板生成失败"),
        /** 数据校验异常（如单元格值不符合规则） */
        DATA_VALIDATE_ERROR("数据校验失败"),
        /** 文件格式异常（如不是Excel文件、版本不支持） */
        FILE_FORMAT_ERROR("文件格式错误"),
        /** IO异常（如文件读写失败） */
        IO_ERROR("文件读写失败");

        @Getter
        private final String desc;

        ExcelErrorType(String desc) {
            this.desc = desc;
        }
    }
}