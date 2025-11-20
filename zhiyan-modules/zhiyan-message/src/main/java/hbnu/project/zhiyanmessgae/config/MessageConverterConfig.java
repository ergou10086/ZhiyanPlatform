package hbnu.project.zhiyanmessgae.config;

import hbnu.project.zhiyanmessgae.model.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 显式注册 MapStruct 生成的 MessageConverter，
 * 避免因组件扫描顺序导致的缺失。
 * @author yxy
 */
@Configuration
public class MessageConverterConfig {

    @Bean
    public MessageConverter messageConverter() {
        return MessageConverter.INSTANCE;
    }
}

