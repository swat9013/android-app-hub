package io.github.swat9013.apphub

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

class LaunchActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val targetPackage = DeepLink.extractPackageName(intent?.dataString)
        if (targetPackage == null) {
            Toast.makeText(this, getString(R.string.usage), Toast.LENGTH_LONG).show()
        } else {
            val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                startActivity(launchIntent)
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.app_not_found, targetPackage),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
        finish()
    }
}
