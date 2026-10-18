const { By } = require('selenium-webdriver');
const WebDriverHelper = require('../utilities/WebDriverHelper');
const logger = require('../utilities/Logger');
const env = require('../config/environment');

class BasePage {
  constructor(driver) {
    this.driver = driver;
    this.helper = new WebDriverHelper(driver);
    this.baseUrl = env.config.baseUrl;
  }

  async open(path = '') {
    const fullUrl = path.startsWith('http') ? path : `${this.baseUrl}${path}`;
    logger.info(`Navigating to URL: ${fullUrl}`);
    await this.driver.get(fullUrl);
    await this.driver.sleep(800); // Allow React reconciliation
  }

  async getCurrentUrl() {
    return await this.driver.getCurrentUrl();
  }

  async getTitle() {
    return await this.driver.getTitle();
  }

  async refreshPage() {
    logger.info('Refreshing page...');
    await this.driver.navigate().refresh();
    await this.driver.sleep(800);
  }

  async navigateBack() {
    logger.info('Navigating browser back...');
    await this.driver.navigate().back();
    await this.driver.sleep(500);
  }

  async navigateForward() {
    logger.info('Navigating browser forward...');
    await this.driver.navigate().forward();
    await this.driver.sleep(500);
  }

  async isToastOrAlertDisplayed() {
    try {
      const toastLocator = By.css('.toast, [role="alert"], .alert');
      return await this.helper.isElementDisplayed(toastLocator);
    } catch (_e) {
      return false;
    }
  }

  async getLocalStorageItem(key) {
    return await this.driver.executeScript(`return window.localStorage.getItem(arguments[0]);`, key);
  }

  async setLocalStorageItem(key, value) {
    await this.driver.executeScript(`window.localStorage.setItem(arguments[0], arguments[1]);`, key, value);
  }

  async clearLocalStorage() {
    await this.driver.executeScript(`window.localStorage.clear();`);
  }
}

module.exports = BasePage;
