#!/bin/bash

echo "MSA 분산 트레이싱 데이터 생성 중..."
echo "=================================="

# Function to generate distributed traces
generate_traces() {
    local count=0
    local success=0
    local failed=0
    
    echo "분산 트레이싱 데이터 생성 중 (user-service → order-service → payment-service)..."
    
    for i in {1..15}; do
        # Make API call to generate traces across services
        response=$(curl -s -w "%{http_code}" -X POST http://localhost:8081/api/users/user$((i % 3 + 1))/orders \
            -H 'Content-Type: application/json' \
            -d "{\"userId\":\"user$((i % 3 + 1))\",\"productId\":\"product$i\",\"quantity\":$((i % 5 + 1)),\"amount\":$((i * 15 + 99)).99}")
        
        http_code="${response: -3}"
        
        if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
            ((success++))
            echo "[SUCCESS] Trace $i: user-service → order-service → payment-service [User: user$((i % 3 + 1))]"
        else
            ((failed++))
            echo "[FAILED] Trace $i: 트레이싱 실패 (HTTP $http_code) [User: user$((i % 3 + 1))]"
        fi
        
        ((count++))
        
        # Random delay between requests to simulate real traffic
        sleep $(echo "scale=1; $RANDOM/32767*2 + 0.3" | bc 2>/dev/null || echo "0.5")
    done
    
    echo ""
    echo "분산 트레이싱 데이터 생성 완료:"
    echo "   총 요청: $count"
    echo "   성공: $success"
    echo "   실패: $failed"
    echo "   성공률: $(echo "scale=2; $success*100/$count" | bc 2>/dev/null || echo "N/A")%"
    echo ""
}

# Function to test order-service directly (order-service → payment-service)
generate_order_traces() {
    local count=0
    local success=0
    local failed=0
    
    echo "Order Service 직접 호출 트레이싱 생성 중 (order-service → payment-service)..."
    
    for i in {1..10}; do
        # Make direct API call to order-service
        response=$(curl -s -w "%{http_code}" -X POST http://localhost:8082/api/orders \
            -H 'Content-Type: application/json' \
            -d "{\"userId\":\"user$((i % 3 + 1))\",\"productId\":\"direct-product$i\",\"quantity\":$((i % 3 + 1)),\"amount\":$((i * 20 + 150)).99}")
        
        http_code="${response: -3}"
        
        if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
            ((success++))
            echo "[SUCCESS] Direct Order $i: order-service → payment-service [Product: direct-product$i]"
        else
            ((failed++))
            echo "[FAILED] Direct Order $i: 트레이싱 실패 (HTTP $http_code)"
        fi
        
        ((count++))
        sleep $(echo "scale=1; $RANDOM/32767*1.5 + 0.4" | bc 2>/dev/null || echo "0.6")
    done
    
    echo ""
    echo "Order Service 직접 호출 완료:"
    echo "   총 요청: $count"
    echo "   성공: $success"
    echo "   실패: $failed"
    echo "   성공률: $(echo "scale=2; $success*100/$count" | bc 2>/dev/null || echo "N/A")%"
    echo ""
}

# Function to test payment-service directly
generate_payment_traces() {
    local count=0
    local success=0
    local failed=0
    
    echo "Payment Service 직접 호출 트레이싱 생성 중..."
    
    for i in {1..8}; do
        # Make direct API call to payment-service
        response=$(curl -s -w "%{http_code}" -X POST http://localhost:8083/api/payments \
            -H 'Content-Type: application/json' \
            -d "{\"orderId\":\"direct-order-$i\",\"userId\":\"user$((i % 2 + 1))\",\"amount\":$((i * 25 + 200)).99,\"paymentMethod\":\"CREDIT_CARD\"}")
        
        http_code="${response: -3}"
        
        if [[ "$http_code" =~ ^2[0-9][0-9]$ ]]; then
            ((success++))
            echo "[SUCCESS] Direct Payment $i: payment-service [Order: direct-order-$i]"
        else
            ((failed++))
            echo "[FAILED] Direct Payment $i: 트레이싱 실패 (HTTP $http_code)"
        fi
        
        ((count++))
        sleep $(echo "scale=1; $RANDOM/32767*1.2 + 0.3" | bc 2>/dev/null || echo "0.5")
    done
    
    echo ""
    echo "Payment Service 직접 호출 완료:"
    echo "   총 요청: $count"
    echo "   성공: $success"
    echo "   실패: $failed"
    echo "   성공률: $(echo "scale=2; $success*100/$count" | bc 2>/dev/null || echo "N/A")%"
    echo ""
}

# Function to test different user scenarios
generate_error_traces() {
    echo "에러 케이스 트레이싱 생성 중..."
    
    for i in {3..7}; do
        # Generate traces with validation errors
        curl -s -X POST http://localhost:8081/api/users/user$i/orders \
            -H 'Content-Type: application/json' \
            -d "{\"userId\":\"user$i\",\"productId\":\"product$i\",\"quantity\":$i,\"amount\":99.99}" > /dev/null
        
        echo "[WARNING] Error Trace $i: user-service (validation error)"
        sleep 0.3
    done
    echo ""
}

# Main execution
echo "MSA 분산 트레이싱 데모 시작"
echo ""

# Generate full distributed traces (user-service → order-service → payment-service)
generate_traces

echo "잠시 대기 중..."
sleep 2

# Generate order-service direct traces (order-service → payment-service)
generate_order_traces

echo "잠시 대기 중..."
sleep 2

# Generate payment-service direct traces
generate_payment_traces

echo "잠시 대기 중..."
sleep 1

# Generate error traces for comprehensive testing
generate_error_traces

echo "분산 트레이싱 데이터 생성 완료!"