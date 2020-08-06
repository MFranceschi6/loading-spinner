package com.make.it.better.compose

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedFloatModel
import androidx.compose.animation.AnimatedValueModel
import androidx.compose.animation.VectorConverter
import androidx.compose.animation.asDisposableClock
import androidx.compose.animation.core.*
import androidx.compose.foundation.drawBackground
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.platform.DensityAmbient
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp


/**
 * Collection of common spinner dimensions,
 */
object SpinnerSize {

    /**
     * [Dp] of 20F.dp
     */
    val Small = 20F.dp

    /**
     * [Dp] of 40F.dp
     */
    val Medium = 40F.dp

    /**
     * [Dp] of 60F.dp
     */
    val Big = 60F.dp

    /**
     * [Dp] of 60F.dp
     */
    val Bigger = 80F.dp

    /**
     * Expands the spinner dimensions to the dimension of the element is applied
     */
    val FillElement = 0F.dp
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
fun Modifier.loadingSpinner(loading: Boolean, color: Color? = null, width: Float = 16F, size: Dp = SpinnerSize.Medium): Modifier = composed {


    val clock = AnimationClockAmbient.current.asDisposableClock()
    val rotationAnimation = remember {
        AnimatedFloatModel(0F, clock)
    }

    val sizeInPixels = with(DensityAmbient.current) { size.toPx() }.let { remember(size) { Size(it, it) } }

    val strokeColor = color ?: MaterialTheme.colors.primary

    val rotationSpec = remember { repeatable<Float>(AnimationConstants.Infinite, tween(1300, easing = LinearEasing)) }


    val arcAnimation = remember {
        AnimatedValueModel(IntSize(0, 270), IntSize.VectorConverter, clock, IntSize(1, 1))
    }


    val arcSpec = remember {
        repeatable<IntSize>(AnimationConstants.Infinite, keyframes {
            durationMillis = 1400
            IntSize(0, 270).at(0)
            IntSize(0, 270).at(200)


            IntSize(0, 15).at(800).with(FastOutSlowInEasing)
            IntSize(0, 15).at(1000)
            IntSize(-255, 270).at(1400).with(FastOutSlowInEasing)
        })
    }

    onCommit {
        rotationAnimation.animateTo(360F, rotationSpec)
        arcAnimation.animateTo(IntSize(-100, 230), arcSpec)

        onDispose {
            rotationAnimation.snapTo(0F)
            rotationAnimation.stop()
            arcAnimation.snapTo(IntSize(0, 270))
            arcAnimation.stop()
        }
    }

    var currentOffSet = remember { -30 }
    var incremented = remember { false }


    if (loading) {
        Modifier.drawWithContent {

            rotate(-rotationAnimation.value, center.x, center.y) {

                val (offset, usedSize) = if (size == SpinnerSize.FillElement) {
                    val smallerDimen = (this@drawWithContent.size).minDimension
                    (smallerDimen - width).let {
                        Pair(
                                center.minus(Offset(it / 2, it / 2)),
                                Size(it, it)
                        )
                    }
                } else {
                    Pair(center.minus(Offset(sizeInPixels.width / 2, sizeInPixels.height / 2)), sizeInPixels)
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
                        style = Stroke(width = width, cap = StrokeCap.Round)
                )
            }
        }
    } else {
        Modifier
    }

}
