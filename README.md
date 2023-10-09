# Filer 
[![](https://jitpack.io/v/tariqzia7523/filer.svg)](https://jitpack.io/#tariqzia7523/filer)

 Filer is native lib to get list of files, from lower and higher android version, This lib does not handles permissions by it self. Let developer handle permissions by there own self.

# Usage 

Add mevan 

    mavenCentral()
    maven { url 'https://jitpack.io' }

## Use this in build.gradle 

    implementation 'com.github.tariqzia7523:filer:Tag'

### Usage in code
After gettings permission use this code to get files 

    Fileer(this).getFiles("all",object : Fileer.FileerList{

        override fun onFileListAquired(list: ArrayList<MyFileModel>) {
            //Use this list the way you want to
            progreesDilog.dismiss() //You can handle forground threeds things
        }

        override fun onFileListFailed() {
            Toast.makeText(this@MainActivity, "Error call", Toast.LENGTH_SHORT).show()
            progreesDilog.dismiss()
        }
    })

#### Enjoy


