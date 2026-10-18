const { By } = require('selenium-webdriver');
const BasePage = require('./BasePage');
const logger = require('../utilities/Logger');

class FeedingPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.locators = {
      mealsHeader: By.xpath("//h1[contains(., 'Feeding')] | //h2[contains(., 'Feeding') or contains(., 'Nutrition')]"),
      openLogModalBtn: By.xpath("//button[contains(., 'Manual Log')]"),
      foodNameInput: By.css("input[placeholder*='Royal Canin'], input[placeholder*='food'], input[placeholder*='Food']"),
      portionInput: By.css("input[type='number']"),
      logMealButton: By.xpath("//button[contains(., 'Save Meal Log') or contains(., 'Log Meal')]"),
      mealRows: By.xpath("//table//tbody//tr")
    };
  }

  async isFeedingSectionVisible() {
    return await this.helper.isElementDisplayed(this.locators.mealsHeader);
  }

  async openManualMealModal() {
    await this.helper.click(this.locators.openLogModalBtn);
    await this.driver.sleep(400);
  }

  async enterFoodName(name) {
    await this.helper.type(this.locators.foodNameInput, name);
  }

  async enterPortion(portionGrams) {
    await this.helper.type(this.locators.portionInput, portionGrams.toString());
  }

  async submitMeal() {
    await this.helper.click(this.locators.logMealButton);
    await this.driver.sleep(600);
  }
}

module.exports = FeedingPage;
