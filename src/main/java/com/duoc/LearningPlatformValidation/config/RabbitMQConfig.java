package com.duoc.LearningPlatformValidation.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.mq.inscripcion.exchange}")
    private String exchangeName;

    @Value("${app.mq.inscripcion.queue}")
    private String queueName;

    @Value("${app.mq.inscripcion.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange inscripcionesExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue resumenInscripcionQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Binding resumenInscripcionBinding() {
        return BindingBuilder
                .bind(resumenInscripcionQueue())
                .to(inscripcionesExchange())
                .with(routingKey);
    }
}