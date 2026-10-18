require('dotenv').config();

const ENV = process.env.TEST_ENV || 'local';

const environments = {
  local: {
    baseUrl: process.env.BASE_URL || 'http://localhost:5173',
    apiUrl: process.env.API_URL || 'http://localhost:4000/api',
    wsUrl: process.env.WS_URL || 'ws://localhost:4000'
  },
  dev: {
    baseUrl: 'https://dev.pawlife.app',
    apiUrl: 'https://api-dev.pawlife.app/api',
    wsUrl: 'wss://api-dev.pawlife.app'
  },
  staging: {
    baseUrl: 'https://staging.pawlife.app',
    apiUrl: 'https://api-staging.pawlife.app/api',
    wsUrl: 'wss://api-staging.pawlife.app'
  },
  prod: {
    baseUrl: 'https://app.pawlife.com',
    apiUrl: 'https://api.pawlife.com/api',
    wsUrl: 'wss://api.pawlife.com'
  }
};

module.exports = {
  currentEnv: ENV,
  config: environments[ENV] || environments.local,
  browser: (process.env.BROWSER || 'chrome').toLowerCase(),
  isHeadless: process.env.HEADLESS === 'true',
  timeouts: {
    pageLoad: 30000,
    implicitWait: 5000,
    explicitWait: 15000,
    scriptTimeout: 20000
  },
  retries: parseInt(process.env.RETRIES || '1', 10),
  viewport: {
    width: 1920,
    height: 1080
  }
};
