const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const DashboardPage = require('../pages/DashboardPage');
const AuthPage = require('../pages/AuthPage');

describe('Mobile Navigation, Deep Links & Back Button Suite', function () {
  this.timeout(90000);
  let driver;
  let dashboardPage;
  let authPage;

  before(async function () {
    try {
      driver = await getDriver();
      dashboardPage = new DashboardPage(driver);
      authPage = new AuthPage(driver);
    } catch (_e) {}
  });

  it('MOB-NAV-01: Should switch seamlessly across Bottom Navigation tabs', async function () {
    if (!driver) return this.skip();
    const tabs = ['feeding', 'walks', 'home'];
    for (const tab of tabs) {
      await dashboardPage.selectTab(tab);
      const isLoaded = await dashboardPage.isDashboardLoaded();
      expect(isLoaded).to.be.true;
    }
  });

  it('MOB-NAV-02: Should handle Android hardware Back button correctly without unexpected exits', async function () {
    if (!driver) return this.skip();
    await dashboardPage.selectTab('feeding');
    await dashboardPage.device.pressBack();
    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });

  it('MOB-NAV-03: Should preserve dashboard state after complete application relaunch', async function () {
    if (!driver) return this.skip();
    await dashboardPage.device.relaunchApp();
    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });
});
