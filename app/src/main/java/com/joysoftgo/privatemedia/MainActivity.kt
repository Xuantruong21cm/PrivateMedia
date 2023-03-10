package com.joysoftgo.privatemedia

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.base.common.base.BaseActivity
import com.base.common.utils.PermissionResult
import com.base.common.utils.requestPermission
import com.joysoftgo.privatemedia.adapter.DirectoriesAdapter
import com.joysoftgo.privatemedia.databinding.ActivityMainBinding
import com.joysoftgo.privatemedia.extensions.*
import com.joysoftgo.privatemedia.helpers.MediaFetcher
import com.joysoftgo.privatemedia.model.Directory
import com.joysoftgo.privatemedia.model.Medium
import java.io.File

class MainActivity : BaseActivity<ActivityMainBinding>() {

    private var mIsGettingDirs = false
    private var mShouldStopFetching = false
    private var mDirs = ArrayList<Directory>()
    private var mLastMediaFetcher: MediaFetcher? = null
    private var mLoadedInitialPhotos = false
    private var mLastMediaHandler = Handler(Looper.getMainLooper())
    private var mTempShowHiddenHandler = Handler(Looper.getMainLooper())
    private var mLatestMediaId = 0L
    private var mLatestMediaDateId = 0L
    private val LAST_MEDIA_CHECK_PERIOD = 3000L
    private lateinit var mDirectoryAdapter : DirectoriesAdapter

    override fun initViews() {
        mDirectoryAdapter = DirectoriesAdapter(this@MainActivity)
        binding.rvContent.apply {
            layoutManager = GridLayoutManager(this@MainActivity,2)
            adapter = mDirectoryAdapter
        }

        requestPermission(
            this@MainActivity,
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
            object : PermissionResult {
                override fun requestSuccess() {
                    getDirectories(false,false)
                }

                override fun requestDenied() {

                }
            })
    }


    private fun getDirectories(getImagesOnly: Boolean, getVideosOnly: Boolean) {
        if (mIsGettingDirs) {
            return
        }

        mShouldStopFetching = true
        mIsGettingDirs = true

        getCachedDirectories(getImagesOnly, getVideosOnly) {
            Log.d("directory", "getDirectories: ${it.size}")
            gotDirectories(addTempFolderIfNeeded(it),false,false)
        }
    }

    private fun gotDirectories(newDirs: ArrayList<Directory>, getImagesOnly : Boolean, getVideosOnly : Boolean) {
        mIsGettingDirs = false
        mShouldStopFetching = false

        if (!config.shouldShowHidden) {
            val favoritesFolder = newDirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null && favoritesFolder.tmb.getFilenameFromPath().startsWith('.')) {
                newDirs.remove(favoritesFolder)
            }
        }

        val dirs = getSortedDirectories(newDirs)
        if (config.groupDirectSubfolders) {
            mDirs = dirs.clone() as ArrayList<Directory>
        }

        //var isPlaceholderVisible = dirs.isEmpty()

        mLastMediaFetcher?.shouldStop = true
        mLastMediaFetcher = MediaFetcher(applicationContext)

        val favoritePaths = getFavoritePaths()
        val hiddenString = getString(R.string.hidden)
        val albumCovers = config.parseAlbumCovers()
        val includedFolders = config.includedFolders
        val noMediaFolders = getNoMediaFoldersSync()
        val tempFolderPath = config.tempFolderPath
        val getProperFileSize = config.directorySorting and SORT_BY_SIZE != 0
        val dirPathsToRemove = ArrayList<String>()
        val lastModifieds = mLastMediaFetcher!!.getLastModifieds()
        val dateTakens = mLastMediaFetcher!!.getDateTakens()

        if (config.showRecycleBinAtFolders && !config.showRecycleBinLast && !dirs.map { it.path }.contains(RECYCLE_BIN)) {
            try {
                if (mediaDB.getDeletedMediaCount() > 0) {
                    val recycleBin = Directory().apply {
                        path = RECYCLE_BIN
                        name = getString(R.string.recycle_bin)
                        location = LOCATION_INTERNAL
                    }

                    dirs.add(0, recycleBin)
                }
            } catch (ignored: Exception) {
            }
        }

