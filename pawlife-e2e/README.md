# 🛡️ Enterprise Selenium E2E Automation Framework (React + Node.js)

A production-grade, scalable End-to-End (E2E) Test Automation Framework designed for modern React Single Page Applications (SPAs). Built with **Selenium WebDriver**, **Node.js**, **Mocha**, **Chai**, **ExcelJS**, and **Winston Logger**.

---

## 🏛️ Framework Architecture

```text
pawlife-e2e/
├── config/
│   └── environment.js             # Environment-driven configuration (Local, Dev, Staging, Prod)
├── pages/                         # Page Object Model (POM) abstractions
│   ├── BasePage.js                # Core page methods, waits, navigation, local storage
│   ├── AuthPage.js                # Authentication, forms, persona switching
│   ├── DashboardPage.js           # Header, navigation tabs, status badges, modals
│   └── FeedingPage.js             # Nutrition and feeding logs
├── utilities/
│   ├── DriverFactory.js           # Multi-browser builder (Chrome, Firefox, Edge, Headless)
│   ├── WebDriverHelper.js         # Enterprise explicit waits, JS execution, scrolling, alerts
│   ├── ExcelReportGenerator.js    # 4-sheet formatted Excel report engine using ExcelJS
│   ├── RouteAndFormScanner.js     # Dynamic React route and form constraint auto-discovery
│   └── Logger.js                  # Winston multi-transport logger (console & file rotation)
├── tests/
│   ├── baseTest.js                # Global Mocha lifecycle hooks, failure screenshot capture
│   ├── authentication.test.js     # Auth, session persistence, boundary & password rules
│   ├── navigation.test.js         # Tab switching, browser back/forward, page reload
│   ├── uiElements.test.js         # Modals, buttons, dropdowns, pet switches
│   ├── formValidation.test.js     # Input boundaries, numeric validation
│   └── dynamicScanner.test.js     # Dynamic tests auto-generated from React AST / JSX
├── reports/
│   ├── excel/                     # E2E_Report.xlsx (Generated after every test run)
│   ├── mochawesome/               # HTML visual report with charts and execution timeline
│   └── failures/                  # Automated failure screenshots and diagnostic captures
├── logs/
│   ├── execution.log              # Comprehensive timestamped run logs
│   └── failures.log               # Detailed stack traces on failure
├── .mocharc.json                  # Mocha test runner configuration & reporter options
├── package.json                   # Dependencies, scripts, and runners
└── README.md                      # Architecture and operational manual
```

---

## 🚀 Key Architectural Features

### 1. Multi-Browser & Headless Support
- **Google Chrome** (Headed & Headless v2)
- **Mozilla Firefox** (Headed & Headless)
- **Microsoft Edge** (Chromium engine)

### 2. Automated Excel Reporting (`E2E_Report.xlsx`)
Generates a multi-sheet Microsoft Excel report with styling, conditional color formatting, and metric calculation:
- **Sheet 1: Summary** — Total tests, Passed, Failed, Skipped, Pass %, Execution duration.
- **Sheet 2: Test Cases** — Test ID, Module, Scenario Name, Browser, Status, Timestamps, Duration.
- **Sheet 3: Failed Tests** — Test Name, Error Reason, Screenshot Path, Browser, URL.
- **Sheet 4: Execution Logs** — Step-by-step audit trail.

### 3. Automatic Failure Capture
Upon any assertion failure or timeout:
- **PNG Screenshot**: Saved under `reports/failures/FAILURE_<TestName>_<Timestamp>.png`.
- **Browser Console Logs**: Extracted and attached to diagnostic logs.
- **Current URL & DOM state**: Recorded in the Excel failure sheet and Mochawesome report.

### 4. 🧠 Dynamic React Route & Form Auto-Discovery (`RouteAndFormScanner.js`)
Instead of testing only hardcoded scenarios, the framework inspects React component sources (`App.jsx`, `AuthScreen.jsx`, etc.):
- Discovers all application tabs and internal routes.
- Analyzes `<input>`, `<select>`, and `<form>` constraints (`required`, `type="email"`, `minLength`, `min`, `max`).
- Dynamically synthesizes and executes test matrices for boundary violations, empty submissions, and malformed inputs.

---

## 💻 Installation & Setup

```bash
# 1. Navigate to the automation directory
cd pawlife-e2e

# 2. Install dependencies
npm install
```

---

## 🏃 Running Tests

### Standard Execution
```bash
# Run full suite (Headless Chrome)
npm test

# Run with visible browser window (Headed)
npm run test:chrome

# Run on Mozilla Firefox
npm run test:firefox

# Run on Microsoft Edge
npm run test:edge
```

### Targeted Test Suites
```bash
# Run Authentication & Session tests only
npm run test:auth

# Run UI Elements & Modals tests only
npm run test:ui

# Run Navigation & Routing tests only
npm run test:nav

# Run Dynamic Route & Form Scanner tests only
npm run test:dynamic

# Run parallel execution across test files
npm run test:parallel
```

### Environment Switching
```bash
# Run against Staging
cross-env TEST_ENV=staging npm test

# Run against Production
cross-env TEST_ENV=prod npm test
```

---

## 📊 Viewing Test Reports
After tests complete:
- **Excel Report**: Open `pawlife-e2e/reports/excel/E2E_Report.xlsx`.
- **HTML Report**: Open `pawlife-e2e/reports/mochawesome/E2E_Test_Report.html` in your browser.
- **Failure Screenshots**: View image files in `pawlife-e2e/reports/failures/`.

---

## 🔄 GitHub Actions CI/CD Integration
Defined in [`.github/workflows/selenium-e2e.yml`](../.github/workflows/selenium-e2e.yml):
- Automatically triggers on every `push` and `pull_request` to `main` and `develop`.
- Spawns the backend service and React web client.
- Executes the Selenium suite in headless mode.
- Archives and uploads the `E2E_Report.xlsx`, Mochawesome HTML report, screenshots, and logs as downloadable artifacts.
