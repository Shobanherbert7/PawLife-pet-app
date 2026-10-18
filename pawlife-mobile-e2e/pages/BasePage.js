const GestureHelper = require('../utilities/GestureHelper');
const DeviceHelper = require('../utilities/DeviceHelper');
const logger = require('../utilities/Logger');
const AppiumConfig = require('../config/appiumConfig');

class BasePage {
  constructor(driver) {
    this.driver = driver;
    this.gestures = new GestureHelper(driver);
    this.device = new DeviceHelper(driver);
    this.timeouts = AppiumConfig.getTimeouts();
  }

  async waitForElement(locator, timeout = this.timeouts.explicit) {
    try {
      const element = await this.driver.$(locator);
      await element.waitForDisplayed({ timeout, timeoutMsg: `Element ${locator} was not displayed within ${timeout}ms` });
      return element;
    } catch (err) {
      logger.error(`[Wait Error] ${err.message}`);
      throw err;
    }
  }

  async waitForClickable(locator, timeout = this.timeouts.explicit) {
    const element = await this.waitForElement(locator, timeout);
    await element.waitForClickable({ timeout, timeoutMsg: `Element ${locator} was not clickable within ${timeout}ms` });
    return element;
  }

  async click(locator) {
    logger.info(`Clicking mobile element: ${locator}`);
    const element = await this.waitForClickable(locator);
    await element.click();
    await this.driver.pause(300);
  }

  async type(locator, text, clearFirst = true) {
    logger.info(`Typing [${text}] into: ${locator}`);
    const element = await this.waitForElement(locator);
    if (clearFirst) {
      await element.clearValue();
    }
    await element.setValue(text);
    await this.hideKeyboard();
  }

  async getText(locator) {
    const element = await this.waitForElement(locator);
    const text = await element.getText();
    return text.trim();
  }

  async isElementDisplayed(locator) {
    try {
      const element = await this.driver.$(locator);
      return await element.isDisplayed();
    } catch (_e) {
      return false;
    }
  }

  async hideKeyboard() {
    try {
      if (await this.driver.isKeyboardShown()) {
        await this.driver.hideKeyboard();
        await this.driver.pause(200);
      }
    } catch (_e) {}
  }

  async acceptAlert() {
    try {
      if (await this.driver.isAlertOpen()) {
        await this.driver.acceptAlert();
        logger.info('Accepted native alert.');
      }
    } catch (_e) {}
  }

  async dismissAlert() {
    try {
      if (await this.driver.isAlertOpen()) {
        await this.driver.dismissAlert();
        logger.info('Dismissed native alert.');
      }
    } catch (_e) {}
  }
}

module.exports = BasePage;