        if (dirs.map { it.path }.contains(FAVORITES)) {
            if (mediaDB.getFavoritesCount() > 0) {
                val favorites = Directory().apply {
                    path = FAVORITES
                    name = getString(R.string.favorites)
                    location = LOCATION_INTERNAL
                }

                dirs.add(0, favorites)
            }
        }

        val android11Files = mLastMediaFetcher?.getAndroid11FolderMedia(getImagesOnly, getVideosOnly, favoritePaths, false, true, dateTakens)
        try {
            for (directory in dirs) {
                if (mShouldStopFetching || isDestroyed || isFinishing) {
                    return
                }

                val sorting = config.getFolderSorting(directory.path)
                val grouping = config.getFolderGrouping(directory.path)
                val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
                        sorting and SORT_BY_DATE_TAKEN != 0 ||
                        grouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
                        grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

                val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
                        sorting and SORT_BY_DATE_MODIFIED != 0 ||
                        grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
                        grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

                val curMedia = mLastMediaFetcher!!.getFilesFrom(
                    directory.path, getImagesOnly, getVideosOnly, getProperDateTaken, getProperLastModified,
                    getProperFileSize, favoritePaths, false, lastModifieds, dateTakens, android11Files
                )

                val newDir = if (curMedia.isEmpty()) {
                    if (directory.path != tempFolderPath) {
                        dirPathsToRemove.add(directory.path)
                    }
                    directory
                } else {
                    createDirectoryFromMedia(directory.path, curMedia, albumCovers, hiddenString, includedFolders, getProperFileSize, noMediaFolders)
                }

                // we are looping through the already displayed folders looking for changes, do not do anything if nothing changed
                if (directory.copy(subfoldersCount = 0, subfoldersMediaCount = 0) == newDir) {
                    continue
                }

                directory.apply {
                    tmb = newDir.tmb
                    name = newDir.name
                    mediaCnt = newDir.mediaCnt
                    modified = newDir.modified
                    taken = newDir.taken
                    this@apply.size = newDir.size
                    types = newDir.types
                    sortValue = getDirectorySortingValue(curMedia, path, name, size)
                }

//                setupAdapter(dirs)
                mDirectoryAdapter.setData(dirs)

                // update directories and media files in the local db, delete invalid items. Intentionally creating a new thread
                updateDBDirectory(directory)
                if (!directory.isRecycleBin() && !directory.areFavorites()) {
                    Thread {
                        try {
                            mediaDB.insertAll(curMedia)
                        } catch (ignored: Exception) {
                        }
                    }.start()
                }

                if (!directory.isRecycleBin()) {
                    getCachedMedia(directory.path, getVideosOnly, getImagesOnly) { it ->
                        val mediaToDelete = ArrayList<Medium>()
                        it.forEach {
                            if (!curMedia.contains(it as Medium)) {
                                val medium = it as? Medium
                                val path = medium?.path
                                if (path != null) {
                                    mediaToDelete.add(medium)
                                }
                            }
                        }
                        mediaDB.deleteMedia(*mediaToDelete.toTypedArray())
                    }
                }
            }

            if (dirPathsToRemove.isNotEmpty()) {
                val dirsToRemove = dirs.filter { dirPathsToRemove.contains(it.path) }
                dirsToRemove.forEach {
                    directoryDB.deleteDirPath(it.path)
                }
                dirs.removeAll(dirsToRemove)
                //setupAdapter(dirs)
                mDirectoryAdapter.setData(dirs)
            }
        } catch (ignored: Exception) {
        }

