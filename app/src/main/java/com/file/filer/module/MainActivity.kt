package com.file.filer.module

import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.filer.Filer
import com.filer.MyFileModel
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log

class MainActivity : AppCompatActivity() {

    var READPERMISSION = arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.get_files).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    getFiles()
                } else {
                    //request for the permission
                    val inten = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    inten.data = uri
                    startActivity(inten)
                }
            } else {
                if (!hasPermissions(this@MainActivity, *READPERMISSION)) {
                    ActivityCompat.requestPermissions(this@MainActivity, READPERMISSION, 1)
                } else {
                    getFiles()
                }
            }
        }

    }

    fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission!!) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            var allfound = true
            for (i in 0 until grantResults.size) {
                val permission = permissions[i];
                if (grantResults.size > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allfound = true
                } else {
                    allfound = false
                    break
                }
            }
            if(!allfound){
               //Permission not granted
            }else{
                getFiles()
            }
        }

    }

    private fun getFiles() {
        val progreesDilog = ProgressDialog (this)
        progreesDilog.setMessage("please wait")
        progreesDilog.show()
        Filer(this).getFiles("bin",object : Filer.FilerList{
            override fun onFileListAquired(list: ArrayList<MyFileModel>) {
                Toast.makeText(this@MainActivity, "Success call", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@MainActivity, "Size "+list.size, Toast.LENGTH_SHORT).show()
                for(item in list)
                    Log.e("***FILE","File path "+item.filePath)
                progreesDilog.dismiss()
            }

            override fun onFileListFailed() {
                Toast.makeText(this@MainActivity, "Error call", Toast.LENGTH_SHORT).show()
                progreesDilog.dismiss()
            }
        })
    }

}