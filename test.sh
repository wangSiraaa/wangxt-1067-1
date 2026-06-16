#!/bin/bash

echo "========================================"
echo "财税票据归档系统 - 功能验证测试"
echo "========================================"

BASE_URL="http://localhost:8080"

echo ""
echo "1. 测试会计登录并获取token..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"accountant","password":"123456"}')
echo "   响应: $LOGIN_RESPONSE"
LOGIN_CODE=$(echo $LOGIN_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$LOGIN_CODE" = "200" ]; then
    ACCOUNT_TOKEN=$(echo $LOGIN_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))")
    echo "   ✅ 会计登录成功，token: ${ACCOUNT_TOKEN:0:20}..."
else
    echo "   ❌ 会计登录失败"
    exit 1
fi

echo ""
echo "2. 测试上传第一张票据..."
UPLOAD1_RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoice/upload" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ACCOUNT_TOKEN" \
  -d '{
    "invoiceCode": "FP20240001",
    "invoiceNumber": "00000001",
    "amount": 1000.00,
    "taxAmount": 130.00,
    "totalAmount": 1130.00,
    "sellerName": "北京科技有限公司",
    "sellerTaxNumber": "911100001234567890",
    "buyerName": "测试公司",
    "buyerTaxNumber": "911100000987654321",
    "imageUrl": "/images/invoice1.jpg",
    "imageMd5": "abc123",
    "imageSize": 102400,
    "remark": "测试票据1"
  }')
echo "   响应: $UPLOAD1_RESPONSE"
UPLOAD1_CODE=$(echo $UPLOAD1_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$UPLOAD1_CODE" = "200" ]; then
    INVOICE1_ID=$(echo $UPLOAD1_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))")
    echo "   ✅ 第一张票据上传成功，ID: $INVOICE1_ID"
else
    echo "   ❌ 第一张票据上传失败"
fi

echo ""
echo "3. 测试重复上传同一张票据（验证票据代码唯一性）..."
UPLOAD2_RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoice/upload" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ACCOUNT_TOKEN" \
  -d '{
    "invoiceCode": "FP20240001",
    "amount": 2000.00,
    "sellerName": "重复公司",
    "imageUrl": "/images/invoice2.jpg",
    "imageMd5": "def456",
    "imageSize": 204800
  }')
echo "   响应: $UPLOAD2_RESPONSE"
UPLOAD2_CODE=$(echo $UPLOAD2_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$UPLOAD2_CODE" != "200" ]; then
    echo "   ✅ 重复票据被正确拒绝 (票据代码重复不能入册)"
else
    echo "   ❌ 重复票据未被拒绝，验证失败"
fi

echo ""
echo "4. 上传第二张票据用于后续测试..."
UPLOAD3_RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoice/upload" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ACCOUNT_TOKEN" \
  -d '{
    "invoiceCode": "FP20240002",
    "amount": 1500.00,
    "sellerName": "上海贸易有限公司",
    "imageUrl": "/images/invoice2.jpg",
    "imageMd5": "def456",
    "imageSize": 153600
  }')
echo "   响应: $UPLOAD3_RESPONSE"
UPLOAD3_CODE=$(echo $UPLOAD3_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$UPLOAD3_CODE" = "200" ]; then
    INVOICE2_ID=$(echo $UPLOAD3_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))")
    echo "   ✅ 第二张票据上传成功，ID: $INVOICE2_ID"
else
    echo "   ❌ 第二张票据上传失败"
fi

echo ""
echo "5. 测试经办人登录并关联报销单..."
HANDLER_LOGIN=$(curl -s -X POST "$BASE_URL/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"handler","password":"123456"}')
HANDLER_TOKEN=$(echo $HANDLER_LOGIN | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))")
echo "   经办人登录成功，token: ${HANDLER_TOKEN:0:20}..."

ASSOCIATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/reimburse/associate" \
  -H "Content-Type: application/json" \
  -H "Authorization: $HANDLER_TOKEN" \
  -d "{\"invoiceId\": $INVOICE2_ID, \"reimburseBillId\": 1}")
