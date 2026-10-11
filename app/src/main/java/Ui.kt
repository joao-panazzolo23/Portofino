package com.portofino.launcher

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()

/** Transparent normally, translucent white when focused. */
fun Context.focusBackground(): StateListDrawable {
    fun shape(color: Int) =
            GradientDrawable().apply {
                setColor(color)
                cornerRadius = dp(12).toFloat()
            }
    return StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), shape(0x33FFFFFF))
        addState(intArrayOf(), shape(Color.TRANSPARENT))
    }
}

class SidebarItem(
        ctx: Context,
        iconRes: Int,
        text: String,
        onClick: () -> Unit,
) : LinearLayout(ctx) {

    val label =
            TextView(ctx).apply {
                this.text = text
                setSingleLine()
                ellipsize = TextUtils.TruncateAt.END
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                alpha = 0f
                layoutParams = LayoutParams(-2, -2).apply { marginStart = ctx.dp(20) }
            }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        layoutParams = LayoutParams(-1, ctx.dp(56))
        setPadding(ctx.dp(20), 0, 0, 0)
        isFocusable = true
        isClickable = true
        background = ctx.focusBackground()
        setOnClickListener { onClick() }

        addView(
                ImageView(ctx).apply {
                    setImageResource(iconRes)
                    layoutParams = LayoutParams(ctx.dp(32), ctx.dp(32))
                }
        )
        addView(label)
    }
}
