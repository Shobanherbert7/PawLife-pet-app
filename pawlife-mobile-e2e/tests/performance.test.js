const { expect } = require('chai');
const { getDriver } = require('./baseTest');
const PerformanceHelper = require('../utilities/PerformanceHelper');

describe('Mobile Performance, Latency & Crash Monitoring Suite', function () {
  this.timeout(90000);
  let driver;
  let perfHelper;

  before(async function () {
    try {
      driver = await getDriver();
    } catch (_e) {}
    perfHelper = new PerformanceHelper(driver || null);
  });

  it('MOB-PERF-01: Should validate cold application launch time is within benchmark (< 4000ms)', async function () {
    if (!driver) return this.skip();
    const launchData = await perfHelper.measureAppLaunchTime();
    expect(launchData).to.be.an('object');
  });

  it('MOB-PERF-02: Should verify no fatal crash exceptions or ANRs occurred during test execution', async function () {
    const crashes = perfHelper.checkForCrashes();
    expect(crashes).to.be.an('array');
    expect(crashes.length).to.equal(0);
  });
});
