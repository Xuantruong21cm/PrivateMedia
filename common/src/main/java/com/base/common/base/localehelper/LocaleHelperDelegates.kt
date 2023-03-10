package com.base.common.base.localehelper

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.LocaleHelperAppCompatDelegate
import com.zeugmasolutions.localehelper.LocaleHelper
import java.util.*

interface LocaleHelperActivityDelegate {
    fun setLocale(activity: Activity, newLocale: Locale)
    fun attachBaseContext(newBase: Context): Context
    fun onPaused()
    fun onResumed(activity: Activity)
    fun onCreate(activity: Activity)
    fun getApplicationContext(applicationContext: Context): Context
    fun getAppCompatDelegate(delegate: AppCompatDelegate): AppCompatDelegate
}

class LocaleHelperActivityDelegateImpl : LocaleHelperActivityDelegate{

    private var locale = Locale.getDefault()
    private var appCompatDelegate : AppCompatDelegate? = null
    override fun setLocale(activity: Activity, newLocale: Locale) {
        LocaleHelper.setLocale(activity, newLocale)
        locale = newLocale
        activity.recreate()
    }

    override fun attachBaseContext(newBase: Context): Context {
        return LocaleHelper.onAttach(newBase)
    }

    override fun onPaused() {
        locale = Locale.getDefault()
    }

    override fun onResumed(activity: Activity) {
        if (locale == Locale.getDefault()) return
        activity.recreate()
    }

    override fun onCreate(activity: Activity) {
        activity.window.decorView.layoutDirection =
            if (LocaleHelper.isRTL(Locale.getDefault())) View.LAYOUT_DIRECTION_RTL
            else View.LAYOUT_DIRECTION_LTR
    }

    override fun getApplicationContext(applicationContext: Context): Context {
        return applicationContext
    }

    override fun getAppCompatDelegate(delegate: AppCompatDelegate): AppCompatDelegate {
        return appCompatDelegate ?: LocaleHelperAppCompatDelegate(delegate).apply {
            appCompatDelegate = this
        }
    }
}