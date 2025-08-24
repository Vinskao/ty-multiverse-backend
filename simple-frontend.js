// 簡化的前端實現 - 重點展示 requestId 的使用

/**
 * 獲取所有角色（簡化版）
 */
async function getPeopleSimple() {
    try {
        console.log('1. 發送請求到 Producer...');
        
        // 發送請求到 Producer
        const response = await fetch('/tymb/people/get-all', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + getJwtToken()
            }
        });
        
        const result = await response.json();
        console.log('2. Producer 回應:', result);
        
        // 獲取 requestId
        const requestId = result.requestId;
        if (!requestId) {
            alert('錯誤：未收到 requestId');
            return;
        }
        
        console.log('3. 獲取到 requestId:', requestId);
        
        // 開始查詢結果
        checkResult(requestId);
        
    } catch (error) {
        console.error('發送請求失敗:', error);
        alert('發送請求失敗: ' + error.message);
    }
}

/**
 * 查詢結果（簡化版）
 */
async function checkResult(requestId) {
    console.log('4. 開始查詢結果...');
    
    // 每 2 秒查詢一次，最多查詢 15 次（30 秒）
    let attempts = 0;
    const maxAttempts = 15;
    
    const poll = async () => {
        attempts++;
        console.log(`第 ${attempts} 次查詢...`);
        
        try {
            const response = await fetch(`/tymb/people/result/${requestId}`, {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + getJwtToken()
                }
            });
            
            if (response.status === 202) {
                // 還在處理中
                if (attempts < maxAttempts) {
                    console.log('還在處理中，2秒後再查詢...');
                    setTimeout(poll, 2000);
                } else {
                    alert('查詢超時，請稍後手動查詢');
                }
                return;
            }
            
            if (response.ok) {
                const result = await response.json();
                console.log('5. 查詢到結果:', result);
                
                if (result.status === 'SUCCESS') {
                    // 成功！顯示數據
                    displayResult(result);
                } else {
                    alert('處理失敗: ' + result.message);
                }
            } else {
                alert('查詢失敗: ' + response.status);
            }
            
        } catch (error) {
            console.error('查詢失敗:', error);
            if (attempts < maxAttempts) {
                setTimeout(poll, 2000);
            }
        }
    };
    
    // 開始查詢
    poll();
}

/**
 * 顯示結果
 */
function displayResult(result) {
    console.log('6. 顯示結果...');
    
    const data = result.data;
    const count = result.count || 0;
    
    // 創建結果顯示區域
    const resultDiv = document.getElementById('result') || createResultDiv();
    
    let html = `<h3>查詢成功！總共 ${count} 個角色</h3>`;
    
    if (Array.isArray(data)) {
        html += '<table border="1" style="border-collapse: collapse; width: 100%;">';
        html += '<tr><th>姓名</th><th>職業</th><th>年齡</th><th>性別</th></tr>';
        
        data.forEach(person => {
            html += `<tr>
                <td>${person.name || 'N/A'}</td>
                <td>${person.job || 'N/A'}</td>
                <td>${person.age || 'N/A'}</td>
                <td>${person.gender || 'N/A'}</td>
            </tr>`;
        });
        
        html += '</table>';
    } else {
        html += '<p>數據格式不正確</p>';
    }
    
    resultDiv.innerHTML = html;
}

/**
 * 創建結果顯示區域
 */
function createResultDiv() {
    const div = document.createElement('div');
    div.id = 'result';
    div.style.marginTop = '20px';
    document.body.appendChild(div);
    return div;
}

/**
 * 獲取 JWT Token
 */
function getJwtToken() {
    // 根據你的實現方式獲取 token
    return localStorage.getItem('jwt_token') || '';
}

// 使用示例
document.addEventListener('DOMContentLoaded', () => {
    // 創建按鈕
    const button = document.createElement('button');
    button.textContent = '獲取所有角色';
    button.onclick = getPeopleSimple;
    button.style.padding = '10px 20px';
    button.style.fontSize = '16px';
    
    document.body.appendChild(button);
    
    // 創建說明
    const info = document.createElement('div');
    info.innerHTML = `
        <h2>使用說明</h2>
        <ol>
            <li>點擊按鈕發送請求到 Producer</li>
            <li>Producer 返回 requestId</li>
            <li>使用 requestId 輪詢查詢結果</li>
            <li>Consumer 處理完成後返回數據</li>
            <li>前端顯示結果</li>
        </ol>
    `;
    info.style.marginBottom = '20px';
    document.body.insertBefore(info, button);
});
