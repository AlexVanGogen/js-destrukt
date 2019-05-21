package edu.avgogen.destrukt

import com.google.javascript.jscomp.NodeTraversal
import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.analyze.JsAssignArrayElementsStrategy
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzer

class JsFindDestructiblePattern: NodeTraversal.AbstractScopedCallback() {

    private val collector = JsAssignmentsCollector()

    init {
        collector.enterNewScope()
    }

    override fun visit(traversal: NodeTraversal, node: Node, parent: Node?) {
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
        collector.exitLastScope();
        val analyzer = JsAssignmentsAnalyzer()
        analyzer.addStrategy(JsAssignArrayElementsStrategy())
        println(collector.applyAnalysis(analyzer).joinToString("\n") { it.joinToString() })
    }

    fun analyzeVisited(analyzer: JsAssignmentsAnalyzer) {
        println(collector.applyAnalysis(analyzer).joinToString("\n") { it.joinToString() })
    }

    fun dumpFoundAssignments() {
        collector.dumpVisited()
    }

    private fun Node.prettyPrint(indent: Int = 0) {
        println("\t".repeat(indent) + this)
        children().forEach { it.prettyPrint(indent + 1) }
    }
}