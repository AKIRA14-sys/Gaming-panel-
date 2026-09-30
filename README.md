# GamePanel AI — Native Android Application

**GamePanel AI** is a lightweight, customizable gaming utility for Android devices, specifically optimized for budget and low-end devices like the **Samsung Galaxy A06**.

---

## 📱 APK Location & Download

The pre-compiled debug APK is committed directly in this repository:

📍 **`release/gamepanel-ai-debug.apk`**

### Installation Steps
1. Download `release/gamepanel-ai-debug.apk` onto your Android device.
2. Enable "Install from unknown sources" in Android settings if prompted.
3. Install and open **GamePanel AI**.
4. Grant the **Display over other apps** permission when starting the floating crosshair overlay.

---

## ✨ Features

- 🎯 **200+ Crosshair Gallery**: Extracted transparent high-clarity crosshair designs.
- 🎮 **Game Profile Selector**: Separate saved profiles for *Free Fire*, *Free Fire MAX*, *Call of Duty: Mobile*, *Blood Strike*, *PUBG Mobile*, and *Other Games*.
- 🎨 **Crosshair Customization**:
  - Opacity presets (20%, 35%, 55%, 75%, 100%) and continuous slider.
  - Preset color palette (White, Red, Green, Blue, Cyan, Yellow, Purple, Pink, Orange) & custom color picker.
  - Size, line thickness, rotation (0° - 360°), and center dot controls.
  - Crosshair position lock toggle.
- 📐 **Visual Position Editor**: Touch drag-and-drop preview and numerical X/Y offset sliders with center/reset reference controls.
- 💡 **Intelligence Mode**: Analyzes device screen resolution, DPI, and target game to recommend visual crosshair configurations with user approval.
- 🎯 **Practice / Calibration Mode**: Safe target grid inside the application for visual alignment.
- ⚡ **Performance & Display Tools**: Displays available RAM, CPU architecture, battery level, Android version, screen resolution, and refresh rate recommendations.
- 📱 **Floating Quick Panel**: Draggable quick control panel on top of games to switch games, adjust opacity/size, and toggle crosshairs.
- 🔒 **Fair Play Guarantee**: Contains **NO cheats, memory modifications, aimbot, recoil automation, or input automation**.

---

## 🛠️ Building from Source

To compile the application locally using Gradle:

```bash
# Clone the repository
git clone https://github.com/your-repo/gaming-panel.git
cd gaming-panel

# Run unit tests
gradle test

# Build Debug APK
gradle assembleDebug
```

The generated APK will be available at `app/build/outputs/apk/debug/app-debug.apk`.
