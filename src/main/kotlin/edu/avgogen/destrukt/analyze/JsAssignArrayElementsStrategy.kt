package edu.avgogen.destrukt.analyze

import com.google.javascript.rhino.IR
import com.google.javascript.rhino.Token
import edu.avgogen.destrukt.JsAssignment

class JsAssignArrayElementsStrategy: JsAssignmentsAnalyzingStrategy {

    override fun analyze(assignments: List<JsAssignment>): StrategySuggestedReplacements {
        val assignmentsInfo: Map<String, List<ElementAssignInfo>> = collectArrayElementsAssignments(assignments)
        return makeSuggestions(assignmentsInfo)
    }

    private fun collectArrayElementsAssignments(
        assignments: List<JsAssignment>
    ): MutableMap<String, MutableList<ElementAssignInfo>> {

        // Keys are array names
        val assignmentsInfo = mutableMapOf<String, MutableList<ElementAssignInfo>>()

        for (assignment in assignments) {
            assignment.expression.let {
                if (it.isGetElem) {
                    // Considered that GETELEM always has two childs: array name and index
                    val arrayName = it.firstChild!!.string
                    val index = it.secondChild!!.double.toInt()
                    assignmentsInfo.putIfAbsent(arrayName, mutableListOf())
                    assignmentsInfo[arrayName]!!.add(ElementAssignInfo(assignment, index))
                }
            }
        }
        return assignmentsInfo
    }

    private fun makeSuggestions(assignmentsInfo: Map<String, List<ElementAssignInfo>>): StrategySuggestedReplacements {
        val suggestedReplaces = mutableListOf<JsAssignmentReplaceInfo>()
        for ((arrayName, assignments) in assignmentsInfo) {
            val targets = ArrayList(assignments.map { IR.name(it.assignment.assignee.string) })
            val newNode = IR.declaration(
                IR.arrayPattern(*targets.toArray(arrayOf())),
                IR.name(arrayName),
                Token.VAR
            )
            suggestedReplaces.add(JsAssignmentReplaceInfo(
                assignments.map { it.assignment },
                newNode
            ))
        }

        return StrategySuggestedReplacements(JsAssignmentsAnalyzingStrategy::class.java, suggestedReplaces)
    }

    private class ElementAssignInfo(
        val assignment: JsAssignment,
        val index: Int
    )
}