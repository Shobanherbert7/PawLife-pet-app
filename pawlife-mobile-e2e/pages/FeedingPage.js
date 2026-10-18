const BasePage = require('./BasePage');
const logger = require('../utilities/Logger');

class FeedingPage extends BasePage {
  constructor(driver) {
    super(driver);
    this.locators = {
      headerTitle: '//android.widget.TextView[contains(@text, "Feeding") or contains(@text, "Meals") or contains(@text, "Nutrition")]',
      logMealButton: '//android.widget.Button[contains(@text, "Log Meal") or contains(@content-desc, "Log Meal")]',
      foodNameInput: '//android.widget.EditText[contains(@text, "Food") or contains(@resource-id, "foodName")]',
      portionInput: '//android.widget.EditText[contains(@resource-id, "portion") or contains(@text, "Portion") or contains(@text, "grams")]',
      saveMealButton: '//android.widget.Button[contains(@text, "Save") or contains(@text, "Confirm")]',
      mealList: '//androidx.recyclerview.widget.RecyclerView | //android.widget.ScrollView',
      calorieBadge: '//android.widget.TextView[contains(@text, "kcal") or contains(@text, "Calories")]'
    };
  }

  async isFeedingScreenVisible() {
    return await this.isElementDisplayed(this.locators.headerTitle);
  }

  async openLogMealDialog() {
    await this.click(this.locators.logMealButton);
    await this.driver.pause(500);
  }

  async logMeal(foodName, portionGrams) {
    logger.info(`Logging meal: ${foodName} (${portionGrams}g)`);
    await this.openLogMealDialog();
    await this.type(this.locators.foodNameInput, foodName);
    await this.type(this.locators.portionInput, portionGrams.toString());
    await this.click(this.locators.saveMealButton);
    await this.driver.pause(800);
  }

  async scrollMealList() {
    await this.gestures.swipeUp(0.5);
  }
}

module.exports = FeedingPage;
