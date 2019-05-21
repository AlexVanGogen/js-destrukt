package edu.avgogen.destrukt.analyze

import com.google.javascript.rhino.IR
import com.google.javascript.rhino.Node
import com.google.javascript.rhino.Token
import edu.avgogen.destrukt.JsAssignment


class JsAssignArrayElementsStrategy: JsAssignmentsAnalyzingStrategy {

    override fun analyze(assignments: List<JsAssignment>): StrategySuggestedReplacements {
        val assignmentsInfo: Map<String, Map<Token, List<ElementAssignInfo>>> = collectArrayElementsAssignments(assignments)
        return makeSuggestions(assignmentsInfo)
    }

    private fun collectArrayElementsAssignments(
        assignments: List<JsAssignment>
    ): Map<String, Map<Token, List<ElementAssignInfo>>> {

        // Keys are array names
        val assignmentsInfo: MutableMap<String, MutableList<ElementAssignInfo>> = mutableMapOf()

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
        return assignmentsInfo.splitByDeclarationType()
    }

    /**
     * Splits all declarations by their type: var / let / const.
     */
    private fun MutableMap<String, MutableList<ElementAssignInfo>>.splitByDeclarationType(): Map<String, Map<Token, List<ElementAssignInfo>>> {
        return this.mapValues { (_, assignmentInfos) ->
            assignmentInfos.groupBy { it.assignment.node.token }
        }
    }

    /**
     * TODO: Filter assignments to prevent removing both var a = arr[1]; var b = arr[1]
     * TODO: Group by declaration type (var / let / const)
     */
    private fun makeSuggestions(assignmentsInfo: Map<String, Map<Token, List<ElementAssignInfo>>>): StrategySuggestedReplacements {
        val suggestedReplaces = mutableListOf<JsAssignmentReplaceInfo>()
        for ((arrayName, assignments) in assignmentsInfo) {
            // It makes no sense to combine only one assignment
            assignments.forEach { token, sameTypeAssignments ->

                // It makes no sense to combine only one assignment
                if (sameTypeAssignments.size <= 1) {
                    return@forEach
                }

                val newNode = makeArrayPattern(token, arrayName, sameTypeAssignments)
                suggestedReplaces.add(
                    JsAssignmentReplaceInfo(
                        sameTypeAssignments.map { it.assignment },
                        newNode
                    )
                )
            }
        }

        return StrategySuggestedReplacements(JsAssignmentsAnalyzingStrategy::class.java, suggestedReplaces)
    }

    private fun makeArrayPattern(token: Token, arrayName: String, assignments: List<ElementAssignInfo>): Node {
        val maximumIndex = assignments.map { it.index }.max() ?: return IR.empty()
        val targets = Array<Node>(maximumIndex + 1) { IR.name("") }
        assignments.forEach {
            targets[it.index] = IR.name(it.assignment.assignee.string)
        }
        return IR.declaration(
            IR.arrayPattern(*targets),
            IR.name(arrayName),
            token
        )
    }

    private class ElementAssignInfo(
        val assignment: JsAssignment,
        val index: Int
    )
}