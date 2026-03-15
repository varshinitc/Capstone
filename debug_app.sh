#!/bin/bash

echo "=== Notification Detector - Debug Info ==="
echo ""

# Check if device is connected
echo "1. Device Connection:"
adb devices
echo ""

# Check if app is installed
echo "2. App Installation:"
if adb shell pm list packages | grep -q "com.aidriven.notificationdetector"; then
    echo "   ✓ App is installed"
else
    echo "   ✗ App not found"
fi
echo ""

# Check notification listener permission
echo "3. Notification Listener Status:"
adb shell cmd notification allow_listener com.aidriven.notificationdetector/com.aidriven.notificationdetector.service.NotificationListener
LISTENER_STATUS=$(adb shell cmd notification list_listeners | grep notificationdetector)
if [ -n "$LISTENER_STATUS" ]; then
    echo "   ✓ Notification Listener is ENABLED"
else
    echo "   ✗ Notification Listener is DISABLED"
    echo "   → Enable in: Settings → Apps → Special Access → Notification Access"
fi
echo ""

# Check if app is running
echo "4. App Process:"
APP_PROCESS=$(adb shell ps | grep notificationdetector)
if [ -n "$APP_PROCESS" ]; then
    echo "   ✓ App is running"
else
    echo "   ⚠ App might not be running"
fi
echo ""

# Check backend connectivity from device
echo "5. Backend Connection Test:"
echo "   Testing from Mac: http://192.168.1.41:5001/health"
BACKEND_TEST=$(curl -s http://192.168.1.41:5001/health 2>&1)
if [ $? -eq 0 ]; then
    echo "   ✓ Backend reachable from Mac"
    echo "   Response: $BACKEND_TEST"
else
    echo "   ✗ Backend not reachable"
    echo "   → Start backend: cd backend && python3 app.py"
fi
echo ""

# Show recent logs
echo "6. Recent App Logs (last 20 lines):"
echo "   ----------------------------------------"
adb logcat -d | grep -i "NotificationListener\|NotificationDetector" | tail -20
echo "   ----------------------------------------"
echo ""

echo "=== Live Log Monitoring ==="
echo "Run this command to watch live logs:"
echo "adb logcat | grep -i 'NotificationListener\|NotificationDetector'"
echo ""
