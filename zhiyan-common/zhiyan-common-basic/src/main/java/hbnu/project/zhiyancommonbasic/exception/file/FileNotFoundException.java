package hbnu.project.zhiyancommonbasic.exception.file;

import hbnu.project.zhiyancommonbasic.exception.FileException;

/**
 * 文件未找到异常
 * 用于处理文件不存在的情况
 *
 * @author ErgouTree
 */
public class FileNotFoundException extends FileException {
    private static final long serialVersionUID = 1L;

    public FileNotFoundException() {
        super("文件不存在", 5005);
    }

    public FileNotFoundException(String message) {
        super(message, 5005);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, 5005, cause);
    }
}
