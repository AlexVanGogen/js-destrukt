package edu.avgogen.destrukt

import com.google.javascript.jscomp.CodePrinter
import com.google.javascript.jscomp.NodeTraversal
import com.google.javascript.rhino.IR
import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.analyze.JsAssignArrayElementsStrategy
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzer

class JsFindDestructiblePattern: NodeTraversal.AbstractScopedCallback() {

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

    /**
     * TODO refactor
     */
    private fun endSearch() {
        collector.exitLastScope();
        val analyzer = JsAssignmentsAnalyzer()
        analyzer.addStrategy(JsAssignArrayElementsStrategy())
        val summary = collector.applyAnalysis(analyzer)
            .flatten()
            .filter { it.replacements.isNotEmpty() }
            .groupBy { it.strategyClass }
//        println(summary.values.flatten().joinToString("\n\n"))
        summary.values.flatten().forEach { it.replacements.forEach { replaceInfo ->
            val assignmentsToReplace = replaceInfo.assignmentsToReplace
            if (assignmentsToReplace.isNotEmpty()) {
                val nodeToReplace = assignmentsToReplace[0]
                nodeToReplace.node.replaceWith(replaceInfo.suggestedAssignment)
                assignmentsToReplace.drop(1).forEach { assignment ->
                    if (assignment.node.parent != null) {
                        assignment.node.replaceWith(IR.empty())
                    }
                }
            }
        } }
        println(CodePrinter.Builder(root).setPrettyPrint(true).build())
    }

    fun analyzeVisited(analyzer: JsAssignmentsAnalyzer) {
        println(collector.applyAnalysis(analyzer).joinToString("\n") { it.joinToString() })
    }

    fun dumpFoundAssignments() {
        collector.dumpVisited()
    }
}