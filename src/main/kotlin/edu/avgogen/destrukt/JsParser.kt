package edu.avgogen.destrukt

import com.google.javascript.jscomp.*
import java.io.File

class JsParser {
    fun parseInputFile(jsFileName: String) {
        val compiler = Compiler()

        val jsFileContent = File(jsFileName).readText()
        val options = CompilerOptions()
        options.setContinueAfterErrors(true)
        compiler.initOptions(options)
        val root = JsAst(SourceFile.fromCode(jsFileName, jsFileContent)).getAstRoot(compiler)
        val jsFinder = JsFindDestructiblePattern()
        NodeTraversal.traverse(compiler, root, jsFinder)
        jsFinder.dumpFoundAssignments()
    }
}

fun main() {
    JsParser().parseInputFile("test.js")
}