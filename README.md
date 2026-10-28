# Safe Number 🔢🛡️

**Safe Number** is a fast-paced, offline Android puzzle game built with **Jetpack Compose** and **Kotlin**. Test your pattern recognition, mental agility, and quick decision-making under time pressure!

---

## 🎮 How to Play

1. **Analyze the Pattern**: A sequence of numbers is displayed following a hidden rule (e.g., `3, 6, 9, 12` with rule `+3`).
2. **Predict the Next Number**: Calculate the continuation of the sequence (the **Dangerous Number**).
3. **Pick Any SAFE Number**: Choose any available option **EXCEPT** the dangerous number before the countdown timer runs out!
4. **Build Your Streak**: Fast choices grant speed bonuses and increase your streak multiplier!

---

## ✨ Features

- 🧠 **Dynamic Pattern Generation**:
  - **Arithmetic**: Constant addition/subtraction steps.
  - **Geometric**: Multiplication sequences with controlled scaling.
  - **Alternating**: Dual-step rules (e.g., `+5, -2`).
  - **Increasing & Repeating Differences**: Evolving steps to challenge your focus.
- 🎯 **3 Difficulty Levels**: Easy, Medium, and Hard with custom time limits and pattern complexities.
- ⚡ **Speed Bonuses & Streak Milestones**: Score bonus points for quick reaction times and unbroken streaks.
- 🎨 **Modern Jetpack Compose UI**: Material 3 design system with full Light & Dark mode support and edge-to-edge layout.
- 🔊 **Audio & Tactile Feedback**: Custom synthesized sound effects and haptic vibration feedback.
- 📊 **Offline Statistics & Record Tracking**: Keeps track of high scores, best streaks, accuracy rate, and total rounds played locally.
- ✈️ **100% Offline**: Requires zero internet connection or intrusive permissions.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Architecture**: MVVM (Model-View-ViewModel) + Unidirectional Data Flow (StateFlow / UI State)
- **Navigation**: Jetpack Compose Navigation
- **State & Preferences**: Android SharedPreferences / Local Preferences Repository
- **Audio & Haptics**: Android `SoundPool` & `Vibrator` API
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)

---

## 📂 Project Structure

```
com.example.safenumber
├── audio/            # SoundPlayer & HapticController
├── data/
│   ├── generator/    # PatternGenerator & OptionGenerator engines
│   ├── model/        # GameState, Difficulty, NumberPattern, GameStatistics
│   └── repository/   # PreferencesRepository for local data
├── navigation/       # Screen definitions & SafeNumberNavGraph
├── ui/
│   ├── components/   # TimerBar, SequenceDisplay, OptionButtons, Dialogs
│   ├── screens/      # HomeScreen, GameScreen, ResultScreen, SettingsScreen, AboutScreen
│   ├── theme/        # Material 3 Color, Type, and Theme definitions
│   └── viewmodel/    # SafeNumberViewModel & SafeNumberUiState
└── MainActivity.kt   # Entry activity with edge-to-edge Compose setup
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (Ladybug | 2024.2.1 or newer recommended)
- **JDK 11** or higher
- **Android SDK**: `minSdk 26` (Android 8.0) | `targetSdk 37`

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Safe-Number.git
   cd Safe-Number
   ```

2. **Build the Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run on Device or Emulator**:
   Open the project in Android Studio and press **Run** (`Shift + F10`).

---

## 📄 License & Disclaimer

Safe Number is an entertainment game designed for casual fun and mental exercise. It is not a mathematical or cognitive assessment tool.

Developed with ❤️ using Kotlin & Jetpack Compose.
