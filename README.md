# 🧘 Zen – Habit, Mood & Wellness Tracker

**Zen** is a modern Android app designed to help users build consistent habits, track their mood, and visualize personal progress through a clean and glanceable UI.  
It's lightweight, fast, and easy to use — built with simple progress input, polished screens, and a compact home screen widget.

---

## ✨ Features & Highlights
 

### ✅ Habit Tracking
- Create habits with custom targets and units (steps, minutes, mL, etc.)
- Quick “+” increments from the habit list, detail page, or widget  
- View daily progress and streaks

### 😊 Mood Logging
- Emoji-first mood selection  
- Add optional notes  
- View recent mood stats and lightweight trends

### 📊 Progress Visualization
- Clean profile with **total habits**, **best streak**, and daily progress  
- Minimal progress bars for tidy, compact visual summaries

### 📱 Home Screen Widget
- Shows overall habit progress  
- Choose a favorite habit to control via the widget  
- One-tap **+** button to add a tick instantly

### 🔔 Smart Reminders
- Scheduled notifications for mood logging & hydration  
- Reschedules automatically after device restart

### 🔐 Local Persistence
- All data stored locally using **SharedPreferences** via `ZenRepository`  
- No cloud sync — simple and privacy-friendly

### 🚶 Step Tracking
- Optional step sensor integration for step-related habits

---

## 🎬 App Demo (Short Video)

*Show a quick demo of the app (habit tracking, mood logging, widget usage):*  
👉 **Insert your video link here**  
You may embed a YouTube/Vimeo video.

---

## 🏗️ App Workflow (High-Level)

1. **Splash Screen**
2. **Onboarding → Signup**  
   - Collects basic user info  
   - Seeds default habits and settings
3. **Home Screen**  
   - Bottom navigation to **Habits**, **Mood**, **Profile**, **Settings**
4. **Tracking**  
   - Add/Edit habits  
   - Tick progress from Habits screen or Widget
5. **Notifications**  
   - Reminders for mood logs and hydration
6. **Profile & Settings**  
   - Compact stats display  
   - Configure reminders, widget habit, and step sensor

---

## ⚙️ Build & Run

### Requirements
- **Android Studio Giraffe+**
- **Compile SDK: 36**

### Steps
1. Clone the repository  
2. Open the project in Android Studio  
3. Select a device/emulator  
4. Click **Run**  
5. Defaults will be auto-seeded on the first launch

---

## 📂 Project Structure & Technology

Zen uses a clean, lightweight architecture focused on readability.

### 🧩 Language  
- **Kotlin**

### 🗂️ Persistence  
- `SharedPreferences` through **ZenRepository.kt**

### 🧱 Core Data Models  
- `Habit.kt`  
- `MoodEntry.kt`  
- `AppSettings.kt`

### 📌 Major Components

#### 🎛 Activities  
- `HomeActivity.kt`  
- `HabitsActivity.kt`  
- `MoodActivity.kt`  
- `ProfileActivity.kt`  
- `SettingsActivity.kt`  
- `SignupActivity.kt`  
- `OnboardingActivity.kt`

#### 🔔 Receivers  
- `ReminderReceiver.kt` (handles alarms & notifications)  
- `BootReceiver.kt` (re-schedules reminders on reboot)

#### 🛰 Services  
- `StepService.kt` (step sensor integration)

#### 🧩 Widget  
- `ZenWidgetProvider.kt` (home screen widget logic)

#### ⏳ Workers  
- `MidnightWorker.kt` (resets daily counters & maintenance)

### 🔧 Shared Utilities  
- **ZenRepository** – all persistence & domain logic  
- **DateUtils** – date formatting & streak calculations  
- **AlarmScheduler** – hydration/mood reminders  
- **PulseNotificationHelper** – building notifications

---

## 📄 License  
This project is licensed under the **MIT License**.

---

## 💚 Thank You for Using Zen  
Feel free to open issues or suggest new features!

