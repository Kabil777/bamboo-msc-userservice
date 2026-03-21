package com.bamboo.userService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange("user.events", true, false);
    }

    @Bean
    public TopicExchange contentTopicExchange() {
        return new TopicExchange("content.events", true, false);
    }

    @Bean
    public TopicExchange documentTopicExchange() {
        return new TopicExchange("collab.events", true, false);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Configuration
    public static class UserCreatedConfig {

        @Bean
        public Queue userQueue() {
            return new Queue("user.queue.created", true);
        }

        @Bean
        public Binding userCreatedBinding(
                @Qualifier("userQueue") Queue userQueue,
                @Qualifier("topicExchange") TopicExchange userTopicExchange) {
            return BindingBuilder.bind(userQueue).to(userTopicExchange).with("user.created");
        }
    }

    @Configuration
    public static class ContentCountConfig {

        @Bean
        public Queue userContentCountQueue() {
            return new Queue("queue.user.content.counts", true);
        }

        @Bean
        public Binding userContentCountBinding(
                @Qualifier("userContentCountQueue") Queue userContentCountQueue,
                @Qualifier("contentTopicExchange") TopicExchange contentTopicExchange) {
            return BindingBuilder.bind(userContentCountQueue)
                    .to(contentTopicExchange)
                    .with("content.counts");
        }
    }

    @Configuration
    public static class deleteDocumentConfig {
        @Bean
        public Queue deleteDocumentQueue() {
            return new Queue("queue.collab.document", true);
        }

        @Bean
        public Binding documentDeleteBinding(
                @Qualifier("deleteDocumentQueue") Queue queue,
                @Qualifier("documentTopicExchange") TopicExchange topicExchange) {
            return BindingBuilder.bind(queue).to(topicExchange).with("collab.document.deleted");
        }
    }
}
