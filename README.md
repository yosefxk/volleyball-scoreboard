# Beach Volleyball Scoreboard 🏐☀️

A high-contrast, sunlight-optimized single-page Beach Volleyball Scoreboard web app designed for phones, tablets, and outdoor courts.

## 🚀 Features

- **Sunlight & Outdoor Visibility**: High-contrast athletic typography and custom color palettes designed to be legible under bright sunlight.
- **Bluetooth HID Clicker / Remote Support**:
  - Connect any Bluetooth presentation clicker, selfie button, or keyboard.
  - Interactive Key Mapping modal to record and bind `event.code` to Team A / Team B.
  - Saved to `localStorage`.
- **Double-Click Scoring Logic**:
  - Single Click = `+1` point (with audio announcement).
  - Rapid Double-Click (< 400ms) = `-1` point (undoes accidental trigger).
- **Web Speech API**: Speaks score aloud after every point (e.g. *"12 serving 9"*, *"Switch sides! 14 serving 7"*).
- **Court Side Switch Alert**: Animated banner reminder when total points reach a multiple of 7 (standard FIVB / AVP beach volleyball rule).
- **Screen Wake Lock API**: Keep the screen awake during entire matches with one tap.
- **Set & Match Tracking**: Supports Best of 3 sets, tiebreak rules, customizable target points (21/15/25), and set history.
- **Court Side Swap**: Instantly flip team positions on screen to match physical court orientation.

## 🐳 Docker Deployment

To run locally with Docker Compose:

```bash
docker compose up -d --build
```

Access the scoreboard at [http://localhost:8085](http://localhost:8085).
