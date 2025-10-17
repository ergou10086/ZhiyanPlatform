package hbnu.project.zhiyancommonoss.exception;

import java.io.Serial;

/**
 * OSS异常类
 *
 * @author ErgouTree
 */
public class OssException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public OssException(String msg) {
        super(msg);
    }

}
