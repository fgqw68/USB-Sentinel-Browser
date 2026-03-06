# USB Sentinel Browser

USB Sentinel Browser is an Android-based managed web container designed to bridge the gap between web applications and native hardware. It demonstrates how to build a specialized browser that responds to Enterprise Mobility Management (EMM) configurations and exposes native USB Peripheral Discovery to the web layer via a custom JavaScript Bridge.

## Overview

This project serves two primary purposes:
1. Managed Connectivity: Automatically loads a URL based on an admin-defined 'start_page' attribute. If no configuration is found, it defaults to a local offline page.
2. Hardware Abstraction: Injects a custom JavaScript API (myweb.usb) into the WebView, allowing web developers to detect USB devices via JavaScript.

## Key Features

* EMM Integration: Uses RestrictionsManager to read a 'start_page' URL from an MDM provider.
* Local Fallback: Includes a built-in index.html in the assets folder for unconfigured states.
* Native-to-JS Bridge: Maps Android's UsbManager to a clean JavaScript namespace.
* Real-time Notifications: Uses callbacks to alert the web app when a USB device is attached or detached.
* MVVM Architecture: Clean separation between the WebView, USB Logic, and ViewModel.

## For Web Developers: Using the USB API

The browser injects a global namespace 'myweb'. You can register a listener to receive updates:

```javascript
myweb.usb.registerForUsbConnectionNotification(function(usbData) {
    const devices = JSON.parse(usbData);
    console.log("Connected USB Devices:", devices);
});

**Setup for Admins (EMM/MDM)**
To configure the start page, use the following managed configuration attribute:

Key: start_page

Type: String

Description: The full URL (e.g., https://your-app.com) to load on launch.

**Learning Objectives**
This project is a reference implementation for:

Bridging Java and JavaScript using @JavascriptInterface.

Implementing app_restrictions.xml for remote configuration.

Handling Android USB Host Mode broadcast receivers.

**License**
This project is open-source and available under the MIT License.

Developed by
Sabir VT
