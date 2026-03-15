# AI-Driven Notification Spam Detector

An Android app that monitors all incoming notifications (WhatsApp, Gmail, SMS, and more) and detects spam, phishing, and scam messages in real time — fully offline, no internet or backend required.

---

## Features

- Monitors notifications from all apps in real time
- Offline spam detection using rule-based pattern matching
- Detects financial scams, urgency tactics, prize scams, phishing, and suspicious URLs
- Color-coded risk alerts — RED for HIGH RISK, ORANGE for SUSPICIOUS
- Heads-up notifications (Telegram-style) that appear even when the app is closed
- Full notification history with risk scores
- Clear all history with one tap
- Works 100% offline — no server, no internet needed

---

## Risk Levels

| Level | Score | Color |
|-------|-------|-------|
| HIGH_RISK | ≥ 60% | Red |
| SUSPICIOUS | 30–60% | Orange |
| SAFE | < 30% | Green |

---

## Project Structure

```
Capstone_project/
├── app/
│   └── src/main/
│       ├── java/com/aidriven/notificationdetector/
│       │   ├── activities/        # All UI screens
│       │   ├── adapters/          # RecyclerView adapters
│       │   ├── database/          # Room database, DAO
│       │   ├── ml/                # OfflineSpamDetector (rule-based engine)
│       │   ├── models/            # Data models
│       │   ├── network/           # Retrofit (optional/legacy)
│       │   ├── repository/        # Data repository
│       │   ├── service/           # NotificationListenerService
│       │   ├── utils/             # Helper utilities
│       │   └── viewmodels/        # ViewModels
│       ├── res/                   # Layouts, drawables, strings
│       └── AndroidManifest.xml
├── backend/                       # Python Flask backend (optional, not required)
│   ├── app.py
│   ├── train_model.py
│   └── requirements.txt
├── build.gradle
├── settings.gradle
└── gradlew
```

---

## Requirements

### Android App
- Android Studio Hedgehog or later
- Android SDK 34
- Kotlin 2.0.0
- Gradle 8.3.0
- Java 8
- Android device or emulator running Android 7.0 (API 24) or higher
- ADB (Android Debug Bridge) installed

### Backend (Optional)
- Python 3.8 or higher
- pip

---

## Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/varshinitc/Capstone.git
cd Capstone
```

### 2. Open in Android Studio

- Open Android Studio
- Click **File → Open**
- Select the `Capstone` folder
- Wait for Gradle sync to complete

### 3. Configure local.properties

Android Studio creates this automatically, but if missing:

```bash
echo "sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk" > local.properties
```

Replace `YOUR_USERNAME` with your macOS username.

---

## Build the APK

### Using Android Studio
- Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- APK will be at `app/build/outputs/apk/debug/app-debug.apk`

### Using Terminal (Command Line)

```bash
# Give execute permission to gradlew (first time only)
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# APK location
ls app/build/outputs/apk/debug/
```

---

## Install on Android Device

### Step 1 — Enable Developer Options on your phone
- Go to **Settings → About Phone**
- Tap **Build Number** 7 times
- Go back to **Settings → Developer Options**
- Enable **USB Debugging**

### Step 2 — Connect device and verify ADB

```bash
adb devices
```

You should see your device listed. Example:
```
List of devices attached
de98523c    device
```

### Step 3 — Install the APK

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Step 4 — Launch the app

```bash
adb shell am start -n com.aidriven.notificationdetector/.activities.SplashActivity
```

---

## Required Permissions (Grant on Device)

After installing, grant these permissions when prompted:

| Permission | Why Needed |
|------------|------------|
| Notification Access | To read and monitor incoming notifications |
| Display over other apps | To show overlay alert banners |
| Post Notifications | To show spam alert heads-up notifications |

### Grant Notification Access manually

```bash
adb shell am start -a android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
```

Find **AI Notification Detector** in the list and toggle it ON.

### Grant Overlay Permission manually

```bash
adb shell am start -a android.settings.action.MANAGE_OVERLAY_PERMISSION -d package:com.aidriven.notificationdetector
```

---

## Run & Test

### Check app logs in real time

```bash
adb logcat -s NotificationListener OfflineSpamDetector
```

### Send a test notification to trigger spam detection

```bash
adb shell am broadcast -a android.intent.action.SEND \
  --es "title" "URGENT: Your bank account is suspended" \
  --es "content" "Click here to verify now: bit.ly/verify123"
```

### View all app logs

```bash
adb logcat | grep "notificationdetector"
```

---

## Backend Setup (Optional)

The app works fully offline. The backend is only needed if you want to retrain the ML model.

### Install Python dependencies

```bash
cd backend
pip install -r requirements.txt
```

### Train the model

```bash
python train_model.py
```

### Run the Flask server

```bash
python app.py
```

Server runs at `http://localhost:5000`

---

## How Spam Detection Works

The `OfflineSpamDetector` uses rule-based keyword matching across 6 categories:

| Category | Keywords | Risk Weight |
|----------|----------|-------------|
| Financial Scam | bank, account, suspended, verify, transaction | +30% |
| Urgency Tactics | urgent, immediately, act fast, today only | +20% |
| Prize Scam | won, winner, lottery, claim, jackpot | +25% |
| Phishing | click here, verify now, reset password, login | +20% |
| Spam Keywords | offer, deal, discount, buy now, exclusive | +15% |
| Suspicious URLs | bit.ly, tinyurl, goo.gl, http:// | +20% |
| Excessive punctuation (3+ !) | — | +10% |
| ALL CAPS title | — | +10% |

---

## Copy APK to Desktop

```bash
cp app/build/outputs/apk/debug/app-debug.apk ~/Desktop/NotificationDetector.apk
```

---

## Uninstall the App

```bash
adb uninstall com.aidriven.notificationdetector
```

---

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Database**: Room (SQLite)
- **Architecture**: MVVM
- **UI**: Material Design 3, ViewBinding
- **Async**: Kotlin Coroutines
- **Spam Engine**: Rule-based offline detector (no ML model required)

---

## Dependencies

```gradle
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.room:room-runtime:2.6.1
androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0
kotlinx-coroutines-android:1.7.3
androidx.recyclerview:recyclerview:1.3.2
```

---

## Troubleshooting

**Gradle sync fails**
```bash
./gradlew clean
./gradlew assembleDebug
```

**ADB device not found**
```bash
adb kill-server
adb start-server
adb devices
```

**Notifications not being detected**
- Make sure Notification Access is granted (see permissions section above)
- Check that monitoring is enabled in the app's Home screen

**Overlay banner not showing**
- Grant "Display over other apps" permission manually from Settings

---

## License

This project is built as a Capstone project for academic purposes.
