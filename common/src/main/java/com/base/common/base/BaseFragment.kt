package com.base.common.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.Group
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import com.base.common.extensions.hideKeyboard
import java.util.*

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    open val TAG = this::class.simpleName
    private lateinit var _binding : VB
    val binding get() = _binding
    private var mRootView : View? = null
    private var hasInitializedRootView = false

    fun isBindingInitialized() : Boolean {
        return ::_binding.isInitialized
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mRootView == null){
            initViewBinding()
        }
        binding.root.hideKeyboard()

        binding.root.postDelayed({
                initDataWithAnimation()
        },300)

        initObserver()

        return mRootView
    }

    private fun initViewBinding() {
        _binding = getViewBinding()
        mRootView = binding.root
    }

    override fun onResume() {
        super.onResume()
        registerListeners()
    }

    override fun onPause() {
        unRegisterListeners()
        super.onPause()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver{
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                onFragmentResume()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                onFragmentPause()
            }
        })

        if (!hasInitializedRootView){
            getFragmentArguments()
            setBindingVariables()
            observeAPICall()
            setupObservers()
            initView(binding.root)
            hasInitializedRootView = true
        }
        view.setOnTouchListener { _, _ -> true }
    }

    override fun onDestroyView() {
        if (view?.parent != null){
            (view?.parent as? ViewGroup)?.endViewTransition(view)
        }
        super.onDestroyView()
    }

    protected open fun getOption(tag: String?): FragmentControllerOption {
        return FragmentControllerOption.Builder()
            .setTag(tag)
            .useAnimation(true)
            .addBackStack(true)
            .isTransactionReplace(true)
            .option
    }

    protected open fun getOption(tag: String?, isReplaceFrag: Boolean): FragmentControllerOption {
        return FragmentControllerOption.Builder()
            .setTag(tag)
            .useAnimation(true)
            .addBackStack(true)
            .isTransactionReplace(isReplaceFrag)
            .option
    }

    protected abstract fun initView(view: View)

    open fun initDataWithAnimation() {}

    protected abstract fun initObserver()

    open fun registerListeners() {}

    open fun unRegisterListeners() {}

    protected abstract fun getFragmentArguments()

    open fun setBindingVariables() {}

    open fun onFragmentResume() {}

    open fun onFragmentPause() {}

    open fun observeAPICall() {}

    protected abstract fun setupObservers()

    protected open fun onRetryClick() {}

    protected open fun reloadData() {}

    abstract fun getViewBinding() : VB

    open fun onLeft(view: View) {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        } else {
            activity?.onBackPressedDispatcher
        }
    }

    protected open fun getChildLayoutReplace(): Int {
        return 0
    }

    fun setLanguage(language: String) {
        (requireActivity() as BaseActivity<*>).updateLocale(language)
    }

    open fun showMessage(message : String){
        Toast.makeText(requireContext(),message, Toast.LENGTH_SHORT).show()
    }

    val currentLanguage: Locale
        get() = Locale.getDefault()

}