package com.vat.conversionservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Value("${rabbitmq.queue.video.name}")
    private String videoQueue;

    @Value("${rabbitmq.queue.audio.name}")
    private String audioQueue;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.video.key}")
    private String videoRoutingKey;

    @Value("${rabbitmq.routing.audio.key}")
    private String audioRoutingKey;

    @Bean("videoQueue")
    public Queue videoQueue() {
        return new Queue(videoQueue, true);
    }

    @Bean("audioQueue")
    public Queue audioQueue() {
        return new Queue(audioQueue, true);
    }

    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(exchange);
    }

    @Bean
    public Binding videoBinding(@Qualifier("videoQueue") Queue videoQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(videoQueue).to(directExchange).with(videoRoutingKey);
    }

    @Bean
    public Binding audioBinding(@Qualifier("audioQueue") Queue audioQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(audioQueue).to(directExchange).with(audioRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
