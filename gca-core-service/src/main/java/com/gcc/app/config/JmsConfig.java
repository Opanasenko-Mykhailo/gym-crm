package com.gcc.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class JmsConfig {

    @Bean
    public MappingJackson2MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);

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
}