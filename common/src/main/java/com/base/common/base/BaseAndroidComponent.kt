package com.base.common.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kotlinx.coroutines.CoroutineScope

interface BaseAndroidComponent<VB : ViewBinding>  {
    val binding: VB

    var safetyScope: CoroutineScope?

    val needToSubscribeEventBus: Boolean

    fun getViewBinding(parent: ViewGroup? = null): VB

    fun init(savedInstanceState: Bundle?) {
        binding.initView(savedInstanceState)
        binding.setupViewModel()
        binding.initListener()
    }

    fun VB.initView(savedInstanceState: Bundle?) {}

    fun VB.initListener() {}

    fun VB.setupViewModel() {}

    fun onBackPress() = false

    fun updateContentHeightAds(height: Int) {
    }
}