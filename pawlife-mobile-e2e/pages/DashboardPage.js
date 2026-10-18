const BasePage = require('./BasePage');
const logger = require('../utilities/Logger');

class DashboardPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.locators = {
      // Bottom Navigation items
      tabHome: '//android.widget.TextView[contains(@text, "Home") or contains(@content-desc, "Home")]',
      tabFeeding: '//android.widget.TextView[contains(@text, "Feeding") or contains(@content-desc, "Feeding")]',
      tabWalks: '//android.widget.TextView[contains(@text, "Walk") or contains(@content-desc, "Walk")]',
      tabTriage: '//android.widget.TextView[contains(@text, "Triage") or contains(@content-desc, "Triage")]',
      tabFamily: '//android.widget.TextView[contains(@text, "Family") or contains(@content-desc, "Family")]',

      // Header and Pet Switcher
      activePetBadge: '//android.widget.TextView[contains(@text, "Max") or contains(@text, "Luna")]',
      petTabMax: '//android.widget.TextView[contains(@text, "Max")]',
      petTabLuna: '//android.widget.TextView[contains(@text, "Luna")]',
      serverStatusPill: '//android.widget.TextView[contains(@text, "Sync") or contains(@text, "Active") or contains(@text, "Server")]',
      
      // Tele-Vet Consultation Dialog
      teleVetButton: '//android.widget.Button[contains(@text, "Tele-Vet") or contains(@content-desc, "Tele-Vet")]',
      teleVetDialog: '//android.widget.TextView[contains(@text, "Tele-Vet") or contains(@text, "Consultation")]',
      closeDialogBtn: '//android.widget.Button[contains(@text, "Close") or contains(@text, "End") or contains(@text, "Back")]',

      // User Profile & Drawer
      profileAvatarBtn: '//android.widget.ImageView[contains(@content-desc, "Profile") or contains(@resource-id, "avatar")]',
      signOutButton: '//android.widget.TextView[contains(@text, "Sign Out") or contains(@text, "Log Out")]'
    };
  }

  async isDashboardLoaded() {
    return (await this.isElementDisplayed(this.locators.tabHome)) ||
           (await this.isElementDisplayed(this.locators.activePetBadge));
  }

  async selectTab(tabKey) {
    logger.info(`Selecting mobile tab: ${tabKey}`);
    const key = tabKey.toLowerCase();
    const tabMap = {
      home: this.locators.tabHome,
      overview: this.locators.tabHome,
      feeding: this.locators.tabFeeding,
      meals: this.locators.tabFeeding,
      walk: this.locators.tabWalks,
      walks: this.locators.tabWalks,
      collar: this.locators.tabWalks,
      triage: this.locators.tabTriage,
      family: this.locators.tabFamily
    };

    const targetLocator = tabMap[key] || `//android.widget.TextView[contains(@text, "${tabKey}")]`;
    await this.click(targetLocator);
    await this.driver.pause(600);
  }

  async switchPet(petName) {
    logger.info(`Switching pet profile to: ${petName}`);
    const locator = `//android.widget.TextView[contains(@text, "${petName}")]`;
    await this.click(locator);
    await this.driver.pause(500);
  }

  async openTeleVet() {
    await this.click(this.locators.teleVetButton);
    await this.driver.pause(500);
  }

  async closeTeleVet() {
    if (await this.isElementDisplayed(this.locators.closeDialogBtn)) {
      await this.click(this.locators.closeDialogBtn);
      await this.driver.pause(300);
    } else {
      await this.device.pressBack();
    }
  }

  async logout() {
    logger.info('Performing logout on mobile dashboard...');
    if (await this.isElementDisplayed(this.locators.profileAvatarBtn)) {
      await this.click(this.locators.profileAvatarBtn);
      await this.driver.pause(400);
    }
    if (await this.isElementDisplayed(this.locators.signOutButton)) {
      await this.click(this.locators.signOutButton);
      await this.driver.pause(800);
    }
  }
}

module.exports = DashboardPage;
