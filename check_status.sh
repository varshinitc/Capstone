#!/bin/bash

echo "=== COMPLETE APP STATUS CHECK ==="
echo ""

# 1. Device connection
echo "1. DEVICE CONNECTION:"
adb devices
echo ""

# 2. App installation
echo "2. APP INSTALLED:"
adb shell pm list packages | grep notificationdetector
echo ""

# 3. Notification listener status
echo "3. NOTIFICATION LISTENER:"
adb shell cmd notification list_listeners | grep notificationdetector
if [ $? -eq 0 ]; then
    echo "   ✓ ENABLED"
else
    echo "   ✗ DISABLED - THIS IS THE PROBLEM!"
fi
echo ""

# 4. Check monitoring preferences
echo "4. MONITORING STATUS:"
adb shell "run-as com.aidriven.notificationdetector cat shared_prefs/monitoring_prefs.xml 2>/dev/null" || echo "   Cannot read preferences"
echo ""

# 5. Check alert preferences
echo "5. ALERT PREFERENCES:"
adb shell "run-as com.aidriven.notificationdetector cat shared_prefs/alert_prefs.xml 2>/dev/null" || echo "   Cannot read preferences"
echo ""

# 6. Check database
echo "6. DATABASE CHECK:"
adb shell "run-as com.aidriven.notificationdetector ls databases/ 2>/dev/null" || echo "   Cannot access database"
echo ""

# 7. Recent logs
echo "7. RECENT APP LOGS (Last 50 lines):"
adb logcat -d | grep -i "NotificationListener\|NotificationDetector\|ApiRepository" | tail -50
echo ""

echo "=== END STATUS CHECK ==="
