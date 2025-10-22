package hbnu.project.zhiyancommonbasic.exception.file;

import hbnu.project.zhiyancommonbasic.exception.FileException;

/**
 * 文件验证异常
 * 用于处理文件格式、大小等验证失败的情况
 *
 * @author ErgouTree
 */
public class FileValidationException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileValidationException() {
        super("文件验证失败", 5004);
    }

    public FileValidationException(String message) {
        super(message, 5004);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, 5004, cause);
    }
}
