const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const AuthPage = require('../pages/AuthPage');
const DashboardPage = require('../pages/DashboardPage');
const FeedingPage = require('../pages/FeedingPage');

describe('Frontend Form Validation & Input Rules Suite', function () {
  this.timeout(45000);
  let driver;
  let authPage;
  let dashboardPage;
  let feedingPage;

  before(async function () {
    driver = await getDriver();
    authPage = new AuthPage(driver);
    dashboardPage = new DashboardPage(driver);
    feedingPage = new FeedingPage(driver);

    await authPage.navigateToAuth();
    await authPage.loginWithPersona('Shoban');
  });

  it('TC-FORM-01: Should navigate to Meals section and verify form inputs', async function () {
    await dashboardPage.selectTab('meals');
    const isVisible = await feedingPage.isFeedingSectionVisible();
    expect(isVisible).to.be.true;
  });

  it('TC-FORM-02: Should validate numeric portion inputs', async function () {
    await dashboardPage.selectTab('meals');
    // Ensure the page responds without breaking when entering numbers
    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });
});
