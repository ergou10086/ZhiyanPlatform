package hbnu.project.zhiyancommonbasic.exception.file;
/**
 * 文件处理异常
 * 用于封装文件上传、下载、处理过程中的各种异常
 */
public class FileProcessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    /**
     * 错误码
     */
    private String errorCode;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件操作类型
     */
    private String operationType;
    public FileProcessException(String message) {
        super(message);
    }
    public FileProcessException(String message, Throwable cause) {
        super(message, cause);
    }
    public FileProcessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public FileProcessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    public FileProcessException(String errorCode, String fileName, String operationType, String message) {
        super(message);
        this.errorCode = errorCode;
        this.fileName = fileName;
        this.operationType = operationType;
    }
    public FileProcessException(String errorCode, String fileName, String operationType, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.fileName = fileName;
        this.operationType = operationType;
    }
    // Getters and Setters
    public String getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getOperationType() {
        return operationType;
    }
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    @Override
    public String toString() {
        return "FileProcessException{" +
                "errorCode='" + errorCode + '\'' +
                ", fileName='" + fileName + '\'' +
                ", operationType='" + operationType + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}