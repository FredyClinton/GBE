package gov.cmr.minfi.db.gbe.app.prediction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GbePlusClientConfig {

    @Bean
    public RestClient gbePlusRestClient(@Value("${gbe-plus.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
