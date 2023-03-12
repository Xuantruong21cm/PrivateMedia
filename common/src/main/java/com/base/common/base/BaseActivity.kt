package com.base.common.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.base.common.R
import com.base.common.base.localehelper.LocaleHelperActivityDelegate
import com.base.common.base.localehelper.LocaleHelperActivityDelegateImpl
import com.base.common.extensions.hideKeyboard
import com.base.common.utils.NetworkUtil
import com.zeugmasolutions.localehelper.LocaleHelper
import kotlinx.coroutines.launch
import java.util.*

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    open val TAG = this::class.simpleName
    private val localeDelegate: LocaleHelperActivityDelegate = LocaleHelperActivityDelegateImpl()

    private var _binding: VB? = null
    val binding get() = _binding!!

    private var networkStateChangeReceiver: BroadcastReceiver? = null

    private var isInternetConnected = true

    var isFirstTimeLaunch = true

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val context = super.createConfigurationContext(overrideConfiguration)
        return LocaleHelper.onAttach(context)
    }

    override fun getApplicationContext(): Context =
        localeDelegate.getApplicationContext(super.getApplicationContext())

    override fun onResume() {
        super.onResume()
        localeDelegate.onResumed(this)
    }

    override fun onPause() {
        super.onPause()
        localeDelegate.onPaused()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                onActivityStarted()
            }
        }

        initViewBinding()
        setContentView(binding.root)

        networkStateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    context?.let {
                        if (isFirstTimeLaunch) {
                            isFirstTimeLaunch = false
                            return
                        }
                        isFirstTimeLaunch = NetworkUtil.isNetworkConnected(it)
                        onNetworkStateChanged(isInternetConnected)
                        if (!isInternetConnected && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                            // Show Toast or SnarkBar no internet
                        } else {
                            // Dismiss Toast or SnarkBar no internet
                        }
                    }
                }
            }
        }

        applicationContext.registerReceiver(
            networkStateChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )

        initViews(savedInstanceState)

    }

    protected open fun onNetworkStateChanged(isConnected: Boolean) {}

    private fun initViewBinding() {
        _binding = inflateLayout(layoutInflater)
        binding.root.hideKeyboard()
    }


    protected open fun getOption(tag: String?): FragmentControllerOption {
        return FragmentControllerOption.Builder()
            .setTag(tag)
            .useAnimation(false)
            .addBackStack(false)
            .isTransactionReplace(true)
            .option
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    abstract fun initViews(savedInstanceState : Bundle?)

    // Override this function for task that need run background and may need time to completed
    open fun onActivityStarted() {}

    open fun updateLocale(language: String) {
        localeDelegate.setLocale(this, Locale(language))
    }

    override fun getDelegate() = localeDelegate.getAppCompatDelegate(super.getDelegate())

    protected open fun getChildLayoutReplace(): Int {
        return 0
    }

    open fun onLeft(view: View) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            this.onBackPressed()
        }
    }

    override fun onDestroy() {
        applicationContext.unregisterReceiver(networkStateChangeReceiver)
        networkStateChangeReceiver = null
        _binding = null
        super.onDestroy()
    }

    fun showToastHasInternet() {
        // show if activity resume
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            // code logic here
        }
    }

    fun replaceFragment(fragment: Fragment, resId: Int) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
            .replace(resId, fragment)
            .addToBackStack(fragment::class.simpleName)
            .commit()
    }

    fun addFragment(fragment: Fragment, resId: Int) {
        supportFragmentManager.beginTransaction().setCustomAnimations(
            R.anim.enter_from_right,
            R.anim.exit_to_left,
            R.anim.enter_from_left,
            R.anim.exit_to_right
        )
            .add(resId, fragment).addToBackStack(fragment::class.simpleName).commit()
    }

    abstract fun inflateLayout(layoutInflater: LayoutInflater) : VB
}