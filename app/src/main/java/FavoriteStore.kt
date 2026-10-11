package com.portofino.launcher

import AppInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object AppRepository {
    fun load(ctx: Context): List<AppInfo> {
        val pm = ctx.packageManager
        fun query(category: String) =
                pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category), 0)

        return (query(Intent.CATEGORY_LEANBACK_LAUNCHER) + query(Intent.CATEGORY_LAUNCHER))
                .filter { it.activityInfo.packageName != ctx.packageName }
                .distinctBy { it.activityInfo.packageName }
                .map {
                    AppInfo(
                            label = it.loadLabel(pm).toString(),
                            pkg = it.activityInfo.packageName,
                            component =
                                    ComponentName(
                                            it.activityInfo.packageName,
                                            it.activityInfo.name
                                    ),
                            icon = it.loadIcon(pm),
                    )
                }
                .sortedBy { it.label.lowercase() }
    }

    fun launch(ctx: Context, app: AppInfo) {
        val intent =
                Intent(Intent.ACTION_MAIN)
                        .setComponent(app.component)
                        .addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        )
        try {
            ctx.startActivity(intent)
        } catch (_: Exception) {}
    }
}

class FavoritesStore(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("favorites", Context.MODE_PRIVATE)

    fun get(): List<String> = prefs.getString("list", "")!!.split(',').filter { it.isNotEmpty() }

    fun toggle(pkg: String) {
        val list = get().toMutableList()
        if (!list.remove(pkg)) list.add(pkg)
        prefs.edit().putString("list", list.joinToString(",")).apply()
    }
}
