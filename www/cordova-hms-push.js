var exec = require('cordova/exec');

var HMSPush = function () {}
HMSPush.prototype.isAndroidDevice = function(){
    return device.platform == 'Android';
}
// 获取到token
HMSPush.prototype.tokenRegistered = function (token) {
    try {
        cordova.fireDocumentEvent('hmspush.tokenRegistered', token);
    } catch(exception) {
        console.log('HuaweiPush:tokenRegistered ' + exception);
    }
}
HMSPush.prototype.onMessageReceived = function (pushData) {
    try {
        cordova.fireDocumentEvent('hmspush.onMessageReceived', pushData);
    } catch(exception) {
        console.log('HuaweiPush:onMessageReceived ' + exception);
    }
}
HMSPush.prototype.init = function(success, error) {
    if (this.isAndroidDevice()) {
        exec(success, error, "CordovaHMSPush", "init", []);
    }
};
module.exports = new HMSPush();
