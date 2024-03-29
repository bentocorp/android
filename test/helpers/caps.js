
exports.ios81 = {
  browserName: '',
  'appium-version': '1.4',
  platformName: 'iOS',
  platformVersion: '8.1',
  deviceName: 'iPhone Simulator',
  app: undefined // will be set later
};

exports.android18 = {
  browserName: '',
  'appium-version': '1.4',
  platformName: 'Android',
  platformVersion: '4.3',
  deviceName: 'Android Emulator',
  app: undefined // will be set later
};

if (process.env.DEV) {
  exports.android19 = {
    browserName: '',
    'appium-version': '1.4',
    platformName: 'Android',
    platformVersion: '4.4.2',
    deviceName: 'Android Emulator',
    app: undefined // will be set later
  };

  exports.selendroid16 = {
    browserName: '',
    'appium-version': '1.4',
    platformName: 'Android',
    platformVersion: '4.1',
    automationName: 'selendroid',
    deviceName: 'Android Emulator',
    app: undefined // will be set later
  };
} else {
  exports.android19 = {
    browserName: '',
    'appium-version': '1.4',
    platformName: 'Android',
    platformVersion: '4.4.2',
    deviceName: 'Android Emulator',
    appPackage: 'com.bentonow.bentonow',
    appWaitActivity: '.HomeAboutActivity,.DeliveryLocationActivity',
    app: undefined // will be set later
  };

  exports.selendroid16 = {
    browserName: '',
    'appium-version': '1.4',
    platformName: 'Android',
    platformVersion: '4.1',
    automationName: 'selendroid',
    deviceName: 'Android Emulator',
    appPackage: 'com.bentonow.bentonow',
    appWaitActivity: '.HomeAboutActivity,.DeliveryLocationActivity',
    app: undefined // will be set later
  };
}