package tw.com.tymbackend.core.config;

/**
 * RabbitMQ 隊列名稱枚舉
 * 
 * 統一管理所有隊列名稱，避免硬編碼
 * 
 * @author TY Backend Team
 * @version 1.0
 * @since 2024
 */
public enum QueueNames {
    
    // 通用隊列
    DAMAGE_CALCULATION("damage-calculation"),
    PEOPLE_GET_ALL("people-get-all"),
    
    // People 模組隊列
    PEOPLE_INSERT("people-insert"),
    PEOPLE_UPDATE("people-update"),
    PEOPLE_INSERT_MULTIPLE("people-insert-multiple"),
    PEOPLE_GET_BY_NAME("people-get-by-name"),
    PEOPLE_DELETE("people-delete"),
    PEOPLE_DELETE_ALL("people-delete-all"),
    PEOPLE_DAMAGE_CALCULATION("people-damage-calculation"),
    
    // Weapon 模組隊列
    WEAPON_GET_ALL("weapon-get-all"),
    WEAPON_GET_BY_NAME("weapon-get-by-name"),
    WEAPON_GET_BY_OWNER("weapon-get-by-owner"),
    WEAPON_SAVE("weapon-save"),
    WEAPON_DELETE("weapon-delete"),
    WEAPON_DELETE_ALL("weapon-delete-all"),
    WEAPON_EXISTS("weapon-exists"),
    WEAPON_UPDATE_ATTRIBUTES("weapon-update-attributes"),
    WEAPON_UPDATE_BASE_DAMAGE("weapon-update-base-damage");
    
    private final String queueName;
    
    QueueNames(String queueName) {
        this.queueName = queueName;
    }
    
    public String getQueueName() {
        return queueName;
    }
    
    @Override
    public String toString() {
        return queueName;
    }
    
    /**
     * 根據隊列名稱獲取枚舉
     */
    public static QueueNames fromQueueName(String queueName) {
        for (QueueNames queue : values()) {
            if (queue.queueName.equals(queueName)) {
                return queue;
            }
        }
        throw new IllegalArgumentException("未知的隊列名稱: " + queueName);
    }
    
    /**
     * 檢查隊列名稱是否存在
     */
    public static boolean contains(String queueName) {
        for (QueueNames queue : values()) {
            if (queue.queueName.equals(queueName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 獲取所有 People 模組相關的隊列
     */
    public static QueueNames[] getPeopleQueues() {
        return new QueueNames[]{
            PEOPLE_INSERT,
            PEOPLE_UPDATE,
            PEOPLE_INSERT_MULTIPLE,
            PEOPLE_GET_ALL,
            PEOPLE_GET_BY_NAME,
            PEOPLE_DELETE,
            PEOPLE_DELETE_ALL,
            PEOPLE_DAMAGE_CALCULATION
        };
    }
    
    /**
     * 獲取所有 Weapon 模組相關的隊列
     */
    public static QueueNames[] getWeaponQueues() {
        return new QueueNames[]{
            WEAPON_GET_ALL,
            WEAPON_GET_BY_NAME,
            WEAPON_GET_BY_OWNER,
            WEAPON_SAVE,
            WEAPON_DELETE,
            WEAPON_DELETE_ALL,
            WEAPON_EXISTS,
            WEAPON_UPDATE_ATTRIBUTES,
            WEAPON_UPDATE_BASE_DAMAGE
        };
    }
}
