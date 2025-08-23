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
    
    // 隊列名稱常量（用於 @RabbitListener 註解，需要常量表達式）
    public static final String DAMAGE_CALCULATION_QUEUE = QueueNames.DAMAGE_CALCULATION.getQueueName();
    public static final String PEOPLE_GET_ALL_QUEUE = QueueNames.PEOPLE_GET_ALL.getQueueName();
    public static final String PEOPLE_INSERT_QUEUE = QueueNames.PEOPLE_INSERT.getQueueName();
    public static final String PEOPLE_UPDATE_QUEUE = QueueNames.PEOPLE_UPDATE.getQueueName();
    public static final String PEOPLE_INSERT_MULTIPLE_QUEUE = QueueNames.PEOPLE_INSERT_MULTIPLE.getQueueName();
    public static final String PEOPLE_GET_BY_NAME_QUEUE = QueueNames.PEOPLE_GET_BY_NAME.getQueueName();
    public static final String PEOPLE_DELETE_QUEUE = QueueNames.PEOPLE_DELETE.getQueueName();
    public static final String PEOPLE_DAMAGE_CALCULATION_QUEUE = QueueNames.PEOPLE_DAMAGE_CALCULATION.getQueueName();
    public static final String WEAPON_GET_ALL_QUEUE = QueueNames.WEAPON_GET_ALL.getQueueName();
    public static final String WEAPON_GET_BY_NAME_QUEUE = QueueNames.WEAPON_GET_BY_NAME.getQueueName();
    public static final String WEAPON_GET_BY_OWNER_QUEUE = QueueNames.WEAPON_GET_BY_OWNER.getQueueName();
    public static final String WEAPON_SAVE_QUEUE = QueueNames.WEAPON_SAVE.getQueueName();
    public static final String WEAPON_DELETE_QUEUE = QueueNames.WEAPON_DELETE.getQueueName();
    public static final String WEAPON_DELETE_ALL_QUEUE = QueueNames.WEAPON_DELETE_ALL.getQueueName();
    public static final String WEAPON_EXISTS_QUEUE = QueueNames.WEAPON_EXISTS.getQueueName();
    public static final String WEAPON_UPDATE_ATTRIBUTES_QUEUE = QueueNames.WEAPON_UPDATE_ATTRIBUTES.getQueueName();
    public static final String WEAPON_UPDATE_BASE_DAMAGE_QUEUE = QueueNames.WEAPON_UPDATE_BASE_DAMAGE.getQueueName();
    
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
        return QueueBuilder.durable(QueueNames.DAMAGE_CALCULATION.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 創建角色列表獲取隊列
     */
    @Bean
    public Queue peopleGetAllQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_GET_ALL.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 創建 People 模組隊列
     */
    @Bean
    public Queue peopleInsertQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_INSERT.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue peopleUpdateQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_UPDATE.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue peopleInsertMultipleQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_INSERT_MULTIPLE.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue peopleGetByNameQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_GET_BY_NAME.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue peopleDeleteQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_DELETE.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue peopleDamageCalculationQueue() {
        return QueueBuilder.durable(QueueNames.PEOPLE_DAMAGE_CALCULATION.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 創建 Weapon 模組隊列
     */
    @Bean
    public Queue weaponGetAllQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_GET_ALL.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponGetByNameQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_GET_BY_NAME.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponGetByOwnerQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_GET_BY_OWNER.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponSaveQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_SAVE.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponDeleteQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_DELETE.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponDeleteAllQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_DELETE_ALL.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponExistsQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_EXISTS.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponUpdateAttributesQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_UPDATE_ATTRIBUTES.getQueueName())
                .withArgument("x-message-ttl", 300000) // 5分鐘 TTL
                .build();
    }
    
    @Bean
    public Queue weaponUpdateBaseDamageQueue() {
        return QueueBuilder.durable(QueueNames.WEAPON_UPDATE_BASE_DAMAGE.getQueueName())
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
     * 綁定 People 模組隊列到交換機
     */
    @Bean
    public Binding peopleInsertBinding(Queue peopleInsertQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleInsertQueue)
                .to(tymbExchange)
                .with("people.insert");
    }
    
    @Bean
    public Binding peopleUpdateBinding(Queue peopleUpdateQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleUpdateQueue)
                .to(tymbExchange)
                .with("people.update");
    }
    
    @Bean
    public Binding peopleInsertMultipleBinding(Queue peopleInsertMultipleQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleInsertMultipleQueue)
                .to(tymbExchange)
                .with("people.insert.multiple");
    }
    
    @Bean
    public Binding peopleGetByNameBinding(Queue peopleGetByNameQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleGetByNameQueue)
                .to(tymbExchange)
                .with("people.get.by.name");
    }
    
    @Bean
    public Binding peopleDeleteBinding(Queue peopleDeleteQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleDeleteQueue)
                .to(tymbExchange)
                .with("people.delete");
    }
    
    @Bean
    public Binding peopleDamageCalculationBinding(Queue peopleDamageCalculationQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleDamageCalculationQueue)
                .to(tymbExchange)
                .with("people.damage.calculation");
    }
    
    /**
     * 綁定 Weapon 模組隊列到交換機
     */
    @Bean
    public Binding weaponGetAllBinding(Queue weaponGetAllQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponGetAllQueue)
                .to(tymbExchange)
                .with("weapon.get.all");
    }
    
    @Bean
    public Binding weaponGetByNameBinding(Queue weaponGetByNameQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponGetByNameQueue)
                .to(tymbExchange)
                .with("weapon.get.by.name");
    }
    
    @Bean
    public Binding weaponGetByOwnerBinding(Queue weaponGetByOwnerQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponGetByOwnerQueue)
                .to(tymbExchange)
                .with("weapon.get.by.owner");
    }
    
    @Bean
    public Binding weaponSaveBinding(Queue weaponSaveQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponSaveQueue)
                .to(tymbExchange)
                .with("weapon.save");
    }
    
    @Bean
    public Binding weaponDeleteBinding(Queue weaponDeleteQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponDeleteQueue)
                .to(tymbExchange)
                .with("weapon.delete");
    }
    
    @Bean
    public Binding weaponDeleteAllBinding(Queue weaponDeleteAllQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponDeleteAllQueue)
                .to(tymbExchange)
                .with("weapon.delete.all");
    }
    
    @Bean
    public Binding weaponExistsBinding(Queue weaponExistsQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponExistsQueue)
                .to(tymbExchange)
                .with("weapon.exists");
    }
    
    @Bean
    public Binding weaponUpdateAttributesBinding(Queue weaponUpdateAttributesQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponUpdateAttributesQueue)
                .to(tymbExchange)
                .with("weapon.update.attributes");
    }
    
    @Bean
    public Binding weaponUpdateBaseDamageBinding(Queue weaponUpdateBaseDamageQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponUpdateBaseDamageQueue)
                .to(tymbExchange)
                .with("weapon.update.base.damage");
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
