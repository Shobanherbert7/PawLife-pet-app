const { remote } = require('webdriverio');
const AppiumConfig = require('../config/appiumConfig');
const logger = require('./Logger');

class DriverFactory {
  constructor() {
    this.driver = null;
  }

  /**
   * Initializes Appium 2.x session with UiAutomator2 driver
   * @param {object} [customCapabilities]
   * @returns {Promise<WebdriverIO.Browser>}
   */
  async createDriver(customCapabilities = {}) {
    if (this.driver) {
      return this.driver;
    }

    const serverConfig = AppiumConfig.getServerConfig();
    const capabilities = {
      ...AppiumConfig.getCapabilities(),
      ...customCapabilities
    };

    logger.info(`Connecting to Appium Server at ${serverConfig.hostname}:${serverConfig.port}`);
    logger.info(`Target Device: [${capabilities['appium:deviceName']}], OS: [${capabilities['appium:platformVersion']}]`);

    const options = {
      hostname: serverConfig.hostname,
      port: serverConfig.port,
      path: serverConfig.path,
      capabilities: capabilities,
      logLevel: process.env.WDIO_LOG_LEVEL || 'warn'
    };

    try {
      this.driver = await remote(options);
      
      const timeouts = AppiumConfig.getTimeouts();
      await this.driver.setTimeouts(timeouts.implicit, timeouts.pageLoad, timeouts.commandTimeout);
      
      logger.info('✅ Appium UiAutomator2 Session initialized successfully.');
      return this.driver;
    } catch (err) {
      logger.error(`Failed to initialize Appium session: ${err.message}`);
      throw err;
    }
  }

  async getDriver() {
    if (!this.driver) {
      return await this.createDriver();
    }
    return this.driver;
  }

  async quitDriver() {
    if (this.driver) {
      try {
        logger.info('Terminating mobile Appium session...');
        await this.driver.deleteSession();
      } catch (err) {
        logger.warn(`Error while closing Appium session: ${err.message}`);
      }
      this.driver = null;
    }
  }
}

module.exports = new DriverFactory();
