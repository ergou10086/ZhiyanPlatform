package hbnu.project.zhiyancommonbasic.annotation;

import java.lang.annotation.*;

/**
 * 标记类或方法的主要修改人信息
 * 用于 Javadoc 文档中记录代码维护历史
 *
 * @author 树上的二狗
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface AuthorModify {
    /**
     * 主要修改人姓名
     */
    String author();

    /**
     * 修改日期（格式：YYYY-MM-DD）
     */
    String date() default "";

    /**
     * 关联版本号
     */
    String version() default "";

    /**
     * 修改描述
     */
    String description();
}
