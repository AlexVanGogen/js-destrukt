package edu.avgogen.destrukt.cli

import com.google.javascript.jscomp.*
import edu.avgogen.destrukt.JsFindDestructiblePattern
import edu.avgogen.destrukt.JsNewFileCreator
import java.io.File
import java.io.FileNotFoundException

fun main(args: Array<String>) {
    if (args.size != 1) {
        printErr("Please specify only one parameter -- JS file name")
        return
    }
    val jsFileName = args[0]
    if (!jsFileName.hasJsFormat()) {
        printErr("Error: file $jsFileName has not .js extension")
        return
    }
    val jsFile = File(jsFileName)
    val newFileContents = run(jsFile)
    val newFileName = newFileContents?.writeToFile(jsFileName)
    newFileName?.let {
        println("File $newFileName created successfully")
    }
}

private fun run(jsFile: File): String? {
    val compiler = Compiler()
    var jsFileContent: String? = null

    try {
        jsFileContent = jsFile.readText()
    } catch (e: FileNotFoundException) {
        printErr("Error: file ${jsFile.name} not found")
        return null
    }

    val options = CompilerOptions()
    options.setContinueAfterErrors(true)
    compiler.initOptions(options)
    val root = JsAst(SourceFile.fromCode(jsFile.name, jsFileContent)).getAstRoot(compiler)
    val jsFinder = JsFindDestructiblePattern()
    return jsFinder.traverse(compiler, root)
}

private fun String.hasJsFormat() = endsWith(".js")

private fun String.writeToFile(
    oldFileName: String,
    newFileCreator: JsNewFileCreator = JsNewFileCreator.DEFAULT
): String {
    return newFileCreator.createFileAndWrite(oldFileName, this)
}

private fun printErr(message: String) {
    System.err.println(message)
}