# cordova-huawei-push
The huawei push for cordova, hms sdk version, now only support android.

## Install
```shell
cordova plugin add cordova-hms-push
```

Open the root project level build.gradle, ensure the following settings added

buildscript {
    repositories {
        maven { url 'http://developer.huawei.com/repo/' }
    }
    dependencies {
        classpath 'com.huawei.agconnect:agcp:1.0.0.300'
    }
}

## How to use

### Init the hms connection

```javascript
cordova.plugins.hmspush.init();
```

### Token Registered

```javascript
document.addEventListener('hmspush.tokenRegistered', function (event) {
    console.log(event.token)
}.bind(this), false);
```
You can get the token value by `event.token`

### Message Received

```javascript
document.addEventListener('hmspush.onMessageReceived', function (event) {
    console.log(event.additionalData)
}.bind(this), false);
```
You can get the token value by `event.additionalData`
