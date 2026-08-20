# Beach Volleyball Scoreboard 🏐☀️

A high-contrast, sunlight-optimized Beach Volleyball Scoreboard available as a **Web App** (Docker/PWA) and a **Native Android APK** with hardware key interception for Bluetooth selfie remotes (e.g. AB Shutter 3).

---

## 📱 Android APK (Native Remote Support)

The native Android app intercepts physical hardware keys (Volume Up, Volume Down, Camera, Enter) from Bluetooth clickers like the **AB Shutter 3** at the OS level:
- **Zero Volume Slider**: Volume buttons are consumed and directly score for Team A / Team B.
- **Native Text-to-Speech**: Instant, clear audio announcements (e.g. *"12 serving 9"*).
- **Always-Awake & Fullscreen**: No sleeping, no address bars.

### 📥 Download APK
1. Go to the [Releases Page](https://github.com/yosefxk/volleyball-scoreboard/releases).
2. Download `app-debug.apk`.
3. Tap to install on your Android phone/tablet.

---

## 🚀 Web App Features

- **Sunlight & Outdoor Visibility**: High-contrast athletic typography and custom color palettes designed to be legible under bright direct sunlight.
- **Double-Click Scoring Logic**: Single Click = `+1` point; Rapid Double-Click (< 400ms) = `-1` point (undo).
- **Web Speech API**: Speaks score aloud after every point change.
- **Court Side Switch Alert**: Animated banner reminder when total points reach a multiple of 7.
- **Screen Wake Lock API**: Keep the screen awake during entire matches.
- **Set & Match Tracking**: Supports Best of 3 sets, tiebreak rules, customizable target points (21/15/25), and set history.

---

## 🐳 Docker Deployment

To run locally with Docker Compose:

```bash
docker compose up -d --build
```

Access the scoreboard at `http://<SERVER_IP>:8086`.
