package tw.com.tymbackend.core.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

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
@EnableRabbit
//@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true") // 臨時註釋掉條件進行測試
public class RabbitMQConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);
    
    @PostConstruct
    public void init() {
        logger.info("=== RabbitMQConfig 已初始化 ===");
        logger.info("RabbitMQ 配置已啟用");
    }
    
    // 隊列名稱定義
    public static final String DAMAGE_CALCULATION_QUEUE = "damage-calculation";
    public static final String PEOPLE_GET_ALL_QUEUE = "people-get-all";
    public static final String PEOPLE_GET_BY_NAME_QUEUE = "people-get-by-name";
    public static final String PEOPLE_GET_NAMES_QUEUE = "people-get-names";
    public static final String PEOPLE_INSERT_QUEUE = "people-insert";
    public static final String PEOPLE_UPDATE_QUEUE = "people-update";
    public static final String PEOPLE_DELETE_ALL_QUEUE = "people-delete-all";
    public static final String WEAPON_GET_ALL_QUEUE = "weapon-get-all";
    public static final String WEAPON_GET_BY_NAME_QUEUE = "weapon-get-by-name";
    public static final String WEAPON_GET_BY_OWNER_QUEUE = "weapon-get-by-owner";
    public static final String WEAPON_SAVE_QUEUE = "weapon-save";
    public static final String WEAPON_DELETE_QUEUE = "weapon-delete";
    public static final String WEAPON_DELETE_ALL_QUEUE = "weapon-delete-all";
    public static final String WEAPON_EXISTS_QUEUE = "weapon-exists";
    public static final String DECKOFCARDS_QUEUE = "deckofcards";
    public static final String ASYNC_RESULT_QUEUE = "async-result";
    
    // 交換機名稱
    public static final String TYMB_EXCHANGE = "tymb-exchange";
    
    /**
     * 創建 RabbitMQ 交換機
     */
    @Bean
    public DirectExchange tymbExchange() {
        return new DirectExchange(TYMB_EXCHANGE);
    }
    
    // TTL 設定（5分鐘 = 300000毫秒）
    private static final long MESSAGE_TTL = 300000;
    
    /**
     * 創建傷害計算隊列
     */
    @Bean
    public Queue damageCalculationQueue() {
        return QueueBuilder.durable(DAMAGE_CALCULATION_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 創建角色列表獲取隊列
     */
    @Bean
    public Queue peopleGetAllQueue() {
        return QueueBuilder.durable(PEOPLE_GET_ALL_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建角色按名稱獲取隊列
     */
    @Bean
    public Queue peopleGetByNameQueue() {
        return QueueBuilder.durable(PEOPLE_GET_BY_NAME_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建角色獲取名稱隊列
     */
    @Bean
    public Queue peopleGetNamesQueue() {
        return QueueBuilder.durable(PEOPLE_GET_NAMES_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建角色新增隊列
     */
    @Bean
    public Queue peopleInsertQueue() {
        return QueueBuilder.durable(PEOPLE_INSERT_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建角色更新隊列
     */
    @Bean
    public Queue peopleUpdateQueue() {
        return QueueBuilder.durable(PEOPLE_UPDATE_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建角色刪除全部隊列
     */
    @Bean
    public Queue peopleDeleteAllQueue() {
        return QueueBuilder.durable(PEOPLE_DELETE_ALL_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器獲取全部隊列
     */
    @Bean
    public Queue weaponGetAllQueue() {
        return QueueBuilder.durable(WEAPON_GET_ALL_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器按名稱獲取隊列
     */
    @Bean
    public Queue weaponGetByNameQueue() {
        return QueueBuilder.durable(WEAPON_GET_BY_NAME_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器按擁有者獲取隊列
     */
    @Bean
    public Queue weaponGetByOwnerQueue() {
        return QueueBuilder.durable(WEAPON_GET_BY_OWNER_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器保存隊列
     */
    @Bean
    public Queue weaponSaveQueue() {
        return QueueBuilder.durable(WEAPON_SAVE_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器刪除隊列
     */
    @Bean
    public Queue weaponDeleteQueue() {
        return QueueBuilder.durable(WEAPON_DELETE_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器刪除全部隊列
     */
    @Bean
    public Queue weaponDeleteAllQueue() {
        return QueueBuilder.durable(WEAPON_DELETE_ALL_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 創建武器存在檢查隊列
     */
    @Bean
    public Queue weaponExistsQueue() {
        return QueueBuilder.durable(WEAPON_EXISTS_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
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
     * 綁定角色按名稱獲取隊列到交換機
     */
    @Bean
    public Binding peopleGetByNameBinding(Queue peopleGetByNameQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleGetByNameQueue)
                .to(tymbExchange)
                .with("people.get.by.name");
    }

    /**
     * 綁定角色刪除全部隊列到交換機
     */
    @Bean
    public Binding peopleDeleteAllBinding(Queue peopleDeleteAllQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(peopleDeleteAllQueue)
                .to(tymbExchange)
                .with("people.delete.all");
    }

    /**
     * 綁定武器獲取全部隊列到交換機
     */
    @Bean
    public Binding weaponGetAllBinding(Queue weaponGetAllQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponGetAllQueue)
                .to(tymbExchange)
                .with("weapon.get.all");
    }

    /**
     * 綁定武器按名稱獲取隊列到交換機
     */
    @Bean
    public Binding weaponGetByNameBinding(Queue weaponGetByNameQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponGetByNameQueue)
                .to(tymbExchange)
                .with("weapon.get.by.name");
    }

    /**
     * 綁定武器按擁有者獲取隊列到交換機
     */
    @Bean
    public Binding weaponGetByOwnerBinding(Queue weaponGetByOwnerQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponGetByOwnerQueue)
                .to(tymbExchange)
                .with("weapon.get.by.owner");
    }

    /**
     * 綁定武器保存隊列到交換機
     */
    @Bean
    public Binding weaponSaveBinding(Queue weaponSaveQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponSaveQueue)
                .to(tymbExchange)
                .with("weapon.save");
    }

    /**
     * 綁定武器刪除隊列到交換機
     */
    @Bean
    public Binding weaponDeleteBinding(Queue weaponDeleteQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponDeleteQueue)
                .to(tymbExchange)
                .with("weapon.delete");
    }

    /**
     * 綁定武器刪除全部隊列到交換機
     */
    @Bean
    public Binding weaponDeleteAllBinding(Queue weaponDeleteAllQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponDeleteAllQueue)
                .to(tymbExchange)
                .with("weapon.delete.all");
    }

    /**
     * 綁定武器存在檢查隊列到交換機
     */
    @Bean
    public Binding weaponExistsBinding(Queue weaponExistsQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(weaponExistsQueue)
                .to(tymbExchange)
                .with("weapon.exists");
    }

    /**
     * 創建 Deckofcards 隊列
     */
    @Bean
    public Queue deckofcardsQueue() {
        return QueueBuilder.durable(DECKOFCARDS_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }

    /**
     * 綁定 Deckofcards 隊列到交換機
     */
    @Bean
    public Binding deckofcardsBinding(Queue deckofcardsQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(deckofcardsQueue)
                .to(tymbExchange)
                .with("deckofcards");
    }
    
    /**
     * 創建異步結果隊列
     */
    @Bean
    public Queue asyncResultQueue() {
        return QueueBuilder.durable(ASYNC_RESULT_QUEUE)
                .withArgument("x-message-ttl", MESSAGE_TTL) // 5分鐘 TTL
                .build();
    }
    
    /**
     * 綁定異步結果隊列到交換機
     */
    @Bean
    public Binding asyncResultBinding(Queue asyncResultQueue, DirectExchange tymbExchange) {
        return BindingBuilder.bind(asyncResultQueue)
                .to(tymbExchange)
                .with("async.result");
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

    /**
     * 使用 Virtual Threads 的 Listener Container Factory
     * 綁定到預設名稱 rabbitListenerContainerFactory，讓 @RabbitListener 自動套用
     * 在資源受限環境下，保守地限制並發與預取，避免 CPU 飆升
     */
    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 使用 Virtual Threads 作為任務執行器
        factory.setTaskExecutor(java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor());
        // 資源受限下的保守並發配置
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
