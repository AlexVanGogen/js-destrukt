package edu.avgogen.destrukt.analyze

import com.google.javascript.rhino.IR
import com.google.javascript.rhino.Node
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

    /**
     * TODO: Filter assignments to prevent removing both var a = arr[1]; var b = arr[1]
     * TODO: Group by declaration type (var / let / const)
     */
    private fun makeSuggestions(assignmentsInfo: Map<String, List<ElementAssignInfo>>): StrategySuggestedReplacements {
        val suggestedReplaces = mutableListOf<JsAssignmentReplaceInfo>()
        for ((arrayName, assignments) in assignmentsInfo) {

            // It makes no sense to combine only one assignment
            if (assignments.size <= 1) {
                continue
            }
            val newNode = makeArrayPattern(arrayName, assignments)
            suggestedReplaces.add(JsAssignmentReplaceInfo(
                assignments.map { it.assignment },
                newNode
            ))
        }

        return StrategySuggestedReplacements(JsAssignmentsAnalyzingStrategy::class.java, suggestedReplaces)
    }

    private fun makeArrayPattern(arrayName: String, assignments: List<ElementAssignInfo>): Node {
        val maximumIndex = assignments.map { it.index }.max() ?: return IR.empty()
        val targets = Array<Node>(maximumIndex + 1) { IR.name("") }
        assignments.forEach {
            targets[it.index] = IR.name(it.assignment.assignee.string)
        }
        targets.forEach { println(it) }
        return IR.declaration(
            IR.arrayPattern(*targets),
            IR.name(arrayName),
            assignments[0].assignment.node.token
        )
    }

    private class ElementAssignInfo(
        val assignment: JsAssignment,
        val index: Int
    )
}