if (process.env.DEV) {
  exports.android = "/Users/normanpaniagua/Projects/Test/Appium/android/app/build/outputs/apk/app-debug.apk";
} else {
  exports.android = __dirname + "/../../app/build/outputs/apk/app-debug.apk";
}
