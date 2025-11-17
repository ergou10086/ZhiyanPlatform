package hbnu.project.zhiyanactivelog.annotation;

import java.lang.annotation.*;

import hbnu.project.zhiyanactivelog.model.enums.BizOperationModule;

/**
 * 业务操作日志注解
 *
 * @author ErgouTree
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizOperationLog {

    /**
     * 业务域：PROJECT / TASK / WIKI / ACHIEVEMENT
     */
    BizOperationModule module();

    /**
     * 操作类型（统一字符串），具体会映射到各自域的枚举 valueOf，如 CREATE/UPDATE/DELETE...
     */
    String type();

    /**
     * 模块名（展示用），默认使用业务域中文名
     */
    String moduleName() default "";

    /**
     * 操作描述,可方便前端展示
     */
    String description() default "";

    /**
     * 是否记录入参
     */
    boolean recordParams() default true;

    /**
     * 是否记录返回结果（成功时）
     */
    boolean recordResult() default false;

    /**
     * 从入参解析 projectId 的 SpEL 表达式，如 "#request.projectId" 或 "#projectId"
     */
    String projectId() default "";

    /**
     * 业务资源ID（如 taskId/wikiPageId/achievementId）的 SpEL
     */
    String bizId() default "";

    /**
     * 业务资源标题（如任务标题/成果标题/Wiki标题）的 SpEL
     */
    String bizTitle() default "";

    /**
     * 指定用户名的 SpEL，默认从安全上下文获取
     */
    String username() default "";
}
