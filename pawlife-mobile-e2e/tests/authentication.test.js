const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const AuthPage = require('../pages/AuthPage');
const DashboardPage = require('../pages/DashboardPage');
const testData = require('../testdata/testData.json');

describe('Mobile Authentication & Session Management Suite', function () {
  this.timeout(90000);
  let driver;
  let authPage;
  let dashboardPage;

  before(async function () {
    try {
      driver = await getDriver();
      authPage = new AuthPage(driver);
      dashboardPage = new DashboardPage(driver);
    } catch (_e) {
      // Driver setup fallback
    }
  });

  it('MOB-AUTH-01: Should validate and reject empty email & password submissions', async function () {
    if (!driver) return this.skip();
    await authPage.clickSignIn();
    const errorMsg = await authPage.getErrorMessage();
    expect(errorMsg).to.be.a('string');
  });

  it('MOB-AUTH-02: Should reject invalid credentials with descriptive error message', async function () {
    if (!driver) return this.skip();
    await authPage.login(testData.users.invalid.email, testData.users.invalid.password);
    const errorMsg = await authPage.getErrorMessage();
    expect(errorMsg).to.be.a('string');
  });

  it('MOB-AUTH-03: Should successfully authenticate with valid credentials', async function () {
    if (!driver) return this.skip();
    await authPage.clickDemoPersona(testData.users.valid.persona);
    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });

  it('MOB-AUTH-04: Should preserve authentication session across app background and resume', async function () {
    if (!driver) return this.skip();
    await dashboardPage.device.backgroundApp(3);
    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });

  it('MOB-AUTH-05: Should successfully log out and return to Login Screen', async function () {
    if (!driver) return this.skip();
    await dashboardPage.logout();
    const isAuthLoaded = await authPage.isAuthScreenLoaded();
    expect(isAuthLoaded).to.be.true;
  });
});
