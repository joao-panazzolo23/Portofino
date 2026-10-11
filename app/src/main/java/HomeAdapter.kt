package com.portofino.launcher

import AppInfo
import Row
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeAdapter(
        private val onClick: (AppInfo) -> Unit,
        private val onLongClick: (AppInfo) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_SECTION = 1
        const val TYPE_APP = 2
    }

    var rows: List<Row> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = rows.size

    override fun getItemViewType(position: Int) =
            when (rows[position]) {
                is Row.Header -> TYPE_HEADER
                is Row.Section -> TYPE_SECTION
                is Row.App -> TYPE_APP
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val ctx = parent.context
        return if (viewType == TYPE_APP) createAppHolder(ctx) else createTextHolder(ctx, viewType)
    }

    override fun onBindViewHolder(h: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Header -> (h.itemView as TextView).text = row.text
            is Row.Section -> (h.itemView as TextView).text = row.text
            is Row.App -> (h as AppVH).bind(row.app)
        }
    }

    private fun createTextHolder(ctx: Context, type: Int): RecyclerView.ViewHolder {
        val tv =
                TextView(ctx).apply {
                    layoutParams = RecyclerView.LayoutParams(-1, -2)
                    if (type == TYPE_HEADER) {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f)
                        setTextColor(Color.WHITE)
                        setPadding(ctx.dp(8), ctx.dp(24), ctx.dp(8), ctx.dp(16))
                    } else {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                        setTextColor(Color.argb(160, 255, 255, 255))
                        setPadding(ctx.dp(8), ctx.dp(24), ctx.dp(8), ctx.dp(8))
                    }
                }
        return object : RecyclerView.ViewHolder(tv) {}
    }

    private fun createAppHolder(ctx: Context): AppVH {
        val icon =
                ImageView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(ctx.dp(64), ctx.dp(64))
                }
        val label =
                TextView(ctx).apply {
                    layoutParams = LinearLayout.LayoutParams(-1, -2).apply { topMargin = ctx.dp(8) }
                    gravity = Gravity.CENTER
                    setSingleLine()
                    ellipsize = TextUtils.TruncateAt.END
                    setTextColor(Color.WHITE)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                }
        val root =
                LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_HORIZONTAL
                    layoutParams =
                            RecyclerView.LayoutParams(-1, -2).apply {
                                setMargins(ctx.dp(8), ctx.dp(8), ctx.dp(8), ctx.dp(8))
                            }
                    setPadding(ctx.dp(12), ctx.dp(12), ctx.dp(12), ctx.dp(12))
                    isFocusable = true
                    isClickable = true
                    background = ctx.focusBackground()
                    addView(icon)
                    addView(label)
                }
        return AppVH(root, icon, label)
    }

    inner class AppVH(
            v: View,
            private val icon: ImageView,
            private val label: TextView,
    ) : RecyclerView.ViewHolder(v) {

        init {
            v.setOnFocusChangeListener { view, hasFocus ->
                val s = if (hasFocus) 1.08f else 1f
                view.animate().scaleX(s).scaleY(s).setDuration(120).start()
            }
        }

        fun bind(app: AppInfo) {
            icon.setImageDrawable(app.icon)
            label.text = app.label
            itemView.setOnClickListener { onClick(app) }
            itemView.setOnLongClickListener {
                onLongClick(app)
                true
            }
        }
    }
}
