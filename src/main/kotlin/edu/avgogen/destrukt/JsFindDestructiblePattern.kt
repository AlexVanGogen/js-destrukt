package edu.avgogen.destrukt

import com.google.javascript.jscomp.AbstractCompiler
import com.google.javascript.jscomp.CodePrinter
import com.google.javascript.jscomp.NodeTraversal
import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.analyze.JsAssignArrayElementsStrategy
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzer

class JsFindDestructiblePattern: NodeTraversal.AbstractScopedCallback() {

    private val collector = JsAssignmentsCollector()
    private var root: Node? = null
    private var newFileContents: String? = null

    init {
        collector.enterNewScope()
    }

    /**
     * Run traversal action and return new file contents
     * // TODO exception
     */
    fun traverse(compiler: AbstractCompiler, root: Node): String {
        NodeTraversal.traverse(compiler, root, this)
        return newFileContents ?: throw RuntimeException()
    }

    override fun visit(traversal: NodeTraversal, node: Node, parent: Node?) {
        if (node.isScript) {
            root = node
        }
        if (node.isVar || node.isConst || node.isLet) {
            node.children().forEach { assignee ->
                assignee.firstChild?.let { assignableExpression ->
                    collector.addAssignment(node, assignee, assignableExpression)
                }
            }
        }
        if (node.isAssign && node.childCount == 2) {
            val assignee = node.firstChild!!
            val assignableExpression = node.secondChild!!
            if (assignee.isName && assignableExpression.isGetElem) {
                collector.addAssignment(node, assignee, assignableExpression)
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
        newFileContents = runTransformerAndGetResult(JsAstTransformer())
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