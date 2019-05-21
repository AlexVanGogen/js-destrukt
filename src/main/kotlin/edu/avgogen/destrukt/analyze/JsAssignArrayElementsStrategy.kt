package edu.avgogen.destrukt.analyze

import com.google.javascript.rhino.IR
import com.google.javascript.rhino.Node
import com.google.javascript.rhino.Token
import edu.avgogen.destrukt.JsAssignment
import edu.avgogen.destrukt.prettyPrint


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
        return assignmentsInfo
            .splitByDeclarationType()
            .filterRepeatingIndexAccess()
    }

    /**
     * Splits all declarations by their type: var / let / const.
     */
    private fun MutableMap<String, MutableList<ElementAssignInfo>>.splitByDeclarationType(): Map<String, Map<Token, List<ElementAssignInfo>>> {
        return this.mapValues { (_, assignmentInfos) ->
            assignmentInfos.groupBy { it.assignment.node.token }
        }
    }
    
    private fun Map<String, Map<Token, List<ElementAssignInfo>>>.filterRepeatingIndexAccess(): Map<String, Map<Token, List<ElementAssignInfo>>> {
        val filteredMap = mutableMapOf<String, Map<Token, List<ElementAssignInfo>>>()
        forEach { arrayName, map ->
            val oneTypeAssignments = mutableMapOf<Token, List<ElementAssignInfo>>()
            map.forEach { token, assignmentsInfos ->
                val indicesMet = mutableSetOf<Int>()
                oneTypeAssignments[token] = assignmentsInfos.filter { info ->
                    if (info.index in indicesMet) {
                        return@filter false
                    }
                    indicesMet.add(info.index)
                    return@filter true
                }
            }
            filteredMap[arrayName] = oneTypeAssignments
        }
        return filteredMap
    }

    private fun makeSuggestions(assignmentsInfo: Map<String, Map<Token, List<ElementAssignInfo>>>): StrategySuggestedReplacements {
        val suggestedReplaces = mutableListOf<JsAssignmentReplaceInfo>()
        for ((arrayName, assignments) in assignmentsInfo) {

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
        return if (token == Token.ASSIGN) {
            IR.assign(
                IR.arrayPattern(*targets),
                IR.name(arrayName)
            )
        } else {
            IR.declaration(
                IR.arrayPattern(*targets),
                IR.name(arrayName),
                token
            )
        }
    }

    private class ElementAssignInfo(
        val assignment: JsAssignment,
        val index: Int
    )
}