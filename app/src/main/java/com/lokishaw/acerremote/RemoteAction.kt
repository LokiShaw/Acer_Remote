package com.lokishaw.acerremote

/** Every command the remote can send, independent of how it reaches the TV. */
enum class RemoteAction {
    VOLUME_UP,
    VOLUME_DOWN,
    MUTE,
    HOME,
    BACK,
    VOICE,
    UP,
    DOWN,
    LEFT,
    RIGHT,
    OK,
}

/** How commands reach the TV. */
enum class ConnectionMode(val label: String) {
    IR("IR"),
    WIFI("Wi-Fi"),
}
