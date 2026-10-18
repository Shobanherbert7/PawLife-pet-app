const { until, By, Key } = require('selenium-webdriver');
const fs = require('fs');
const path = require('path');
const logger = require('./Logger');
const env = require('../config/environment');

class WebDriverHelper {
  constructor(driver) {
    this.driver = driver;
    this.defaultTimeout = env.timeouts.explicitWait;
  }

  // ==========================================
  // Explicit Waits
  // ==========================================

  async waitForElementPresent(locator, timeout = this.defaultTimeout) {
    try {
      return await this.driver.wait(until.elementLocated(locator), timeout, `Element ${locator} was not located within ${timeout}ms`);
    } catch (err) {
      logger.error(`[Wait Error] Element not located: ${locator} - ${err.message}`);
      throw err;
    }
  }

  async waitForElementVisible(locator, timeout = this.defaultTimeout) {
    try {
      const element = await this.waitForElementPresent(locator, timeout);
      await this.driver.wait(until.elementIsVisible(element), timeout, `Element ${locator} was not visible within ${timeout}ms`);
      return element;
    } catch (err) {
      logger.error(`[Wait Error] Element not visible: ${locator} - ${err.message}`);
      throw err;
    }
  }

  async waitForElementClickable(locator, timeout = this.defaultTimeout) {
    try {
      const element = await this.waitForElementVisible(locator, timeout);
      await this.driver.wait(until.elementIsEnabled(element), timeout, `Element ${locator} was not clickable within ${timeout}ms`);
      return element;
    } catch (err) {
      logger.error(`[Wait Error] Element not clickable: ${locator} - ${err.message}`);
      throw err;
    }
  }

  async waitForTextPresent(locator, expectedText, timeout = this.defaultTimeout) {
    try {
      const element = await this.waitForElementVisible(locator, timeout);
      await this.driver.wait(until.elementTextContains(element, expectedText), timeout, `Text "${expectedText}" not found in ${locator}`);
      return true;
    } catch (err) {
      logger.error(`[Wait Error] Text "${expectedText}" not present in ${locator}`);
      throw err;
    }
  }

  async waitForUrlContains(expectedSubstr, timeout = this.defaultTimeout) {
    try {
      await this.driver.wait(until.urlContains(expectedSubstr), timeout, `URL did not contain "${expectedSubstr}" within ${timeout}ms`);
      return true;
    } catch (err) {
      logger.error(`[Wait Error] URL did not contain "${expectedSubstr}"`);
      throw err;
    }
  }

  async waitForInvisibility(locator, timeout = this.defaultTimeout) {
    try {
      const elements = await this.driver.findElements(locator);
      if (elements.length > 0) {
        await this.driver.wait(until.elementIsNotVisible(elements[0]), timeout, `Element ${locator} is still visible`);
      }
      return true;
    } catch (_e) {
      return true;
    }
  }

  // ==========================================
  // Actions & Clicks
  // ==========================================

  async click(locator, timeout = this.defaultTimeout) {
    try {
      const element = await this.waitForElementClickable(locator, timeout);
      await this.scrollToElement(element);
      await element.click();
      logger.info(`Clicked element: ${locator}`);
    } catch (err) {
      logger.warn(`Standard click failed on ${locator}. Falling back to JavaScript click. (${err.message})`);
      await this.jsClick(locator);
    }
  }

  async jsClick(locator) {
    try {
      const element = await this.waitForElementPresent(locator);
      await this.driver.executeScript('arguments[0].click();', element);
      logger.info(`JavaScript clicked element: ${locator}`);
    } catch (err) {
      logger.error(`JS Click failed on ${locator}: ${err.message}`);
      throw err;
    }
  }

  async type(locator, text, clearFirst = true) {
    try {
      const element = await this.waitForElementVisible(locator);
      if (clearFirst) {
        await element.sendKeys(Key.chord(Key.CONTROL, 'a'), Key.BACK_SPACE);
        await element.clear();
      }
      await element.sendKeys(text);
      logger.info(`Typed into ${locator}: [${text}]`);
    } catch (err) {
      logger.error(`Failed to type into ${locator}: ${err.message}`);
      throw err;
    }
  }

  async getText(locator) {
    const element = await this.waitForElementVisible(locator);
    const text = await element.getText();
    return text.trim();
  }

  async getValue(locator) {
    const element = await this.waitForElementPresent(locator);
    return await element.getAttribute('value');
  }

