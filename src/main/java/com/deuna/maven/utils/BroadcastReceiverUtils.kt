package com.deuna.maven.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity


enum class DeunaBroadcastReceiverAction(val value: String) {
    CHECKOUT("com.deuna.maven.CLOSE_CHECKOUT"),
    ELEMENTS("com.deuna.maven.CLOSE_ELEMENTS")
}

class BroadcastReceiverUtils {
    companion object {
        fun register(
            context: Context,
            broadcastReceiver: BroadcastReceiver,
            action: DeunaBroadcastReceiverAction,
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    broadcastReceiver,
                    IntentFilter(action.value),
                    AppCompatActivity.RECEIVER_NOT_EXPORTED,
                )
            } else {
                context.registerReceiver(
                    broadcastReceiver,
                    IntentFilter(action.value),
                )
            }
        }
    }
}