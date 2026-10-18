const ExcelJS = require('exceljs');
const path = require('path');
const fs = require('fs');
const logger = require('./Logger');
const AppiumConfig = require('../config/appiumConfig');

class MobileExcelReportGenerator {
  constructor() {
    this.workbook = new ExcelJS.Workbook();
    this.testCases = [];
    this.failures = [];
    this.executionLogs = [];
    this.startTime = new Date();
    this.deviceInfo = AppiumConfig.detectConnectedDevice();
  }

  recordTestCase(testData) {
    this.testCases.push(testData);
  }

  recordFailure(failureData) {
    this.failures.push(failureData);
  }

  recordLog(logEntry) {
    this.executionLogs.push({
      timestamp: new Date().toLocaleTimeString(),
      ...logEntry
    });
  }

  async generateReport(customOutputPath) {
    const defaultDir = path.resolve(__dirname, '../reports/excel');
    const mirrorDir = path.resolve(__dirname, '../excel');
    
    if (!fs.existsSync(defaultDir)) fs.mkdirSync(defaultDir, { recursive: true });
    if (!fs.existsSync(mirrorDir)) fs.mkdirSync(mirrorDir, { recursive: true });

    const filePath = customOutputPath || path.join(defaultDir, 'Mobile_E2E_Report.xlsx');
    const mirrorPath = path.join(mirrorDir, 'Mobile_E2E_Report.xlsx');

    const endTime = new Date();
    const durationSeconds = Math.max(1, Math.round((endTime - this.startTime) / 1000));
    const durationStr = `${Math.floor(durationSeconds / 60)}m ${durationSeconds % 60}s`;

    const totalTests = this.testCases.length;
    const passedTests = this.testCases.filter(t => t.status === 'PASSED').length;
    const failedTests = this.testCases.filter(t => t.status === 'FAILED').length;
    const skippedTests = this.testCases.filter(t => t.status === 'SKIPPED').length;
    const passPercentage = totalTests > 0 ? `${((passedTests / totalTests) * 100).toFixed(1)}%` : '0.0%';

    // Styling definitions
    const headerFill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF0F172A' } // Dark Slate Navy
    };

    const headerFont = {
      name: 'Calibri',
      size: 11,
      bold: true,
      color: { argb: 'FFFFFFFF' }
    };

    const thinBorder = {
      top: { style: 'thin', color: { argb: 'FFCBD5E1' } },
      left: { style: 'thin', color: { argb: 'FFCBD5E1' } },
      bottom: { style: 'thin', color: { argb: 'FFCBD5E1' } },
      right: { style: 'thin', color: { argb: 'FFCBD5E1' } }
    };

    // ==========================================
    // Sheet 1 - Summary
    // ==========================================
    const summarySheet = this.workbook.addWorksheet('Summary');
    summarySheet.columns = [
      { header: 'Execution Date', key: 'execDate', width: 22 },
      { header: 'Device Name', key: 'deviceName', width: 25 },
      { header: 'Android Version', key: 'androidVersion', width: 18 },
      { header: 'Total Tests', key: 'total', width: 14 },
      { header: 'Passed', key: 'passed', width: 12 },
      { header: 'Failed', key: 'failed', width: 12 },
      { header: 'Skipped', key: 'skipped', width: 12 },
      { header: 'Pass Percentage', key: 'passRate', width: 18 },
      { header: 'Execution Duration', key: 'duration', width: 20 }
    ];

