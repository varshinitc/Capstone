#!/bin/bash

echo "=== FINAL COMPREHENSIVE TEST ==="
echo ""
echo "Make sure:"
echo "1. Backend is running (python3 app.py)"
echo "2. App is open with monitoring ON"
echo "3. Real-time alerts ON"
echo ""
read -p "Press ENTER to continue..."

# Clear logs
adb logcat -c

echo ""
echo "Sending HIGH RISK notification..."
adb shell "cmd notification post -S bigtext -t 'Congratulations Winner!' 'Tag1' 'You have WON \$5000 CASH PRIZE! Click here immediately to claim your reward: http://bit.ly/prize123'"

sleep 5

echo ""
echo "=== CHECKING RESULTS ==="
echo ""
echo "1. APP LOGS:"
adb logcat -d | grep -E "ANALYZING NOTIFICATION|Analysis result|ALERT SHOWN|Saved to database" | tail -10

echo ""
echo "2. BACKEND LOGS (check your backend terminal)"
echo ""
echo "3. ON YOUR PHONE:"
echo "   - Did you see a RED alert popup?"
echo "   - Check Home screen - Today's Summary updated?"
echo "   - Check History screen - Notification saved?"
echo ""
