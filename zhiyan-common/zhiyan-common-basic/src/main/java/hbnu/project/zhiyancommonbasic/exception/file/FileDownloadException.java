package hbnu.project.zhiyancommonbasic.exception.file;

import hbnu.project.zhiyancommonbasic.exception.FileException;

/**
 * 文件下载异常
 * 用于处理文件下载过程中的异常
 *
 * @author ErgouTree
 */
public class FileDownloadException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileDownloadException() {
        super("文件下载失败", 5002);
    }

    public FileDownloadException(String message) {
        super(message, 5002);
    }

    public FileDownloadException(String message, Throwable cause) {
        super(message, 5002, cause);
    }
}
