package com.gcm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcm.service.dto.TrainerWorkloadRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("com.gcc.app.integration.workload.dto.TrainerWorkloadRequestDto", TrainerWorkloadRequestDto.class);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate(CachingConnectionFactory connectionFactory,
                                   MappingJackson2MessageConverter converter,
                                   @Value("${jms.request-timeout-ms:5000}") long receiveTimeout) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setReceiveTimeout(receiveTimeout);

        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            CachingConnectionFactory connectionFactory, MappingJackson2MessageConverter converter) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setConcurrency("3-10");
        factory.setSessionTransacted(true);

        return factory;
    }
}