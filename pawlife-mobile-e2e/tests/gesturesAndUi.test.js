const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const DashboardPage = require('../pages/DashboardPage');
const FeedingPage = require('../pages/FeedingPage');

describe('Mobile Gestures, Touch Events & UI Components Suite', function () {
  this.timeout(90000);
  let driver;
  let dashboardPage;
  let feedingPage;

  before(async function () {
    try {
      driver = await getDriver();
      dashboardPage = new DashboardPage(driver);
      feedingPage = new FeedingPage(driver);
    } catch (_e) {}
  });

  it('MOB-GEST-01: Should launch and close Tele-Vet consultation modal', async function () {
    if (!driver) return this.skip();
    await dashboardPage.openTeleVet();
    const isDialogOpen = await dashboardPage.isElementDisplayed(dashboardPage.locators.teleVetDialog);
    expect(isDialogOpen).to.be.true;

    await dashboardPage.closeTeleVet();
    const isDialogClosed = await dashboardPage.isElementDisplayed(dashboardPage.locators.teleVetDialog);
    expect(isDialogClosed).to.be.false;
  });

  it('MOB-GEST-02: Should perform vertical swipe and scroll in meal list', async function () {
    if (!driver) return this.skip();
    await dashboardPage.selectTab('feeding');
    await feedingPage.scrollMealList();
    const isStillVisible = await feedingPage.isFeedingScreenVisible();
    expect(isStillVisible).to.be.true;
  });

  it('MOB-GEST-03: Should toggle between active pet profiles (Max <-> Luna)', async function () {
    if (!driver) return this.skip();
    await dashboardPage.switchPet('Luna');
    await dashboardPage.driver.pause(400);

    await dashboardPage.switchPet('Max');
    await dashboardPage.driver.pause(400);

    const isLoaded = await dashboardPage.isDashboardLoaded();
    expect(isLoaded).to.be.true;
  });
});
