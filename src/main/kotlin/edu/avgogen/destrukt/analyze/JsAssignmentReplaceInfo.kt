package edu.avgogen.destrukt.analyze

import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.JsAssignment
import edu.avgogen.destrukt.prettyPrint

class JsAssignmentReplaceInfo(
    val assignmentsToReplace: List<JsAssignment>,
    val suggestedAssignment: Node
) {
    override fun toString(): String {
        val builder = StringBuilder()
        builder
            .append("\n")
            .append("Assignments to replace:")
            .append("\n")
            .append(assignmentsToReplace.joinToString("\n"))
            .append("\n")
            .append("Replace with:")
            .append("\n")
            .append(suggestedAssignment.prettyPrint())
            .append("\n")
        return builder.toString()
    }
}