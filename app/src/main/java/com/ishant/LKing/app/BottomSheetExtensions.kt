package com.ishant.LKing.app

import android.app.Activity
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetDefaults
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.contains
import androidx.fragment.app.Fragment
import com.ishant.LKing.utils.AppPreference

import com.ishant.corelibcompose.toolkit.ui.bottomsheets.BottomSheetBack.Fixed
import com.ishant.corelibcompose.toolkit.ui.bottomsheets.BottomSheetBack.Floating
import com.ishant.corelibcompose.toolkit.ui.sdp.sdp
import com.ishant.corelibcompose.toolkit.ui.theme.CoreTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

var bottomSheetView: ComposeView? = null

fun Activity.showAsBottomSheet(
    wrapWithNewBottomSheetUI: Boolean = true,
    shouldWrap: Boolean = true,
    isDashboard: Boolean = false,
    isDismissAble: Boolean = true,
    skipHalf: Boolean = true,
    isBackColorDarkGrey: Boolean = false,
    onSwipeDownDismiss: (() -> Unit)? = null,
    swipeDownDismiss: ((Boolean) -> Unit)? = null,
    content: @Composable (() -> Unit) -> Unit
) {
    val viewGroup: ViewGroup = this.findViewById(android.R.id.content)
    addContentToView(
        wrapWithNewBottomSheetUI = wrapWithNewBottomSheetUI,
        shouldWrap = shouldWrap,
        viewGroup = viewGroup,
        isDashboard = isDashboard,
        isBackColorDarkGrey = isBackColorDarkGrey,
        isDismissAble = isDismissAble,
        skipHalf = skipHalf,
        swipeDownDismiss = swipeDownDismiss,
        onSwipeDownDismiss = onSwipeDownDismiss,
        content = content
    )
}

fun Fragment.showAsBottomSheet(
    wrapWithNewBottomSheetUI: Boolean = false,
    shouldWrap: Boolean = true,
    isDashboard: Boolean = false,
    isDismissAble: Boolean = true,
    skipHalf: Boolean = true,
    onSwipeDownDismiss: (() -> Unit)? = null,
    isBackColorDarkGrey: Boolean = false,
    swipeDownDismiss: ((Boolean) -> Unit)? = null,
    content: @Composable (() -> Unit) -> Unit
) {
    val viewGroup: ViewGroup = requireActivity().findViewById(android.R.id.content)
    addContentToView(
        wrapWithNewBottomSheetUI = wrapWithNewBottomSheetUI,
        shouldWrap = shouldWrap,
        viewGroup = viewGroup,
        isDashboard = isDashboard,
        isBackColorDarkGrey = isBackColorDarkGrey,
        onSwipeDownDismiss = onSwipeDownDismiss,
        isDismissAble = isDismissAble,
        skipHalf = skipHalf,
        swipeDownDismiss = swipeDownDismiss,
        content = content
    )
}

fun Activity.removeBottomSheet() {
    val viewGroup: ViewGroup = this.findViewById(android.R.id.content)
    bottomSheetView?.let {
        if (viewGroup.contains(it)) {
            viewGroup.removeView(it)
        }
    }
    bottomSheetView = null
}

