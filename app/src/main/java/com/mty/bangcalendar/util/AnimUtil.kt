package com.mty.bangcalendar.util

object AnimUtil {

    private const val ANIM_SLOW = 800
    private const val ANIM_FAST = 450
    private var animPreference = ANIM_FAST

    fun setAnimPreference(isPreference: Boolean) {
        animPreference = if (isPreference) ANIM_SLOW else ANIM_FAST
    }

    fun getAnimPreference() = animPreference

}