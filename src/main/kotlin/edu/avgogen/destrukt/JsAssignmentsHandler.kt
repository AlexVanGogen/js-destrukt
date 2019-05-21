package edu.avgogen.destrukt

import com.google.javascript.rhino.Node

interface JsAssignmentsHandler {
    fun addAssignment(node: Node, assignee: Node, expression: Node)
    fun removeAssignment(assigneeName: String)
    fun hasNotAssignments(): Boolean
}