// 前端輪詢查詢 Consumer 處理結果的示例代碼

/**
 * 發送獲取所有角色請求
 */
async function getAllPeople() {
    try {
        // 1. 發送請求到 Producer
        const response = await fetch('/tymb/people/get-all', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + getJwtToken() // 你的 JWT token
            }
        });
        
        const result = await response.json();
        console.log('Producer 回應:', result);
        
        if (result.requestId) {
            // 2. 開始輪詢查詢結果
            pollForResult(result.requestId);
        }
        
    } catch (error) {
        console.error('發送請求失敗:', error);
    }
}

/**
 * 輪詢查詢處理結果
 */
async function pollForResult(requestId, maxAttempts = 30, interval = 2000) {
    let attempts = 0;
    
    const poll = async () => {
        try {
            attempts++;
            console.log(`第 ${attempts} 次查詢結果...`);
            
            // 查詢結果
            const response = await fetch(`/tymb/people/result/${requestId}`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + getJwtToken()
                }
            });
            
            if (response.status === 202) {
                // 還在處理中，繼續輪詢
                if (attempts < maxAttempts) {
                    setTimeout(poll, interval);
                } else {
                    console.error('查詢超時，請稍後手動查詢');
                    showTimeoutMessage();
                }
                return;
            }
            
            if (response.ok) {
                const result = await response.json();
                console.log('Consumer 處理結果:', result);
                
                if (result.status === 'SUCCESS') {
                    // 處理成功，顯示數據
                    displayPeopleData(result.data);
                    
                    // 清理結果（可選）
                    cleanupResult(requestId);
                } else if (result.status === 'ERROR') {
                    // 處理失敗
                    showErrorMessage(result.message, result.errorDetails);
                }
            } else {
                console.error('查詢失敗:', response.status);
            }
            
        } catch (error) {
            console.error('輪詢查詢失敗:', error);
            if (attempts < maxAttempts) {
                setTimeout(poll, interval);
            }
        }
    };
    
    // 開始輪詢
    poll();
}

/**
 * 檢查結果是否存在（可選的預檢查）
 */
async function checkResultExists(requestId) {
    try {
        const response = await fetch(`/tymb/people/result/${requestId}/exists`, {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + getJwtToken()
            }
        });
        
        if (response.ok) {
            const result = await response.json();
            return result.exists;
        }
        return false;
    } catch (error) {
        console.error('檢查結果存在性失敗:', error);
        return false;
    }
}

/**
 * 清理結果（可選）
 */
async function cleanupResult(requestId) {
    try {
        await fetch(`/tymb/people/result/${requestId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + getJwtToken()
            }
        });
        console.log('結果已清理');
    } catch (error) {
        console.error('清理結果失敗:', error);
    }
}

/**
 * 顯示角色數據
 */
function displayPeopleData(peopleData) {
    console.log('顯示角色數據:', peopleData);
    
    // 這裡實現你的 UI 顯示邏輯
    // 例如：更新表格、列表等
    
    if (Array.isArray(peopleData)) {
        // 如果是數組，顯示所有角色
        peopleData.forEach(person => {
            console.log(`角色: ${person.name}, 職業: ${person.job}, 年齡: ${person.age}`);
        });
    } else if (peopleData.people && Array.isArray(peopleData.people)) {
        // 如果是包含 people 和 count 的對象
        console.log(`總共 ${peopleData.count} 個角色`);
        peopleData.people.forEach(person => {
            console.log(`角色: ${person.name}, 職業: ${person.job}, 年齡: ${person.age}`);
        });
    }
}

/**
 * 顯示錯誤消息
 */
function showErrorMessage(message, details) {
    console.error('處理失敗:', message, details);
    // 實現你的錯誤提示 UI
}

/**
 * 顯示超時消息
 */
function showTimeoutMessage() {
    console.warn('查詢超時，請稍後手動查詢');
    // 實現你的超時提示 UI
}

/**
 * 獲取 JWT Token（根據你的實現方式）
 */
function getJwtToken() {
    // 從 localStorage、sessionStorage 或其他地方獲取
    return localStorage.getItem('jwt_token') || '';
}

// 使用示例
document.addEventListener('DOMContentLoaded', () => {
    // 綁定按鈕事件
    const getPeopleBtn = document.getElementById('getPeopleBtn');
    if (getPeopleBtn) {
        getPeopleBtn.addEventListener('click', getAllPeople);
    }
});
