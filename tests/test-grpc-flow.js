#!/usr/bin/env node

/**
 * 测试 Gateway → gRPC → Backend 流程
 * 
 * 验证路径: 前端 → Gateway REST → Gateway gRPC Client → Backend gRPC Service → Backend Module Service → 数据库
 */

const GATEWAY_BASE = 'http://localhost:8082/tymg';

const tests = [
  {
    name: 'People - Get All (Gateway → gRPC)',
    method: 'GET',
    url: `${GATEWAY_BASE}/people/get-all`,
    expectedStatus: [200, 202],
    description: 'Gateway PeopleController → PeopleGrpcClient → Backend GrpcPeopleServiceImpl → PeopleService'
  },
  {
    name: 'Weapons - Get All (Gateway → gRPC)',
    method: 'GET',
    url: `${GATEWAY_BASE}/weapons`,
    expectedStatus: [200],
    description: 'Gateway WeaponController → WeaponGrpcClient → Backend GrpcWeaponServiceImpl → WeaponService'
  },
  {
    name: 'Gallery - Get All (Gateway → gRPC)',
    method: 'POST',
    url: `${GATEWAY_BASE}/gallery/getAll`,
    expectedStatus: [200],
    description: 'Gateway GalleryController → GalleryGrpcClient → Backend GrpcGalleryServiceImpl → GalleryService'
  },
  {
    name: 'Deckofcards - Blackjack Status (Gateway → gRPC)',
    method: 'GET',
    url: `${GATEWAY_BASE}/deckofcards/blackjack/status`,
    expectedStatus: [200],
    description: 'Gateway DeckofcardsController → DeckofcardsGrpcClient → Backend GrpcDeckofcardsServiceImpl'
  },
  {
    name: 'People Images - Direct Route (Spring Cloud Gateway)',
    method: 'GET',
    url: `${GATEWAY_BASE}/people-images`,
    expectedStatus: [200, 404], // 404 if no images, but route should work
    description: 'Spring Cloud Gateway Route → Backend PeopleImageController (REST) - Should Keep This'
  }
];

async function testEndpoint(test) {
  try {
    console.log(`\n${'='.repeat(80)}`);
    console.log(`📝 测试: ${test.name}`);
    console.log(`🔗 URL: ${test.url}`);
    console.log(`📊 方法: ${test.method}`);
    console.log(`📋 流程: ${test.description}`);
    console.log(`${'='.repeat(80)}`);

    const options = {
      method: test.method,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    };

    const startTime = Date.now();
    const response = await fetch(test.url, options);
    const duration = Date.now() - startTime;

    let data;
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }

    const passed = test.expectedStatus.includes(response.status);
    const status = passed ? '✅ PASS' : '❌ FAIL';

    console.log(`\n${status}`);
    console.log(`📡 状态码: ${response.status} ${response.statusText}`);
    console.log(`⏱️  响应时间: ${duration}ms`);
    
    if (response.ok) {
      console.log(`✅ Gateway 成功转发请求到 Backend gRPC`);
      if (typeof data === 'object') {
        console.log(`📦 响应数据类型: ${Array.isArray(data) ? 'Array' : 'Object'}`);
        if (Array.isArray(data)) {
          console.log(`📊 数组长度: ${data.length}`);
        } else if (data.people && Array.isArray(data.people)) {
          console.log(`📊 People 数量: ${data.people.length}`);
        }
      }
    } else {
      console.log(`⚠️  响应内容: ${typeof data === 'object' ? JSON.stringify(data, null, 2) : data}`);
    }

    return { test: test.name, passed, status: response.status, duration, data };
  } catch (error) {
    console.log(`\n❌ 网络错误`);
    console.log(`🔴 错误: ${error.message}`);
    return { test: test.name, passed: false, error: error.message };
  }
}

async function main() {
  console.log('\n🚀 开始测试 Gateway → gRPC → Backend 流程\n');
  console.log(`Gateway 地址: ${GATEWAY_BASE}`);
  console.log(`测试数量: ${tests.length}`);

  const results = [];
  for (const test of tests) {
    const result = await testEndpoint(test);
    results.push(result);
    await new Promise(resolve => setTimeout(resolve, 500)); // 延迟避免过载
  }

  // 统计
  console.log(`\n${'='.repeat(80)}`);
  console.log('📊 测试总结');
  console.log(`${'='.repeat(80)}`);

  const passed = results.filter(r => r.passed).length;
  const failed = results.filter(r => !r.passed).length;
  const total = results.length;

  console.log(`\n总测试数: ${total}`);
  console.log(`✅ 通过: ${passed}`);
  console.log(`❌ 失败: ${failed}`);
  console.log(`📈 成功率: ${((passed / total) * 100).toFixed(2)}%`);

  if (passed === total) {
    console.log('\n🎉 所有测试通过！Gateway → gRPC → Backend 流程正常工作');
    console.log('✅ 可以安全删除 Backend module 层的 REST Controllers');
    console.log('⚠️  保留 PeopleImageController - 因为 Spring Cloud Gateway 直接转发');
  } else {
    console.log('\n⚠️  部分测试失败，请检查服务是否正常运行');
    console.log('   - Backend 是否启动？(Port 8080)');
    console.log('   - Gateway 是否启动？(Port 8082)');
    console.log('   - gRPC 服务是否正常？(Port 50051)');
  }

  console.log('\n');
}

main().catch(error => {
  console.error('❌ 测试脚本执行失败:', error);
  process.exit(1);
});

