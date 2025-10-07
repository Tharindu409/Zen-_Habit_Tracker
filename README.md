# Zen — Habit, Mood, and Wellness Tracker

Zen is a modern Android app that helps you build consistent habits, track your daily mood, and visualize progress with a clean, glanceable UI. It’s designed to be lightweight, fast, and friendly—featuring a customizable home screen widget, simple progress input, and a polished profile with personal stats.

## Highlights
- Track daily habits with targets, units (steps, minutes, mL, etc.), and quick increments.
- Log moods with an emoji-first experience and view simple trends.
- Compact home screen widget with a quick “+” button for your selected habit.
- Notification scheduling for mood entries and hydration reminders.
- Clean profile view with streaks and habit totals.
- Local data storage using SharedPreferences (no cloud backup).

---

## App Workflow (High-level)
1. Splash → Onboarding/Signup → Home
2. Home hosts bottom navigation to Habits, Mood, Profile, and Settings
3. Add/Edit habits and tick progress directly from Home/Habits or the Widget
4. Notifications (if enabled) remind you to log moods or drink water
5. Profile shows compact stats; Settings configures notifications and widget habit

---

## Project Structure
```
Zen/
├─ app/
│  ├─ src/main/
│  │  ├─ AndroidManifest.xml
│  │  ├─ java/com/example/zen/
│  │  │  ├─ MainActivity.kt
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
│  │  └─ res/
│  │     ├─ layout/
│  │     └─ xml/
├─ build.gradle.kts (root)
├─ settings.gradle.kts
├─ gradle.properties
└─ README.md
```

---

## AndroidManifest and Key XML
- `AndroidManifest.xml`
  - Declares activities (Splash, Home, Habits, Mood, Profile, Settings, etc.).
  - Registers receivers: `ReminderReceiver` (alarms), `BootReceiver` (re-scheduling on boot), `ZenWidgetProvider` (home screen widget).
  - Declares `StepService` for step sensor integration.
  - Permissions for POST_NOTIFICATIONS, ACTIVITY_RECOGNITION, and exact alarms.
- `res/xml/zen_widget_info.xml`
  - Config for the home screen widget (layout, resize, config activity).
- `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`
  - Opt-in backup/extraction rules for app data.

---

## Kotlin Sources — Detailed File Guide
Below is an explicit two-sentence description for each Kotlin file.

### Root package
- `MainActivity.kt` — Simple container/entry activity used for navigation hand-off or hosting fragments when required. Most user navigation occurs via the dedicated activities in the `activities/` package.

### activities/
- `SplashActivity.kt` — Launch screen that sets up theme and routes to onboarding or directly to Home. It performs minimal bootstrapping using repository state.
- `OnboardingActivity.kt` — Shows onboarding screens for first-time users. Guides to `SignupActivity` or `HomeActivity` depending on state.
- `SignupActivity.kt` — Collects initial user info and seeds default data. It writes to `ZenRepository` and then navigates to Home.
- `HomeActivity.kt` — Main hub with bottom navigation and home feed. Orchestrates quick actions and summary widgets for habits/mood.
- `HabitsActivity.kt` — Displays the list of habits, allows creation/deletion and navigation to `HabitDetailActivity`. It reads/writes habit data via `ZenRepository`.
- `HabitDetailActivity.kt` — Shows detailed progress, target, unit, and quick increment actions for a single habit. Lets users tick progress and view per-day stats.
- `MoodActivity.kt` — Mood logging screen with emoji-first interactions and recent history. Uses custom views to visualize recent mood distribution.
- `ProfileActivity.kt` — Profile page with compact stats (Total Habits, Best Streak) using `ProfileStatsView`. Loads metrics from the repository and renders text-forward progress without charts.
- `EditProfileActivity.kt` — Edit user name/avatar and related profile settings. Commits updates back to `ZenRepository`.
- `SettingsActivity.kt` — Configures notifications (mood, hydration), step sensor, and widget habit. Persists to `AppSettings`, (re)schedules alarms, and enables/disables schedule cards from switches.
- `AddMoodActivity.kt` — Lightweight activity to quickly add a mood entry. Often launched from notifications or shortcuts.

