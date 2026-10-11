import android.content.ComponentName
import android.graphics.drawable.Drawable

public data class AppInfo(
        val label: String,
        val pkg: String,
        val component: ComponentName,
        val icon: Drawable,
)