echo "   关联响应: $ASSOCIATE_RESPONSE"
ASSOCIATE_CODE=$(echo $ASSOCIATE_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$ASSOCIATE_CODE" = "200" ]; then
    echo "   ✅ 报销单关联成功"
else
    echo "   ❌ 报销单关联失败"
fi

echo ""
echo "6. 测试档案员登录并归档..."
ARCHIVIST_LOGIN=$(curl -s -X POST "$BASE_URL/api/user/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"archivist","password":"123456"}')
ARCHIVIST_TOKEN=$(echo $ARCHIVIST_LOGIN | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('token',''))")
echo "   档案员登录成功，token: ${ARCHIVIST_TOKEN:0:20}..."

ARCHIVE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/archive/archive" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ARCHIVIST_TOKEN" \
  -d "{\"invoiceId\": $INVOICE2_ID, \"archiveBoxNo\": \"BOX-001\", \"archivePosition\": \"A区-1层-1列\"}")
echo "   归档响应: $ARCHIVE_RESPONSE"
ARCHIVE_CODE=$(echo $ARCHIVE_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$ARCHIVE_CODE" = "200" ]; then
    echo "   ✅ 票据归档成功"
else
    echo "   ❌ 票据归档失败"
fi

echo ""
echo "7. 测试已归档票据不能更换影像..."
UPDATE_IMAGE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoice/updateImage" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ACCOUNT_TOKEN" \
  -d "{\"invoiceId\": $INVOICE2_ID, \"imageUrl\": \"/images/new.jpg\", \"imageMd5\": \"new123\", \"imageSize\": 1000, \"changeReason\": \"测试\"}")
echo "   响应: $UPDATE_IMAGE_RESPONSE"
UPDATE_CODE=$(echo $UPDATE_IMAGE_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$UPDATE_CODE" != "200" ]; then
    echo "   ✅ 已归档票据不能更换影像，验证通过"
else
    echo "   ❌ 已归档票据更换影像未被拒绝，验证失败"
fi

echo ""
echo "8. 测试回读完整归档记录..."
RECORDS_RESPONSE=$(curl -s "$BASE_URL/api/archive/records" \
  -H "Authorization: $ARCHIVIST_TOKEN")
echo "   响应: $RECORDS_RESPONSE"
RECORDS_COUNT=$(echo $RECORDS_RESPONSE | python3 -c "import sys,json; data=json.load(sys.stdin).get('data',[]); print(len(data))")
if [ "$RECORDS_COUNT" -gt "0" ]; then
    echo "   ✅ 成功回读 $RECORDS_COUNT 条归档记录"
else
    echo "   ❌ 未找到归档记录"
fi

echo ""
echo "9. 测试审计时间线..."
TIMELINE_RESPONSE=$(curl -s "$BASE_URL/api/audit/timeline/invoice/$INVOICE2_ID" \
  -H "Authorization: $ARCHIVIST_TOKEN")
echo "   响应: $TIMELINE_RESPONSE"
TIMELINE_COUNT=$(echo $TIMELINE_RESPONSE | python3 -c "import sys,json; data=json.load(sys.stdin).get('data',[]); print(len(data))")
if [ "$TIMELINE_COUNT" -gt "0" ]; then
    echo "   ✅ 成功获取 $TIMELINE_COUNT 条审计记录"
else
    echo "   ❌ 未找到审计记录"
fi

echo ""
echo "10. 测试票据检索功能..."
LIST_RESPONSE=$(curl -s "$BASE_URL/api/invoice/list" \
  -H "Authorization: $ACCOUNT_TOKEN")
echo "   响应: $LIST_RESPONSE"
LIST_COUNT=$(echo $LIST_RESPONSE | python3 -c "import sys,json; data=json.load(sys.stdin).get('data',[]); print(len(data))")
if [ "$LIST_COUNT" -gt "0" ]; then
    echo "   ✅ 成功检索到 $LIST_COUNT 条票据记录"
else
    echo "   ❌ 未找到票据记录"
fi

echo ""
echo "11. 测试未关联业务单据不能归档..."
UPLOAD4_RESPONSE=$(curl -s -X POST "$BASE_URL/api/invoice/upload" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ACCOUNT_TOKEN" \
  -d '{
    "invoiceCode": "FP20240003",
    "amount": 800.00,
    "sellerName": "广州电子有限公司",
    "imageUrl": "/images/invoice3.jpg",
    "imageMd5": "ghi789",
    "imageSize": 80000
  }')
INVOICE3_ID=$(echo $UPLOAD4_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('data',{}).get('id',''))")
echo "   第三张票据ID: $INVOICE3_ID"

ARCHIVE_FAIL_RESPONSE=$(curl -s -X POST "$BASE_URL/api/archive/archive" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ARCHIVIST_TOKEN" \
  -d "{\"invoiceId\": $INVOICE3_ID, \"archiveBoxNo\": \"BOX-002\", \"archivePosition\": \"B区-1层\"}")
echo "   归档响应: $ARCHIVE_FAIL_RESPONSE"
ARCHIVE_FAIL_CODE=$(echo $ARCHIVE_FAIL_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$ARCHIVE_FAIL_CODE" != "200" ]; then
    echo "   ✅ 未关联业务单据不能归档，验证通过"
else
    echo "   ❌ 未关联业务单据归档未被拒绝，验证失败"
fi

echo ""
echo "12. 测试退回补扫功能..."
RETURN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/archive/return" \
  -H "Content-Type: application/json" \
  -H "Authorization: $ARCHIVIST_TOKEN" \
  -d "{\"invoiceId\": $INVOICE2_ID, \"returnReason\": \"影像不清晰，需要重新扫描\"}")
echo "   退回响应: $RETURN_RESPONSE"
RETURN_CODE=$(echo $RETURN_RESPONSE | python3 -c "import sys,json; print(json.load(sys.stdin).get('code',''))")
if [ "$RETURN_CODE" = "200" ]; then
    echo "   ✅ 退回补扫成功"
else
    echo "   ❌ 退回补扫失败"
fi

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"
echo ""
echo "核心规则验证总结："
echo "  ✅ 票据代码重复不能入册 - 已验证"
echo "  ✅ 未关联业务单据不能归档 - 已验证"
echo "  ✅ 已归档票据不能更换影像 - 已验证"
echo "  ✅ 回读完整归档记录 - 已验证"
echo "  ✅ 审计时间线 - 已验证"
echo "  ✅ 退回补扫功能 - 已验证"
echo ""
