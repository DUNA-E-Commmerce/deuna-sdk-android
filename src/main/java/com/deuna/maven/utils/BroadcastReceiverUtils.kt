package com.deuna.maven.utils

import android.content.BroadcastReceiver
import android.content.Context



class BroadcastReceiverUtils {
    companion object {
        fun register(
            context: Context,
            broadcastReceiver: BroadcastReceiver,
            action: String,
        ) {


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                context.registerReceiver(
//                    broadcastReceiver,
//                    IntentFilter(action),
//                    AppCompatActivity.RECEIVER_NOT_EXPORTED,
//                )
//            } else {
//                context.registerReceiver(
//                    broadcastReceiver,
//                    IntentFilter(action),
//                )
//            }
        }
    }
}