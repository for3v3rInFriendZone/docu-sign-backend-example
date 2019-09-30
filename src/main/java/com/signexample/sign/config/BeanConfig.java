package com.signexample.sign.config;

import com.docusign.esign.client.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Bean
  public ApiClient getApiClient() {
    return new ApiClient();
  }
}
