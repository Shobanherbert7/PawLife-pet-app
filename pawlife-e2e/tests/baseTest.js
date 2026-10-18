const DriverFactory = require('../utilities/DriverFactory');
const excelReporter = require('../utilities/ExcelReportGenerator');
const logger = require('../utilities/Logger');
const env = require('../config/environment');

let driver = null;
let testStartTime = null;

const mochaHooks = {
  async beforeAll() {
    logger.info(`=======================================================`);
    logger.info(`Starting E2E Test Run: Env=[${env.currentEnv.toUpperCase()}], Browser=[${env.browser.toUpperCase()}]`);
    logger.info(`=======================================================`);
    if (!driver) {
      driver = await DriverFactory.createDriver();
    }
  },

  async beforeEach() {
    testStartTime = new Date();
    if (!driver) {
      driver = await DriverFactory.createDriver();
    }
  },

  async afterEach() {
    const test = this.currentTest;
    const testEndTime = new Date();
    const durationMs = test.duration || (testEndTime - testStartTime);
    const durationStr = `${(durationMs / 1000).toFixed(2)}s`;
    const status = test.state === 'passed' ? 'PASSED' : test.state === 'failed' ? 'FAILED' : 'SKIPPED';

    let screenshotPath = 'N/A';
    let currentUrl = 'N/A';

    if (driver) {
      try {
        currentUrl = await driver.getCurrentUrl();
      } catch (_e) {}

      if (status === 'FAILED') {
        try {
          const helper = new (require('../utilities/WebDriverHelper'))(driver);
          screenshotPath = await helper.captureFailureScreenshot(test.fullTitle());
          const consoleLogs = await helper.getBrowserConsoleLogs();
          logger.error(`[DIAGNOSTIC] Console logs for failed test "${test.title}":\n${consoleLogs.join('\n')}`);
        } catch (diagErr) {
          logger.error(`Failed to gather failure diagnostics: ${diagErr.message}`);
        }

        excelReporter.recordFailure({
          testName: test.fullTitle(),
          failureReason: test.err ? test.err.message : 'Unknown assertion error',
          screenshotPath,
          browser: env.browser,
          url: currentUrl
        });
      }
    }

    excelReporter.recordTestCase({
      testId: `TC-${excelReporter.testCases.length + 1}`,
      module: test.parent ? test.parent.title : 'General',
      scenarioName: test.title,
      browser: env.browser,
      status,
      startTime: testStartTime.toLocaleTimeString(),
      endTime: testEndTime.toLocaleTimeString(),
      duration: durationStr
    });

    excelReporter.recordLog({
      testName: test.title,
      stepDescription: `Execution finished with status: ${status}`,
      result: status,
      remarks: status === 'FAILED' ? (test.err ? test.err.message.substring(0, 100) : '') : 'Clean run'
    });
  },

  async afterAll() {
    if (driver) {
      try {
        logger.info('Closing WebDriver session...');
        await driver.quit();
      } catch (err) {
        logger.warn(`Error while closing driver: ${err.message}`);
      }
      driver = null;
    }

    try {
      await excelReporter.generateReport();
    } catch (excelErr) {
      logger.error(`Failed to write Excel report: ${excelErr.message}`);
    }

    logger.info(`E2E Test Run Completed.`);
  }
};

async function getDriver() {
  if (!driver) {
    driver = await DriverFactory.createDriver();
  }
  return driver;
}

module.exports = {
  mochaHooks,
  getDriver
};
