package com.simplemobiletools.commons.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import com.google.android.material.card.MaterialCardView
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.databinding.ActivityFaqBinding
import com.simplemobiletools.commons.extensions.getProperBackgroundColor
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.removeUnderlines
import com.simplemobiletools.commons.helpers.APP_FAQ
import com.simplemobiletools.commons.helpers.APP_ICON_IDS
import com.simplemobiletools.commons.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.views.MyTextView

class FAQActivity : BaseSimpleActivity() {
    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    private lateinit var mBinding : ActivityFaqBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)

        mBinding = ActivityFaqBinding.inflate(layoutInflater)

        setContentView(mBinding.root)

        updateMaterialActivityViews(mBinding.faqCoordinator,mBinding.faqHolder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(mBinding.faqNestedScrollview,mBinding.faqToolbar)

        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()

        val inflater = LayoutInflater.from(this)
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        faqItems.forEach {
            val faqItem = it
            inflater.inflate(R.layout.item_faq, null).apply {
               findViewById<MaterialCardView>(R.id.faq_card).setCardBackgroundColor(backgroundColor)
               findViewById<MyTextView>(R.id.faq_title).apply {
                    text = if (faqItem.title is Int) getString(faqItem.title) else faqItem.title as String
                    setTextColor(primaryColor)
                }

              findViewById<MyTextView>(R.id.faq_text).apply {
                    text = if (faqItem.text is Int) Html.fromHtml(getString(faqItem.text)) else faqItem.text as String
                    setTextColor(textColor)
                    setLinkTextColor(primaryColor)

                    movementMethod = LinkMovementMethod.getInstance()
                    removeUnderlines()
                }

               mBinding.faqHolder.addView(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(mBinding.faqToolbar, NavigationIcon.Arrow)
    }
}
