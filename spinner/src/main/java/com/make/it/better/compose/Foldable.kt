package com.make.it.better.compose

import androidx.ui.core.CombinedModifier
import androidx.ui.core.Modifier

internal class Foldable(
    private val inner: Modifier,
    private val outer: Modifier
): Modifier {
    override fun all(predicate: (Modifier.Element) -> Boolean): Boolean = inner.all(predicate) && outer.all(predicate)

    override fun any(predicate: (Modifier.Element) -> Boolean): Boolean = inner.any(predicate) || outer.any(predicate)

    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R =
        inner.foldIn(outer.foldIn(initial, operation), operation)

    override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R =
        outer.foldOut(inner.foldOut(initial, operation), operation)

    override operator fun plus(other: Modifier): Modifier {
        // Log.d("PLUS ON FOLDABLE", "i'm $this, other is $other")

        return when{
            other === Modifier -> this
            other is Unfold -> Foldable(other.element, this)
            else -> CombinedModifier(other, this)
        }
    }

    override fun equals(other: Any?): Boolean =
        other is Foldable && outer == other.outer && inner == other.inner

    override fun hashCode(): Int = inner.hashCode() + 31 * outer.hashCode()

    override fun toString() = "[" + foldIn("") { acc, element ->
        if (acc.isEmpty()) element.toString() else "$acc, $element"
    } + "]"

    class Element: Modifier.Element {
        override operator fun plus(other: Modifier): Modifier {
            //    Log.d("PLUS ON FOLDABLE Elem", "i'm $this, other is $other")
            return if (other === Modifier) this else Foldable(this, other)
        }
    }

    class Unfold(val element: Modifier) : Modifier.Element {

    }
}

internal fun Modifier.foldable(): Modifier = Foldable.Element() + this

internal fun Modifier.unfold(): Modifier = Foldable.Unfold(this)