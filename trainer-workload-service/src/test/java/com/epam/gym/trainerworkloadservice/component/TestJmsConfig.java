package com.epam.gym.trainerworkloadservice.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@TestConfiguration
@EnableJms
@Profile("stg")
public class TestJmsConfig {

    private static final String BROKER_URL = "vm://embedded-broker?create=false";

    @Bean(initMethod = "start", destroyMethod = "stop")
    @Primary
    public BrokerService embeddedBroker() throws Exception {
        log.info("Starting embedded ActiveMQ broker...");
        BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector("tcp://localhost:61616");
        broker.setBrokerName("embedded-broker");
        log.info("Embedded broker configured");
        return broker;
    }

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        log.info("Creating ConnectionFactory for embedded broker: {}", BROKER_URL);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(BROKER_URL);
        factory.setTrustAllPackages(true);
        return factory;
    }

    @Bean(name = "jacksonJmsMessageConverter")
    @Primary
    public MessageConverter messageConverter() {
        log.info("Creating MessageConverter with type mappings");
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");

        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("TrainerWorkloadRequest",
                com.epam.gym.trainerworkloadservice.dto.request.TrainerWorkloadRequest.class);
        converter.setTypeIdMappings(typeIdMappings);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Bean
    @Primary
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory,
                                   MessageConverter messageConverter) {
        log.info("Creating JmsTemplate");
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setDeliveryPersistent(false); // Non-persistent for embedded
        return template;
    }

    @Bean
    @Primary
    public JmsListenerContainerFactory<?> jmsListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        log.info("Creating JmsListenerContainerFactory");
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setConcurrency("1-1");
        factory.setAutoStartup(true);
        factory.setRecoveryInterval(1000L);
        factory.setErrorHandler(t -> log.error("JMS Listener error: {}", t.getMessage(), t));
        return factory;
    }
}