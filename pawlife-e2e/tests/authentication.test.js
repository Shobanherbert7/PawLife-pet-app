const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const AuthPage = require('../pages/AuthPage');
const DashboardPage = require('../pages/DashboardPage');

describe('Authentication & Session Management Suite', function () {
  this.timeout(45000);
  let driver;
  let authPage;
  let dashboardPage;

  before(async function () {
    driver = await getDriver();
    authPage = new AuthPage(driver);
    dashboardPage = new DashboardPage(driver);
  });

  beforeEach(async function () {
    await authPage.navigateToAuth();
  });

  it('TC-AUTH-01: Should prevent submission with empty email and password', async function () {
    await authPage.enterEmail('');
    await authPage.enterPassword('');
    await authPage.clickSubmit();

    const isStillOnAuth = await authPage.isAuthScreenDisplayed();
    expect(isStillOnAuth).to.be.true;
  });

  it('TC-AUTH-02: Should reject malformed email format', async function () {
    await authPage.enterEmail('not-a-valid-email');
    await authPage.enterPassword('Password123!');
    await authPage.clickSubmit();

    const isStillOnAuth = await authPage.isAuthScreenDisplayed();
    expect(isStillOnAuth).to.be.true;
  });

  it('TC-AUTH-03: Should reject short passwords during account creation', async function () {
    await authPage.switchToCreateAccount();
    await authPage.enterFullName('QA Tester');
    await authPage.enterEmail('qa.tester@pawlife.app');
    await authPage.enterPassword('123');
    await authPage.clickSubmit();

    const isStillOnAuth = await authPage.isAuthScreenDisplayed();
    expect(isStillOnAuth).to.be.true;
  });

  it('TC-AUTH-04: Should successfully login via Demo Persona and redirect to Dashboard', async function () {
    await authPage.loginWithPersona('Shoban');
    const isDashboard = await dashboardPage.isDashboardLoaded();
    expect(isDashboard).to.be.true;
  });

  it('TC-AUTH-05: Should maintain user session after browser page refresh', async function () {
    await authPage.loginWithPersona('Shoban');
    await dashboardPage.refreshPage();

    const isDashboard = await dashboardPage.isDashboardLoaded();
    expect(isDashboard).to.be.true;

    const savedAuth = await dashboardPage.getLocalStorageItem('pawlife_auth_user');
    expect(savedAuth).to.not.be.null;
    expect(savedAuth).to.include('Shoban');
  });

  it('TC-AUTH-06: Should successfully log out and return to Auth screen', async function () {
    await authPage.loginWithPersona('Shoban');
    await dashboardPage.logout();

    const isAuth = await authPage.isAuthScreenDisplayed();
    expect(isAuth).to.be.true;

    const savedAuth = await dashboardPage.getLocalStorageItem('pawlife_auth_user');
    expect(savedAuth).to.be.null;
  });
});
