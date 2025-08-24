import React, { useState, useEffect } from 'react';

const PeopleList = () => {
    const [loading, setLoading] = useState(false);
    const [people, setPeople] = useState([]);
    const [error, setError] = useState(null);
    const [pollingStatus, setPollingStatus] = useState('');

    const getAllPeople = async () => {
        setLoading(true);
        setError(null);
        setPollingStatus('發送請求中...');

        try {
            // 1. 發送請求
            const response = await fetch('/tymb/people/get-all', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + localStorage.getItem('jwt_token')
                }
            });

            const result = await response.json();
            console.log('Producer 回應:', result);

            if (result.requestId) {
                // 2. 開始輪詢
                pollForResult(result.requestId);
            } else {
                setError('未收到 requestId');
                setLoading(false);
            }

        } catch (error) {
            setError('發送請求失敗: ' + error.message);
            setLoading(false);
        }
    };

    const pollForResult = async (requestId, maxAttempts = 30) => {
        let attempts = 0;

        const poll = async () => {
            try {
                attempts++;
                setPollingStatus(`第 ${attempts} 次查詢結果...`);

                const response = await fetch(`/tymb/people/result/${requestId}`, {
                    method: 'GET',
                    headers: {
                        'Authorization': 'Bearer ' + localStorage.getItem('jwt_token')
                    }
                });

                if (response.status === 202) {
                    // 還在處理中
                    if (attempts < maxAttempts) {
                        setTimeout(poll, 2000);
                    } else {
                        setError('查詢超時，請稍後重試');
                        setLoading(false);
                    }
                    return;
                }

                if (response.ok) {
                    const result = await response.json();
                    console.log('Consumer 處理結果:', result);

                    if (result.status === 'SUCCESS') {
                        // 處理成功
                        if (result.data && result.data.people) {
                            setPeople(result.data.people);
                        } else if (Array.isArray(result.data)) {
                            setPeople(result.data);
                        }
                        setPollingStatus('處理完成！');
                        setLoading(false);

                        // 清理結果
                        cleanupResult(requestId);
                    } else if (result.status === 'ERROR') {
                        setError(result.message);
                        setLoading(false);
                    }
                } else {
                    setError('查詢失敗: ' + response.status);
                    setLoading(false);
                }

            } catch (error) {
                console.error('輪詢失敗:', error);
                if (attempts < maxAttempts) {
                    setTimeout(poll, 2000);
                } else {
                    setError('輪詢失敗: ' + error.message);
                    setLoading(false);
                }
            }
        };

        poll();
    };

    const cleanupResult = async (requestId) => {
        try {
            await fetch(`/tymb/people/result/${requestId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': 'Bearer ' + localStorage.getItem('jwt_token')
                }
            });
        } catch (error) {
            console.error('清理結果失敗:', error);
        }
    };

    return (
        <div className="people-list">
            <h2>角色列表</h2>
            
            <button 
                onClick={getAllPeople} 
                disabled={loading}
                className="btn btn-primary"
            >
                {loading ? '處理中...' : '獲取所有角色'}
            </button>

            {pollingStatus && (
                <div className="polling-status">
                    <p>{pollingStatus}</p>
                </div>
            )}

            {error && (
                <div className="error-message">
                    <p style={{ color: 'red' }}>{error}</p>
                </div>
            )}

            {people.length > 0 && (
                <div className="people-data">
                    <h3>總共 {people.length} 個角色</h3>
                    <table className="table">
                        <thead>
                            <tr>
                                <th>姓名</th>
                                <th>職業</th>
                                <th>年齡</th>
                                <th>性別</th>
                                <th>種族</th>
                            </tr>
                        </thead>
                        <tbody>
                            {people.map((person, index) => (
                                <tr key={index}>
                                    <td>{person.name}</td>
                                    <td>{person.job}</td>
                                    <td>{person.age}</td>
                                    <td>{person.gender}</td>
                                    <td>{person.race}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default PeopleList;
