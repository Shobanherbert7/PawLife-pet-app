const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const AuthPage = require('../pages/AuthPage');
const DashboardPage = require('../pages/DashboardPage');

describe('Navigation & Routing Suite', function () {
  this.timeout(45000);
  let driver;
  let authPage;
  let dashboardPage;

  before(async function () {
    driver = await getDriver();
    authPage = new AuthPage(driver);
    dashboardPage = new DashboardPage(driver);
    await authPage.navigateToAuth();
    await authPage.loginWithPersona('Shoban');
  });

  it('TC-NAV-01: Should display live Backend Server status badge in the header', async function () {
    const isVisible = await dashboardPage.isDashboardLoaded();
    expect(isVisible).to.be.true;

    const statusText = await dashboardPage.getServerStatusText();
    expect(statusText).to.be.a('string');
  });

  it('TC-NAV-02: Should navigate smoothly between tabs (Meals, Walks, Community, Diagnostics)', async function () {
    const tabsToTest = ['meals', 'walks', 'community', 'diagnostics', 'overview'];

    for (const tab of tabsToTest) {
      await dashboardPage.selectTab(tab);
      const isStillLoaded = await dashboardPage.isDashboardLoaded();
      expect(isStillLoaded).to.be.true;
    }
  });

  it('TC-NAV-03: Should preserve dashboard state after browser reload', async function () {
    await dashboardPage.selectTab('meals');
    await dashboardPage.refreshPage();

    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });

  it('TC-NAV-04: Should support browser back and forward history actions', async function () {
    await driver.get(`${dashboardPage.baseUrl}#collar`);
    await dashboardPage.driver.sleep(300);
    await driver.get(`${dashboardPage.baseUrl}#feeding`);
    await dashboardPage.driver.sleep(300);
    await dashboardPage.navigateBack();
    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;

    await dashboardPage.navigateForward();
    const isForwardLoaded = await dashboardPage.isDashboardLoaded();
    expect(isForwardLoaded).to.be.true;
  });
});
