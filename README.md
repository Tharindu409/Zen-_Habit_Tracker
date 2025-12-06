
🧘 Zen - Habit, Mood, and Wellness Tracker
Zen is a modern Android application designed to help users build consistent habits, track their daily mood, and visualize progress with a clean, glanceable UI. It's lightweight, fast, and user-friendly, featuring simple progress input and a polished profile.



✨ Features and Highlights

Habit Tracking: Track daily habits with customizable targets, units (steps, minutes, mL, etc.), and quick increments. Tick progress via detail pages, lists, or the home screen widget.




Mood Logging: Log moods using an emoji-first experience and view recent stats and trends.



Progress Visualization: Clean profile view displaying compact stats like streaks and habit totals. Lightweight progress bars are used for a tidy visual summary, avoiding heavy charts.





Home Screen Widget: A compact widget shows overall completion and a selected habit with a quick + button to add a tick and immediately refresh the widget.





Reminders: Notification scheduling for mood entries and hydration reminders.



Local Persistence: Data is stored locally using SharedPreferences via the ZenRepository. There is no cloud backup.




Step Tracking: Toggle support for the step sensor for relevant habits.


🎬 App Demo (Short Video)

 

🏗️ App Workflow (High-level)

Splash Screen.


Onboarding/Signup: First-time users are routed through onboarding screens and then to the signup process to collect initial info and seed default data.




Home: The main hub, hosting bottom navigation to Habits, Mood, Profile, and Settings.



Tracking: Add/Edit habits and tick progress directly from the Home/Habits screens or the customizable Widget.


Notifications: If enabled, notifications remind you to log moods or drink water.


Profile & Settings: Profile shows compact stats, and Settings allows configuration of notifications, schedules, and the widget habit.

⚙️ Build and Run
Prerequisites

Android Studio Giraffe+ recommended.

Compile SDK: 36.

Instructions
Clone the repository.

Open the project in Android Studio.

Set your device or emulator.

Click Run.

The application will automatically seed default habits and initial state on first launch.


📂 Project Structure and Technology
The codebase favors clarity over heavy architecture, utilizing a repository-based approach with activities and lightweight custom views.

Language: Kotlin


Persistence: SharedPreferences via ZenRepository.kt.


Data Models: Key models include Habit.kt, MoodEntry.kt, and AppSettings.kt.


Core Components:


Activities: Dedicated activities for each major screen (e.g., HomeActivity.kt, HabitsActivity.kt, MoodActivity.kt).


Receivers: ReminderReceiver.kt (for alarms) and BootReceiver.kt (for re-scheduling on boot).





Service: StepService.kt for step sensor integration.



Widget: ZenWidgetProvider.kt manages the home screen widget functionality.



Workers: MidnightWorker.kt performs daily rollovers/maintenance.


Shared Utilities: Core logic is centralized in utilities like ZenRepository (persistence), DateUtils (date/time), AlarmScheduler (reminders), and PulseNotificationHelper (notifications).
