package hbnu.project.zhiyancommonoss.exception;


import hbnu.project.zhiyancommonbasic.exception.FileException;

import java.io.Serial;

/**
 * OSS异常类
 * 继承自FileException，专门处理OSS相关异常
 *
 * @author ErgouTree
 */
public class OssException extends FileException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OssException(String msg) {
        super(msg, 5100);
    }

    public OssException(String msg, Throwable cause) {
        super(msg, 5100, cause);
    }
}
