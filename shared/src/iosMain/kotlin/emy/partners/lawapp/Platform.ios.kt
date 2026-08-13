package emy.partners.lawapp

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun LockSystemBack(enabled: Boolean) {
    // iOS n'a pas de bouton retour materiel; les menus sont masques pendant l'evaluation.
}