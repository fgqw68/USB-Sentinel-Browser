package com.example.usbsentinel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

/**
 * UsbReceiver handles USB device attach/detach broadcasts.
 */
public class UsbReceiver extends BroadcastReceiver {

    private UsbViewModel usbViewModel;

    public UsbReceiver(UsbViewModel usbViewModel) {
        this.usbViewModel = usbViewModel;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        if (usbViewModel == null) {
            return;
        }

        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            usbViewModel.onUsbDeviceChanged();
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
            usbViewModel.onUsbDeviceChanged();
        }
    }
}