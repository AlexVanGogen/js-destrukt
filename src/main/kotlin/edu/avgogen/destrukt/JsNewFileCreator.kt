package edu.avgogen.destrukt

import java.io.File

class JsNewFileCreator(val fileNameTransformer: (String) -> String) {

    fun createFileAndWrite(oldFileName: String, newFileContents: String) {
        File(fileNameTransformer(oldFileName)).writeText(newFileContents)
    }

    companion object {
        val DEFAULT = JsNewFileCreator { fileName -> fileName.replace(".js", ".out.js")}
    }
}