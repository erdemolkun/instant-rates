package dynoapps.exchange_rates

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 * Saves a screenshot to the app's external files dir (`files/screenshots`), where the CI emulator
 * job pulls it from with adb.
 */
object Screenshots {
    fun take(name: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot() ?: return
        val dir = File(ApplicationProvider.getApplicationContext<App>().getExternalFilesDir(null), "screenshots")
        dir.mkdirs()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
