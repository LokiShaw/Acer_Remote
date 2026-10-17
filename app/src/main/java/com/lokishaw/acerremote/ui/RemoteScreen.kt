package com.lokishaw.acerremote.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lokishaw.acerremote.ConnectionMode
import com.lokishaw.acerremote.RemoteAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

private val Background = Color(0xFF000000)
private val ButtonColor = Color(0xFF1C1C1E)
private val ButtonPressed = Color(0xFF3A3A3C)
private val PadColor = Color(0xFF121214)
private val Foreground = Color(0xFFF2F2F7)
private val Muted = Color(0xFF8E8E93)

@Composable
fun RemoteScreen(
    mode: ConnectionMode,
    onModeChange: (ConnectionMode) -> Unit,
    onAction: (RemoteAction) -> Unit,
) {
    val view = LocalView.current
    val send: (RemoteAction) -> Unit = { action ->
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        onAction(action)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ModeToggle(mode, onModeChange)
        Spacer(Modifier.height(32.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            RoundButton(Icons.AutoMirrored.Filled.ArrowBack, "Back") { send(RemoteAction.BACK) }
            RoundButton(Icons.Filled.Home, "Home") { send(RemoteAction.HOME) }
            RoundButton(Icons.Filled.Mic, "Google voice") { send(RemoteAction.VOICE) }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            RepeatingRoundButton(Icons.AutoMirrored.Filled.VolumeDown, "Volume down") {
                send(RemoteAction.VOLUME_DOWN)
            }
            RoundButton(Icons.AutoMirrored.Filled.VolumeOff, "Mute") { send(RemoteAction.MUTE) }
            RepeatingRoundButton(Icons.AutoMirrored.Filled.VolumeUp, "Volume up") {
                send(RemoteAction.VOLUME_UP)
            }
        }

        Spacer(Modifier.height(32.dp))
        TouchPad(
            onAction = send,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun ModeToggle(mode: ConnectionMode, onModeChange: (ConnectionMode) -> Unit) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(ButtonColor)
            .padding(4.dp),
    ) {
        ConnectionMode.entries.forEach { option ->
            val selected = option == mode
            Text(
                text = option.label,
                color = if (selected) Background else Muted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) Foreground else Color.Transparent)
                    .clickable { onModeChange(option) }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun RoundButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ButtonColor)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = Foreground, modifier = Modifier.size(30.dp))
    }
}

/** Fires once on press, then keeps repeating while held (for volume). */
@Composable
private fun RepeatingRoundButton(icon: ImageVector, label: String, onRepeat: () -> Unit) {
    val scope = rememberCoroutineScope()
    val currentOnRepeat by rememberUpdatedState(onRepeat)
    var pressed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (pressed) ButtonPressed else ButtonColor)
            .semantics { contentDescription = label }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    pressed = true
                    val job = scope.launch {
                        currentOnRepeat()
                        delay(400)
                        while (isActive) {
                            currentOnRepeat()
                            delay(120)
                        }
                    }
                    tryAwaitRelease()
                    job.cancel()
                    pressed = false
                })
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = Foreground, modifier = Modifier.size(30.dp))
    }
}

/**
 * Large drag pad: each [StepDistance] of travel sends one arrow in the dominant
 * direction, so a long swipe moves several items. A tap sends OK.
 */
@Composable
private fun TouchPad(onAction: (RemoteAction) -> Unit, modifier: Modifier = Modifier) {
    val currentOnAction by rememberUpdatedState(onAction)
    val stepPx = with(LocalDensity.current) { StepDistance.toPx() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(PadColor)
            .semantics { contentDescription = "Touch pad. Swipe to move, tap to select." }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { currentOnAction(RemoteAction.OK) })
            }
            .pointerInput(Unit) {
                var dx = 0f
                var dy = 0f
                detectDragGestures(onDragStart = { dx = 0f; dy = 0f }) { change, drag ->
                    change.consume()
                    dx += drag.x
                    dy += drag.y
                    while (abs(dx) >= stepPx || abs(dy) >= stepPx) {
                        if (abs(dx) > abs(dy)) {
                            currentOnAction(if (dx > 0) RemoteAction.RIGHT else RemoteAction.LEFT)
                            dx -= if (dx > 0) stepPx else -stepPx
                            dy = 0f
                        } else {
                            currentOnAction(if (dy > 0) RemoteAction.DOWN else RemoteAction.UP)
                            dy -= if (dy > 0) stepPx else -stepPx
                            dx = 0f
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("Swipe to move · Tap to select", color = Muted.copy(alpha = 0.6f), fontSize = 13.sp)
    }
}

private val StepDistance = 56.dp

@Preview(widthDp = 360, heightDp = 760)
@Composable
private fun RemoteScreenPreview() {
    RemoteScreen(mode = ConnectionMode.WIFI, onModeChange = {}, onAction = {})
}
