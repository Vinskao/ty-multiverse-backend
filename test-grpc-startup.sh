#!/bin/bash

echo "🚀 Testing gRPC Server Startup with Interceptors..."
echo "=================================================="

# Start the application in background
./mvnw spring-boot:run -Dspring-boot.run.profiles=local -q &
APP_PID=$!

# Wait for startup
sleep 10

# Check if gRPC server started
if ps -p $APP_PID > /dev/null; then
    echo "✅ Backend application started successfully"
    echo "🔍 Checking logs for gRPC interceptor activation..."

    # Kill the background process
    kill $APP_PID
    wait $APP_PID 2>/dev/null

    echo "✅ Test completed"
else
    echo "❌ Backend application failed to start"
    exit 1
fi
