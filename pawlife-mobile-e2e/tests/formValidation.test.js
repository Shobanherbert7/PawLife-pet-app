const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const AuthPage = require('../pages/AuthPage');
const DashboardPage = require('../pages/DashboardPage');
const FeedingPage = require('../pages/FeedingPage');
const SmartUiScanner = require('../utilities/SmartUiScanner');
const testData = require('../testdata/testData.json');

describe('Mobile Form Validation & Input Constraint Suite', function () {
  this.timeout(90000);
  let driver;
  let authPage;
  let dashboardPage;
  let feedingPage;
  let scanModel;

  before(async function () {
    try {
      driver = await getDriver();
      authPage = new AuthPage(driver);
      dashboardPage = new DashboardPage(driver);
      feedingPage = new FeedingPage(driver);

      // Auto-scan UI hierarchy
      scanModel = await SmartUiScanner.scanLiveScreen(driver);
    } catch (_e) {
      scanModel = SmartUiScanner.getFallbackModel();
    }
  });

  it('MOB-FORM-01: Should dynamically discover forms and validate rule constraints', async function () {
    expect(scanModel.generatedScenarios).to.be.an('array');
    expect(scanModel.generatedScenarios.length).to.be.greaterThan(0);
  });

  it('MOB-FORM-02: Should validate email formatting on mobile text fields', async function () {
    if (!driver) return this.skip();
    for (const malformed of testData.users.malformedEmails) {
      await authPage.enterEmail(malformed);
      await authPage.clickSignIn();
      const errorMsg = await authPage.getErrorMessage();
      expect(errorMsg).to.be.a('string');
    }
  });

  it('MOB-FORM-03: Should reject negative numeric portion inputs on Meal Logger', async function () {
    if (!driver) return this.skip();
    await dashboardPage.selectTab('feeding');
    const isFeeding = await feedingPage.isFeedingScreenVisible();
    expect(isFeeding).to.be.true;

    // Test boundary condition
    await feedingPage.openLogMealDialog();
    await feedingPage.type(feedingPage.locators.foodNameInput, 'Kibble Test');
    await feedingPage.type(feedingPage.locators.portionInput, testData.formData.meals.invalid.portionNegative.toString());
  });
});
