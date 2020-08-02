package com.make.it.better.compose

import android.annotation.SuppressLint
import android.util.Log
import androidx.animation.*
import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.ui.animation.AnimatedFloatModel
import androidx.ui.animation.AnimatedValueModel
import androidx.ui.animation.IntSizeToVectorConverter
import androidx.ui.animation.asDisposableClock
import androidx.ui.core.AnimationClockAmbient
import androidx.ui.core.ContentDrawScope
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.Color
import androidx.ui.graphics.StrokeCap
import androidx.ui.graphics.drawscope.Stroke
import androidx.ui.graphics.drawscope.rotate
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.Dp
import androidx.ui.unit.IntSize

@SuppressLint("Range")
@Composable
internal fun drawLoadingWithControllOnAnimation(
    strokeColor: Color? = null,
    size: Dp = SpinnerSize.Medium,
    strokeWidth: Float = 16F
): Pair<@Composable () -> Unit, ContentDrawScope.(center: Offset?, startingRotation: Float?) -> Unit> {
    val clock = AnimationClockAmbient.current.asDisposableClock()
    var incremented = remember { false }
    var currentOffSet = remember { 105 }

    val arcSpec = remember {  repeatable<IntSize>(AnimationConstants.Infinite, keyframes {
        durationMillis = 1400
        IntSize(0, 270).at(0)
        IntSize(0,270).at(200)


        IntSize(0, 15).at(800).with(FastOutSlowInEasing)
        IntSize(0, 15).at(1000)
        IntSize(-255, 270).at(1400).with(FastOutSlowInEasing)
    })
    }
    val actualStrokeColor = strokeColor ?: MaterialTheme.colors.primary

    val rotationSpec = remember { repeatable<Float>(AnimationConstants.Infinite, tween(1300, easing = LinearEasing)) }

    val rotationAnimation = remember {
        AnimatedFloatModel(0F, clock)
    }

    val arcAnimation = remember {
        AnimatedValueModel(IntSize(0, 270), IntSizeToVectorConverter, clock, IntSize(1,1))
    }

    return Pair({ onCommit {
        Log.d("ANIMATION STARTING", "starting animation")
        rotationAnimation.animateTo(360F, rotationSpec)
        arcAnimation.animateTo(IntSize(-255, 270), arcSpec)

        onDispose {
            Log.d("ANIMATION STOPPING", "stopping animation")
            rotationAnimation.snapTo(0F)
            rotationAnimation.stop()
            arcAnimation.snapTo(IntSize(0, 270))
            arcAnimation.stop()

        }
    }
    },{ center: Offset?, startingRotation: Float? ->
        val sizeInPixels = (size.toPx()).let { Size(it, it) }
        val usedCenter = center ?: this.center

        val rotationAngle = (rotationAnimation.value + (startingRotation ?: 0f))
        rotate(rotationAngle, usedCenter.x, usedCenter.y) {

            val (offset, usedSize) = if (size == SpinnerSize.FillElement) {
                val smallerDimen = (this.size).minDimension
                (smallerDimen - strokeWidth).let {
                    Pair(
                        usedCenter.minus(Offset(it / 2, it / 2)),
                        Size(it, it)
                    )
                }
            } else {
                Pair(
                    usedCenter.minus(Offset(sizeInPixels.width / 2, sizeInPixels.height / 2)),
                    sizeInPixels
                )
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

            val startAngle = -(arcAnimation.value.width - currentOffSet) + 30
            val actualSweepAngle = -arcAnimation.value.height

            Log.d("STARTANGLE", "arcAnimation Value: ${arcAnimation.value.width}, currentOffset: $currentOffSet, startingRotation: $startingRotation")
            Log.d("DRAW LOADING", "startAngle $startAngle, sweepAngle $actualSweepAngle")
            drawArc(
                color = actualStrokeColor,
                startAngle = startAngle.toFloat(),
                sweepAngle = actualSweepAngle.toFloat(),
                useCenter = false,
                topLeft = offset,
                size = usedSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.round)
            )
        }
    })
}

@Composable
internal fun drawLoadingSpinner(
    strokeColor: Color? = null,
    size: Dp = SpinnerSize.Medium,
    strokeWidth: Float = 16F
): ContentDrawScope.(center: Offset?, startingRotation: Float?) -> Unit {

    val (start, ret) = drawLoadingWithControllOnAnimation(strokeColor, size, strokeWidth)

    start()

    return ret

}