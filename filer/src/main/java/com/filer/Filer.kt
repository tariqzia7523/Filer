package com.filer

import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import java.io.File
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class Filer(var context: Activity) {

    fun getExternalFileList(mimeType: String): ArrayList<MyFileModel> {
        Log.e("***mime","mimetype "+mimeType)
        val cr = context.contentResolver
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val projection =
            arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME)
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE)
        val selectionArgs: Array<String>? = null
        val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val selectionArgsPdf = arrayOf(mimeType)

//        val selectionArgsPdf = mimeType
        val cursor: Cursor = cr.query(uri, projection, selectionMimeType, selectionArgsPdf, null)!!
        val uriList: ArrayList<MyFileModel> = ArrayList()
        cursor.moveToFirst()
        while (!cursor.isAfterLast()) {
            val fileId: Long = cursor.getLong(0)
            val fileUri: Uri = Uri.parse(uri.toString() + "/" + fileId)
            Log.e("***URI","File Uri "+fileUri.toString())
            val filepath = FileerPath.getPath(context, fileUri);
            Log.e("***URI","Filepath "+filepath)
            val displayName: String = cursor.getString(1)
            val file = File(filepath!!)
            uriList.add(MyFileModel(displayName,filepath, file.extension,fileUri,file))
            cursor.moveToNext()
        }
        cursor.close()
        return uriList
    }

    private fun getFilesInHigerVersion( filetype :String): ArrayList<MyFileModel>{
        Log.e("***FileType","File type is $filetype")
        val files = ArrayList<MyFileModel>()
        if(filetype.equals("word",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")!!))
        }else if(filetype.equals("ppt",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")!!))
        }else if(filetype.equals("excel",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")!!))
        }else if(filetype.equals("pdf",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!))
        }else if(filetype.equals("txt",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")!!))
        }else if(filetype.equals("xml",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("xml")!!))
        }else if((filetype.equals("html",false))){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("html")!!))
        }else if(filetype.equals("csv",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("csv")!!))
        }else if(filetype.equals("rtf",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtf")!!))
            files.addAll(getExternalFileList( "application/rtf"))
        }else if(filetype.equals("bin",false)){
            Log.e("***FileType","File type is $filetype called")
            files.clear()
            getFilesInLowerVersion(Environment.getExternalStorageDirectory().toString(),files,filetype)
        }else if(filetype.equals("all",false)){
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("docx")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("doc")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("pptx")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("ppt")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("xlsx")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("xls")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("xml")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("html")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("csv")!!))
            files.addAll(getExternalFileList( MimeTypeMap.getSingleton().getMimeTypeFromExtension("rtf")!!))
            files.addAll(getExternalFileList( "application/rtf"))
            getFilesInLowerVersion(Environment.getExternalStorageDirectory().toString(),files,"bin")

        }
        return files
    }

    fun getFiles(fileType : String,filerListInterface: FilerList){
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            try{
                val list = ArrayList<MyFileModel>()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    list.addAll(getFilesInHigerVersion(fileType))
                }else{
                    val p: String = Environment.getExternalStorageDirectory().toString()
                    getFilesInLowerVersion(p, list,fileType)
                }
                handler.post {
                    filerListInterface.onFileListAquired(list)
                }
            }catch (e : Exception){
                e.printStackTrace()
                handler.post {
                    filerListInterface.onFileListFailed()
                }
            }

        }

    }
    fun getFilesInLowerVersion(directoryName: String, files: ArrayList<MyFileModel>, fileType : String) {

        val directory = File(directoryName)

        // Get all files from a directory.
        val fList = directory.listFiles()
        if (fList != null) for (file in fList) {
            if (file.isFile) {
                if(fileType.equals("pdf",false) && file.name.endsWith("pdf")){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("word",false)  && (file.name.endsWith("doc") || file.name.endsWith("docx") )){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("ppt",false)  && (file.name.endsWith("ppt") || file.name.endsWith("pptx") )){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("excel",false) && ((file.name.endsWith("xls")) || file.name.endsWith("xlsx"))){
                    Log.e("***FileType","File type called three")
                    Log.e("***FileType","File type $fileType file ends with ${file.name}")
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("txt",false)  && file.name.endsWith("txt")){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("html",false) && file.name.endsWith("html")){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("xml",false) && file.name.endsWith("xml")){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("rtf",false) && file.name.endsWith("rtf")){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("csv",false) && file.name.endsWith("csv")){
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("bin",false) && file.name.endsWith("bin")){
                    Log.e("***FileType","File type called twice")
                    files.add(fileToMyFile(file))
                }else if(fileType.equals("all",false) ){
                    if(file.name.endsWith("pdf")
                        || file.name.endsWith("doc")
                        || file.name.endsWith("docx")
                        || file.name.endsWith("txt")
                        || file.name.endsWith("html")
                        || file.name.endsWith("rtf")
                        || file.name.endsWith("csv")
                        || file.name.endsWith("bin")
                        || file.name.endsWith("xls") || file.name.endsWith("xlsx")
                        || (file.name.endsWith("ppt") || file.name.endsWith("pptx")))
                        files.add(fileToMyFile(file))
                }
            } else if (file.isDirectory) {
                getFilesInLowerVersion(file.absolutePath, files,fileType)
            }
        }
    }

    fun fileToMyFile(file : File) : MyFileModel{
        return MyFileModel(file.name,file.absolutePath,file.extension,file.toUri(),file)
    }

    interface FilerList{
        fun onFileListAquired(list : ArrayList<MyFileModel>)
        fun onFileListFailed()
    }
}