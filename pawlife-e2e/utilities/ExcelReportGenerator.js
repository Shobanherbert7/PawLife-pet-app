const ExcelJS = require('exceljs');
const path = require('path');
const fs = require('fs');
const logger = require('./Logger');
const env = require('../config/environment');

class ExcelReportGenerator {
  constructor() {
    this.testCases = [];
    this.failedTests = [];
    this.executionLogs = [];
    this.startTime = Date.now();
  }

  recordTestCase({ testId, module, scenarioName, browser, status, startTime, endTime, duration }) {
    this.testCases.push({
      testId: testId || `TC-${this.testCases.length + 1}`,
      module: module || 'General',
      scenarioName,
      browser: (browser || env.browser).toUpperCase(),
      status: status || 'PASSED',
      startTime: startTime || new Date().toISOString(),
      endTime: endTime || new Date().toISOString(),
      duration: duration || '0s'
    });
  }

  recordFailure({ testName, failureReason, screenshotPath, browser, url }) {
    this.failedTests.push({
      testName,
      failureReason: failureReason ? failureReason.substring(0, 300) : 'Unknown failure',
      screenshotPath: screenshotPath || 'N/A',
      browser: (browser || env.browser).toUpperCase(),
      url: url || 'N/A'
    });
  }

  recordLog({ testName, stepDescription, result, remarks }) {
    this.executionLogs.push({
      timestamp: new Date().toLocaleTimeString(),
      testName: testName || 'General',
      stepDescription,
      result: result || 'SUCCESS',
      remarks: remarks || ''
    });
  }

  async generateReport(outputPath) {
    const reportDir = path.resolve(__dirname, '../reports/excel');
    if (!fs.existsSync(reportDir)) {
      fs.mkdirSync(reportDir, { recursive: true });
    }

    const finalPath = outputPath || path.join(reportDir, 'E2E_Report.xlsx');
    logger.info(`Generating Excel E2E Report at: ${finalPath}`);

    const workbook = new ExcelJS.Workbook();
    workbook.creator = 'PawLife QA Automation Engine';
    workbook.created = new Date();

    const total = this.testCases.length;
    const passed = this.testCases.filter(t => t.status === 'PASSED').length;
    const failed = this.testCases.filter(t => t.status === 'FAILED').length;
    const skipped = total - passed - failed;
    const passPercentage = total > 0 ? ((passed / total) * 100).toFixed(2) + '%' : '0%';
    const totalDurationSeconds = ((Date.now() - this.startTime) / 1000).toFixed(1) + 's';

    // ==========================================
    // Sheet 1: Summary
    // ==========================================
    const summarySheet = workbook.addWorksheet('Summary', {
      views: [{ showGridLines: true }]
    });

    summarySheet.columns = [
      { header: 'Metric Name', key: 'metric', width: 26 },
      { header: 'Value', key: 'value', width: 34 }
    ];

    const summaryData = [
      { metric: 'Execution Date', value: new Date().toLocaleString() },
      { metric: 'Environment', value: env.currentEnv.toUpperCase() },
      { metric: 'Target Base URL', value: env.config.baseUrl },
      { metric: 'Default Browser', value: env.browser.toUpperCase() },
      { metric: 'Total Tests', value: total },
      { metric: 'Passed', value: passed },
      { metric: 'Failed', value: failed },
      { metric: 'Skipped', value: skipped },
      { metric: 'Pass Percentage', value: passPercentage },
      { metric: 'Execution Duration', value: totalDurationSeconds }
    ];

    summarySheet.addRows(summaryData);

    // Style Summary Sheet
    summarySheet.getRow(1).eachCell((cell) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF1E3A8A' } }; // Dark Blue
      cell.alignment = { horizontal: 'center' };
    });

    summarySheet.eachRow((row, rowNumber) => {
      if (rowNumber > 1) {
        row.getCell(1).font = { bold: true };
        const metricName = row.getCell(1).value;
        if (metricName === 'Passed') {
          row.getCell(2).font = { color: { argb: 'FF15803D' }, bold: true }; // Green
        } else if (metricName === 'Failed' && failed > 0) {
          row.getCell(2).font = { color: { argb: 'FFB91C1C' }, bold: true }; // Red
        } else if (metricName === 'Pass Percentage') {
          row.getCell(2).font = { color: { argb: 'FF1D4ED8' }, bold: true };
        }
      }
    });

    // ==========================================
    // Sheet 2: Test Cases
    // ==========================================
    const testCasesSheet = workbook.addWorksheet('Test Cases', {
      views: [{ showGridLines: true }]
    });

    testCasesSheet.columns = [
      { header: 'Test ID', key: 'testId', width: 14 },
      { header: 'Module', key: 'module', width: 18 },
      { header: 'Scenario Name', key: 'scenarioName', width: 42 },
      { header: 'Browser', key: 'browser', width: 12 },
      { header: 'Status', key: 'status', width: 14 },
      { header: 'Start Time', key: 'startTime', width: 22 },
      { header: 'End Time', key: 'endTime', width: 22 },
      { header: 'Duration', key: 'duration', width: 14 }
    ];

    testCasesSheet.addRows(this.testCases);

    testCasesSheet.getRow(1).eachCell((cell) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF0F766E' } }; // Teal
      cell.alignment = { horizontal: 'center' };
    });

    testCasesSheet.eachRow((row, rowNumber) => {
      if (rowNumber > 1) {
        const statusCell = row.getCell('status');
        if (statusCell.value === 'PASSED') {
          statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFDCFCE7' } }; // Light Green
          statusCell.font = { color: { argb: 'FF166534' }, bold: true };
        } else if (statusCell.value === 'FAILED') {
          statusCell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFFEE2E2' } }; // Light Red
          statusCell.font = { color: { argb: 'FF991B1B' }, bold: true };
        }
      }
    });

    // ==========================================
    // Sheet 3: Failed Tests
    // ==========================================
    const failedSheet = workbook.addWorksheet('Failed Tests', {
      views: [{ showGridLines: true }]
    });

    failedSheet.columns = [
      { header: 'Test Name', key: 'testName', width: 36 },
      { header: 'Failure Reason', key: 'failureReason', width: 50 },
      { header: 'Screenshot Path', key: 'screenshotPath', width: 44 },
      { header: 'Browser', key: 'browser', width: 12 },
      { header: 'URL', key: 'url', width: 34 }
    ];

    failedSheet.addRows(this.failedTests);

    failedSheet.getRow(1).eachCell((cell) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FFB91C1C' } }; // Red
      cell.alignment = { horizontal: 'center' };
    });

    // ==========================================
    // Sheet 4: Execution Logs
    // ==========================================
    const logsSheet = workbook.addWorksheet('Execution Logs', {
      views: [{ showGridLines: true }]
    });

    logsSheet.columns = [
      { header: 'Timestamp', key: 'timestamp', width: 16 },
      { header: 'Test Name', key: 'testName', width: 30 },
      { header: 'Step Description', key: 'stepDescription', width: 45 },
      { header: 'Result', key: 'result', width: 14 },
      { header: 'Remarks', key: 'remarks', width: 32 }
    ];

    logsSheet.addRows(this.executionLogs);

    logsSheet.getRow(1).eachCell((cell) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: 'FF374151' } }; // Slate Gray
      cell.alignment = { horizontal: 'center' };
    });

    await workbook.xlsx.writeFile(finalPath);
    logger.info(`✅ Excel report successfully generated at: ${finalPath}`);
    return finalPath;
  }
}

// Global Singleton Instance
const excelReporter = new ExcelReportGenerator();
module.exports = excelReporter;
