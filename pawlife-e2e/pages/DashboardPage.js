const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');
const logger = require('../utilities/Logger');

class DashboardPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.locators = {
      userProfileBadge: By.xpath("//header//button[contains(., 'Shoban') or contains(., 'Pet Parent') or contains(., 'Priya')]"),
      profileDropdown: By.xpath("//div[contains(., 'Switch Demo Persona') and contains(., 'Sign Out')]"),
      signOutButton: By.xpath("//button[contains(., 'Sign Out')]"),
      serverStatusPill: By.xpath("//div[contains(., 'Server & SQLite Active') or contains(., 'Server Offline') or contains(., 'Connecting')]"),
      teleVetButton: By.xpath("//header//button[contains(., 'Tele-Vet Live')]"),
      teleVetModal: By.xpath("//div[contains(., 'PawLife Tele-Vet Video Session')]"),
      closeModalButton: By.xpath("//button[contains(., 'End Call')]"),
      // Nav Tabs
      tabOverview: By.xpath("//aside//button[contains(., 'Overview')]"),
      tabMeals: By.xpath("//aside//button[contains(., 'Feeding Tracker')]"),
      tabWalks: By.xpath("//aside//button[contains(., 'Collar & Walk GPS')]"),
      tabDiagnostics: By.xpath("//aside//button[contains(., 'AI Health Triage')]"),
      tabFamilyHub: By.xpath("//aside//button[contains(., 'Family Hub')]"),
      tabCommunity: By.xpath("//aside//button[contains(., 'BarkConnect Social')]"),
      tabAgility: By.xpath("//aside//button[contains(., 'AR Agility Coach')]"),
      tabAcoustics: By.xpath("//aside//button[contains(., 'PawAcoustics AI')]"),
      tabMarketplace: By.xpath("//aside//button[contains(., 'Store & Pantry')]"),
      tabLostPet: By.xpath("//aside//button[contains(., 'Lost Pet Mode')]"),
      // Pet Switcher buttons
      petTabMax: By.xpath("//button[contains(., 'Max')]"),
      petTabLuna: By.xpath("//button[contains(., 'Luna')]")
    };
  }

  async isDashboardLoaded() {
    try {
      const isPill = await this.helper.isElementDisplayed(this.locators.serverStatusPill);
      if (isPill) return true;
      return await this.helper.isElementDisplayed(By.xpath("//h2[contains(., 'PawLife')]"));
    } catch (_e) {
      return false;
    }
  }

  async getServerStatusText() {
    return await this.helper.getText(this.locators.serverStatusPill);
  }

  async selectTab(tabName) {
    logger.info(`Switching dashboard tab to: ${tabName}`);
    const key = tabName.toLowerCase().replace(/[^a-z]/g, '');
    const tabLocators = {
      overview: this.locators.tabOverview,
      feeding: this.locators.tabMeals,
      meals: this.locators.tabMeals,
      walks: this.locators.tabWalks,
      collar: this.locators.tabWalks,
      community: this.locators.tabCommunity,
      barkconnect: this.locators.tabCommunity,
      diagnostics: this.locators.tabDiagnostics,
      aitriage: this.locators.tabDiagnostics,
      family: this.locators.tabFamilyHub,
      marketplace: this.locators.tabMarketplace,
      acoustics: this.locators.tabAcoustics,
      lostpet: this.locators.tabLostPet,
      vet: By.xpath("//aside//button[contains(., 'Tele-Vet & Booking')]")
    };

    const targetLocator = tabLocators[key] || By.xpath(`//aside//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '${key}')]`);
    await this.helper.click(targetLocator);
    await this.driver.sleep(500);
  }

  async openTeleVetModal() {
    await this.helper.click(this.locators.teleVetButton);
    await this.driver.sleep(600);
  }

  async closeTeleVetModal() {
    await this.helper.click(this.locators.closeModalButton);
    await this.driver.sleep(400);
  }

  async logout() {
    logger.info('Logging out from dashboard...');
    await this.helper.click(this.locators.userProfileBadge);
    await this.driver.sleep(300);
    await this.helper.click(this.locators.signOutButton);
    await this.driver.sleep(800);
  }
}

module.exports = DashboardPage;