    const summaryRow = summarySheet.addRow({
      execDate: new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }),
      deviceName: this.deviceInfo.model,
      androidVersion: `Android ${this.deviceInfo.osVersion}`,
      total: totalTests,
      passed: passedTests,
      failed: failedTests,
      skipped: skippedTests,
      passRate: passPercentage,
      duration: durationStr
    });

    summaryRow.getCell('passRate').fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: failedTests === 0 ? 'FFDCFCE7' : 'FFFEE2E2' }
    };
    summaryRow.getCell('passRate').font = {
      bold: true,
      color: { argb: failedTests === 0 ? 'FF166534' : 'FF991B1B' }
    };

    // ==========================================
    // Sheet 2 - Test Cases
    // ==========================================
    const testCasesSheet = this.workbook.addWorksheet('Test Cases');
    testCasesSheet.columns = [
      { header: 'Test ID', key: 'testId', width: 15 },
      { header: 'Module', key: 'module', width: 25 },
      { header: 'Scenario', key: 'scenario', width: 45 },
      { header: 'Device', key: 'device', width: 24 },
      { header: 'Status', key: 'status', width: 14 },
      { header: 'Start Time', key: 'startTime', width: 15 },
      { header: 'End Time', key: 'endTime', width: 15 },
      { header: 'Duration', key: 'duration', width: 14 }
    ];

    this.testCases.forEach((tc) => {
      const row = testCasesSheet.addRow({
        testId: tc.testId,
        module: tc.module,
        scenario: tc.scenario,
        device: tc.device || this.deviceInfo.model,
        status: tc.status,
        startTime: tc.startTime,
        endTime: tc.endTime,
        duration: tc.duration
      });

      const statusCell = row.getCell('status');
      statusCell.fill = {
        type: 'pattern',
        pattern: 'solid',
        fgColor: { argb: tc.status === 'PASSED' ? 'FFDCFCE7' : tc.status === 'FAILED' ? 'FFFEE2E2' : 'FFFEF3C7' }
      };
      statusCell.font = {
        bold: true,
        color: { argb: tc.status === 'PASSED' ? 'FF166534' : tc.status === 'FAILED' ? 'FF991B1B' : 'FF92400E' }
      };
    });

    // ==========================================
    // Sheet 3 - Failed Tests
    // ==========================================
    const failedSheet = this.workbook.addWorksheet('Failed Tests');
    failedSheet.columns = [
      { header: 'Test Name', key: 'testName', width: 40 },
      { header: 'Failure Reason', key: 'failureReason', width: 50 },
      { header: 'Screenshot Path', key: 'screenshotPath', width: 45 },
      { header: 'Device', key: 'device', width: 24 },
      { header: 'Android Version', key: 'androidVersion', width: 18 },
      { header: 'Activity Name', key: 'activityName', width: 35 }
    ];

    if (this.failures.length === 0) {
      failedSheet.addRow({
        testName: 'None',
        failureReason: 'All mobile test suites passed with zero failures.',
        screenshotPath: 'N/A',
        device: this.deviceInfo.model,
        androidVersion: `Android ${this.deviceInfo.osVersion}`,
        activityName: 'Clean Run'
      });
    } else {
      this.failures.forEach(f => {
        failedSheet.addRow({
          testName: f.testName,
          failureReason: f.failureReason,
          screenshotPath: f.screenshotPath,
          device: f.device || this.deviceInfo.model,
          androidVersion: `Android ${this.deviceInfo.osVersion}`,
          activityName: f.activityName || 'MainActivity'
        });
      });
    }

    // ==========================================
    // Sheet 4 - Execution Logs
    // ==========================================
    const logsSheet = this.workbook.addWorksheet('Execution Logs');
    logsSheet.columns = [
      { header: 'Timestamp', key: 'timestamp', width: 16 },
      { header: 'Test Name', key: 'testName', width: 35 },
      { header: 'Step', key: 'step', width: 45 },
      { header: 'Result', key: 'result', width: 15 },
      { header: 'Remarks', key: 'remarks', width: 40 }
    ];

    this.executionLogs.forEach(l => {
      logsSheet.addRow({
        timestamp: l.timestamp,
        testName: l.testName,
        step: l.step,
        result: l.result,
        remarks: l.remarks
      });
    });

    // Format all sheets
    [summarySheet, testCasesSheet, failedSheet, logsSheet].forEach(sheet => {
      const headerRow = sheet.getRow(1);
      headerRow.height = 24;
      headerRow.eachCell(cell => {
        cell.fill = headerFill;
        cell.font = headerFont;
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
      });

      sheet.eachRow((row, rowNumber) => {
        if (rowNumber > 1) {
          row.eachCell(cell => {
            cell.border = thinBorder;
            cell.alignment = { vertical: 'middle' };
          });
        }
      });
    });

    await this.workbook.xlsx.writeFile(filePath);
    await this.workbook.xlsx.writeFile(mirrorPath);
    logger.info(`✅ Mobile Excel report generated at: ${filePath}`);
    return filePath;
  }
}

module.exports = new MobileExcelReportGenerator();
