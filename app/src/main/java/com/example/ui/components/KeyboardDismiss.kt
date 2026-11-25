package com.example.ui.components

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.abs

/**
 * Coordinates touch-outside and scroll-to-dismiss keyboard behavior across the entire app
 * without consuming pointer events or interfering with buttons, cards, or scrolling.
 */
@Stable
class KeyboardDismissController {
    private val protectedPointerIds = HashSet<PointerId>()
    private val focusedInputKeys = HashSet<Any>()

    val hasFocusedInput: Boolean
        get() = focusedInputKeys.isNotEmpty()

    fun markProtectedPointer(id: PointerId) {
        if (protectedPointerIds.size > 32) {
            protectedPointerIds.clear()
        }
        protectedPointerIds.add(id)
    }

    fun clearProtectedPointer(id: PointerId) {
        protectedPointerIds.remove(id)
    }

    fun isProtectedPointer(id: PointerId): Boolean = protectedPointerIds.contains(id)

    fun setInputFocused(key: Any, focused: Boolean) {
        if (focused) {
            focusedInputKeys.add(key)
        } else {
            focusedInputKeys.remove(key)
        }
    }
}

val LocalKeyboardDismissController = compositionLocalOf { KeyboardDismissController() }

/**
 * Marks a composable (such as a text field or chat send button) so touches inside its bounds
 * do NOT trigger outside-touch keyboard dismissal.
 */
fun Modifier.keepKeyboardOpenOnTouch(trackFocus: Boolean = false): Modifier = composed {
    val controller = LocalKeyboardDismissController.current
    val focusKey = remember { Any() }

    if (trackFocus) {
        DisposableEffect(controller, focusKey) {
            onDispose { controller.setInputFocused(focusKey, false) }
        }
    }

    this
        .then(
            if (trackFocus) {
                Modifier.onFocusChanged { state ->
                    controller.setInputFocused(focusKey, state.hasFocus)
                }
            } else {
                Modifier
            }
        )
        .pointerInput(controller) {
            val activeIds = HashSet<PointerId>()
            try {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        for (change in event.changes) {
                            if (change.changedToDownIgnoreConsumed()) {
                                activeIds.add(change.id)
                                controller.markProtectedPointer(change.id)
                            } else if (!change.pressed && activeIds.remove(change.id)) {
                                controller.clearProtectedPointer(change.id)
                            }
                        }
                    }
                }
            } finally {
                activeIds.forEach { controller.clearProtectedPointer(it) }
                activeIds.clear()
            }
        }
}

/**
 * Dismisses the soft keyboard and clears text focus whenever the user taps or scrolls
 * anywhere outside a composable marked with [keepKeyboardOpenOnTouch].
 *
 * Uses [PointerEventPass.Final] without consuming events so clicks, ripples, and scrolls
 * on child composables remain completely unaffected.
 */
fun Modifier.dismissKeyboardOnOutsideTouch(): Modifier = composed {
    val controller = LocalKeyboardDismissController.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    val dismissIfNeeded = remember(controller, focusManager, keyboardController, view) {
        {
            val isImeVisible = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            if (isImeVisible || controller.hasFocusedInput) {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
        }
    }

    val scrollDismissConnection = remember(dismissIfNeeded) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput &&
                    (abs(available.x) > 1f || abs(available.y) > 1f)
                ) {
                    dismissIfNeeded()
                }
                return Offset.Zero
            }
        }
    }

    this
        .nestedScroll(scrollDismissConnection)
        .pointerInput(controller, dismissIfNeeded) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Final)
                    val touchedOutsideInput = event.changes.any { change ->
                        change.changedToDownIgnoreConsumed() &&
                            !controller.isProtectedPointer(change.id)
                    }
                    if (touchedOutsideInput) {
                        dismissIfNeeded()
                    }
                }
            }
        }
}
