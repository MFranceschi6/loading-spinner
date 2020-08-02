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
import androidx.ui.unit.IntSize
import androidx.ui.unit.Dp



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
 * @param color color to apply to the spinner, if null the primary color taken from [MaterialTheme.colors] will be used
 * @param width width of the stroke to draw the spinner
 * @param size desired size of the spinner default to [SpinnerSize.Medium]
 *
 * @see SpinnerSize
 */
@SuppressLint("Range")
fun Modifier.loadingSpinner(loading: Boolean, color: Color? = null, width: Float = 16F, size: Dp = SpinnerSize.Medium): Modifier = composed {


    val clock = AnimationClockAmbient.current.asDisposableClock()
    val rotationAnimation = remember {
        AnimatedFloatModel(0F, clock)
    }

    val drawSpinner = drawLoadingSpinner(color, strokeWidth = width, size = size)

    val rotationSpec = remember { repeatable<Float>(AnimationConstants.Infinite, tween(1300, easing = LinearEasing)) }


    val arcAnimation = remember {
        AnimatedValueModel(IntSize(0, 270), IntSizeToVectorConverter, clock, IntSize(1,1))
    }


    val arcSpec = remember {  repeatable<IntSize>(AnimationConstants.Infinite, keyframes {
        durationMillis = 1400
        IntSize(0, 270).at(0)
        IntSize(0,270).at(200)


        IntSize(0, 15).at(800).with(FastOutSlowInEasing)
        IntSize(0, 15).at(1000)
        IntSize(-255, 270).at(1400).with(FastOutSlowInEasing)
    })}

    onCommit {
        rotationAnimation.animateTo(360F, rotationSpec)
        arcAnimation.animateTo(IntSize(-100,230), arcSpec)

        onDispose {
            rotationAnimation.snapTo(0F)
            rotationAnimation.stop()
            arcAnimation.snapTo(IntSize(0, 270))
            arcAnimation.stop()
        }
    }


    if(loading) {
        Modifier.drawWithContent {
            drawSpinner(null, null)
        }
    } else {
        Modifier
    }

}
