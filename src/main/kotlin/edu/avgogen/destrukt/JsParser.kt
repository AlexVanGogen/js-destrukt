package edu.avgogen.destrukt

import com.google.javascript.jscomp.*
import edu.avgogen.destrukt.analyze.JsAssignArrayElementsStrategy
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzer
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
    }
}

fun main() {
    JsParser().parseInputFile("test.js")
}