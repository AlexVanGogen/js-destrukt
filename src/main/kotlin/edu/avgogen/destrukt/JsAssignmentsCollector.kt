package edu.avgogen.destrukt

import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzer

class JsAssignmentsCollector: JsAssignmentsHandler {

    private val nestedScopes = mutableListOf<JsScopedAssignmentsHandler>()
    private val fullyVisitedScopes = mutableListOf<JsScopedAssignmentsHandler>()

    /**
     * One scope we add explicitly in the init section of [JsFindDestructiblePattern]
     */
    fun noMoreScopes() = nestedScopes.size == 1

    fun enterNewScope() {
        nestedScopes.add(JsScopedAssignmentsHandler())
    }

    fun exitLastScope() {
        fullyVisitedScopes.add(nestedScopes.last())
        nestedScopes.removeAt(nestedScopes.lastIndex)
    }

    fun applyAnalysis(analyzer: JsAssignmentsAnalyzer) = fullyVisitedScopes.map { analyzer.analyze(it.assignmentsAsList) }

    override fun addAssignment(node: Node, assignee: Node, expression: Node) {
        nestedScopes.last().addAssignment(node, assignee, expression)
    }

    override fun removeAssignment(assigneeName: String) {
        nestedScopes.last().removeAssignment(assigneeName)
    }

    override fun hasNotAssignments(): Boolean {
        return nestedScopes.all { it.hasNotAssignments() } && fullyVisitedScopes.all { it.hasNotAssignments() }
    }

    fun dumpVisited() {
        fullyVisitedScopes.filterNot { it.hasNotAssignments() }.forEach { it.dump() }
    }

    /**
     * Represents assignments located within the same scope.
     */
    private class JsScopedAssignmentsHandler: JsAssignmentsHandler {
        private val assignments = mutableMapOf<String, JsAssignment>()

        override fun addAssignment(node: Node, assignee: Node, expression: Node) {
            assignments[assignee.toString()] = JsAssignment(node, assignee, expression)
        }

        override fun removeAssignment(assigneeName: String) {
            assignments.remove(assigneeName)
        }

        override fun hasNotAssignments(): Boolean {
            return assignments.isEmpty()
        }

        internal val assignmentsAsList
            get() = assignments.values.toList()

        fun dump() {
            println()
            println("Enter scope")
            assignments.forEach { _, assignment ->
                println(assignment)
            }
            println("End scope")
            println()
        }
    }
}