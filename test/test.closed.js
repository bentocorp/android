"use strict";

require("./helpers/setup");

var wd = require("wd"),
    _ = require('underscore'),
    serverConfigs = require('./helpers/appium-servers');

describe("Bento - Closed", function () {
  this.timeout(300000);
  var driver;
  var allPassed = true;

  before(function () {
    var serverConfig = process.env.SAUCE ?
      serverConfigs.sauce : serverConfigs.local;
    driver = wd.promiseChainRemote(serverConfig);
    require("./helpers/logging").configure(driver);

    var asserters = wd.asserters; // commonly used asserters

    var desired = process.env.SAUCE ?
      _.clone(require("./helpers/caps").android18) :
      _.clone(require("./helpers/caps").android19);
    desired.app = require("./helpers/apps").android;
    
    if (process.env.SAUCE) {
      desired.name = 'Bento';
      desired.tags = ['bento'];
    }

    return driver
      .init(desired)
      .setImplicitWaitTimeout(3000);
  });

  after(function () {
    return driver
      .quit()
      .finally(function () {
        if (process.env.SAUCE) {
          return driver.sauceJobStatus(allPassed);
        }
      });
  });

  afterEach(function () {
    allPassed = allPassed && this.currentTest.state === 'passed';
  });

  it("Complete email address", function () {
    return driver
      .elementById('com.bentonow.bentonow:id/btn_get_started')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/email_address')
        .setText('norman@10x.co')
      .elementById('com.bentonow.bentonow:id/submit')
        .click()
  });

  it("Tap on OK", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_ok')
        .click()
  });
});
