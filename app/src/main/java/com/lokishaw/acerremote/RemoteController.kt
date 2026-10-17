package com.lokishaw.acerremote

import android.util.Log

/** Sends [RemoteAction]s to the TV over one transport (IR or Wi-Fi). */
interface RemoteController {
    fun send(action: RemoteAction)
}

/** Placeholder until the IR and Wi-Fi transports are implemented. */
class LoggingController(private val mode: ConnectionMode) : RemoteController {
    override fun send(action: RemoteAction) {
        Log.d("AcerRemote", "[${mode.label}] $action")
    }
}
