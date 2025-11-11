#!/usr/bin/env node

/**
 * SecurityConfig Coverage Test
 *
 * Tests whether all endpoints defined in AGENTS.md are covered by SecurityConfig.java
 *
 * Usage: node test-security-config-coverage.js
 */

const { execSync } = require('child_process');
const path = require('path');

// Extract all requestMatchers from SecurityConfig using grep and sed
const securityConfigPath = path.join(__dirname, 'ty-multiverse-backend/src/main/java/tw/com/tymbackend/core/config/security/SecurityConfig.java');

let requestMatchers = [];
try {
    const grepResult = execSync(`grep -o '\\.requestMatchers([^)]*)' "${securityConfigPath}" | sed 's/\\.requestMatchers(//' | sed 's/)$//' | sed 's/"//g' | sed 's/ //g'`, { encoding: 'utf8' });
    requestMatchers = grepResult.trim().split('\n').filter(line => line.length > 0);
    console.log('Found requestMatchers in SecurityConfig:', requestMatchers.length);
} catch (error) {
    console.error('Error extracting requestMatchers:', error.message);
    process.exit(1);
}

// Define all endpoints from AGENTS.md
const expectedEndpoints = [
    // Public endpoints (permitAll)
    '/tymb/actuator/**',
    '/tymb/health/**',
    '/tymb/swagger-ui/**',
    '/tymb/v3/api-docs/**',
    '/tymb/webjars/**',
    '/tymb/auth/visitor',
    '/tymb/auth/health',
    '/tymb/keycloak/introspect',

    // SELECT endpoints (GET + authenticated)
    'GET,/tymb/people/**',
    'GET,/tymb/weapons/**',
    'GET,/tymb/gallery/**',
    'GET,/tymb/api/**',
    'GET,/tymb/people-images/**',
    'GET,/tymb/blackjack/**',

    // CRUD endpoints (POST/PUT/DELETE + authenticated)
    'POST,/tymb/people/**',
    'PUT,/tymb/people/**',
    'DELETE,/tymb/people/**',

    'POST,/tymb/weapons/**',
    'PUT,/tymb/weapons/**',
    'DELETE,/tymb/weapons/**',

    'POST,/tymb/gallery/**',
    'PUT,/tymb/gallery/**',
    'DELETE,/tymb/gallery/**',

    'POST,/tymb/api/**',
    'PUT,/tymb/api/**',
    'DELETE,/tymb/api/**',

    'POST,/tymb/people-images/**',
    'PUT,/tymb/people-images/**',
    'DELETE,/tymb/people-images/**',

    'POST,/tymb/blackjack/**',
    'PUT,/tymb/blackjack/**',
    'DELETE,/tymb/blackjack/**',

    // Admin-only bulk delete endpoints
    'DELETE,/tymb/people/delete-all',
    'DELETE,/tymb/weapons/delete-all',
    'DELETE,/tymb/gallery/delete-all',

    // Auth test endpoints (authenticated)
    '/tymb/auth/admin',
    '/tymb/auth/user',
    '/tymb/auth/test',
    '/tymb/auth/logout-test'
];

// Debug: Show extracted requestMatchers
console.log('📋 Extracted requestMatchers from SecurityConfig:');
requestMatchers.forEach((matcher, index) => {
    console.log(`  ${index + 1}. ${matcher}`);
});
console.log();

// Test coverage
console.log('🔍 SecurityConfig Coverage Test\n');
console.log('=' .repeat(80));

let coveredCount = 0;
let totalCount = expectedEndpoints.length;

expectedEndpoints.forEach(endpoint => {
    // Check if endpoint is covered by any requestMatcher
    const isCovered = requestMatchers.some(matcher => {
        if (endpoint.includes(',')) {
            // Handle HTTP method + path patterns (e.g., "GET,/tymb/people/**")
            const [method, path] = endpoint.split(',');
            return matcher === `${method},${path}`;
        } else {
            // Handle simple path patterns (e.g., "/tymb/auth/admin")
            return matcher === endpoint;
        }
    });

    if (isCovered) {
        console.log(`✅ ${endpoint}`);
        coveredCount++;
    } else {
        console.log(`❌ ${endpoint} - NOT COVERED`);
        // Debug: show what we're looking for vs what's available
        console.log(`   Looking for: ${endpoint}`);
        console.log(`   Available matchers: ${requestMatchers.join(', ')}`);
        console.log();
    }
});

console.log('\n' + '=' .repeat(80));
console.log(`📊 Coverage: ${coveredCount}/${totalCount} (${Math.round(coveredCount/totalCount*100)}%)`);

if (coveredCount === totalCount) {
    console.log('🎉 All endpoints from AGENTS.md are covered by SecurityConfig!');
    console.log('🔒 Security configuration is complete and comprehensive.');
    process.exit(0);
} else {
    console.log('⚠️  Some endpoints are not covered. Please check SecurityConfig.java');
    console.log('\n💡 Tips:');
    console.log('  - Check if HTTP methods are specified correctly (GET, POST, PUT, DELETE)');
    console.log('  - Verify path patterns match exactly');
    console.log('  - Ensure admin endpoints use hasRole("ADMIN")');
    process.exit(1);
}
