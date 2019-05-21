package edu.avgogen.destrukt

import com.google.javascript.rhino.IR
import edu.avgogen.destrukt.analyze.StrategySuggestedReplacements

class JsAstTransformer {

    fun transform(replacements: List<StrategySuggestedReplacements>) {
        replacements.forEach {
            it.replacements.forEach { replaceInfo ->
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
            }
        }
    }
}