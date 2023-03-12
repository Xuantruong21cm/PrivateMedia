package com.example.privategallery.async

import android.content.Context
import com.example.privategallery.extensions.config
import com.example.privategallery.extensions.getFavoritePaths
import com.example.privategallery.helpers.*
import com.simplemobiletools.commons.helpers.*
import com.example.privategallery.models.Medium
import com.example.privategallery.models.ThumbnailItem
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class GetMediaAsynctask(
    val context: Context, val mPath: String, val isPickImage: Boolean = false, val isPickVideo: Boolean = false,
    val showAll: Boolean, val callback: (media: ArrayList<ThumbnailItem>) -> Unit
) :
    CoroutineScope {

    private var job: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun cancel() {
        job.cancel()
    }

    fun execute() = launch {
        onPreExecute()
        val result = doInBackground() // runs in background thread without blocking the Main Thread
        onPostExecute(result)
    }

    private fun onPreExecute() {
        // show progress
    }

    private fun onPostExecute(media: ArrayList<ThumbnailItem>) {
        // hide progress
        callback(media)
    }

    private suspend fun doInBackground(): ArrayList<ThumbnailItem> = withContext(Dispatchers.IO) {
        // do async work
        delay(100) // simulate async work
        //return@withContext "SomeResult"
        val pathToUse = if (showAll) SHOW_ALL else mPath
        val folderGrouping = context.config.getFolderGrouping(pathToUse)
        val folderSorting = context.config.getFolderSorting(pathToUse)
        val getProperDateTaken = folderSorting and SORT_BY_DATE_TAKEN != 0 ||
            folderGrouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
            folderGrouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

        val getProperLastModified = folderSorting and SORT_BY_DATE_MODIFIED != 0 ||
            folderGrouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
            folderGrouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

        val getProperFileSize = folderSorting and SORT_BY_SIZE != 0
        val favoritePaths = context.getFavoritePaths()
        val getVideoDurations = context.config.showThumbnailVideoDuration
        val lastModifieds = if (getProperLastModified) mediaFetcher.getLastModifieds() else HashMap()
        val dateTakens = if (getProperDateTaken) mediaFetcher.getDateTakens() else HashMap()

        val media = if (showAll) {
            val foldersToScan = mediaFetcher.getFoldersToScan().filter { it != RECYCLE_BIN && it != FAVORITES && !context.config.isFolderProtected(it) }
            val media = ArrayList<Medium>()
            foldersToScan.forEach {
                val newMedia = mediaFetcher.getFilesFrom(
                    it, isPickImage, isPickVideo, getProperDateTaken, getProperLastModified, getProperFileSize,
                    favoritePaths, getVideoDurations, lastModifieds, dateTakens.clone() as HashMap<String, Long>, null
                )
                media.addAll(newMedia)
            }

            mediaFetcher.sortMedia(media, context.config.getFolderSorting(SHOW_ALL))
            media
        } else {
            mediaFetcher.getFilesFrom(
                mPath, isPickImage, isPickVideo, getProperDateTaken, getProperLastModified, getProperFileSize, favoritePaths,
                getVideoDurations, lastModifieds, dateTakens, null
            )
        }

        return@withContext mediaFetcher.groupMedia(media, pathToUse)
    }


        private val mediaFetcher = MediaFetcher(context)

//    @Deprecated("Deprecated in Java")
//    override fun doInBackground(vararg params: Void): ArrayList<ThumbnailItem> {
//        val pathToUse = if (showAll) SHOW_ALL else mPath
//        val folderGrouping = context.config.getFolderGrouping(pathToUse)
//        val folderSorting = context.config.getFolderSorting(pathToUse)
//        val getProperDateTaken = folderSorting and SORT_BY_DATE_TAKEN != 0 ||
//            folderGrouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
//            folderGrouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0
//
//        val getProperLastModified = folderSorting and SORT_BY_DATE_MODIFIED != 0 ||
//            folderGrouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
//            folderGrouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0
//
//        val getProperFileSize = folderSorting and SORT_BY_SIZE != 0
//        val favoritePaths = context.getFavoritePaths()
//        val getVideoDurations = context.config.showThumbnailVideoDuration
//        val lastModifieds = if (getProperLastModified) mediaFetcher.getLastModifieds() else HashMap()
//        val dateTakens = if (getProperDateTaken) mediaFetcher.getDateTakens() else HashMap()
//
//        val media = if (showAll) {
//            val foldersToScan = mediaFetcher.getFoldersToScan().filter { it != RECYCLE_BIN && it != FAVORITES && !context.config.isFolderProtected(it) }
//            val media = ArrayList<Medium>()
//            foldersToScan.forEach {
//                val newMedia = mediaFetcher.getFilesFrom(
//                    it, isPickImage, isPickVideo, getProperDateTaken, getProperLastModified, getProperFileSize,
//                    favoritePaths, getVideoDurations, lastModifieds, dateTakens.clone() as HashMap<String, Long>, null
//                )
//                media.addAll(newMedia)
//            }
//
//            mediaFetcher.sortMedia(media, context.config.getFolderSorting(SHOW_ALL))
//            media
//        } else {
//            mediaFetcher.getFilesFrom(
//                mPath, isPickImage, isPickVideo, getProperDateTaken, getProperLastModified, getProperFileSize, favoritePaths,
//                getVideoDurations, lastModifieds, dateTakens, null
//            )
//        }
//
//        return mediaFetcher.groupMedia(media, pathToUse)
//    }

//    @Deprecated("Deprecated in Java")
//    override fun onPostExecute(media: ArrayList<ThumbnailItem>) {
//        super.onPostExecute(media)
//        callback(media)
//    }

    fun stopFetching() {
        mediaFetcher.shouldStop = true
        //cancel(true)
        cancel()
    }
}
