package com.make.it.better.compose

import android.util.Log
import androidx.animation.spring
import androidx.compose.*
import androidx.ui.animation.animatedFloat
import androidx.ui.core.*
import androidx.ui.core.gesture.DragObserver
import androidx.ui.core.gesture.ScrollCallback
import androidx.ui.core.gesture.dragGestureFilter
import androidx.ui.core.gesture.scrollGestureFilter
import androidx.ui.core.gesture.scrollorientationlocking.Orientation
import androidx.ui.foundation.Box
import androidx.ui.geometry.Offset
import androidx.ui.geometry.Size
import androidx.ui.graphics.Color
import androidx.ui.graphics.Path
import androidx.ui.graphics.StrokeCap
import androidx.ui.graphics.drawscope.*
import androidx.ui.material.MaterialTheme
import androidx.ui.unit.Density
import androidx.ui.unit.dp
import kotlinx.coroutines.launch

private enum class AnimationState{
    Idle, Dragging, Loading, Springing
}

@Composable
fun Refreshable(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    circleColor: Color = MaterialTheme.colors.surface,
    spinnerColor: Color = MaterialTheme.colors.onSurface,
    children: @Composable (modifier: Modifier) -> Unit){


    val animationLimit = ContextAmbient.current.resources.displayMetrics.heightPixels / 2f - ContextAmbient.current.resources.displayMetrics.heightPixels / 4.7f
    val animationLimitForCancellation = with(DensityAmbient.current){ remember { 110f.dp.toPx() } }
    val animationPositionForLoading = with(DensityAmbient.current){ remember { 105f.dp.toPx() } }

    val animation = animatedFloat(initVal = 0F)
    var animationState by state { AnimationState.Idle }


    Log.d("TARGET FOR LOADING", animationPositionForLoading.toString())
    var finalAnimationValue by state { 0f }

    val (startAnimation, drawAnimation) = drawLoadingWithControllOnAnimation(strokeColor = spinnerColor, size = 20.dp, strokeWidth = 10f)

    fun updateAnimation(distance: Float) {
        animation.snapTo((animation.targetValue + distance).coerceIn(0f, animationLimit))
    }

    fun cancelAnimation(){
        animation.snapTo(0f)
    }

    if(animationState == AnimationState.Loading){
        startAnimation()
        launchInComposition {
            onRefresh()
            animationState = AnimationState.Idle
        }
    }

    val coroutineScope = rememberCoroutineScope()

    fun concludeAnimation(){
        when {
            animation.value <= animationLimitForCancellation -> {
                animationState = AnimationState.Idle
                animation.snapTo(0f)
            }
            else -> {
                animationState = AnimationState.Springing
                finalAnimationValue = animation.value
                animation.animateTo(animationPositionForLoading, spring()){ _, _ ->
                    coroutineScope.launch {
                        kotlinx.coroutines.delay(40)
                        animationState = AnimationState.Loading
                    }
                }
            }
        }
    }

    val dragObserver = remember {
        object : DragObserver {

            override fun onCancel() {
                super.onCancel()
                cancelAnimation()
            }

            override fun onDrag(dragDistance: Offset): Offset {
                updateAnimation(dragDistance.y)
                return dragDistance
            }

            override fun onStop(velocity: Offset) {
                concludeAnimation()
            }
        }
    }


    val animationStarter = remember {
        object : ScrollCallback {

            override fun onStart(downPosition: Offset) {
                super.onStart(downPosition)
                if(animationState != AnimationState.Loading)
                    animation.snapTo(0f)
            }

            override fun onScroll(scrollDistance: Float): Float {
                animationState = AnimationState.Dragging
                updateAnimation(scrollDistance)
                return scrollDistance
            }

            override fun onStop(velocity: Float) {
                super.onStop(velocity)
                concludeAnimation()
            }
        }
    }

    val scrollObserver = remember {
        object : ScrollCallback {

            override fun onScroll(scrollDistance: Float): Float {
                updateAnimation(scrollDistance)
                return scrollDistance
            }

            override fun onStop(velocity: Float) {
                super.onStop(velocity)
                concludeAnimation()
            }
        }
    }


    val childModifier = remember { Modifier
        .scrollGestureFilter(scrollObserver, Orientation.Vertical, canDrag = {animationState == AnimationState.Dragging})
        .dragGestureFilter(dragObserver, canDrag = {animationState == AnimationState.Dragging}).foldable()
    }
    Box(
        modifier = modifier.scrollGestureFilter(
            animationStarter,
            Orientation.Vertical
        ) + Modifier.clipToBounds().drawWithContent {
            drawContent()
            val (startX, startY) = Pair(toPx(-2f), toPx(8.5f))
            val arrowPath = with(Path()) {
                moveTo(startX, startY)
                lineTo(toPx(-3f), toPx(9.5f))
                lineTo(toPx(-2f), toPx(10.5f))
                lineTo(startX, startY)
                close()
                this
            }
            val center = Offset(center.x, (toPx(-60f) + animation.value).coerceIn(toPx(-60f), animationLimit))
            val (rotationAngle, angle, alpha) =  when(animationState){
                AnimationState.Dragging -> Triple(
                    (animation.value / animationLimit  * 240f) - 120f,
                    getAnimationValue(animation.value, 60f, 110f) * 300f,
                    getAnimationValue(animation.value, 40f, 110f)
                )
                else -> Triple(
                    (finalAnimationValue / animationLimit  * 240f) - 120f,
                    getAnimationValue(finalAnimationValue, 60f, 110f) * 300f,
                    1f
                )
            }


            if(animationState != AnimationState.Idle)
                drawCircle(
                    circleColor,
                    radius = toPx(20f),
                    center = center
                )
            when(animationState){
                AnimationState.Dragging, AnimationState.Springing -> {
                    rotate(rotationAngle, center.x, center.y) {
                        drawArc(
                            spinnerColor,
                            startAngle = 90f,
                            sweepAngle = angle,
                            useCenter = false,
                            topLeft = center.minus(Offset(toPx(10f), toPx(10f))),
                            alpha = alpha,
                            style = Stroke(width = 10f, cap = StrokeCap.square),
                            size = Size(toPx(20f), toPx(20f))
                        )

                        if(animationState == AnimationState.Dragging)
                            translate(center.x, center.y) {
                                rotate(degrees = angle, 0f, 0f) {
                                    scale(angle / 270f, angle / 270f, toPx(-2f), toPx(10f)) {
                                        drawPath(
                                            arrowPath,
                                            spinnerColor,
                                            alpha = alpha,
                                            style = Stroke(width = 10F)
                                        )
                                    }
                                }
                            }
                    }
                }
                AnimationState.Loading -> {
                    drawAnimation(center, rotationAngle)
                }
                else -> {}
            }
        }
    ){
        children(childModifier)
    }
}

fun Density.toPx(value: Float): Float{
    return value.dp.toPx()
}


fun Density.getAnimationValue(currentValue: Float, firstExtreme: Float, secondExtreme: Float): Float {
    return (currentValue.coerceIn(toPx(firstExtreme), toPx(secondExtreme)) - toPx(firstExtreme)) / toPx(secondExtreme - firstExtreme)
}