# 🧘 Zen - Habit, Mood, and Wellness Tracker

[cite_start]Zen is a modern Android application designed to help users build consistent habits, track their daily mood, and visualize their progress with a **clean, glanceable UI**[cite: 16]. [cite_start]It is engineered to be **lightweight, fast, and friendly**, featuring a customizable home screen widget, simple progress input, and a polished profile with personalized stats[cite: 17].

---

## 📄 Table of Contents

1.  [✨ Core Features & Highlights](#-core-features--highlights)
2.  [🎬 App Demo](#-app-demo)
3.  [⚙️ Technology Stack](#-technology-stack)
4.  [🏗️ Application Architecture & Data Flow](#-application-architecture--data-flow)
5.  [🚀 Build and Run](#-build-and-run)
6.  [💡 Contributing & Notes](#-contributing--notes)

---

## ✨ Core Features & Highlights

Zen provides a focused set of tools to manage daily wellness and consistency:

### 🎯 Habit Tracking
* [cite_start]**Customizable Habits:** Track daily habits with specific **targets, units** (e.g., steps, minutes, mL), and quick increments[cite: 19, 216].
* [cite_start]**Flexible Ticking:** Progress can be marked from multiple touchpoints: habit detail pages, main lists, or via the home screen widget[cite: 216].
* [cite_start]**Step Sensor Integration:** Includes built-in support for the step sensor, which can be toggled in settings for relevant habits[cite: 223].

### 🌙 Mood Logging & Reminders
* [cite_start]**Emoji-First Logging:** Log moods using an intuitive emoji-first experience, complete with an optional note[cite: 20, 218].
* [cite_start]**Scheduled Reminders:** Users can configure **notification scheduling** for mood entries and hydration reminders to maintain consistency[cite: 22, 218]. [cite_start]The notifications comply with Android 13+ runtime permission models[cite: 238].

### 📊 Progress & Visualization
* [cite_start]**Clean Profile View:** The profile displays compact metrics like **streaks** and **habit totals**[cite: 23, 220].
* [cite_start]**Text-Forward Stats:** Visualization is designed for readability, using lightweight progress bars for tidy visual summaries instead of heavy charts[cite: 93, 221].
* [cite_start]**Custom Views:** Renders key stats using custom components like `ProfileStatsView` and utilizes `DateUtils` for all date/streak calculus[cite: 92, 145].

### 📱 Home Screen Widget
* [cite_start]**Quick Actions:** A compact home screen widget shows overall completion and a selected habit with a quick **`+`** button[cite: 21, 225].
* [cite_start]**Immediate Refresh:** Tapping the quick add button adds a tick to the chosen habit and refreshes the widget immediately[cite: 226].
* [cite_start]**Configuration:** The widget is configured via the `WidgetConfigActivity.kt` where the user chooses the habit it will control[cite: 160].

---

## 🎬 App Demo

*(The best way to showcase the app is with a short GIF or a video link. Insert your preferred method below.)*

**[Insert your GIF or YouTube/Vimeo embed code/link here]**

---

## ⚙️ Technology Stack

Zen is a pure Kotlin Android application that minimizes external dependencies to achieve high performance and a compact footprint.

| Category | Technology | Purpose in Zen |
| :--- | :--- | :--- |
| **Language** | **Kotlin** | Primary development language. |
| **Persistence** | **SharedPreferences** | [cite_start]Single-process local data storage for simplicity[cite: 24, 229]. |
| **Concurrency** | **WorkManager** | [cite_start]Used for the `MidnightWorker` to handle daily rollovers and maintenance[cite: 165]. |
| **System** | **AlarmManager** | [cite_start]Manages exact and inexact alarms for all scheduled reminders[cite: 141]. |
| **Sensors** | **Android Sensor API** | [cite_start]Integrated via `StepService` for optional step counting[cite: 61, 138].

---

## 🏗️ Application Architecture & Data Flow

The application follows a pragmatic, clear architecture, centralizing data operations and utilizing standard Android components for system integration.

### Data Flow and Persistence
[cite_start]The entire application is centered around the **`ZenRepository.kt`**[cite: 240].

* [cite_start]**Single Source of Truth:** The Repository acts as the single source of truth, backed entirely by `SharedPreferences`[cite: 132, 228].
* [cite_start]**Local-Only:** The design choice is focused on local simplicity, deliberately excluding cloud backup or external database dependencies[cite: 229].
* [cite_start]**Data Models:** Key data is represented by models such as `Habit.kt`, `MoodEntry.kt`, and `UserProfile.kt`[cite: 119, 122, 124].

### Key System Components

| File | Type | Responsibility |
| :--- | :--- | :--- |
| [cite_start]`SplashActivity.kt` [cite: 76] | Activity | Launch screen that sets up the theme and routes to onboarding or Home. |
| [cite_start]`StepService.kt` [cite: 138] | Service | Foreground/background service for step counting when enabled. |
| [cite_start]`MidnightWorker.kt` [cite: 165] | Worker | Handles nightly maintenance, daily counter rollovers, and reminder resets safely. |
| [cite_start]`ReminderReceiver.kt` [cite: 128] | BroadcastReceiver | Handles alarm broadcasts and triggers user notifications, delegating to `PulseNotificationHelper.kt`. |
| [cite_start]`BootReceiver.kt` [cite: 126] | BroadcastReceiver | Reschedules alarms after a device restart using `BOOT_COMPLETED`. |
| [cite_start]`AlarmScheduler.kt` [cite: 141] | Utility | Creates, manages, schedules, and cancels exact/inexact alarms for reminders. |

### Manifest and Permissions

[cite_start]The `AndroidManifest.xml` declares activities, registers receivers (`ReminderReceiver`, `BootReceiver`), and declares `StepService` for sensor integration[cite: 59, 60, 61]. It also requests necessary permissions:

* [cite_start]`POST_NOTIFICATIONS` (for Android 13+ compliance) [cite: 62, 238]
* [cite_start]`ACTIVITY_RECOGNITION` (for step sensor) [cite: 62]
* [cite_start]Permissions for exact alarms [cite: 62]

---

## 🚀 Build and Run

Zen is a standard Android project, easily built with Android Studio.

### Prerequisites
* [cite_start]**Android Studio Giraffe+** is recommended[cite: 232].
* [cite_start]Compile SDK: **36**[cite: 233].

### Instructions

1.  Clone the repository:
    ```bash
    git clone [Your Repository URL]
    ```
2.  Open the project in Android Studio.
3.  Ensure your device or emulator is set up.
4.  Click **Run**. [cite_start]The app will automatically seed default habits and initial state on first launch[cite: 234].

---

## 💡 Contributing & Notes

* [cite_start]**Code Philosophy:** The codebase favors clarity over heavy architecture, using a combination of the repository pattern, activities, and lightweight custom views[cite: 236].
* [cite_start]**Consistency:** Shared utilities like `DateUtils` and `EmojiPalette` are used across the application to ensure consistent date formatting, calculations, and emoji usage[cite: 241, 242].
