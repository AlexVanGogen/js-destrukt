package edu.avgogen.destrukt

import com.google.javascript.rhino.Node
import kotlin.math.exp

class JsAssignmentsCollector: JsAssignmentsHandler {

    private val nestedScopes = mutableListOf<JsScopedAssignmentsHandler>()
    private val fullyVisitedScopes = mutableListOf<JsScopedAssignmentsHandler>()

    fun noMoreScopes() = nestedScopes.isEmpty()

    fun enterNewScope() {
        nestedScopes.add(JsScopedAssignmentsHandler())
    }

    fun exitLastScope() {
        fullyVisitedScopes.add(nestedScopes.last())
        nestedScopes.removeAt(nestedScopes.lastIndex)
    }

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
            assignments[assignee.string!!] = JsAssignment(node, assignee, expression)
        }

        override fun removeAssignment(assigneeName: String) {
            assignments.remove(assigneeName)
        }

        override fun hasNotAssignments(): Boolean {
            return assignments.isEmpty()
        }

        fun dump() {
            println()
            println("Enter scope")
            assignments.forEach { name, assignment ->
                println("$name = ${assignment.expression}")
            }
            println("End scope")
            println()
        }
    }
}