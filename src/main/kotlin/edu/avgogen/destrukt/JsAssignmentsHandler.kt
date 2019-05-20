package edu.avgogen.destrukt

import com.google.javascript.rhino.Node

interface JsAssignmentsHandler {
    fun addAssignment(assigneeName: String, expression: Node)
    fun removeAssignment(assigneeName: String)
    fun hasNotAssignments(): Boolean
}