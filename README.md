# 🧘 Zen - Habit, Mood, and Wellness Tracker

[cite_start]Zen is a modern Android application that helps you build consistent habits, track your daily mood, and visualize progress with a clean, glanceable UI[cite: 16]. [cite_start]It is designed to be lightweight, fast, and user-friendly, featuring a customizable home screen widget, simple progress input, and a polished profile with personal stats[cite: 17].

---

## ✨ Core Features

* [cite_start]**Habit Tracking:** Users can create habits with specific targets per day, customizable units (steps, minutes, mL, etc.), and default increments[cite: 19, 216]. [cite_start]Progress can be ticked via detail pages, lists, or the compact home screen widget[cite: 216].
* [cite_start]**Mood Logging:** Log moods using an **emoji-first experience** with optional notes and view recent stats and trends[cite: 20, 218]. [cite_start]The app offers notification scheduling to encourage regular logging[cite: 22, 218].
* [cite_start]**Progress & Profile:** The clean profile view displays compact metrics like **streaks** and **habit totals**[cite: 23, 220]. [cite_start]Lightweight progress bars are used to maintain tidy visuals, avoiding heavy chart dependencies[cite: 221].
* [cite_start]**Home Screen Widget:** A compact widget shows overall completion and a selected habit with a quick **`+`** button[cite: 21, 225]. [cite_start]Tapping the button adds a tick to the habit and refreshes the widget immediately[cite: 226].
* [cite_start]**Settings & Customization:** Users can toggle mood and hydration reminders, configure schedules and intervals, choose which habit the widget controls, and toggle step sensor support[cite: 223].

---

## 🎬 App Demo (Short Video)

*(Please insert your short video or GIF here. A short, high-quality animated GIF is recommended for auto-playing demos in a GitHub README.)*

**[Insert GIF or YouTube/Vimeo embed code/link here]**

---

## 🏗️ App Workflow (High-level)

[cite_start]The application utilizes a simple flow for user engagement and setup[cite: 25]:

1.  [cite_start]**Launch:** The flow begins with a **Splash Screen** [cite: 27] [cite_start]which handles theme setup and routes the user to onboarding or directly to Home[cite: 76].
2.  [cite_start]**Onboarding:** First-time users are guided through onboarding and signup screens where initial user information is collected, and default data is seeded to the repository[cite: 78, 80].
3.  [cite_start]**Main Navigation:** The **Home** screen acts as the main hub, hosting bottom navigation to Habits, Mood, Profile, and Settings[cite: 29, 82].
4.  [cite_start]**Interaction:** Users can add/edit habits and tick progress directly from the Home/Habits screens or the customizable Widget[cite: 30].
5.  [cite_start]**Notifications:** If enabled, notifications remind you to log moods or drink water[cite: 31].
6.  [cite_start]**Configuration:** The Profile shows compact stats, while Settings configures notifications, schedules, and the widget habit[cite: 32].

---

## 🛠️ Technical Implementation and Codebase

[cite_start]The codebase is structured to favor clarity, using a repository pattern for data management and dedicated Kotlin files for specific features[cite: 236].

### Data & Persistence (Repo and Models)

[cite_start]The core persistence logic is centralized to ensure a single source of truth[cite: 240].

* [cite_start]**`ZenRepository.kt`** [cite: 132][cite_start]: The single source of truth backed by **SharedPreferences**[cite: 132, 228]. [cite_start]It manages habits, ticks, moods, settings, and seeding, exposing simple CRUD and query helpers[cite: 133].
* [cite_start]**`Habit.kt` / `HabitTick.kt`** [cite: 119, 120][cite_start]: Data models representing a habit's properties (title, unit, target) and a single recorded progress "tick"[cite: 119, 121]. Ticks are aggregated to calculate daily progress.
* [cite_start]**`AppSettings.kt`** [cite: 118][cite_start]: The settings data model, including notification toggles, schedules, widget selection, and the step sensor flag[cite: 118, 228].
* [cite_start]**`SeedData.kt`**[cite: 135]: Provides default habits and initial state on first run, called by the repository during initialization.

### System Components

These components handle critical background and system-level interactions.

* [cite_start]**`StepService.kt`** [cite: 138][cite_start]: A foreground/background service responsible for step counting when enabled, updating relevant habit data, and supporting live summaries[cite: 139].
* [cite_start]**`MidnightWorker.kt`**[cite: 165]: A **WorkManager** worker that performs daily rollovers and maintenance to ensure daily counters and reminders reset safely.
* [cite_start]**`BootReceiver.kt`** [cite: 126][cite_start]: Listens for `BOOT_COMPLETED` to reread `AppSettings` and re-schedule alarms after a device restart[cite: 126].
* [cite_start]**`AlarmScheduler.kt`** [cite: 141][cite_start]: Creates and manages exact/inexact alarms for hydration and mood reminders, calculating intervals and cooperating with the `ReminderReceiver`[cite: 141].
* [cite_start]**`PulseNotificationHelper.kt`** [cite: 147][cite_start]: Responsible for building and displaying notifications, ensuring compliance with Android 13+ permission models[cite: 147, 238].

### UI and View Logic

The application uses dedicated Activities for major screens and custom views for unique data presentation.

* [cite_start]**Activities:** Dedicated activities are used for key screens such as `SplashActivity.kt` [cite: 76][cite_start], `HomeActivity.kt` [cite: 82][cite_start], `HabitsActivity.kt` [cite: 85][cite_start], and `MoodActivity.kt`[cite: 90].
* [cite_start]**`ZenWidgetProvider.kt`** [cite: 158][cite_start]: The `AppWidgetProvider` for the home screen widget, handling rendering of overall progress and the selected habit card, and managing quick **`+`** actions[cite: 159].
* [cite_start]**`ProfileStatsView.kt`** [cite: 150][cite_start]: A custom view that renders two rows (Total Habits, Best Streak) with an emoji, big value text, and a right-side progress bar, animating on data set[cite: 150, 92].
* [cite_start]**`EmojiSliderView.kt`**[cite: 155]: An emoji-based slider input used for mood intensity or similar scalar values, providing smooth dragging and value callbacks.

---

## 💻 Build and Run

### Prerequisites

* [cite_start]**Android Studio Giraffe+** recommended[cite: 232].
* [cite_start]Compile SDK: **36**[cite: 233].

### Instructions

1.  Clone the repository:
    ```bash
    git clone [Your Repository URL]
    ```
2.  Open the project in Android Studio.
3.  Set your desired device or emulator.
4.  Click **Run**. [cite_start]The app seeds default habits on first launch[cite: 234].
