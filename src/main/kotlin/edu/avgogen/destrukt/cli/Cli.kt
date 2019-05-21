package edu.avgogen.destrukt.cli

import com.google.javascript.jscomp.*
import edu.avgogen.destrukt.JsFindDestructiblePattern
import edu.avgogen.destrukt.JsNewFileCreator
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Please specify only one parameter -- JS file name")
    }
    val jsFileName = args[0]
    val newFileContents = run(jsFileName)
    newFileContents.writeToFile(jsFileName)
}

private fun run(jsFileName: String): String {
    val compiler = Compiler()

    val jsFileContent = File(jsFileName).readText()
    val options = CompilerOptions()
    options.setContinueAfterErrors(true)
    compiler.initOptions(options)
    val root = JsAst(SourceFile.fromCode(jsFileName, jsFileContent)).getAstRoot(compiler)
    val jsFinder = JsFindDestructiblePattern()
    return jsFinder.traverse(compiler, root)
}

private fun String.writeToFile(
    oldFileName: String,
    newFileCreator: JsNewFileCreator = JsNewFileCreator.DEFAULT
) {
    newFileCreator.createFileAndWrite(oldFileName, this)
}