        val foldersToScan = mLastMediaFetcher!!.getFoldersToScan()
        foldersToScan.remove(FAVORITES)
        foldersToScan.add(0, FAVORITES)
        if (config.showRecycleBinAtFolders) {
            if (foldersToScan.contains(RECYCLE_BIN)) {
                foldersToScan.remove(RECYCLE_BIN)
                foldersToScan.add(0, RECYCLE_BIN)
            } else {
                foldersToScan.add(0, RECYCLE_BIN)
            }
        } else {
            foldersToScan.remove(RECYCLE_BIN)
        }

        dirs.filterNot { it.path == RECYCLE_BIN || it.path == FAVORITES }.forEach {
            foldersToScan.remove(it.path)
        }

        // check the remaining folders which were not cached at all yet
        for (folder in foldersToScan) {
            if (mShouldStopFetching || isDestroyed || isFinishing) {
                return
            }

            val sorting = config.getFolderSorting(folder)
            val grouping = config.getFolderGrouping(folder)
            val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
                    sorting and SORT_BY_DATE_TAKEN != 0 ||
                    grouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
                    grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

            val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
                    sorting and SORT_BY_DATE_MODIFIED != 0 ||
                    grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
                    grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

            val newMedia = mLastMediaFetcher!!.getFilesFrom(
                folder, getImagesOnly, getVideosOnly, getProperDateTaken, getProperLastModified,
                getProperFileSize, favoritePaths, false, lastModifieds, dateTakens, android11Files
            )

            if (newMedia.isEmpty()) {
                continue
            }

//            if (isPlaceholderVisible) {
//                isPlaceholderVisible = false
//                runOnUiThread {
//                    directories_empty_placeholder.beGone()
//                    directories_empty_placeholder_2.beGone()
//                    directories_fastscroller.beVisible()
//                }
//            }

            val newDir = createDirectoryFromMedia(folder, newMedia, albumCovers, hiddenString, includedFolders, getProperFileSize, noMediaFolders)
            dirs.add(newDir)
            //setupAdapter(dirs)
            mDirectoryAdapter.setData(dirs)

            // make sure to create a new thread for these operations, dont just use the common bg thread
            Thread {
                try {
                    directoryDB.insert(newDir)
                    if (folder != RECYCLE_BIN && folder != FAVORITES) {
                        mediaDB.insertAll(newMedia)
                    }
                } catch (ignored: Exception) {
                }
            }.start()
        }

        mLoadedInitialPhotos = true
        if (config.appRunCount > 1) {
            checkLastMediaChanged()
        }

//        runOnUiThread {
//            directories_refresh_layout.isRefreshing = false
//            checkPlaceholderVisibility(dirs)
//        }

        checkInvalidDirectories(dirs)
        if (mDirs.size > 50) {
            excludeSpamFolders()
        }

        val excludedFolders = config.excludedFolders
        val everShownFolders = config.everShownFolders.toMutableSet() as HashSet<String>

        // do not add excluded folders and their subfolders at everShownFolders
        dirs.filter { dir ->
            if (excludedFolders.any { dir.path.startsWith(it) }) {
                return@filter false
            }
            return@filter true
        }.mapTo(everShownFolders) { it.path }

        try {
            // scan the internal storage from time to time for new folders
            if (config.appRunCount == 1 || config.appRunCount % 30 == 0) {
                everShownFolders.addAll(getFoldersWithMedia(config.internalStoragePath))
            }

            // catch some extreme exceptions like too many everShownFolders for storing, shouldnt really happen
            config.everShownFolders = everShownFolders
        } catch (e: Exception) {
            config.everShownFolders = HashSet()
        }

