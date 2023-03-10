package com.base.common.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.constraintlayout.widget.Group
import androidx.viewbinding.ViewBinding
import com.base.common.utils.OnSingleClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.lang.reflect.ParameterizedType

fun View.show() {
    if (visibility == View.VISIBLE) return

    visibility = View.VISIBLE
    if (this is Group) {
        this.requestLayout()
    }
}

fun View.hide() {
    if (visibility == View.GONE) return

    visibility = View.GONE
    if (this is Group) {
        this.requestLayout()
    }
}

fun View.hidden() {
    if (visibility == View.INVISIBLE) return

    visibility = View.INVISIBLE
    if (this is Group) {
        this.requestLayout()
    }
}

fun View.enable() {
    isEnabled = true
    alpha = 1f
}

fun View.disable() {
    isEnabled = false
    alpha = 0.5f
}

fun View.setOnSingleClickListener(timeDelayClick : Long = 500,clickListener: (View?) -> Unit) {
    clickListener.also {
        setOnClickListener(OnSingleClickListener(timeDelayClick,it))
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun <VB : ViewBinding> Any.inflateViewBinding(
    inflater: LayoutInflater,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
): VB {
    val clazz =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>
    return clazz.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    ).invoke(null, inflater, parent, attachToParent) as VB
}


private fun ImageView.glideLoadImage(requestBuilder: RequestBuilder<Drawable>, type: ImageViewType, cornerRadius: Float = 5f) {
    scaleType = ImageView.ScaleType.CENTER_CROP
    val option = requestBuilder.diskCacheStrategy(DiskCacheStrategy.ALL)
    when (type) {
        ImageViewType.CIRCLE -> {
            option.transition(DrawableTransitionOptions.withCrossFade())
                .transform(CircleCrop())
                .into(this)
        }
        ImageViewType.HRECT, ImageViewType.VRECT, ImageViewType.SQUARE -> {
            val opt = if (cornerRadius > 0) {
                RequestOptions().transform(CenterCrop(), RoundedCorners(dipToPix(cornerRadius, context).toInt()))
            } else {
                RequestOptions().transform(CenterCrop())
            }
            option.apply(opt).into(this)
        }
        else -> {
            requestBuilder.diskCacheStrategy(DiskCacheStrategy.ALL).transform(FitCenter()).into(this)
        }
    }
}

fun ImageView.loadImageAsset(path: String, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(Uri.parse("file:///android_asset/$path")), type, cornerRadius)
}

fun ImageView.loadImageFile(f: File, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(f), type, cornerRadius)
}

fun ImageView.loadImageFilePath(path: String, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(File(path)), type, cornerRadius)
}

fun ImageView.loadImageRes(resId : Int, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(resId), type, cornerRadius)
}

fun ImageView.loadImageUrl(url: String, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(url), type, cornerRadius)
}

fun ImageView.loadImageUri(url: Uri, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(url), type, cornerRadius)
}

fun ImageView.loadImageBitmap(bm: Bitmap, type: ImageViewType, cornerRadius: Float = 5f) {
    glideLoadImage(Glide.with(this).load(bm), type, cornerRadius)
}

enum class ImageViewType {
    NONE, CIRCLE, HRECT, VRECT, SQUARE
}