### adapters/
- `MoodAdapter.kt` — RecyclerView adapter for showing a list of mood entries. Binds mood emoji, note, and timestamp with lightweight view holders.

### dialogs/
- `MoodBottomSheetDialog.kt` — Bottom sheet for mood selection and input. Exposes callbacks to persist data to the repository.
- `EmojiGridAdapter.kt` — Adapter for the emoji picker grid within dialogs/sheets. Provides a compact emoji set and selection handling.

### fragments/
- `AddHabitBottomSheetFragment.kt` — Bottom sheet to add a new habit with title, emoji, unit, target, and default increment. Validates input and sends the result back to the host.

### models/
- `AppSettings.kt` — Settings data model including notification toggles, schedules, widget selection, and step sensor flag. Stored and retrieved via `ZenRepository`.
- `Habit.kt` — Represents a habit: id, title, unit, target per day, default increment, and emoji. Used across UI and repository.
- `HabitTick.kt` — A recorded progress “tick” for a habit on a given date with an amount. Aggregated to calculate daily progress and completion.
- `MoodEntry.kt` — A single mood log entry with emoji/value, optional note, and timestamp. Used to compute recent mood trends and charts.
- `UserProfile.kt` — User profile data including name, avatar, and streaks. Drives Profile UI and initial onboarding personalization.

### receivers/
- `BootReceiver.kt` — Listens for BOOT_COMPLETED to reschedule alarms after device restart. Reads `AppSettings` and re-triggers alarm setup.
- `ReminderReceiver.kt` — Handles alarm broadcasts and triggers user notifications. Delegates to `PulseNotificationHelper` to build and show notifications.

### repo/
- `ZenRepository.kt` — Single source of truth backed by SharedPreferences; manages habits, ticks, moods, settings, and seeding. Exposes simple CRUD and query helpers and resets all data when requested.
- `PrefsKeys.kt` — Centralized keys for SharedPreferences. Ensures a consistent schema and avoids typos.
- `SeedData.kt` — Provides default habits and initial state on first run or reset. Called by the repository during initialization.

### sensors/
- `StepService.kt` — Foreground/background service for step counting when enabled. Updates relevant habit data and supports live summaries.

### util/
- `AlarmScheduler.kt` — Creates and manages exact/inexact alarms for hydration/mood reminders. Calculates intervals, schedules/cancels alarms, and cooperates with `ReminderReceiver`.
- `ChartUtils.kt` — Utilities for chart math and formatting. Used by older or optional chart views; largely supplementary with the text-first redesign.
- `DateUtils.kt` — Helpers for date formatting, now-date strings, and day differences/streak calculus. Used across Profile and tracking logic.
- `EmojiPalette.kt` — Central list and helpers for emoji selections. Keeps UI emoji usage consistent.
- `PulseNotificationHelper.kt` — Builds and displays notifications for reminders with channels and actions. Ensures compliance with Android 13+ permission models.
- `SerializationUtils.kt` — Small utilities for serializing and deserializing app data where needed. Keeps persistence code tidy.

### views/
- `ProfileStatsView.kt` — Custom view that renders two rows (Total Habits, Best Streak) with emoji, big value text, and a right-side progress bar. Animates on data set and uses dp/sp conversions for crisp layout.
- `HabitProgressChartView.kt` — Simple habit progress visualization view. Useful for compact visual summaries in detail pages.
- `MoodChartView.kt` — Minimal mood trend visualization block. Complements text stats with a subtle chart.
- `MoodStatsView.kt` — Text-first mood stats renderer aggregating recent entries. Focused on readability and scannability.
- `EmojiSliderView.kt` — Emoji-based slider input for mood intensity or similar scalar values. Provides smooth dragging and value callbacks.

