# 📱 Enterprise Appium E2E Automation Framework for Android (Node.js)

A production-grade Mobile Test Automation Framework engineered for native Android applications and modern Jetpack Compose architectures. Built with **Appium 2.x**, **UiAutomator2**, **Node.js**, **Mocha**, **Chai**, **ExcelJS**, and **Winston Logger**.

---

## 🏛️ Framework Architecture

```text
pawlife-mobile-e2e/
├── config/
│   └── appiumConfig.js            # Dynamic device detection & capability provisioning
├── pages/                         # Page Object Model (POM) abstractions
│   ├── BasePage.js                # Common element waits, keyboard actions, alerts, gestures
│   ├── AuthPage.js                # Mobile login, signup, persona selector, error validation
│   ├── DashboardPage.js           # Bottom navigation, Tele-Vet modal, pet toggling
│   └── FeedingPage.js             # Nutrition tracker, meal logs, portion size validations
├── utilities/
│   ├── DriverFactory.js           # Appium 2.x session lifecycle management
│   ├── GestureHelper.js           # W3C Actions API (Tap, LongPress, Swipe, Scroll, Zoom)
│   ├── DeviceHelper.js            # ADB bridge, logcat extraction, failure screenshot capture
│   ├── PerformanceHelper.js       # App launch duration, screen load latency, crash detection
│   ├── SmartUiScanner.js          # Dynamic Android UI analyzer & test scenario generator
│   ├── ExcelReportGenerator.js    # 4-sheet formatted Excel report engine using ExcelJS
│   └── Logger.js                  # Winston multi-transport logger (console & rotating files)
├── tests/
│   ├── baseTest.js                # Global Mocha lifecycle hooks, diagnostics on failure
│   ├── authentication.test.js     # Empty inputs, password length, credentials, session
│   ├── formValidation.test.js     # Form input rules, required fields, numeric boundaries
│   ├── navigation.test.js         # Bottom tabs, hardware Back button, app relaunch
│   ├── gesturesAndUi.test.js      # Touch gestures, vertical scroll, modal dialogs
│   └── performance.test.js        # Cold launch benchmarking, fatal crash & ANR monitoring
├── testdata/
│   └── testData.json              # Data-driven test datasets (credentials, boundaries)
├── drivers/
│   └── .gitkeep                   # Local driver / APK storage
├── reports/
│   ├── excel/                     # Mobile_E2E_Report.xlsx (Generated after every test run)
│   ├── mochawesome/               # HTML visual report with charts and timelines
│   └── failures/                  # Failure screenshots (.png) and logcat files
├── screenshots/                   # Mirror directory for screenshots
├── logs/
│   ├── mobile-execution.log       # Timestamped execution audit trail
│   └── failures.log               # Stack traces and fatal logs
├── excel/
│   └── Mobile_E2E_Report.xlsx     # Direct mirror of primary Excel report
├── .github/workflows/
│   └── appium-e2e.yml             # GitHub Actions Android emulator CI/CD pipeline
├── .mocharc.json                  # Mocha test runner configuration & reporter options
├── package.json                   # Dependencies, scripts, and runners
└── README.md                      # Operational and architecture manual
```

---

## 🚀 Key Architectural Features

### 1. Dual Application Launch Modes
- **APK Execution Mode**: Automatically installs and launches the debug/release APK.
  ```bash
  cross-env LAUNCH_MODE=apk APK_PATH=../PawLife/app/build/outputs/apk/debug/app-debug.apk npm test
  ```
- **Installed App Mode**: Connects directly to pre-installed application without reinstallation.
  ```bash
  cross-env LAUNCH_MODE=installed APP_PACKAGE=com.pawlife.app APP_ACTIVITY=com.pawlife.app.presentation.MainActivity npm test
  ```

