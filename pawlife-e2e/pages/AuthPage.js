const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');
const logger = require('../utilities/Logger');

class AuthPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.locators = {
      signInTab: By.xpath("//button[text()='Sign In']"),
      createAccountTab: By.xpath("//button[text()='Create Account']"),
      fullNameInput: By.css("input[placeholder*='Shoban Herbert']"),
      emailInput: By.css("input[type='email']"),
      passwordInput: By.css("input[placeholder*='••••']"),
      rememberMeCheckbox: By.id('rememberMeCheck'),
      submitButton: By.css("button[type='submit']"),
      googleLoginButton: By.xpath("//button[contains(., 'Continue with Google')]"),
      errorMessage: By.xpath("//div[contains(@style, '239, 68, 68') or contains(., 'Error') or contains(., 'required')]"),
      successMessage: By.xpath("//div[contains(@style, '16, 185, 129') and contains(., 'Welcome')]"),
      personaCardShoban: By.xpath("//button[contains(., 'Shoban') and contains(., 'Log In')]"),
      personaCardPriya: By.xpath("//button[contains(., 'Dr. Priya') and contains(., 'Log In')]")
    };
  }

  async navigateToAuth() {
    await this.open('/');
    await this.clearLocalStorage();
    await this.refreshPage();
  }

  async switchToCreateAccount() {
    await this.helper.click(this.locators.createAccountTab);
  }

  async switchToSignIn() {
    await this.helper.click(this.locators.signInTab);
  }

  async enterFullName(name) {
    await this.helper.type(this.locators.fullNameInput, name);
  }

  async enterEmail(email) {
    await this.helper.type(this.locators.emailInput, email);
  }

  async enterPassword(password) {
    await this.helper.type(this.locators.passwordInput, password);
  }

  async clickSubmit() {
    await this.helper.click(this.locators.submitButton);
    await this.driver.sleep(600);
  }

  async loginWithPersona(personaName = 'Shoban') {
    logger.info(`Selecting demo persona: ${personaName}`);
    const locator = personaName === 'Dr. Priya' ? this.locators.personaCardPriya : this.locators.personaCardShoban;
    await this.helper.click(locator);
    await this.driver.sleep(1200); // Allow auth transition and dashboard state update
  }

  async getErrorMessage() {
    try {
      return await this.helper.getText(this.locators.errorMessage);
    } catch (_e) {
      return null;
    }
  }

  async isAuthScreenDisplayed() {
    return await this.helper.isElementDisplayed(this.locators.emailInput);
  }
}

module.exports = AuthPage;
