package hbnu.project.zhiyancommonidempotent.config;


import hbnu.project.zhiyancommonidempotent.aspectj.IdempotentAspect;
import hbnu.project.zhiyancommonidempotent.aspectj.RepeatSubmitAspect;
import hbnu.project.zhiyancommonidempotent.controller.IdempotentTokenController;
import hbnu.project.zhiyancommonidempotent.service.IdempotentTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * 幂等功能自动配置
 *
 * @author yui
 */
@Slf4j
@AutoConfiguration
public class IdempotentAutoConfiguration {

	/**
	 * 幂等Token服务
	 */
	@Bean
	@ConditionalOnMissingBean
	public IdempotentTokenService idempotentTokenService() {
		log.info("初始化幂等Token服务");
		return new IdempotentTokenService();
	}

	/**
	 * 幂等切面处理器
	 * 支持多种幂等策略：PARAM（参数防重）、TOKEN（Token机制）、SPEL（自定义key）
	 */
	@Bean
	@ConditionalOnMissingBean
	public IdempotentAspect idempotentAspect(IdempotentTokenService idempotentTokenService) {
		log.info("初始化幂等切面处理器");
		return new IdempotentAspect(idempotentTokenService);
	}

	/**
	 * 防重复提交切面处理器（兼容旧版本）
	 * 推荐使用 @Idempotent 注解替代
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "idempotent", name = "enable-repeat-submit", havingValue = "true", matchIfMissing = true)
	public RepeatSubmitAspect repeatSubmitAspect() {
		log.info("初始化防重复提交切面处理器（兼容模式）");
		return new RepeatSubmitAspect();
	}

	/**
	 * 幂等Token控制器
	 * 提供Token申请接口
	 */
	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = "idempotent", name = "enable-controller", havingValue = "true", matchIfMissing = true)
	public IdempotentTokenController idempotentTokenController(IdempotentTokenService idempotentTokenService) {
		log.info("初始化幂等Token控制器");
		return new IdempotentTokenController(idempotentTokenService);
	}

}
