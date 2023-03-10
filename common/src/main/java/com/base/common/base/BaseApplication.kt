package com.base.common.base

import android.content.Context
import com.base.common.utils.NetworkUtil
import com.zeugmasolutions.localehelper.LocaleAwareApplication
import timber.log.Timber
import java.lang.ref.WeakReference

abstract class BaseApplication : LocaleAwareApplication() {

    companion object {
        private lateinit var instance: BaseApplication
        private var isAppLaunching = true

        private fun getInstance(): BaseApplication {
            return instance
        }

        fun checkNetwork(): Boolean {
            return NetworkUtil.isNetworkAvailable(getInstance())
        }

    }

    private var data: MutableMap<String, WeakReference<Any>>? = HashMap()

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    fun getAppContext(): Context {
        return instance.applicationContext
    }

    fun setData(id: String, `object`: Any) {
        data!![id] = WeakReference(`object`)
        Timber.d("SIZE: ", "" + data!!.size)
    }

    fun getData(id: String): Any? {
        if (data != null) {
            val objectWeakReference = data!![id]
            if (objectWeakReference != null) {
                return objectWeakReference.get()
            }
        } else {
            return null
        }
        return null
    }

    fun isAppLaunching(): Boolean = isAppLaunching

    fun setIsAppLaunching(value: Boolean) {
        isAppLaunching = value
    }

}