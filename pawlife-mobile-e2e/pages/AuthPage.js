const BasePage = require('./BasePage');
const logger = require('../utilities/Logger');

class AuthPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.locators = {
      // Android Accessibility IDs, Resource IDs, and XPath fallbacks
      emailInput: '//android.widget.EditText[contains(@text, "Email") or contains(@resource-id, "email") or contains(@content-desc, "Email")]',
      passwordInput: '//android.widget.EditText[contains(@password, "true") or contains(@resource-id, "password") or contains(@content-desc, "Password")]',
      signInButton: '//android.widget.Button[contains(@text, "Sign In") or contains(@text, "Log In") or contains(@content-desc, "Sign In")]',
      signUpTab: '//android.widget.TextView[contains(@text, "Sign Up") or contains(@text, "Register")]',
      nameInput: '//android.widget.EditText[contains(@text, "Name") or contains(@resource-id, "name")]',
      googleSignInBtn: '//android.widget.Button[contains(@text, "Google") or contains(@content-desc, "Google")]',
      errorMessage: '//android.widget.TextView[contains(@resource-id, "error") or contains(@text, "required") or contains(@text, "invalid")]',
      welcomeBanner: '//android.widget.TextView[contains(@text, "Welcome") or contains(@text, "PawLife")]',
      demoPersonaShoban: '//android.widget.Button[contains(@text, "Shoban")]'
    };
  }

  async isAuthScreenLoaded() {
    return (await this.isElementDisplayed(this.locators.emailInput)) || 
           (await this.isElementDisplayed(this.locators.signInButton));
  }

  async enterEmail(email) {
    await this.type(this.locators.emailInput, email);
  }

  async enterPassword(password) {
    await this.type(this.locators.passwordInput, password);
  }

  async clickSignIn() {
    await this.click(this.locators.signInButton);
    await this.driver.pause(600);
  }

  async login(email, password) {
    logger.info(`Performing login with: ${email}`);
    await this.enterEmail(email);
    await this.enterPassword(password);
    await this.clickSignIn();
  }

  async getErrorMessage() {
    if (await this.isElementDisplayed(this.locators.errorMessage)) {
      return await this.getText(this.locators.errorMessage);
    }
    return '';
  }

  async clickDemoPersona(personaName = 'Shoban') {
    logger.info(`Clicking demo persona: ${personaName}`);
    const locator = `//android.widget.Button[contains(@text, "${personaName}")]`;
    await this.click(locator);
    await this.driver.pause(800);
  }
}

module.exports = AuthPage;
