package com.make.it.better.compose

import android.annotation.SuppressLint
import android.util.Log
import androidx.animation.*
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.ui.animation.AnimatedFloatModel
import androidx.ui.animation.AnimatedValueModel
import androidx.ui.animation.IntSizeToVectorConverter
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.composed
import androidx.ui.core.drawWithContent
import androidx.ui.foundation.drawBackground
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.Color
import androidx.ui.graphics.StrokeCap
import androidx.ui.graphics.drawscope.Stroke
import androidx.ui.graphics.drawscope.rotate
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.IntSize


/**
 * Helper function to create a [Size] to be passed to the [Modifier.loadingSpinner]
 */
fun SpinnerSize(dimension: Float) = Size(dimension, dimension)

/**
 * Collection of common spinner dimensions,
 */
object SpinnerSize{

    /**
     * [Size] of 40F
     */
    val Small = SpinnerSize(40F)

    /**
     * [Size] of 80F
     */
    val Medium = SpinnerSize(80F)

    /**
     * [Size] of 120F
     */
    val Big = SpinnerSize(120F)

    /**
     * [Size] of 160F
     */
    val Bigger = SpinnerSize(160F)

    /**
     * Expands the spinner dimensions to the dimension of the element is applied
     */
    val FillElement = SpinnerSize(0F)
}

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
fun Modifier.loadingSpinner(loading: Boolean, color: Color? = null, width: Float = 16F, size: Size = SpinnerSize.Medium): Modifier = composed {

    check(size.height == size.width) { "Spinner size must have the same height and width, received size: $size" }


    val clock = AnimationClockAmbient.current.asDisposableClock()
    val rotationAnimation = remember {
        AnimatedFloatModel(0F, clock)
    }


    val strokeColor = color ?: MaterialTheme.colors.primary

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

    var currentOffSet = remember { -30 }
    var incremented = remember{ false }


    this + if(loading) {
        Modifier.drawWithContent {

            Log.d("ANIMATION MEASURES", "current Rotation: ${rotationAnimation.value}, current Arc: ${arcAnimation.value}")
            rotate(-rotationAnimation.value, center.x, center.y) {

                val (offset, usedSize) = if (size == SpinnerSize.FillElement) {
                    val smallerDimen = (this@drawWithContent.size).minDimension
                    Pair(
                        center.minus(Offset(smallerDimen / 2, smallerDimen / 2)),
                        SpinnerSize(smallerDimen)
                    )
                } else {
                    Pair(center.minus(Offset(size.width / 2, size.height / 2)), size)
                }

                if (arcAnimation.value.width == 0) {
                    if (!incremented) {
                        incremented = true
                        currentOffSet -= 105
                        currentOffSet %= 360
                    }
                }
                if (arcAnimation.value.width != 0) {
                    incremented = false
                }

                val startAngle = arcAnimation.value.width - currentOffSet
                val sweepAngle = arcAnimation.value.height
                drawArc(
                    color = strokeColor,
                    startAngle = startAngle.toFloat(),
                    sweepAngle = sweepAngle.toFloat(),
                    useCenter = false,
                    topLeft = offset,
                    size = usedSize,
                    style = Stroke(width = width, cap = StrokeCap.round)
                )
            }
        }
    } else {
        Modifier
    }
}

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
 * @param dimension desired size of the spinner default to 80F which is the dimension of the [SpinnerSize.Medium]
 *
 * @see SpinnerSize
 */
fun Modifier.loadingSpinner(loading: Boolean, color: Color? = null, width: Float = 16F, dimension: Float = 80F) = loadingSpinner(loading, color, width, SpinnerSize(dimension))

