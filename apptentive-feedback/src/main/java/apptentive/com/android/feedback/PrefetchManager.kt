package apptentive.com.android.feedback

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import apptentive.com.android.concurrent.ExecutorQueue
import apptentive.com.android.feedback.platform.DefaultStateMachine
import apptentive.com.android.feedback.utils.FileStorageUtil
import apptentive.com.android.feedback.utils.FileUtil
import apptentive.com.android.util.InternalUseOnly
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.PREFETCH_RESOURCES
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

@InternalUseOnly
object PrefetchManager {
    private lateinit var prefetchPath: String
    internal val prefetchedFileURIFromDisk: MutableList<String> = mutableListOf()
    private val downloadExecutor = ExecutorQueue.createConcurrentQueue("Prefetch")

    fun initPrefetchDirectory() {
        DefaultStateMachine.conversationRoster.activeConversation?.path?.let { path ->
            prefetchPath = path
            val prefetchDirectory = FileStorageUtil.getPrefetchDirForActiveUser(path)
            if (prefetchDirectory.isDirectory) {
                for (child in requireNotNull(prefetchDirectory.listFiles())) {
                    if (child.isFile) {
                        prefetchedFileURIFromDisk += child.path
                    }
                }
            }
        }
    }

    fun downloadPrefetchableResources(prefetchFromManifest: List<URL>) {
        deleteOutdatedResourcesFromLocal(getAsHashCodeNames(prefetchFromManifest))
        for (file in prefetchFromManifest) {
            val hashCodedFileName = getHashCodedFileNameFromUrl(file.toString())
            if (!prefetchedFileURIFromDisk.map { getFileNameFromFilePath(it) }.contains(hashCodedFileName)) {
                downloadFile(file, hashCodedFileName)
            }
        }
    }

    internal fun deleteOutdatedResourcesFromLocal(prefetchFromManifest: List<String>) {
        val prefechedFileURIFromDiskIterator = prefetchedFileURIFromDisk.iterator()
        while (prefechedFileURIFromDiskIterator.hasNext()) {
            val file = prefechedFileURIFromDiskIterator.next()
            if (!prefetchFromManifest.contains(getFileNameFromFilePath(file))) {
                FileUtil.deleteFile(file)
                prefechedFileURIFromDiskIterator.remove()
            }
        }
    }

    internal fun downloadFile(url: URL, hashCodedFileName: String) {
        downloadExecutor.execute {
            try {
                val prefetchFile = BitmapFactory.decodeStream(url.openStream())
                saveBitmapToFile(prefetchFile, getHashCodedFileNameFromUrl(url.toString()))
                prefetchedFileURIFromDisk += hashCodedFileName
            } catch (e: IOException) {
                Log.e(PREFETCH_RESOURCES, "Error downloading file: ${e.message}")
            }
        }
    }

    internal fun getAsHashCodeNames(files: List<URL>): List<String> =
        files.map { getHashCodedFileNameFromUrl(it.toString()) }

    internal fun getFileNameFromFilePath(file: String): String =
        file.substring(file.lastIndexOf("/") + 1)

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String) {
        val file = FileStorageUtil.getPrefetchFileForActiveUser(prefetchPath, fileName)
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
    }

    fun getImage(url: String): Bitmap? {
        val fileName = getHashCodedFileNameFromUrl(url)
        return if (prefetchedFileURIFromDisk.contains(fileName)) {
            Log.d(PREFETCH_RESOURCES, "Loading image from disk $url")
            loadImageFromDisk(fileName)
        } else {
            try {
                Log.d(PREFETCH_RESOURCES, "downloading image from URL $url")
                val bitmap = BitmapFactory.decodeStream(URL(url).openStream())
                saveBitmapToFile(bitmap, fileName)
                prefetchedFileURIFromDisk += fileName
                bitmap
            } catch (e: IOException) {
                Log.e(PREFETCH_RESOURCES, "Error downloading file: ${e.message}")
                null
            }
        }
    }

    private fun loadImageFromDisk(fileName: String): Bitmap? {
        return try {
            val file = FileStorageUtil.getPrefetchFileForActiveUser(prefetchPath, fileName)
            Log.v(PREFETCH_RESOURCES, "Image loaded from disk: ${file.absolutePath}")
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: IOException) {
            Log.e(PREFETCH_RESOURCES, "Error loading image from disk: ${e.message}")
            null
        }
    }

    internal fun getHashCodedFileNameFromUrl(url: String): String = url.hashCode().toString()
}