  async isElementDisplayed(locator) {
    try {
      const elements = await this.driver.findElements(locator);
      if (elements.length === 0) return false;
      return await elements[0].isDisplayed();
    } catch (_e) {
      return false;
    }
  }

  // ==========================================
  // JavaScript Execution & Scrolling
  // ==========================================

  async executeScript(script, ...args) {
    return await this.driver.executeScript(script, ...args);
  }

  async scrollToElement(elementOrLocator) {
    let el = elementOrLocator;
    if (typeof elementOrLocator.using === 'string' || typeof elementOrLocator === 'object' && elementOrLocator.locator) {
      el = await this.waitForElementPresent(elementOrLocator);
    }
    await this.driver.executeScript('arguments[0].scrollIntoView({ behavior: "smooth", block: "center" });', el);
    await this.driver.sleep(200);
  }

  async scrollToBottom() {
    await this.driver.executeScript('window.scrollTo({ top: document.body.scrollHeight, behavior: "smooth" });');
    await this.driver.sleep(250);
  }

  async scrollToTop() {
    await this.driver.executeScript('window.scrollTo({ top: 0, behavior: "smooth" });');
    await this.driver.sleep(250);
  }

  // ==========================================
  // Windows & Tabs
  // ==========================================

  async switchToNewWindow() {
    const handles = await this.driver.getAllWindowHandles();
    if (handles.length > 1) {
      await this.driver.switchTo().window(handles[handles.length - 1]);
      logger.info(`Switched to new window/tab: ${handles[handles.length - 1]}`);
    }
  }

  async closeCurrentWindowAndSwitchBack() {
    const handles = await this.driver.getAllWindowHandles();
    if (handles.length > 1) {
      await this.driver.close();
      await this.driver.switchTo().window(handles[0]);
      logger.info('Closed active window and returned to primary handle.');
    }
  }

  // ==========================================
  // Alert Handling
  // ==========================================

  async acceptAlert(timeout = 5000) {
    await this.driver.wait(until.alertIsPresent(), timeout);
    const alert = await this.driver.switchTo().alert();
    const alertText = await alert.getText();
    await alert.accept();
    logger.info(`Accepted alert with text: "${alertText}"`);
    return alertText;
  }

  async dismissAlert(timeout = 5000) {
    await this.driver.wait(until.alertIsPresent(), timeout);
    const alert = await this.driver.switchTo().alert();
    const alertText = await alert.getText();
    await alert.dismiss();
    logger.info(`Dismissed alert with text: "${alertText}"`);
    return alertText;
  }

  // ==========================================
  // Screenshots & Failure Diagnostic Logging
  // ==========================================

  async captureFailureScreenshot(testName) {
    const failuresDir = path.resolve(__dirname, '../reports/failures');
    if (!fs.existsSync(failuresDir)) {
      fs.mkdirSync(failuresDir, { recursive: true });
    }

    const sanitizedName = testName.replace(/[^a-zA-Z0-9_-]/g, '_');
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const fileName = `FAILURE_${sanitizedName}_${timestamp}.png`;
    const fullPath = path.join(failuresDir, fileName);

    try {
      const screenshot = await this.driver.takeScreenshot();
      fs.writeFileSync(fullPath, screenshot, 'base64');
      logger.error(`[DIAGNOSTIC] Screenshot saved on failure: ${fullPath}`);
      return fullPath;
    } catch (err) {
      logger.error(`Failed to capture screenshot: ${err.message}`);
      return null;
    }
  }

  async getBrowserConsoleLogs() {
    try {
      const logs = await this.driver.manage().logs().get('browser');
      return logs.map(l => `[${l.level.name}] ${l.message}`);
    } catch (_e) {
      return ['Browser console logs not supported or available on current browser.'];
    }
  }

  // ==========================================
  // Robust Retry Action Pattern
  // ==========================================

  async retryAction(actionFn, maxRetries = 2, delayMs = 1000) {
    let lastError = null;
    for (let attempt = 1; attempt <= maxRetries + 1; attempt++) {
      try {
        return await actionFn();
      } catch (err) {
        lastError = err;
        logger.warn(`Action failed on attempt ${attempt}/${maxRetries + 1}: ${err.message}`);
        if (attempt <= maxRetries) {
          await this.driver.sleep(delayMs);
        }
      }
    }
    throw lastError;
  }
}

module.exports = WebDriverHelper;
