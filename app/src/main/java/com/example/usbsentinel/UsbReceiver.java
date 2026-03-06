package com.example.usbsentinel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

/**
 * UsbReceiver handles USB device attach/detach broadcasts.
 * Registered in AndroidManifest.xml to receive system broadcasts.
 */
public class UsbReceiver extends BroadcastReceiver {

    private static UsbViewModel usbViewModel;

    /**
     * Sets the ViewModel reference for the receiver to notify.
     * This must be called before any USB events are expected.
     *
     * @param viewModel The ViewModel to notify on USB events
     */
    public static void setUsbViewModel(UsbViewModel viewModel) {
        usbViewModel = viewModel;
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