"use strict";

require("./helpers/setup");

var wd = require("wd"),
    _ = require('underscore'),
    serverConfigs = require('./helpers/appium-servers');

describe("Bento", function () {
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
      .setImplicitWaitTimeout(5000);
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

  it("Tap on Get Started", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_get_started')
        .click()
  });

  it("Close GPS alert (if present)", function () {
    return driver
      .waitForElementById('android:id/button2')
        .click()
  });

  it("Change Address", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/autoCompleteTextView')
        .setText('726 Market Street, San Francisco, CA 94102, USA')
      .back()
      .back();
  });
  
  it("Tap on Help", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_help')
        .click()
  });

  it("Tap on I Agree", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_iagree')
        .click()
  });

  it("Tap on Help", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_help')
        .click()
  });

  it("Tap on btn_cancel", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_cancel', 5000)
        .click()
  });

  it("Uncheck TOS", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/chckIagree')
        .click()
  });

  it("Try to continue without TOS checked", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_continue')
        .click()
  });
  
  it("Check TOS", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/chckIagree')
        .click()
  });

  it("Confirm Address", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_confirm_address')
        .click();
  });

  it("Select main dish", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/build_main')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/main_menu_list_items')
      .then(function(){
        var touch = new wd.TouchAction();
        touch
          .tap({x:300, y:400});

        return driver
          .performTouchAction(touch)
          .waitForElementById('com.bentonow.bentonow:id/btn_add_to_bento')
            .click();
      });
  });

  it("Select side dish 1", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_bento_side_1')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/menu_item_side_listview')
      .then(function(){
        var touch = new wd.TouchAction();
        touch
          .tap({x:200, y:400});

        return driver
          .performTouchAction(touch)
          .waitForElementById('com.bentonow.bentonow:id/btn_add_to_bento')
            .click();
      });
  });

  it("Select side dish 2", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_bento_side_2')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/menu_item_side_listview')
      .then(function(){
        var touch = new wd.TouchAction();
        touch
          .tap({x:550, y:400});

        return driver
          .performTouchAction(touch)
          .waitForElementById('com.bentonow.bentonow:id/btn_add_to_bento')
            .click();
      });
  });

  it("Select side dish 3", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_bento_side_3')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/menu_item_side_listview')
      .then(function(){
        var touch = new wd.TouchAction();
        touch
          .tap({x:200, y:775});

        return driver
          .performTouchAction(touch)
          .waitForElementById('com.bentonow.bentonow:id/btn_add_to_bento')
            .click();
      });
  });

  it("Select side dish 4", function () {
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_bento_side_4')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/menu_item_side_listview')
      .then(function(){
        var touch = new wd.TouchAction();
        touch
          .tap({x:550, y:775});

        return driver
          .performTouchAction(touch)
          .waitForElementById('com.bentonow.bentonow:id/btn_add_to_bento')
            .click();
      });
  });

  it('Finalize Order', function(){
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_continue_active')
        .click();
  });

  it('Signin', function(){
    var email_address = 'vincent+5@bentonow.com';
    var password = 'somepassword716*';
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_go_to_sign_in_activity')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/email_address')
        .setText(email_address)
      .waitForElementById('com.bentonow.bentonow:id/password')
        .setText(password)
      .back()
      .waitForElementById('com.bentonow.bentonow:id/btn_sign_in')
        .click();
  });
  
  it('Change Address', function(){
    return driver
      .sleep(5000)
      .waitForElementById('com.bentonow.bentonow:id/btn_edit_address')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/autoCompleteTextView')
        .setText('Kearny St, San Francisco, CA 94108, USA')
      .back()
      .back()
      .waitForElementById('com.bentonow.bentonow:id/btn_confirm_address')
        .click();
  });
  
  it ('Change Credit Card', function(){
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_edit_credit_card')
        .click()
      .waitForElementById('com.bentonow.bentonow:id/number')
        .setText('4242424242424242')
      .waitForElementById('com.bentonow.bentonow:id/expMonth')
        .setText('1020')
      .waitForElementById('com.bentonow.bentonow:id/cvc')
        .setText('123')
      .waitForElementById('com.bentonow.bentonow:id/save')
        .click();
  });

  it ('Check total', function(){
    return driver
      .waitForElementById('com.bentonow.bentonow:id/order_total_textview')
        .text().should.become('$14.25');
  });

  it ('Change delivery tip', function(){
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_tip_positive')
        .click()
        .click()
        .click()
        .click()
        .click()
      .waitForElementById('com.bentonow.bentonow:id/tip_percent')
        .text().should.become('30%')
      .waitForElementById('com.bentonow.bentonow:id/order_total_textview')
        .text().should.become('$16.65');
  });

  it ('Let eat', function(){
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_confirm_pay_order')
        .click();
  });

  it ('Order confirmed', function(){
    return driver
      .waitForElementById('com.bentonow.bentonow:id/btn_build_another_bento')
        .click();
  });
});
