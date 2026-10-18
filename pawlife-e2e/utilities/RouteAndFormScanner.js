const fs = require('fs');
const path = require('path');
const logger = require('./Logger');

class RouteAndFormScanner {
  /**
   * Scans React project source code or live DOM to extract routes, forms, and validation rules
   * @param {string} [projectSrcDir]
   */
  static scanReactSource(projectSrcDir) {
    const targetDir = projectSrcDir || path.resolve(__dirname, '../../pawlife-web/src');
    logger.info(`[Dynamic Scanner] Scanning React components at: ${targetDir}`);

    const result = {
      routes: [],
      forms: [],
      generatedTestCases: []
    };

    if (!fs.existsSync(targetDir)) {
      logger.warn(`Project source dir not found at ${targetDir}. Using fallback discovered rules.`);
      return this.getFallbackRules();
    }

    const files = fs.readdirSync(targetDir).filter(f => f.endsWith('.jsx') || f.endsWith('.tsx') || f.endsWith('.js'));

    files.forEach(file => {
      const filePath = path.join(targetDir, file);
      const content = fs.readFileSync(filePath, 'utf-8');

      // 1. Discover Navigation Routes & Tabs
      const tabMatches = content.matchAll(/setActiveTab\(['"]([^'"]+)['"]\)/g);
      for (const match of tabMatches) {
        if (!result.routes.includes(match[1])) {
          result.routes.push(match[1]);
        }
      }

      // 2. Discover Input Fields & Validation Rules
      // Match inputs with types, required, min, max, etc.
      const inputRegex = /<input\s+([^>]+)>/g;
      let inputMatch;
      while ((inputMatch = inputRegex.exec(content)) !== null) {
        const attrString = inputMatch[1];
        const field = {
          file,
          type: this.extractAttr(attrString, 'type') || 'text',
          placeholder: this.extractAttr(attrString, 'placeholder') || '',
          required: attrString.includes('required'),
          name: this.extractAttr(attrString, 'name') || this.extractAttr(attrString, 'id') || '',
          minLength: parseInt(this.extractAttr(attrString, 'minLength') || '0', 10),
          min: this.extractAttr(attrString, 'min'),
          max: this.extractAttr(attrString, 'max')
        };

        if (field.type || field.placeholder) {
          result.forms.push(field);
        }
      }
    });

    // 3. Dynamically Generate Test Matrices from Discovered Rules
    result.forms.forEach((formField, index) => {
      const fieldId = formField.name || formField.placeholder || `field_${index}`;

      // A. Required field validation
      if (formField.required) {
        result.generatedTestCases.push({
          id: `DYN-REQ-${index + 1}`,
          type: 'REQUIRED_CHECK',
          field: fieldId,
          file: formField.file,
          description: `Validate empty submission triggers required validation on "${fieldId}"`,
          testValue: '',
          expectedError: true
        });
      }

      // B. Email format validation
      if (formField.type === 'email' || fieldId.toLowerCase().includes('email')) {
        result.generatedTestCases.push({
          id: `DYN-EMAIL-${index + 1}`,
          type: 'FORMAT_CHECK',
          field: fieldId,
          file: formField.file,
          description: `Validate invalid email regex triggers error on "${fieldId}"`,
          testValue: 'invalid-email-format-without-at',
          expectedError: true
        });
      }

      // C. Password complexity validation
      if (formField.type === 'password' || fieldId.toLowerCase().includes('password')) {
        result.generatedTestCases.push({
          id: `DYN-PWD-${index + 1}`,
          type: 'LENGTH_CHECK',
          field: fieldId,
          file: formField.file,
          description: `Validate short password (< 6 chars) is rejected on "${fieldId}"`,
          testValue: '123',
          expectedError: true
        });
      }

      // D. Numeric boundaries
      if (formField.type === 'number' && (formField.min || formField.max)) {
        result.generatedTestCases.push({
          id: `DYN-NUM-${index + 1}`,
          type: 'BOUNDARY_CHECK',
          field: fieldId,
          file: formField.file,
          description: `Validate negative/zero boundary condition on numeric "${fieldId}"`,
          testValue: '-15',
          expectedError: true
        });
      }
    });

    logger.info(`[Dynamic Scanner] Discovered ${result.routes.length} routes, ${result.forms.length} form inputs, and generated ${result.generatedTestCases.length} dynamic tests.`);
    return result;
  }

  static extractAttr(str, attrName) {
    const match = str.match(new RegExp(`${attrName}=["'{]([^"'{}]+)["'}]`));
    return match ? match[1] : null;
  }

  static getFallbackRules() {
    return {
      routes: ['overview', 'meals', 'walks', 'diagnostics', 'places', 'community', 'agility', 'acoustics', 'marketplace'],
      forms: [
        { type: 'email', name: 'email', required: true, placeholder: 'name@example.com' },
        { type: 'password', name: 'password', required: true, minLength: 6, placeholder: '••••••••' },
        { type: 'text', name: 'foodName', required: true, placeholder: 'e.g. Royal Canin' },
        { type: 'number', name: 'portionGrams', min: '1', max: '2000', placeholder: '150' },
        { type: 'text', name: 'memberName', required: true, placeholder: 'Sarah' },
        { type: 'tel', name: 'phone', required: false, placeholder: '+91 98401...' }
      ],
      generatedTestCases: [
        { id: 'DYN-1', type: 'REQUIRED_CHECK', field: 'email', description: 'Validate required email field', testValue: '', expectedError: true },
        { id: 'DYN-2', type: 'FORMAT_CHECK', field: 'email', description: 'Validate malformed email', testValue: 'user@@fail', expectedError: true },
        { id: 'DYN-3', type: 'LENGTH_CHECK', field: 'password', description: 'Validate short password', testValue: '12', expectedError: true },
        { id: 'DYN-4', type: 'REQUIRED_CHECK', field: 'foodName', description: 'Validate empty feeding food name', testValue: '', expectedError: true },
        { id: 'DYN-5', type: 'BOUNDARY_CHECK', field: 'portionGrams', description: 'Validate negative portion size', testValue: '-50', expectedError: true }
      ]
    };
  }
}

module.exports = RouteAndFormScanner;
