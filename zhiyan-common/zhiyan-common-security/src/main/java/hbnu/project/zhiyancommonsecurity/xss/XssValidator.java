package hbnu.project.zhiyancommonsecurity.xss;

import hbnu.project.zhiyancommonbasic.utils.StringUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义xss校验注解实现
 * 
 * @author ErgouTree
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

    /**
     * HTML标签检测正则
     */
    private static final Pattern HTML_PATTERN = Pattern.compile(
            "<(\\w+)(\\s+[^>]*)?>.*?</\\1>|<\\w+(\\s+[^>]*)?/>|<script[\\s\\S]*?</script>|<style[\\s\\S]*?</style>",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void initialize(Xss constraintAnnotation) {
        // 初始化时可以获取注解参数
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 空值不校验
        if (value == null || value.isEmpty()) {
            return true;
        }
        // 检查是否包含HTML标签
        return !containsHtml(value);
    }

    public static boolean containsHtml(String value) {
        return HTML_PATTERN.matcher(value).find();
    }
}