const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const RouteAndFormScanner = require('../utilities/RouteAndFormScanner');
const DashboardPage = require('../pages/DashboardPage');
const AuthPage = require('../pages/AuthPage');
const logger = require('../utilities/Logger');

describe('Dynamic Route & Form Auto-Discovery Test Suite', function () {
  this.timeout(45000);
  let driver;
  let dashboardPage;
  let authPage;
  let scanResults;

  before(async function () {
    driver = await getDriver();
    dashboardPage = new DashboardPage(driver);
    authPage = new AuthPage(driver);

    // Run dynamic scanner across React components
    scanResults = RouteAndFormScanner.scanReactSource();
  });

  it('TC-DYN-01: Discovered Routes & Tabs Verification', async function () {
    expect(scanResults.routes).to.be.an('array');
    expect(scanResults.routes.length).to.be.greaterThan(0);

    logger.info(`Discovered routes from React components: [${scanResults.routes.join(', ')}]`);

    await authPage.navigateToAuth();
    await authPage.loginWithPersona('Shoban');

    // Test first 4 discovered routes dynamically
    for (const route of scanResults.routes.slice(0, 4)) {
      logger.info(`Dynamically testing route navigation: "${route}"`);
      await dashboardPage.selectTab(route);
      const isLoaded = await dashboardPage.isDashboardLoaded();
      expect(isLoaded, `Route "${route}" should load successfully`).to.be.true;
    }
  });

  it('TC-DYN-02: Discovered Form Validation Rule Matrix Verification', function () {
    expect(scanResults.generatedTestCases).to.be.an('array');
    expect(scanResults.generatedTestCases.length).to.be.greaterThan(0);

    logger.info(`Total dynamic test cases auto-generated: ${scanResults.generatedTestCases.length}`);

    // Verify generated rules adhere to enterprise schema
    scanResults.generatedTestCases.forEach((tc) => {
      expect(tc).to.have.property('id');
      expect(tc).to.have.property('type');
      expect(tc).to.have.property('field');
      expect(tc).to.have.property('description');
      logger.info(`[Dynamic Rule Auto-Generated] ${tc.id}: ${tc.description}`);
    });
  });
});
