const { Builder } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const firefox = require('selenium-webdriver/firefox');
const edge = require('selenium-webdriver/edge');
const env = require('../config/environment');
const logger = require('./Logger');

class DriverFactory {
  /**
   * Builds and returns a WebDriver instance based on target browser and options
   * @param {string} [browserName] - Optional override for browser ('chrome'|'firefox'|'edge')
   * @param {boolean} [headless] - Optional override for headless execution
   * @returns {Promise<ThenableWebDriver>}
   */
  static async createDriver(browserName = env.browser, headless = env.isHeadless) {
    logger.info(`Initializing WebDriver: Browser=[${browserName.toUpperCase()}], Headless=[${headless}]`);

    const builder = new Builder();

    switch (browserName.toLowerCase()) {
      case 'firefox': {
        const options = new firefox.Options();
        if (headless) {
          options.addArguments('-headless');
        }
        options.addArguments(`--width=${env.viewport.width}`);
        options.addArguments(`--height=${env.viewport.height}`);
        builder.forBrowser('firefox').setFirefoxOptions(options);
        break;
      }

      case 'edge': {
        const options = new edge.Options();
        if (headless) {
          options.addArguments('--headless=new');
        }
        options.addArguments(
          '--disable-gpu',
          '--no-sandbox',
          '--disable-dev-shm-usage',
          `--window-size=${env.viewport.width},${env.viewport.height}`
        );
        builder.forBrowser('MicrosoftEdge').setEdgeOptions(options);
        break;
      }

      case 'chrome':
      default: {
        const options = new chrome.Options();
        if (headless) {
          options.addArguments('--headless=new');
        }
        options.addArguments(
          '--disable-gpu',
          '--no-sandbox',
          '--disable-dev-shm-usage',
          '--disable-extensions',
          '--disable-popup-blocking',
          '--ignore-certificate-errors',
          `--window-size=${env.viewport.width},${env.viewport.height}`
        );
        // Enable performance & console logging
        const loggingPrefs = { browser: 'ALL' };
        options.setLoggingPrefs(loggingPrefs);

        builder.forBrowser('chrome').setChromeOptions(options);
        break;
      }
    }

    const driver = await builder.build();

    // Configure timeouts
    await driver.manage().setTimeouts({
      implicit: env.timeouts.implicitWait,
      pageLoad: env.timeouts.pageLoad,
      script: env.timeouts.scriptTimeout
    });

    if (!headless) {
      await driver.manage().window().setRect({
        width: env.viewport.width,
        height: env.viewport.height
      });
    }

    logger.info(`WebDriver initialized successfully for ${browserName}.`);
    return driver;
  }
}

module.exports = DriverFactory;
