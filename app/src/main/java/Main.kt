package com.portofino.launcher

import AppInfo
import Row
import android.animation.ValueAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val io = Executors.newSingleThreadExecutor()
    private lateinit var favorites: FavoritesStore
    private lateinit var adapter: HomeAdapter
    private lateinit var sidebar: LinearLayout
    private lateinit var grid: RecyclerView

    private var allApps: List<AppInfo> = emptyList()
    private var sidebarExpanded = false
    private var sidebarAnim: ValueAnimator? = null

    private val collapsedPx by lazy { dp(72) }
    private val expandedPx by lazy { dp(240) }

    private val pkgReceiver =
            object : BroadcastReceiver() {
                override fun onReceive(c: Context?, i: Intent?) = reload()
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favorites = FavoritesStore(this)

        adapter =
                HomeAdapter(
                        onClick = { AppRepository.launch(this, it) },
                        onLongClick = {
                            favorites.toggle(it.pkg)
                            rebuildRows()
                        },
                )

        val root =
                FrameLayout(this).apply {
                    background =
                            GradientDrawable(
                                    GradientDrawable.Orientation.TL_BR,
                                    intArrayOf(
                                            Color.rgb(10, 10, 14),
                                            Color.rgb(24, 24, 36),
                                            Color.rgb(14, 18, 30),
                                    ),
                            )
                }

        val columns = 6
        val grid =
                RecyclerView(this).apply {
                    layoutParams = FrameLayout.LayoutParams(-1, -1)
                    setPadding(dp(96), 0, dp(32), 0)
                    clipToPadding = false
                    layoutManager =
                            GridLayoutManager(context, columns).apply {
                                spanSizeLookup =
                                        object : GridLayoutManager.SpanSizeLookup() {
                                            override fun getSpanSize(pos: Int) =
                                                    if (this@MainActivity.adapter.getItemViewType(
                                                                    pos
                                                            ) == HomeAdapter.TYPE_APP
                                                    )
                                                            1
                                                    else columns
                                        }
                            }
                    adapter = this@MainActivity.adapter
                    setHasFixedSize(true)
                    itemAnimator = null
                    setItemViewCacheSize(24)
                }

        sidebar =
                LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = FrameLayout.LayoutParams(collapsedPx, -1)
                    setPadding(0, dp(32), 0, 0)
                    setBackgroundColor(Color.rgb(28, 28, 34))
                    elevation = dp(8).toFloat()
                }

        sidebar.addView(
                SidebarItem(this, android.R.drawable.ic_menu_manage, "Settings") {
                    startActivity(
                            Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                },
        )
        sidebar.addView(SidebarItem(this, android.R.drawable.ic_menu_rotate, "Reload") { reload() })

        root.addView(grid)
        root.addView(sidebar)
        setContentView(root)

        root.viewTreeObserver.addOnGlobalFocusChangeListener { _, newFocus ->
            setSidebarExpanded(newFocus != null && isInside(newFocus, sidebar))
        }

        reload()
    }

    override fun onStart() {
        super.onStart()
        rebuildRows()
    }

    override fun onStop() {
        unregisterReceiver(pkgReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        unregisterReceiver(pkgReceiver)
        super.onDestroy()
    }

    @Deprecated("Deprecated in Java") override fun onBackPressed() {}

    private fun reload() {
        io.execute {
            val apps = AppRepository.load(this)
            runOnUiThread {
                allApps = apps
                rebuildRows()
            }
        }
    }

    private fun goHome() {
        grid.scrollToPosition(0)
        grid.post {
            (0 until grid.childCount)
                    .map { grid.getChildAt(it) }
                    .firstOrNull { it.isFocusable }
                    ?.requestFocus()
        }
    }

    private fun rebuildRows() {
        val favPkgs = favorites.get()
        val byPkg = allApps.associateBy { it.pkg }
        val favs = favPkgs.mapNotNull { byPkg[it] }
        val rest = allApps.filter { it.pkg !in favPkgs }

        val rows = ArrayList<Row>(allApps.size + 3)
        rows += Row.Header(greeting())
        if (favs.isNotEmpty()) {
            rows += Row.Section("Favorites")
            favs.mapTo(rows) { Row.App(it) }
        }
        rows += Row.Section("All apps")
        rest.mapTo(rows) { Row.App(it) }
        adapter.rows = rows
    }

    private fun greeting(): String {
        val part =
                when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                    in 5..11 -> "Good morning"
                    in 12..17 -> "Good afternoon"
                    else -> "Good evening"
                }
        return "$part, ${userName()}"
    }

    private fun userName(): String {
        getSharedPreferences("settings", MODE_PRIVATE).getString("name", null)?.let {
            return it
        }
        return Settings.Global.getString(contentResolver, "device_name") ?: "there"
    }

    private fun setSidebarExpanded(expand: Boolean) {
        if (expand == sidebarExpanded) return
        sidebarExpanded = expand

        sidebarAnim?.cancel()
        val from = sidebar.layoutParams.width
        val to = if (expand) expandedPx else collapsedPx
        sidebarAnim =
                ValueAnimator.ofInt(from, to).apply {
                    duration = 180
                    addUpdateListener {
                        val w = it.animatedValue as Int
                        sidebar.layoutParams.width = w
                        sidebar.requestLayout()
                        val frac = (w - collapsedPx).toFloat() / (expandedPx - collapsedPx)
                        for (i in 0 until sidebar.childCount) {
                            (sidebar.getChildAt(i) as SidebarItem).label.alpha = frac
                        }
                    }
                    start()
                }
    }

    private fun isInside(v: View, parent: View): Boolean {
        var p: View? = v
        while (p != null) {
            if (p === parent) return true
            p = p.parent as? View
        }
        return false
    }
}
