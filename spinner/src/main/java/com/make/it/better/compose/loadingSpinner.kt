package com.make.it.better.compose

import android.annotation.SuppressLint
import androidx.animation.*
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.ui.animation.AnimatedFloatModel
import androidx.ui.animation.AnimatedValueModel
import androidx.ui.animation.IntSizeToVectorConverter
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.*
import androidx.ui.foundation.drawBackground
import androidx.ui.graphics.Color
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.*


/**
 * Configure component to display the Android loading spinner if [loading]
 *
 * Add this [Modifier] to the element to make it show a loading spinner instead of the content of the element,
 * set [loading] to false to show the content of the element instead.
 *
 * It's important to set any drawing modifier you are interested in for example [Modifier.drawBackground] before this modifier,
 * otherwise it will prevent them to be shown.
 *
 * example:
 * ```
 * Surface(modifier = Modifier.drawBackground(Color.Red).loadingSpinner(isLoading).drawBackgrount(Color.Green)){
 *
 * }
 * ```
 * This will show a [Color.Red] background while loading and then a [Color.Green] background to show it's elements
 *
 * @param loading control if to show or not the spinner
 * @param strokeColor color to apply to the spinner, if null the primary color taken from [MaterialTheme.colors] will be used
 * @param strokeWidth width of the stroke to draw the spinner
 * @param spinnerSize desired size of the spinner default to [SpinnerSize.Medium]
 *
 * @see SpinnerSize
 */
fun Modifier.loadingSpinner(loading: Boolean, strokeColor: Color? = null, strokeWidth: Dp = 6.1f.dp, spinnerSize: Dp = SpinnerSize.Medium): Modifier = composed {

    val drawSpinner = drawLoadingSpinner(strokeColor, strokeWidth = strokeWidth, size = spinnerSize)
    if(loading) {
        Modifier.drawWithContent {
            drawSpinner(null, null)
        }
    } else {
        Modifier
    }

}
