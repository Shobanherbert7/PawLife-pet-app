const logger = require('./Logger');
const { execSync } = require('child_process');

class PerformanceHelper {
  constructor(driver) {
    this.driver = driver;
    this.metrics = [];
  }

  /**
   * Measures cold launch time of Android application
   * @param {string} packageName
   * @param {string} activityName
   */
  async measureAppLaunchTime(packageName = 'com.pawlife.app', activityName = 'com.pawlife.app.presentation.MainActivity') {
    logger.info(`Measuring launch time for ${packageName}/${activityName}...`);
    try {
      const output = execSync(`adb shell am start -W -n ${packageName}/${activityName}`, { encoding: 'utf-8', timeout: 15000 });
      const totalTimeMatch = output.match(/TotalTime:\s*(\d+)/);
      const waitTimeMatch = output.match(/WaitTime:\s*(\d+)/);

      const totalTimeMs = totalTimeMatch ? parseInt(totalTimeMatch[1], 10) : 0;
      const waitTimeMs = waitTimeMatch ? parseInt(waitTimeMatch[1], 10) : 0;

      const record = {
        type: 'APP_LAUNCH',
        packageName,
        activityName,
        totalTimeMs,
        waitTimeMs,
        status: totalTimeMs < 3000 ? 'OPTIMAL' : 'SLOW'
      };

      this.metrics.push(record);
      logger.info(`⚡ App Launch Time: ${totalTimeMs}ms (Wait: ${waitTimeMs}ms) - Status: [${record.status}]`);
      return record;
    } catch (err) {
      logger.warn(`Failed to measure launch time: ${err.message}`);
      return { totalTimeMs: 0, status: 'UNKNOWN' };
    }
  }

  /**
   * Measures latency when transitioning to a target screen
   * @param {string} screenName
   * @param {Function} transitionAction
   * @param {Function} waitCondition
   */
  async measureScreenLoadTime(screenName, transitionAction, waitCondition) {
    logger.info(`Measuring screen transition time for [${screenName}]...`);
    const startTime = Date.now();

    await transitionAction();
    await waitCondition();

    const durationMs = Date.now() - startTime;
    const record = {
      type: 'SCREEN_LOAD',
      screenName,
      durationMs,
      status: durationMs < 2000 ? 'OPTIMAL' : durationMs < 4000 ? 'ACCEPTABLE' : 'SLOW'
    };

    this.metrics.push(record);
    logger.info(`⏱️ Screen "${screenName}" loaded in ${durationMs}ms - Status: [${record.status}]`);
    return record;
  }

  /**
   * Scans logcat for fatal crash events and ANR traces
   * @returns {string[]} Array of detected crashes
   */
  checkForCrashes() {
    try {
      const output = execSync('adb logcat -d -s AndroidRuntime:E ActivityManager:E', { encoding: 'utf-8', timeout: 5000 });
      const fatalErrors = output.split('\n').filter(l => l.includes('FATAL EXCEPTION') || l.includes('ANR in'));
      if (fatalErrors.length > 0) {
        logger.error(`🚨 DETECTED APPLICATION CRASHES:\n${fatalErrors.join('\n')}`);
        return fatalErrors;
      }
      return [];
    } catch (_e) {
      return [];
    }
  }

  getMetricsSummary() {
    return this.metrics;
  }
}

module.exports = PerformanceHelper;