### 2. Dynamic Device & Emulator Auto-Discovery
`AppiumConfig.detectConnectedDevice()` executes ADB inspection to automatically detect:
- Connected real Android devices (Samsung, OnePlus, Pixel, Xiaomi, etc.)
- Android Virtual Devices (AVD / Emulators)
- Dynamic Android OS version extraction (`ro.build.version.release`) supporting Android 10, 11, 12, 13, 14, and 15+.

### 3. Comprehensive W3C Gesture Automation (`GestureHelper.js`)
All touch events are implemented using the standard **W3C Actions API**:
- **Tap & Double Tap**: Exact coordinate touch dispatch.
- **Long Press**: Configurable hold duration (default 1500ms).
- **Swipes**: `swipeUp`, `swipeDown`, `swipeLeft`, `swipeRight` with percentage-based viewport calculations.
- **Scroll Until Visible**: Repeated swipe loop until target element is displayed.
- **Drag & Drop**: Smooth pointer move between source and target coordinates.
- **Pinch & Zoom**: Dual-pointer simultaneous gestures for maps and images.

### 4. Automated 4-Sheet Excel Reporting (`Mobile_E2E_Report.xlsx`)
- **Sheet 1: Summary** — Execution Date, Device Name, Android Version, Total Tests, Passed, Failed, Skipped, Pass %, Execution Duration.
- **Sheet 2: Test Cases** — Test ID, Module, Scenario, Device, Status, Start Time, End Time, Duration.
- **Sheet 3: Failed Tests** — Test Name, Failure Reason, Screenshot Path, Device, Android Version, Activity Name.
- **Sheet 4: Execution Logs** — Step-by-step diagnostic audit trail.

### 5. Failure Diagnostics (`DeviceHelper.js`)
On any test assertion failure:
- **PNG Screenshot**: Saved under `reports/failures/FAILURE_<TestName>_<Timestamp>.png`.
- **Logcat Extraction**: Last 120 lines of Android system logs saved under `logs/logcat_<TestName>.log`.
- **Active Window Activity**: Logged to Excel and Mochawesome report.

### 6. 🧠 Smart AI UI Analysis (`SmartUiScanner.js`)
- Inspects Android view hierarchy and Compose nodes.
- Discovers `<EditText>`, buttons, and bottom navigation items automatically.
- Generates dynamic validation test matrices (`DYN-MOB-*`) for required fields, password constraints, and email formats.

---

## 💻 Installation & Setup

### Prerequisites
1. **Node.js**: v18.x or v20.x or v22.x
2. **Java JDK**: 17+
3. **Android SDK & platform-tools** (`adb` on system PATH)
4. **Appium 2.x**:
   ```bash
   npm install -g appium
   appium driver install uiautomator2
   ```

### Framework Setup
```bash
# Navigate to framework root
cd "C:\Saveetha pdd\pawlife-mobile-e2e"

# Install dependencies
npm install
```

---

## 🏃 Test Execution

```bash
# 1. Start Appium Server (in a separate terminal)
appium

# 2. Run all mobile E2E tests
npm test

# 3. Run specific test modules
npm run test:auth          # Authentication & session persistence
npm run test:forms         # Form rules & input validation
npm run test:nav           # Bottom tabs & hardware back button
npm run test:gestures      # Swiping, scrolling & modals
npm run test:perf          # Launch time & crash monitoring

# 4. Run against an APK file
npm run test:apk

# 5. Run against already installed app
npm run test:installed
```

---

## 🔄 GitHub Actions CI/CD Pipeline
Defined in [`.github/workflows/appium-e2e.yml`](../.github/workflows/appium-e2e.yml):
- Provisions a macOS runner with hardware-accelerated Android Emulator (Pixel 6, API 34).
- Builds the `PawLife` debug APK using Gradle.
- Starts Appium 2.x and executes the complete test suite.
- Publishes `Mobile_E2E_Report.xlsx`, Mochawesome HTML report, screenshots, and logcat logs as build artifacts.