private fun addContentToView(
    wrapWithNewBottomSheetUI: Boolean,
    shouldWrap: Boolean,
    viewGroup: ViewGroup,
    isDashboard: Boolean,
    isBackColorDarkGrey: Boolean,
    isDismissAble: Boolean,
    skipHalf: Boolean = true,
    onSwipeDownDismiss: (() -> Unit)? = null,
    swipeDownDismiss: ((Boolean) -> Unit)? = null,
    content: @Composable (() -> Unit) -> Unit
) {
    bottomSheetView = ComposeView(viewGroup.context).apply {
        setContent {
            CoreTheme(
                darkTheme = AppPreference.isDarkModeEnable
            ) {
                BottomSheetWrapper(
                    wrapWithNewBottomSheetUI = wrapWithNewBottomSheetUI,
                    shouldWrap = shouldWrap,
                    parent = viewGroup,
                    composeView = this,
                    isDashboard = isDashboard,
                    isBackColorDarkGrey = isBackColorDarkGrey,
                    isDismissAble = isDismissAble,
                    skipHalf = skipHalf,
                    onSwipeDownDismiss = onSwipeDownDismiss,
                    swipeDownDismiss = swipeDownDismiss,
                    content = content
                )
            }
        }
    }
    viewGroup.addView(
        bottomSheetView
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BottomSheetWrapper(
    wrapWithNewBottomSheetUI: Boolean,
    shouldWrap: Boolean,
    parent: ViewGroup,
    composeView: ComposeView,
    isDashboard: Boolean,
    isBackColorDarkGrey: Boolean,
    isDismissAble: Boolean,
    skipHalf: Boolean = true,
    onSwipeDownDismiss: (() -> Unit)? = null,
    swipeDownDismiss: ((Boolean) -> Unit)? = null,
    content: @Composable (() -> Unit) -> Unit
) {
    val TAG = parent::class.java.simpleName
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState =
        rememberModalBottomSheetState(
            ModalBottomSheetValue.Hidden,
            confirmValueChange = {
                if (isDismissAble) {
                    true
                } else {
                    it != ModalBottomSheetValue.Hidden
                }
            },
            skipHalfExpanded = skipHalf
        )
    var isSheetOpened by remember { mutableStateOf(false) }

    ModalBottomSheetLayout(
        sheetGesturesEnabled = isDismissAble,
        sheetShape = if (!shouldWrap) RectangleShape else RoundedCornerShape(
            topStart = 16.sdp,
            topEnd = 16.sdp
        ),
        sheetBackgroundColor = Color.Transparent,
        scrimColor = Color.Transparent.copy(alpha = 0.65f),
        modifier = Modifier
            .statusBarsPadding(),
        sheetElevation = if (wrapWithNewBottomSheetUI) 0.sdp else ModalBottomSheetDefaults.Elevation,
        sheetState = modalBottomSheetState,
        sheetContent = {
            if (!shouldWrap) {
                content {
                    animateHideBottomSheet(coroutineScope, swipeDownDismiss, modalBottomSheetState)
                }
            } else if (wrapWithNewBottomSheetUI) {
                Floating {
                    content {
                        animateHideBottomSheet(
                            coroutineScope,
                            swipeDownDismiss,
                            modalBottomSheetState
                        )
                    }
                }
            } else {
                Fixed(
                    modifier = Modifier
                        .padding(top = 4.sdp),
                    isDashboard = isDashboard,
                    isBackColorDarkGrey = isBackColorDarkGrey
                ) {
                    content {
                        animateHideBottomSheet(
                            coroutineScope,
                            swipeDownDismiss,
                            modalBottomSheetState
                        )
                    }
                }
            }
        }
    ) {}

    BackHandler {
        if (isDismissAble) {
            animateHideBottomSheet(coroutineScope, swipeDownDismiss, modalBottomSheetState)
        }
    }

    if (modalBottomSheetState.currentValue != ModalBottomSheetValue.Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                onSwipeDownDismiss?.invoke()
            }
        }
    }

    // Take action based on hidden state
    LaunchedEffect(modalBottomSheetState.currentValue) {
        when (modalBottomSheetState.currentValue) {
            ModalBottomSheetValue.Hidden -> {
                when {
                    isSheetOpened -> {
                        swipeDownDismiss?.invoke(true)
                        parent.removeView(composeView)
                    }

                    else -> {
                        swipeDownDismiss?.invoke(false)
                        isSheetOpened = true
                        modalBottomSheetState.show()
                    }
                }
            }

            else -> {

            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun animateHideBottomSheet(
    coroutineScope: CoroutineScope,
    swipeDownDismiss: ((Boolean) -> Unit)? = null,
    modalBottomSheetState: ModalBottomSheetState
) {
    coroutineScope.launch {
        swipeDownDismiss?.invoke(true)
        modalBottomSheetState.hide() // will trigger the LaunchedEffect
    }
}