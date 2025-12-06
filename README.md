🌿 Zen — Habit, Mood & Wellness Tracker

A clean, modern Android app to build better habits, track your mood, and understand your wellness — all offline, fast, and beautifully designed.

<p align="center"> <img src="banner.png" alt="Zen Banner" width="80%" /> </p>

Tip: Replace banner.png with your real banner image (or ask me to design one).

<p align="center"> <img src="https://img.shields.io/badge/Android-13%2B-brightgreen" /> <img src="https://img.shields.io/badge/Kotlin-100%25-blueviolet" /> <img src="https://img.shields.io/badge/Architecture-Lightweight%20MVVM%2BRepo-orange" /> <img src="https://img.shields.io/badge/License-MIT-yellow" /> </p>
📸 Screenshots
<p align="center"> <img src="screenshots/splash.png" width="22%" /> <img src="screenshots/home.png" width="22%" /> <img src="screenshots/habits.png" width="22%" /> <img src="screenshots/mood.png" width="22%" /> </p>

Add real images inside a /screenshots folder — I can help design mockups too.

✨ Overview

Zen is a lightweight Android wellness tracker focusing on simple habit creation, emoji-first mood logging, clean progress insights, and a quick-action home screen widget.

No cloud sync, no accounts — just clean, private data stored locally.

🌟 Features
🏆 Habit Tracking

Create habits with custom units (steps, minutes, mL, etc.)

Set daily targets + default increments

One-tap progress from Home, Habit list, and the Widget

View detailed stats & daily completion

😄 Mood Tracking

Emoji-centered entry with optional note

Quick mood reminders

Recent mood trends (text + mini chart)

👤 Profile Insights

Total habits created

Best streak

Lightweight animated custom views

🔔 Smart Notifications

Hydration reminders

Mood logging reminders

Exact/inexact alarm scheduling

🟩 Home Screen Widget

Compact progress widget

Single habit quick “+” button

Fully configurable

📌 Offline-First

All data stored locally via SharedPreferences

No internet required

Clean and fast architecture

🧭 App Flow
Splash → Onboarding → Signup → Home


Home contains bottom navigation to:

Habits

Mood

Profile

Settings

🗂️ Project Structure
Zen/
├─ app/
│  ├─ src/main/
│  │  ├─ AndroidManifest.xml
│  │  ├─ java/com/example/zen/
│  │  │  ├─ activities/
│  │  │  ├─ adapters/
│  │  │  ├─ dialogs/
│  │  │  ├─ fragments/
│  │  │  ├─ models/
│  │  │  ├─ receivers/
│  │  │  ├─ repo/
│  │  │  ├─ sensors/
│  │  │  ├─ util/
│  │  │  ├─ views/
│  │  │  ├─ widgets/
│  │  │  └─ work/
│  │  └─ res/layout, xml/
├─ build.gradle.kts
└─ README.md

🧩 Core Components
Repository

ZenRepository

Single source of truth

CRUD for habits, mood entries, ticks, profile, settings

SharedPreferences-based storage

Seeds default data on first run

Reminders & Notifications

AlarmScheduler

ReminderReceiver

BootReceiver

PulseNotificationHelper

Custom Views

ProfileStatsView

HabitProgressChartView

MoodStatsView

EmojiSliderView

Widget

ZenWidgetProvider

WidgetConfigActivity

Quick-add mechanism via PendingIntent

Background Work

MidnightWorker for daily counter resets

📱 Layout Overview

Screens:

Splash

Onboarding

Signup

Home

Habits

Habit Detail

Mood

Add Mood

Profile

Edit Profile

Settings

Widget Config

reusable habit, mood, emoji item layouts

full widget layout

🔧 Build & Run

Open with Android Studio Giraffe or newer

Use Compile SDK 36

Run on emulator/device

Defaults are seeded automatically on first launch

🤝 Contributing

Contributions are welcome!
This project’s architecture is intentionally simple — feel free to improve:

UI/UX

Animations

Widget features

Step sensor integrations

New visualization components

📜 License
MIT License
