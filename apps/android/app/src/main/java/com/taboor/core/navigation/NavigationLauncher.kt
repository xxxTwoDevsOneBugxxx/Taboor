package com.taboor.core.navigation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

/**
 * Navigation apps supported for direct turn-by-turn routing.
 */
enum class NavigationApp(val id: String) {
    GOOGLE_MAPS("google_maps"),
    WAZE("waze"),
    SYSTEM_DEFAULT("system_default")
}

/**
 * Universal Navigation Launcher for opening turn-by-turn driving directions in external map apps.
 * Provides 1:1 parity with iOS `NavigationLauncher.swift`.
 */
object NavigationLauncher {

    const val PACKAGE_GOOGLE_MAPS = "com.google.android.apps.maps"
    const val PACKAGE_WAZE = "com.waze"

    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun isGoogleMapsInstalled(context: Context): Boolean =
        isAppInstalled(context, PACKAGE_GOOGLE_MAPS)

    fun isWazeInstalled(context: Context): Boolean =
        isAppInstalled(context, PACKAGE_WAZE)

    fun open(
        context: Context,
        app: NavigationApp,
        latitude: Double,
        longitude: Double,
        destinationName: String = "الوجهة"
    ) {
        when (app) {
            NavigationApp.GOOGLE_MAPS -> {
                try {
                    // Turn-by-turn driving navigation intent in Google Maps
                    val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage(PACKAGE_GOOGLE_MAPS)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(mapIntent)
                } catch (_: Exception) {
                    // Web directions fallback
                    val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=driving")
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(webIntent)
                    } catch (_: Exception) {}
                }
            }

            NavigationApp.WAZE -> {
                try {
                    val wazeUri = Uri.parse("waze://?ll=$latitude,$longitude&navigate=yes")
                    val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri).apply {
                        setPackage(PACKAGE_WAZE)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(wazeIntent)
                } catch (_: Exception) {
                    val webUri = Uri.parse("https://waze.com/ul?ll=$latitude,$longitude&navigate=yes")
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(webIntent)
                    } catch (_: Exception) {}
                }
            }

            NavigationApp.SYSTEM_DEFAULT -> {
                try {
                    val encodedLabel = Uri.encode(destinationName)
                    val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
                    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    val chooser = Intent.createChooser(intent, "اختر تطبيق الملاحة والخرائط").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(chooser)
                } catch (_: Exception) {
                    open(context, NavigationApp.GOOGLE_MAPS, latitude, longitude, destinationName)
                }
            }
        }
    }
}
