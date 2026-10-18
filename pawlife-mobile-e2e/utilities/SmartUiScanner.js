const logger = require('./Logger');

class SmartUiScanner {
  /**
   * Analyzes live Appium XML page source or Compose node hierarchy
   * @param {WebdriverIO.Browser} driver
   */
  static async scanLiveScreen(driver) {
    logger.info('🧠 [Smart AI Scanner] Scanning live Android view hierarchy...');
    try {
      const pageSource = await driver.getPageSource();
      const discovery = {
        screenTitle: 'UnknownScreen',
        textFields: [],
        buttons: [],
        bottomTabs: [],
        generatedScenarios: []
      };

      // Match input fields (EditText / TextInput / Compose TextField)
      const inputMatches = pageSource.matchAll(/class="(?:android\.widget\.EditText|androidx\.compose[^"]*)"\s+[^>]*text="([^"]*)"\s+[^>]*content-desc="([^"]*)"/g);
      for (const m of inputMatches) {
        discovery.textFields.push({ text: m[1], desc: m[2] });
      }

      // Fallback regex matching standard Android attributes
      const editTextRegex = /<[^>]+class="[^"]*EditText[^"]*"[^>]*>/g;
      let match;
      let fieldIndex = 1;
      while ((match = editTextRegex.exec(pageSource)) !== null) {
        const tag = match[0];
        const resourceId = (tag.match(/resource-id="([^"]+)"/) || [])[1] || '';
        const text = (tag.match(/text="([^"]+)"/) || [])[1] || '';
        const contentDesc = (tag.match(/content-desc="([^"]+)"/) || [])[1] || '';
        const isPassword = tag.includes('password="true"') || resourceId.toLowerCase().includes('password');

        const fieldIdentifier = resourceId || contentDesc || text || `field_${fieldIndex}`;

        discovery.textFields.push({
          id: fieldIdentifier,
          isPassword,
          resourceId,
          contentDesc
        });

        // Auto-generate validation scenarios
        if (isPassword) {
          discovery.generatedScenarios.push({
            id: `DYN-MOB-PWD-${fieldIndex}`,
            type: 'PASSWORD_LENGTH',
            target: fieldIdentifier,
            description: `Verify short password rejection (< 6 chars) on [${fieldIdentifier}]`,
            payload: '123'
          });
        } else if (fieldIdentifier.toLowerCase().includes('email')) {
          discovery.generatedScenarios.push({
            id: `DYN-MOB-EMAIL-${fieldIndex}`,
            type: 'EMAIL_FORMAT',
            target: fieldIdentifier,
            description: `Verify malformed email regex rejection on [${fieldIdentifier}]`,
            payload: 'invalid-email-format'
          });
        } else {
          discovery.generatedScenarios.push({
            id: `DYN-MOB-REQ-${fieldIndex}`,
            type: 'EMPTY_FIELD_CHECK',
            target: fieldIdentifier,
            description: `Verify empty submission triggers validation error on [${fieldIdentifier}]`,
            payload: ''
          });
        }

        fieldIndex++;
      }

      logger.info(`🧠 [Smart AI Scanner] Discovered ${discovery.textFields.length} inputs and generated ${discovery.generatedScenarios.length} dynamic tests.`);
      return discovery;
    } catch (err) {
      logger.warn(`Failed to scan live screen: ${err.message}. Using fallback model.`);
      return this.getFallbackModel();
    }
  }

  static getFallbackModel() {
    return {
      screenTitle: 'PawLife Authentication & Onboarding',
      textFields: [
        { id: 'com.pawlife.app:id/emailInput', isPassword: false },
        { id: 'com.pawlife.app:id/passwordInput', isPassword: true },
        { id: 'com.pawlife.app:id/foodNameInput', isPassword: false },
        { id: 'com.pawlife.app:id/portionInput', isPassword: false }
      ],
      generatedScenarios: [
        { id: 'DYN-MOB-1', type: 'EMPTY_CHECK', target: 'email', description: 'Validate empty email input rejection', payload: '' },
        { id: 'DYN-MOB-2', type: 'EMAIL_FORMAT', target: 'email', description: 'Validate invalid email format rejection', payload: 'shoban@@fail.com' },
        { id: 'DYN-MOB-3', type: 'PASSWORD_LENGTH', target: 'password', description: 'Validate short password (< 6 chars) rejection', payload: '12' },
        { id: 'DYN-MOB-4', type: 'NUMERIC_BOUNDARY', target: 'portion', description: 'Validate negative portion size rejection', payload: '-10' }
      ]
    };
  }
}

module.exports = SmartUiScanner;
