package edu.avgogen.destrukt

import java.io.File

class JsNewFileCreator(val fileNameTransformer: (String) -> String) {

    fun createFileAndWrite(oldFileName: String, newFileContents: String): String {
        val newFileName = fileNameTransformer(oldFileName)
        File(fileNameTransformer(oldFileName)).writeText(newFileContents)
        return newFileName
    }

    companion object {
        val DEFAULT = JsNewFileCreator { fileName -> fileName.removeSuffix(".js") + ".out.js" }
    }
}