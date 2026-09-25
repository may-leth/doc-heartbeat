package com.may.soul.doc_heartbeat.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport;
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client;
import org.apache.hc.core5.http.HttpHost;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        Rest5Client rest5Client = Rest5Client.builder(
                new HttpHost("http", "localhost", 9200)
        ).build();

        ElasticsearchTransport transport = new Rest5ClientTransport(
                rest5Client, new JacksonJsonpMapper()
        );

        return new ElasticsearchClient(transport);
    }
}