        mDirs = dirs.clone() as ArrayList<Directory>

    }

    private fun getFoldersWithMedia(path: String): HashSet<String> {
        val folders = HashSet<String>()
        try {
            val files = File(path).listFiles()
            if (files != null) {
                files.sortBy { !it.isDirectory }
                for (file in files) {
                    if (file.isDirectory && !file.startsWith("${config.internalStoragePath}/Android")) {
                        folders.addAll(getFoldersWithMedia(file.absolutePath))
                    } else if (file.isFile && file.isMediaFile()) {
                        folders.add(file.parent ?: "")
                        break
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        return folders
    }

    private fun excludeSpamFolders() {
        ensureBackgroundThread {
            try {
                val internalPath = internalStoragePath
                val checkedPaths = ArrayList<String>()
                val oftenRepeatedPaths = ArrayList<String>()
                val paths = mDirs.map { it.path.removePrefix(internalPath) }.toMutableList() as ArrayList<String>
                paths.forEach {
                    val parts = it.split("/")
                    var currentString = ""
                    for (i in 0 until parts.size) {
                        currentString += "${parts[i]}/"

                        if (!checkedPaths.contains(currentString)) {
                            val cnt = paths.count { it.startsWith(currentString) }
                            if (cnt > 50 && currentString.startsWith("/Android/data", true)) {
                                oftenRepeatedPaths.add(currentString)
                            }
                        }

                        checkedPaths.add(currentString)
                    }
                }

                val substringToRemove = oftenRepeatedPaths.filter {
                    val path = it
                    it == "/" || oftenRepeatedPaths.any { it != path && it.startsWith(path) }
                }

                oftenRepeatedPaths.removeAll(substringToRemove)
                val OTGPath = config.OTGPath
                oftenRepeatedPaths.forEach {
                    val file = File("$internalPath/$it")
                    if (getDoesFilePathExist(file.absolutePath, OTGPath)) {
                        config.addExcludedFolder(file.absolutePath)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun checkInvalidDirectories(dirs: ArrayList<Directory>) {
        val invalidDirs = ArrayList<Directory>()
        val OTGPath = config.OTGPath
        dirs.filter { !it.areFavorites() && !it.isRecycleBin() }.forEach {
            if (!getDoesFilePathExist(it.path, OTGPath)) {
                invalidDirs.add(it)
            } else if (it.path != config.tempFolderPath && (!isRPlus() || isExternalStorageManager())) {
                // avoid calling file.list() or listfiles() on Android 11+, it became way too slow
                val children = if (isPathOnOTG(it.path)) {
                    getOTGFolderChildrenNames(it.path)
                } else {
                    File(it.path).list()?.asList()
                }

                val hasMediaFile = children?.any {
                    it != null && (it.isMediaFile() || (it.startsWith("img_", true) && File(it).isDirectory))
                } ?: false

                if (!hasMediaFile) {
                    invalidDirs.add(it)
                }
            }
        }

        if (getFavoritePaths().isEmpty()) {
            val favoritesFolder = dirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null) {
                invalidDirs.add(favoritesFolder)
            }
        }

        if (config.useRecycleBin) {
            try {
                val binFolder = dirs.firstOrNull { it.path == RECYCLE_BIN }
                if (binFolder != null && mediaDB.getDeletedMedia().isEmpty()) {
                    invalidDirs.add(binFolder)
                }
            } catch (ignored: Exception) {
            }
        }

        if (invalidDirs.isNotEmpty()) {
            dirs.removeAll(invalidDirs)
            //setupAdapter(dirs)
            mDirectoryAdapter.setData(dirs)
            invalidDirs.forEach {
                try {
                    directoryDB.deleteDirPath(it.path)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun checkLastMediaChanged() {
        if (isDestroyed) {
            return
        }

        mLastMediaHandler.postDelayed({
            ensureBackgroundThread {
                val mediaId = getLatestMediaId()
                val mediaDateId = getLatestMediaByDateId()
                if (mLatestMediaId != mediaId || mLatestMediaDateId != mediaDateId) {
                    mLatestMediaId = mediaId
                    mLatestMediaDateId = mediaDateId
                    runOnUiThread {
                        getDirectories(false,false)
                    }
                } else {
                    mLastMediaHandler.removeCallbacksAndMessages(null)
                    checkLastMediaChanged()
                }
            }
        }, LAST_MEDIA_CHECK_PERIOD)
    }



    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }
}