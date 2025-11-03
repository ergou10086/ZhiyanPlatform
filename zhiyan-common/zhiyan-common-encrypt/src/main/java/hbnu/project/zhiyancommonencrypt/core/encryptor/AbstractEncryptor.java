package hbnu.project.zhiyancommonencrypt.core.encryptor;

import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import hbnu.project.zhiyancommonencrypt.core.EncryptContext;
import hbnu.project.zhiyancommonencrypt.core.IEncryptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 加密器抽象基类
 * 提供通用的加密器功能和模板方法
 *
 * @author ErgouTree
 * @version 2.0.0
 */
@Slf4j
public abstract class AbstractEncryptor implements IEncryptor {

    /**
     * 加密上下文
     */
    protected final EncryptContext context;

    /**
     * 构造函数
     *
     * @param context 加密上下文
     */
    public AbstractEncryptor(EncryptContext context) {
        this.context = context;
        validateContext();
    }

    /**
     * 验证加密上下文
     * 子类可以重写此方法进行自定义验证
     */
    protected void validateContext() {
        if (context == null) {
            throw new IllegalArgumentException("加密上下文不能为空");
        }
    }

    /**
     * 检查字符串是否为空
     *
     * @param value 字符串
     * @return 是否为空
     */
    protected boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    /**
     * 获取加密上下文
     *
     * @return 加密上下文
     */
    public EncryptContext getContext() {
        return context;
    }
}
