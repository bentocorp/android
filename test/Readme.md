Bento Test
---

Instalation guide:

- Install [Appium app](http://appium.io/downloads.html) also follow the [getting started guide](http://appium.io/getting-started.html?lang=en)
- Install [nodejs](https://nodejs.org) or [iojs](https://iojs.org/es/)
- Install mocha 'npm install -g mocha'
- Install dependencies (go to the test folder and run 'npm install')
- Run Appium app, select Android and click on launch (no further configuration needed)
- Connect your device to adb (real device or emulator)
- Open terminal, go to the test folder and run:
	- 'npm test': run all tests
	- 'npm run-script test-closed': test when Bento is closed
	- 'npm run-script test-out-stock': test when Bento is out of stock
	- 'npm run-script test-out-area': test when is out of the service area

Thats it