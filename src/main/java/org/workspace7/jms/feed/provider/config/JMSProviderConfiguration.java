package org.workspace7.jms.feed.provider.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.Logger;
import feign.auth.BasicAuthRequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.web.client.RestTemplate;
import org.workspace7.jms.feed.provider.data.CouchDBClient;
import org.workspace7.jms.feed.provider.data.CouchDBErrorDecoder;
import org.workspace7.jms.feed.provider.service.OpenWhiskAPIService;

import javax.jms.ConnectionFactory;

import static java.lang.reflect.Modifier.TRANSIENT;

@Configuration
@EnableConfigurationProperties(OpenWhiskProperties.class)
@EnableJms
@Slf4j
public class JMSProviderConfiguration implements JmsListenerConfigurer {

  @Value("${COUCHDB_USER}")
  private String couchdbUser;

  @Value("${COUCHDB_PASSWORD}")
  private String couchdbPassword;

  @Value("${TRIGGERSTORE_SERVICE_HOST}")
  private String couchdbHost = "triggerstore";

  @Value("${TRIGGERSTORE_SERVICE_PORT}")
  private String couchdbPort;


  @Autowired
  ConnectionFactory connectionFactory;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public OpenWhiskAPIService openWhiskAPIService() {
    return new OpenWhiskAPIService();
  }

  @Bean
  public CouchDBClient couchDBClient() {
    String couchdbRestURI = String.format("http://%s:%s", couchdbHost, couchdbPort);
    log.info("Using Trigger Store {}", couchdbRestURI);
    return Feign.builder().decoder(new GsonDecoder())
      .encoder(new GsonEncoder())
      .logLevel(Logger.Level.BASIC)
      .logger(new Slf4jLogger())
      .errorDecoder(new CouchDBErrorDecoder())
      .requestInterceptor(new BasicAuthRequestInterceptor(couchdbUser, couchdbPassword))
      .target(CouchDBClient.class, couchdbRestURI);
  }

  @Bean
  public Gson gson() {
    return new GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .excludeFieldsWithModifiers(TRANSIENT)
      .create();
  }

  @Bean
  public JmsListenerContainerFactory jmsFeedContainerFactory() {
    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    return factory;
  }

  @Bean
  public JmsListenerEndpointRegistry jmsListenerEndpointRegistry() {
    return new JmsListenerEndpointRegistry();
  }

  @Override
  public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
    registrar.setContainerFactory(jmsFeedContainerFactory());
    registrar.setEndpointRegistry(jmsListenerEndpointRegistry());
  }

  @Bean
  public MessageConverter jacksonJmsMessageConverter() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    return converter;
  }

}
