package hbnu.project.zhiyancommonredis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis健康检查指示器
 * 用于监控Redis连接状态
 *
 * @author ErgouTree
 */
@Slf4j
@Component
public class RedisHealthIndicator implements HealthIndicator {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try {
            // 尝试执行PING命令
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            
            if ("PONG".equalsIgnoreCase(pong)) {
                return Health.up()
                        .withDetail("redis", "连接正常")
                        .withDetail("response", pong)
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", "响应异常")
                        .withDetail("response", pong)
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis健康检查失败", e);
            return Health.down()
                    .withDetail("redis", "连接失败")
                    .withDetail("error", e.getMessage())
                    .withDetail("cause", e.getClass().getName())
                    .build();
        }
    }
}

