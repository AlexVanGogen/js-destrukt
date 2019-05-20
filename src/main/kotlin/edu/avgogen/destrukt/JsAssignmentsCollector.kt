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

    override fun addAssignment(assigneeName: String, expression: Node) {
        nestedScopes.last().addAssignment(assigneeName, expression)
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
        private val assignments = mutableMapOf<String, Node>()

        override fun addAssignment(assigneeName: String, expression: Node) {
            assignments[assigneeName] = expression
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
            assignments.forEach { name, expression ->
                println("$name = $expression")
            }
            println("End scope")
            println()
        }
    }
}