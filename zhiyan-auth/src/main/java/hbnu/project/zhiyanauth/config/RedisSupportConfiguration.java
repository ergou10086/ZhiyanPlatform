package hbnu.project.zhiyanauth.config;

import hbnu.project.zhiyancommonredis.config.RedisConfig;
import hbnu.project.zhiyancommonredis.service.RedisService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({RedisService.class, RedisConfig.class})
/**
 * Redis 支持配置类
 *
 * @author ErgouTree
 */
public class RedisSupportConfiguration {
}



