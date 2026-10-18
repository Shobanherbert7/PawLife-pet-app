const { execSync } = require('child_process');
const path = require('path');
const fs = require('fs');
const logger = require('./Logger');

class DeviceHelper {
  constructor(driver) {
    this.driver = driver;
  }

  /**
   * Executes adb shell command safely
   * @param {string} command
   */
  executeAdb(command) {
    try {
      return execSync(`adb ${command}`, { encoding: 'utf-8', timeout: 10000 });
    } catch (err) {
      logger.warn(`ADB execution error for command [${command}]: ${err.message}`);
      return '';
    }
  }

  async getCurrentActivity() {
    try {
      const activity = await this.driver.getCurrentActivity();
      return activity;
    } catch (_e) {
      const output = this.executeAdb('shell dumpsys window | grep -E "mCurrentFocus|mFocusedApp"');
      return output.trim() || 'UnknownActivity';
    }
  }

  async getCurrentPackage() {
    try {
      return await this.driver.getCurrentPackage();
    } catch (_e) {
      return 'com.pawlife.app';
    }
  }

  async pressBack() {
    logger.info('Pressing Android Back button');
    await this.driver.pressKeyCode(4); // Android Keycode 4: BACK
    await this.driver.pause(400);
  }

  async pressHome() {
    logger.info('Pressing Android Home button');
    await this.driver.pressKeyCode(3); // Android Keycode 3: HOME
    await this.driver.pause(600);
  }

  async backgroundApp(seconds = 5) {
    logger.info(`Sending app to background for ${seconds} seconds`);
    await this.driver.background(seconds);
  }

  async relaunchApp() {
    logger.info('Relaunching application...');
    const pkg = await this.getCurrentPackage();
    await this.driver.terminateApp(pkg);
    await this.driver.pause(600);
    await this.driver.activateApp(pkg);
    await this.driver.pause(1000);
  }

  async captureScreenshot(testName) {
    const failuresDir = path.resolve(__dirname, '../reports/failures');
    const screenshotsDir = path.resolve(__dirname, '../screenshots');

    if (!fs.existsSync(failuresDir)) fs.mkdirSync(failuresDir, { recursive: true });
    if (!fs.existsSync(screenshotsDir)) fs.mkdirSync(screenshotsDir, { recursive: true });

    const safeName = testName.replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `FAILURE_${safeName}_${Date.now()}.png`;
    const failurePath = path.join(failuresDir, filename);
    const mirrorPath = path.join(screenshotsDir, filename);

    try {
      await this.driver.saveScreenshot(failurePath);
      fs.copyFileSync(failurePath, mirrorPath);
      logger.info(`📸 Failure screenshot saved at: ${failurePath}`);
      return failurePath;
    } catch (err) {
      logger.error(`Failed to capture screenshot: ${err.message}`);
      return 'N/A';
    }
  }

  async captureLogcat(testName, maxLines = 120) {
    const logsDir = path.resolve(__dirname, '../logs');
    if (!fs.existsSync(logsDir)) fs.mkdirSync(logsDir, { recursive: true });

    const safeName = testName.replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `logcat_${safeName}_${Date.now()}.log`;
    const logPath = path.join(logsDir, filename);

    try {
      let logs = [];
      try {
        const rawLogs = await this.driver.getLogs('logcat');
        logs = rawLogs.slice(-maxLines).map(l => `[${l.timestamp}] [${l.level}]: ${l.message}`);
      } catch (_wdioLogErr) {
        const output = this.executeAdb(`logcat -d -t ${maxLines}`);
        logs = output.split('\n');
      }

      fs.writeFileSync(logPath, logs.join('\n'), 'utf-8');
      logger.info(`📋 Device logcat captured at: ${logPath}`);
      return logPath;
    } catch (err) {
      logger.warn(`Failed to capture logcat: ${err.message}`);
      return 'N/A';
    }
  }
}

module.exports = DeviceHelper;
