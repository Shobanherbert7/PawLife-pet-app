const path = require('path');
const { execSync } = require('child_process');
require('dotenv').config();

class AppiumConfig {
  /**
   * Dynamically inspects ADB to discover connected real devices or running emulators
   * @returns {{ deviceId: string, model: string, osVersion: string, isEmulator: boolean }}
   */
  static detectConnectedDevice() {
    try {
      const output = execSync('adb devices -l', { encoding: 'utf-8', timeout: 5000 });
      const lines = output.split('\n').filter(l => l.trim() && !l.includes('List of devices attached'));

      if (lines.length > 0) {
        const firstLine = lines[0].trim();
        const parts = firstLine.split(/\s+/);
        const deviceId = parts[0];

        let model = 'Generic Android';
        const modelMatch = firstLine.match(/model:(\S+)/);
        if (modelMatch) model = modelMatch[1].replace(/_/g, ' ');

        let osVersion = '14.0';
        try {
          osVersion = execSync(`adb -s ${deviceId} shell getprop ro.build.version.release`, {
            encoding: 'utf-8',
            timeout: 3000
          }).trim();
        } catch (_e) {}

        const isEmulator = deviceId.startsWith('emulator-') || model.toLowerCase().includes('sdk');

        return { deviceId, model, osVersion, isEmulator };
      }
    } catch (_err) {
      // ADB not found or device list empty
    }

    return {
      deviceId: process.env.ANDROID_DEVICE_ID || 'emulator-5554',
      model: process.env.ANDROID_DEVICE_NAME || 'Pixel 8 Pro (Emulator)',
      osVersion: process.env.ANDROID_PLATFORM_VERSION || '14.0',
      isEmulator: true
    };
  }

  static getCapabilities() {
    const device = this.detectConnectedDevice();
    const launchMode = (process.env.LAUNCH_MODE || 'installed').toLowerCase();
    const appPackage = process.env.APP_PACKAGE || 'com.pawlife.app';
    const appActivity = process.env.APP_ACTIVITY || 'com.pawlife.app.presentation.MainActivity';
    const apkPath = process.env.APK_PATH || path.resolve(__dirname, '../../PawLife/app/build/outputs/apk/debug/app-debug.apk');

    const baseCapabilities = {
      platformName: 'Android',
      'appium:automationName': 'UiAutomator2',
      'appium:deviceName': device.model,
      'appium:udid': device.deviceId,
      'appium:platformVersion': device.osVersion,
      'appium:autoGrantPermissions': true,
      'appium:newCommandTimeout': 240,
      'appium:adbExecTimeout': 60000,
      'appium:uiautomator2ServerInstallTimeout': 60000,
      'appium:uiautomator2ServerLaunchTimeout': 60000,
      'appium:noReset': process.env.NO_RESET === 'true',
      'appium:fullReset': false,
      'appium:clearDeviceLogsOnStart': true,
      'appium:ensureWebviewsHavePages': true,
      'appium:nativeWebScreenshot': true
    };

    if (launchMode === 'apk') {
      return {
        ...baseCapabilities,
        'appium:app': apkPath,
        'appium:appWaitActivity': `${appPackage}.*`
      };
    }

    return {
      ...baseCapabilities,
      'appium:appPackage': appPackage,
      'appium:appActivity': appActivity,
      'appium:appWaitActivity': `${appPackage}.*`
    };
  }

  static getServerConfig() {
    return {
      hostname: process.env.APPIUM_HOST || '127.0.0.1',
      port: parseInt(process.env.APPIUM_PORT || '4723', 10),
      path: '/'
    };
  }

  static getTimeouts() {
    return {
      implicit: 6000,
      explicit: 20000,
      pageLoad: 35000,
      commandTimeout: 180000
    };
  }
}

module.exports = AppiumConfig;
