package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment

class JsAssignmentReplaceInfo(
    val assignmentsToReplace: List<JsAssignment>,
    val suggestedAssignment: JsAssignment
) {
    override fun toString(): String {
        val builder = StringBuilder()
        builder
            .append("Assignments to replace:")
            .append("\n")
            .append(assignmentsToReplace.joinToString("\n"))
            .append("Replace with:")
            .append("\n")
            .append(suggestedAssignment)
            .append("\n")
        return builder.toString()
    }
}