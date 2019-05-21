package edu.avgogen.destrukt

import com.google.javascript.jscomp.CodePrinter
import com.google.javascript.jscomp.NodeTraversal
import com.google.javascript.rhino.IR
import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.analyze.JsAssignArrayElementsStrategy
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzer
import edu.avgogen.destrukt.analyze.StrategySuggestedReplacements

class JsFindDestructiblePattern(
    val fileName: String,
    val newFileCreator: JsNewFileCreator = JsNewFileCreator.DEFAULT
): NodeTraversal.AbstractScopedCallback() {

    private val collector = JsAssignmentsCollector()
    private var root: Node? = null

    init {
        collector.enterNewScope()
    }

    override fun visit(traversal: NodeTraversal, node: Node, parent: Node?) {
        if (node.isScript) {
            root = node
        }
        if (node.isVar) {
            node.children().forEach { assignee ->
                assignee.firstChild?.let { assignableExpression ->
                    collector.addAssignment(node, assignee, assignableExpression)
                }
            }
        }
    }

    override fun enterScope(t: NodeTraversal?) {
        collector.enterNewScope()
        super.enterScope(t)
    }

    override fun exitScope(t: NodeTraversal?) {
        collector.exitLastScope()
        if (collector.noMoreScopes()) {
            endSearch()
        }
        super.exitScope(t)
    }

    private fun endSearch() {
        collector.exitLastScope()
        val newFileContents = runTransformerAndGetResult(JsAstTransformer())
        newFileCreator.createFileAndWrite(fileName, newFileContents)
    }

    private fun runTransformerAndGetResult(transformer: JsAstTransformer): String {
        val analyzer = JsAssignmentsAnalyzer()
        analyzer.addStrategy(JsAssignArrayElementsStrategy())
        val summary = collector.applyAnalysis(analyzer)
            .flatten()
            .filter { it.replacements.isNotEmpty() }
            .groupBy { it.strategyClass }
        val replacements = summary.values.flatten()
        transformer.transform(replacements)

        return CodePrinter.Builder(root).setPrettyPrint(true).build()
    }

    fun analyzeVisited(analyzer: JsAssignmentsAnalyzer) {
        println(collector.applyAnalysis(analyzer).joinToString("\n") { it.joinToString() })
    }

    fun dumpFoundAssignments() {
        collector.dumpVisited()
    }
}