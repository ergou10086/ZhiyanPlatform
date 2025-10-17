package hbnu.project.zhiyancommonelasticsearch.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 9.1.4 配置类
 * 使用新版 Elasticsearch Java API Client
 */
@Slf4j
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String[] elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:60000}")
    private int socketTimeout;

    /**
     * 创建 RestClient
     */
    @Bean
    public RestClient restClient() {
        // 解析 Elasticsearch URI
        HttpHost[] hosts = new HttpHost[elasticsearchUris.length];
        for (int i = 0; i < elasticsearchUris.length; i++) {
            String uri = elasticsearchUris[i];
            // 简单解析 URI（实际项目可以使用更复杂的解析逻辑）
            String[] parts = uri.replace("http://", "").replace("https://", "").split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
            String scheme = uri.startsWith("https") ? "https" : "http";
            hosts[i] = new HttpHost(host, port, scheme);
        }

        RestClientBuilder builder = RestClient.builder(hosts);

        // 配置认证信息（如果有）
        if (username != null && !username.isEmpty()) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
            );
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            );
        }

        // 配置超时时间
        builder.setRequestConfigCallback(requestConfigBuilder ->
            requestConfigBuilder
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
        );

        RestClient restClient = builder.build();
        log.info("Elasticsearch RestClient 初始化成功, 连接地址: {}", (Object) elasticsearchUris);
        return restClient;
    }

    /**
     * 创建 ElasticsearchTransport
     * 使用 Jackson 作为 JSON 映射器
     */
    @Bean
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        ElasticsearchTransport transport = new RestClientTransport(
            restClient,
            new JacksonJsonpMapper()
        );
        log.info("ElasticsearchTransport 初始化成功");
        return transport;
    }

    /**
     * 创建 ElasticsearchClient
     * Elasticsearch 9.x 推荐使用的客户端
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        ElasticsearchClient client = new ElasticsearchClient(transport);
        log.info("ElasticsearchClient 初始化成功");
        return client;
    }
}