### widgets/
- `ZenWidgetProvider.kt` — AppWidgetProvider for the home screen widget; renders overall progress and a selected habit card. Handles quick “+” actions via broadcast PendingIntent and refreshes all widgets.
- `WidgetConfigActivity.kt` — Widget configuration UI to choose the habit the widget will control. Saves selection into `AppSettings` via repository.
- `NoMoveFabBehavior.kt` — Prevents a FloatingActionButton from moving under certain CoordinatorLayout behaviors. Handy for stable FAB placement.

### work/
- `MidnightWorker.kt` — WorkManager worker that performs midnight rollovers or maintenance. Ensures daily counters and reminders reset safely.

---

## Layout Resources — What Each Screen/Item Does
- `activity_splash.xml` — Splash screen container for the app’s initial load.
- `activity_onboarding.xml` — Onboarding screen(s) layout with pager content.
- `activity_signup.xml` — Signup form for capturing initial user data.
- `activity_home.xml` — Home dashboard with quick glance progress and navigation.
- `activity_habits.xml` — Habit list with add button and rows.
- `activity_habit_detail.xml` — Detailed habit view with title, target, progress, and actions.
- `activity_mood.xml` — Mood logging interface with emoji controls and recent logs.
- `activity_add_mood.xml` — Focused quick-add mood entry UI.
- `activity_profile.xml` — Profile screen with header and “Your Progress” card using `ProfileStatsView`.
- `activity_edit_profile.xml` — Profile edit UI (name, avatar, etc.).
- `activity_settings.xml` — Settings page with notification toggles, schedules, and widget configuration.
- `activity_main.xml` — Generic host activity layout (if used).
- `activity_widget_config.xml` — Widget configuration screen.
- `fragment_add_habit.xml` — Bottom sheet content for adding a habit.
- `bottom_sheet_mood.xml` — Mood entry bottom sheet UI.
- `item_habit.xml` — A card/row for a habit in lists.
- `item_habit_row.xml` — Alternate compact habit row.
- `item_favorite_habit.xml` — Favorite habit list item.
- `item_mood_entry.xml` — Mood entry list item.
- `item_onboarding_page.xml` — A single onboarding page template.
- `item_emoji_grid.xml` — Emoji grid item for pickers.
- `item_emoji_slider.xml` — Emoji slider thumb row item.
- `widget_pulse.xml` — The home screen widget layout with overall progress and quick add controls.

---

## Feature Walkthrough
- Habits
  - Create habits with units, targets per day, and default increments. Tick progress via detail pages, lists, or the widget.
- Mood
  - Log moods with emojis and optional notes; view recent stats and trends. Schedule reminders to encourage regular logging.
- Profile
  - See total habits and best streak rendered in a simple text + emoji style. Lightweight progress bars keep visuals tidy without heavy charts.
- Settings
  - Toggle mood and hydration reminders; configure schedules and intervals. Choose which habit the widget controls; toggle step sensor support.
- Widget
  - Shows overall completion and a selected habit with a quick “+” button. Tapping adds a tick and refreshes the widget immediately.

---

## Data & Persistence
- Single-process storage via SharedPreferences through `ZenRepository`.
- `AppSettings` stores notification toggles/schedules, widget selection, and sensor flags.
- No cloud backup or external database—focused on local simplicity.

---

## Build & Run
- Android Studio Giraffe+ recommended.
- Compile SDK 36; AGP may warn if not the latest compatible version.
- Set your device/emulator and Run. The app seeds defaults on first launch.

---

## Contributing & Notes
- The codebase favors clarity over heavy architecture; repository + activities + lightweight views.
- Custom views use dp/sp conversions and avoid overdraw; animations kept subtle.
- Notifications comply with Android 13+ runtime permission for posting notifications.

---

## What’s Shared Between Screens
- `ZenRepository` for all persistence and domain operations.
- `DateUtils` for all date/time formatting and calculations.
- `EmojiPalette` for consistent emoji usage.
- `AlarmScheduler` + `ReminderReceiver` for reminder flows (hydration & mood).
- `PulseNotificationHelper` for building notifications and channels.
- Layout tokens/styles from themes and shared item layouts across lists.
