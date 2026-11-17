<div align="center">

# 🏐 Beach Volleyball Scoreboard ☀️

**A high-contrast, 100% offline Beach Volleyball Scoreboard built for outdoor sunlight — available as a Native Android APK (with Bluetooth clicker support) and an installable Web App / Docker container.**

[![Latest Release](https://img.shields.io/github/v/release/yosefxk/volleyball-scoreboard?style=for-the-badge&color=00e5ff&label=Latest%20APK)](https://github.com/yosefxk/volleyball-scoreboard/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Web%20PWA%20%7C%20Docker-ffeb3b?style=for-the-badge)](https://github.com/yosefxk/volleyball-scoreboard/releases)
[![Offline](https://img.shields.io/badge/Offline-100%25%20Ready-22c55e?style=for-the-badge)](#)

[📥 **Download Latest Android APK**](https://github.com/yosefxk/volleyball-scoreboard/releases/latest) • [🌐 **Open Live Web App**](https://yosefxk.github.io/volleyball-scoreboard/)

</div>

---

## ✨ Key Features

- **☀️ Sunlight-Optimized Split Arena**: Massive edge-to-edge typography with **Neon Yellow (Team A)** vs. **Neon Cyan (Team B)** on pure black for maximum outdoor contrast.
- **🎯 Deterministic Scoring & Hold-to-Undo**:
  - **Short Tap / Click (`< 0.55s`)** → `+1` Point (with a `450ms` cooldown so accidental double-taps are ignored).
  - **Long Hold (`≥ 0.55s`)** → `-1` Point (Undo) with an animated hold progress bar and haptic feedback.
- **🏐 Live Serve Indicator**: Automatically highlights the scoring team's `SERVE` badge, or tap either badge directly to switch server manually.
- **📋 Full-Screen Match & Point Log**:
  - Landscape split-view dashboard showing all archived games on the left and a scrollable, timestamped point-by-point timeline on the right.
  - **Real-Time File Logging (Android)**: Every point, key press, and reset is flushed to disk on the fly and cleanly synced to a single `Download/volleyball-scoreboard.log` file, with one-tap **📁 View File** and **Share ↗** actions.
- **🔄 3-Option New Game Dialog**: Start fresh cleanly with **New Game & Save Score** (Green), **New Game & Discard Score** (Red), or **Cancel** (Grey).
- **🔒 100% Offline**: Bundles all assets locally in the Android APK and caches via Service Worker (`sw.js`) on the web—zero internet required at the beach.

---

## 📱 Native Android APK & Bluetooth Remote Support

Because web browsers block HID Bluetooth selfie remotes (like the **AB Shutter 3**), the native Android wrapper intercepts hardware keys directly at the OS level via `dispatchKeyEvent`:

- **No Volume Slider Popups**: Volume & camera shutter events are consumed cleanly before Android can show system UI.
- **Always-Awake & Immersive Landscape**: Locks to landscape orientation with `FLAG_KEEP_SCREEN_ON` and hides system navigation/status bars.
- **Hardware Debounce**: `120ms` debounce and `repeatCount == 0` filtering prevent accidental multi-point jumps.

### 🎮 Default Controls & Key Mappings

| Team | Touch / Mouse | Android Hardware / Bluetooth Remote Keys | Web Keyboard |
| :--- | :--- | :--- | :--- |
| **Team A (Yellow)** | Tap left half | `Volume Up`, `Camera`, `Enter`, `D-Pad Center`, `Page Up`, `Button A`, `Media Previous` | `ArrowLeft`, `A`, `1`, `PageUp` |
| **Team B (Cyan)** | Tap right half | `Volume Down`, `Space`, `Page Down`, `Button B`, `Media Next`, `Play/Pause`, `Headset Hook` | `ArrowRight`, `D`, `2`, `Space`, `PageDown` |

### 📥 Installation (Android)
1. Open the [**Releases Page**](https://github.com/yosefxk/volleyball-scoreboard/releases/latest).
2. Download **`volleyball-scoreboard.apk`**.
3. Open the APK on your Android device to install (Android 7.0+ / API 24+).

---

## 🐳 Docker / Self-Hosting

Run the web scoreboard locally with Docker Compose:

```bash
docker compose up -d --build
```

Then open `http://<SERVER_IP>:8086` in any browser (or install it as a PWA).

---

## 🛠️ Tech Stack

- **Android Wrapper**: Kotlin (`MainActivity.kt`, `ScoreboardLogger.kt`), `WebView` + `@JavascriptInterface`, `MediaStore` & `FileProvider`
- **Frontend**: Zero-dependency vanilla HTML/CSS/JS + Service Worker (`sw.js`) + `manifest.json`
- **CI/CD**: Automated GitHub Actions workflow (`.github/workflows/build-apk.yml`) building signed APKs on every push to `main`
