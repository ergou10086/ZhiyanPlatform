package hbnu.project.zhiyancommonbasic.exception.file;

import hbnu.project.zhiyancommonbasic.exception.FileException;

/**
 * 文件删除异常
 * 用于处理文件删除过程中的异常
 *
 * @author ErgouTree
 */
public class FileDeleteException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileDeleteException() {
        super("文件删除失败", 5003);
    }

    public FileDeleteException(String message) {
        super(message, 5003);
    }

    public FileDeleteException(String message, Throwable cause) {
        super(message, 5003, cause);
    }
}
