package hbnu.project.zhiyancommonidempotent.config;


import hbnu.project.zhiyancommonidempotent.aspectj.RepeatSubmitAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 幂等功能配置
 *
 * @author yui
 */
@AutoConfiguration
public class IdempotentAutoConfiguration {

	@Bean
	public RepeatSubmitAspect repeatSubmitAspect() {
		return new RepeatSubmitAspect();
	}

}
