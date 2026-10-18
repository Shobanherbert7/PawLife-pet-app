const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const AuthPage = require('../pages/AuthPage');
const DashboardPage = require('../pages/DashboardPage');

describe('UI Elements, Modals & Components Suite', function () {
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

  it('TC-UI-01: Should launch and close Tele-Vet Live consultation modal', async function () {
    await dashboardPage.openTeleVetModal();
    const isModalOpen = await dashboardPage.helper.isElementDisplayed(dashboardPage.locators.teleVetModal);
    expect(isModalOpen).to.be.true;

    await dashboardPage.closeTeleVetModal();
    await dashboardPage.driver.sleep(500);
    const isModalClosed = await dashboardPage.helper.isElementDisplayed(dashboardPage.locators.teleVetModal);
    expect(isModalClosed).to.be.false;
  });

  it('TC-UI-02: Should toggle User Profile dropdown menu upon clicking avatar badge', async function () {
    await dashboardPage.helper.click(dashboardPage.locators.userProfileBadge);
    await dashboardPage.driver.sleep(400);
    const isDropdownVisible = await dashboardPage.helper.isElementDisplayed(dashboardPage.locators.profileDropdown);
    expect(isDropdownVisible).to.be.true;

    // Click again to close
    await dashboardPage.helper.click(dashboardPage.locators.userProfileBadge);
    await dashboardPage.driver.sleep(400);
  });

  it('TC-UI-03: Should toggle between active pet profiles (Max <-> Luna)', async function () {
    await dashboardPage.helper.click(dashboardPage.locators.petTabLuna);
    await dashboardPage.driver.sleep(400);

    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;

    // Switch back to Max
    await dashboardPage.helper.click(dashboardPage.locators.petTabMax);
    await dashboardPage.driver.sleep(400);
  });
});
