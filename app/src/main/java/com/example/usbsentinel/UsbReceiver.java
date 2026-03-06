package com.example.usbsentinel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

/**
 * UsbReceiver handles USB device attach/detach broadcasts.
 * Registered in AndroidManifest.xml to receive system broadcasts even when app is in background.
 * Updates the UsbRepository with the latest device list.
 */
public class UsbReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())
                || UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
            // Update the repository with the latest device list
            UsbRepository.getInstance(context).updateDeviceList();
        }
    }
}