package hbnu.project.zhiyancommonbasic.exception.file;

import hbnu.project.zhiyancommonbasic.exception.FileException;

/**
 * 文件上传异常
 * 用于处理文件上传过程中的异常
 *
 * @author ErgouTree
 */
public class FileUploadException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileUploadException() {
        super("文件上传失败", 5001);
    }

    public FileUploadException(String message) {
        super(message, 5001);
    }

    public FileUploadException(String message, Throwable cause) {
        super(message, 5001, cause);
    }
}
