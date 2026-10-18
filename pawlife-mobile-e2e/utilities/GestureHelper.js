const logger = require('./Logger');

class GestureHelper {
  constructor(driver) {
    this.driver = driver;
  }

  async getWindowSize() {
    return await this.driver.getWindowRect();
  }

  // ==========================================
  // Tap & Press Gestures
  // ==========================================

  async tap(x, y) {
    logger.info(`Tapping coordinate: (${x}, ${y})`);
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x, y },
          { type: 'pointerDown', button: 0 },
          { type: 'pause', duration: 100 },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }

  async doubleTap(x, y) {
    logger.info(`Double tapping coordinate: (${x}, ${y})`);
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x, y },
          { type: 'pointerDown', button: 0 },
          { type: 'pause', duration: 80 },
          { type: 'pointerUp', button: 0 },
          { type: 'pause', duration: 80 },
          { type: 'pointerDown', button: 0 },
          { type: 'pause', duration: 80 },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }

  async longPress(x, y, durationMs = 1500) {
    logger.info(`Long pressing coordinate (${x}, ${y}) for ${durationMs}ms`);
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x, y },
          { type: 'pointerDown', button: 0 },
          { type: 'pause', duration: durationMs },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }

  // ==========================================
  // Swipe Gestures
  // ==========================================

  async swipe(startX, startY, endX, endY, durationMs = 600) {
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x: startX, y: startY },
          { type: 'pointerDown', button: 0 },
          { type: 'pause', duration: 100 },
          { type: 'pointerMove', duration: durationMs, x: endX, y: endY },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }

  async swipeUp(distanceRatio = 0.5) {
    const { width, height } = await this.getWindowSize();
    const startX = Math.round(width / 2);
    const startY = Math.round(height * 0.8);
    const endY = Math.round(startY - (height * distanceRatio));
    logger.info(`Swiping UP: (${startX}, ${startY}) -> (${startX}, ${endY})`);
    await this.swipe(startX, startY, startX, endY);
  }

  async swipeDown(distanceRatio = 0.5) {
    const { width, height } = await this.getWindowSize();
    const startX = Math.round(width / 2);
    const startY = Math.round(height * 0.25);
    const endY = Math.round(startY + (height * distanceRatio));
    logger.info(`Swiping DOWN: (${startX}, ${startY}) -> (${startX}, ${endY})`);
    await this.swipe(startX, startY, startX, endY);
  }

  async swipeLeft(distanceRatio = 0.6) {
    const { width, height } = await this.getWindowSize();
    const startY = Math.round(height / 2);
    const startX = Math.round(width * 0.85);
    const endX = Math.round(startX - (width * distanceRatio));
    logger.info(`Swiping LEFT: (${startX}, ${startY}) -> (${endX}, ${startY})`);
    await this.swipe(startX, startY, endX, startY);
  }

  async swipeRight(distanceRatio = 0.6) {
    const { width, height } = await this.getWindowSize();
    const startY = Math.round(height / 2);
    const startX = Math.round(width * 0.15);
    const endX = Math.round(startX + (width * distanceRatio));
    logger.info(`Swiping RIGHT: (${startX}, ${startY}) -> (${endX}, ${startY})`);
    await this.swipe(startX, startY, endX, startY);
  }

  // ==========================================
  // Scrolling
  // ==========================================

  async scrollUntilVisible(targetElementLocator, maxSwipes = 6) {
    logger.info(`Scrolling until element visible: ${targetElementLocator}`);
    let swipeCount = 0;

    while (swipeCount < maxSwipes) {
      const elements = await this.driver.$$(targetElementLocator);
      if (elements.length > 0 && await elements[0].isDisplayed()) {
        logger.info(`Element located after ${swipeCount} swipes.`);
        return elements[0];
      }
      await this.swipeUp(0.4);
      await this.driver.pause(500);
      swipeCount++;
    }

    throw new Error(`Element ${targetElementLocator} not visible after ${maxSwipes} scroll attempts.`);
  }

  // ==========================================
  // Drag & Drop
  // ==========================================

  async dragAndDrop(sourceX, sourceY, targetX, targetY, holdDurationMs = 800) {
    logger.info(`Dragging from (${sourceX}, ${sourceY}) to (${targetX}, ${targetY})`);
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x: sourceX, y: sourceY },
          { type: 'pointerDown', button: 0 },
          { type: 'pause', duration: holdDurationMs },
          { type: 'pointerMove', duration: 700, x: targetX, y: targetY },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }

  // ==========================================
  // Pinch & Zoom
  // ==========================================

  async zoom(scale = 1.5) {
    const { width, height } = await this.getWindowSize();
    const centerX = Math.round(width / 2);
    const centerY = Math.round(height / 2);

    logger.info(`Performing pinch ZOOM outward at center: (${centerX}, ${centerY})`);
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x: centerX - 50, y: centerY },
          { type: 'pointerDown', button: 0 },
          { type: 'pointerMove', duration: 600, x: Math.round(centerX - (150 * scale)), y: centerY },
          { type: 'pointerUp', button: 0 }
        ]
      },
      {
        type: 'pointer',
        id: 'finger2',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x: centerX + 50, y: centerY },
          { type: 'pointerDown', button: 0 },
          { type: 'pointerMove', duration: 600, x: Math.round(centerX + (150 * scale)), y: centerY },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }

  async pinch(scale = 0.5) {
    const { width, height } = await this.getWindowSize();
    const centerX = Math.round(width / 2);
    const centerY = Math.round(height / 2);

    logger.info(`Performing PINCH inward at center: (${centerX}, ${centerY})`);
    await this.driver.performActions([
      {
        type: 'pointer',
        id: 'finger1',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x: centerX - 200, y: centerY },
          { type: 'pointerDown', button: 0 },
          { type: 'pointerMove', duration: 600, x: Math.round(centerX - (200 * scale)), y: centerY },
          { type: 'pointerUp', button: 0 }
        ]
      },
      {
        type: 'pointer',
        id: 'finger2',
        parameters: { pointerType: 'touch' },
        actions: [
          { type: 'pointerMove', duration: 0, x: centerX + 200, y: centerY },
          { type: 'pointerDown', button: 0 },
          { type: 'pointerMove', duration: 600, x: Math.round(centerX + (200 * scale)), y: centerY },
          { type: 'pointerUp', button: 0 }
        ]
      }
    ]);
  }
}

module.exports = GestureHelper;
