package org.example.project

import android.app.Application
import org.osmdroid.config.Configuration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Must be called before any MapView is created
        Configuration.getInstance().apply {
            userAgentValue = packageName
            // Set a cache path to speed up tile loading
            osmdroidBasePath = getExternalFilesDir(null)
            osmdroidTileCache = getExternalFilesDir(null)
        }
    }
}
