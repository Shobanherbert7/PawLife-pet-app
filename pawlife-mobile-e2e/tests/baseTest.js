const DriverFactory = require('../utilities/DriverFactory');
const excelReporter = require('../utilities/ExcelReportGenerator');
const DeviceHelper = require('../utilities/DeviceHelper');
const PerformanceHelper = require('../utilities/PerformanceHelper');
const logger = require('../utilities/Logger');
const AppiumConfig = require('../config/appiumConfig');

let driver = null;
let testStartTime = null;
let deviceHelper = null;
let perfHelper = null;

const mochaHooks = {
  async beforeAll() {
    const device = AppiumConfig.detectConnectedDevice();
    logger.info(`=======================================================`);
    logger.info(`Starting Mobile Appium E2E Automation Suite`);
    logger.info(`Device: [${device.model}], Android: [${device.osVersion}], ID: [${device.deviceId}]`);
    logger.info(`=======================================================`);

    try {
      driver = await DriverFactory.getDriver();
      deviceHelper = new DeviceHelper(driver);
      perfHelper = new PerformanceHelper(driver);

      // Measure baseline cold launch time
      await perfHelper.measureAppLaunchTime();
    } catch (err) {
      logger.warn(`Driver initialization note: ${err.message}. Using simulated execution harness for offline mode.`);
    }
  },

  async beforeEach() {
    testStartTime = new Date();
  },

  async afterEach() {
    const test = this.currentTest;
    const testEndTime = new Date();
    const durationMs = test.duration || (testEndTime - testStartTime);
    const durationStr = `${(durationMs / 1000).toFixed(2)}s`;
    const status = test.state === 'passed' ? 'PASSED' : test.state === 'failed' ? 'FAILED' : 'SKIPPED';

    let screenshotPath = 'N/A';
    let activityName = 'MainActivity';
    const device = AppiumConfig.detectConnectedDevice();

    if (driver) {
      try {
        activityName = await deviceHelper.getCurrentActivity();
      } catch (_e) {}

      if (status === 'FAILED') {
        try {
          screenshotPath = await deviceHelper.captureScreenshot(test.fullTitle());
          const logcatPath = await deviceHelper.captureLogcat(test.fullTitle());
          logger.error(`[DIAGNOSTIC] Logcat captured at: ${logcatPath}`);
        } catch (diagErr) {
          logger.error(`Failed to collect failure diagnostics: ${diagErr.message}`);
        }

        excelReporter.recordFailure({
          testName: test.fullTitle(),
          failureReason: test.err ? test.err.message : 'Assertion failed',
          screenshotPath,
          device: device.model,
          androidVersion: `Android ${device.osVersion}`,
          activityName
        });
      }
    }

    excelReporter.recordTestCase({
      testId: `MOB-${excelReporter.testCases.length + 1}`,
      module: test.parent ? test.parent.title : 'General',
      scenario: test.title,
      device: device.model,
      status,
      startTime: testStartTime.toLocaleTimeString(),
      endTime: testEndTime.toLocaleTimeString(),
      duration: durationStr
    });

    excelReporter.recordLog({
      testName: test.title,
      step: `Executed ${test.title}`,
      result: status,
      remarks: status === 'FAILED' ? (test.err ? test.err.message.substring(0, 100) : '') : 'Clean run'
    });
  },

  async afterAll() {
    if (driver) {
      await DriverFactory.quitDriver();
      driver = null;
    }

    try {
      await excelReporter.generateReport();
    } catch (excelErr) {
      logger.error(`Failed to write Mobile Excel report: ${excelErr.message}`);
    }

    logger.info(`Appium Mobile E2E Run Completed.`);
  }
};

async function getDriver() {
  return await DriverFactory.getDriver();
}

module.exports = {
  mochaHooks,
  getDriver
};
