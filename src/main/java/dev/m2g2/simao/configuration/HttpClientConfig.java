package dev.m2g2.simao.configuration;

import dev.m2g2.simao.service.WahaClientService;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfig {

    @Value("${waha.api.key}")
    private String apiKey;

    @Bean
    public WahaClientService wahaClientService() {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        RestClient restClient = RestClient.builder()
                .requestFactory(new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(httpClient))
                .baseUrl("http://localhost:3000/api")
                .defaultHeader("X-Api-Key", apiKey)
                .build();

        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(WahaClientService.class);
    }
}
