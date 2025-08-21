package tw.com.tymbackend.core.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置類別
 * 
 * 根據環境變數 RABBITMQ_ENABLED 決定是否啟用 RabbitMQ
 * - 本機環境：RABBITMQ_ENABLED=false，禁用 RabbitMQ
 * - Production 環境：RABBITMQ_ENABLED=true，啟用 RabbitMQ
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class RabbitMQConfig {
    
    // 隊列名稱定義
    public static final String DAMAGE_CALCULATION_QUEUE = "damage-calculation";
    public static final String PEOPLE_GET_ALL_QUEUE = "people-get-all";
    
    // 交換機名稱
    public static final String TYMB_EXCHANGE = "tymb-exchange";
    
    /**
     * 創建 RabbitMQ 交換機
     */
    @Bean
    public DirectExchange tymbExchange() {
        return new DirectExchange(TYMB_EXCHANGE);
    }
    
    /**
     * 創建傷害計算隊列
     */
    @Bean
    public Queue damageCalculationQueue() {
        return QueueBuilder.durable(DAMAGE_CALCULATION_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 創建角色列表獲取隊列
     */
    @Bean
    public Queue peopleGetAllQueue() {
        return QueueBuilder.durable(PEOPLE_GET_ALL_QUEUE)
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 綁定傷害計算隊列到交換機
     */
    @Bean
    public Binding damageCalculationBinding(Queue damageCalculationQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(damageCalculationQueue)
                .to(tymbExchange)
                .with("damage.calculation");
    }
    
    /**
     * 綁定角色列表隊列到交換機
     */
    @Bean
    public Binding peopleGetAllBinding(Queue peopleGetAllQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleGetAllQueue)
                .to(tymbExchange)
                .with("people.get.all");
    }
    
    /**
     * 配置 JSON 消息轉換器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 配置 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
