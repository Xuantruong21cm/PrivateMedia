package com.simplemobiletools.gallery.pro.interfaces

import com.joysoftgo.privatemedia.model.ThumbnailItem
import com.simplemobiletools.commons.models.FileDirItem

interface MediaOperationsListener {
    fun refreshItems()

    fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>)

    fun selectedPaths(paths: ArrayList<String>)

    fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>)
}
