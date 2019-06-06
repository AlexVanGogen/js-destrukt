package edu.avgogen.destrukt

import com.google.javascript.rhino.Node
import edu.avgogen.destrukt.analyze.StrategySuggestedReplacements

class JsAstTransformer {

    fun transform(replacements: List<StrategySuggestedReplacements>) {
        replacements.forEach {
            it.replacements.forEach { replaceInfo ->
                val assignmentsToReplace = replaceInfo.assignmentsToReplace
                removeDuplications(assignmentsToReplace)
                assignmentsToReplace.firstOrNull()?.node?.let { nodeToInsertBefore ->
                    nodeToInsertBefore.parent?.addChildBefore(replaceInfo.suggestedAssignment, nodeToInsertBefore)
                }
                removeConvertibleAssignments(assignmentsToReplace)
            }
        }
    }

    private fun removeConvertibleAssignments(assignmentsToReplace: List<JsAssignment>) {
        assignmentsToReplace.forEach {
            removeAssignment(it)
        }
    }

    private fun removeAssignment(assignment: JsAssignment) {
        assignment.node.removeChild(assignment.assignee)
        if (assignment.node.isAssign) {
            assignment.node.removeChild(assignment.expression)
        }
        removeBottomUpEmptyNodes(assignment)
    }

    private fun removeBottomUpEmptyNodes(assignment: JsAssignment) {
        var node: Node? = assignment.node
        while (node != null && node.childCount == 0) {
            val child = node
            node = node.parent
            node?.removeChild(child)
        }
    }

    /**
     * Removes assignments shadowed by following assignments to the same variable in the same statement.
     * It will eliminate problems with such declarations:
     *    var a = 1, a = arr[0], b = arr[1]
     * which could be translated to
     *    var [a, b] = arr;
     *    var a = 1;
     *
     * Maybe it would be more useful to remove duplications in every declaration statement,
     * we'll strive to locality of changes, and not concern statements out of the general task scope.
     */
    private fun removeDuplications(assignmentsToReplace: List<JsAssignment>) {
        val declarations = assignmentsToReplace.map { it.node }.distinct()
        declarations.forEach {
            val alreadyDeclaredNames = mutableSetOf<String>()
            it.children().reversed().forEach { node ->
                if (node.isName) {
                    val name = node.string
                    if (name in alreadyDeclaredNames) {
                        node.parent?.removeChild(node)
                    } else {
                        alreadyDeclaredNames.add(name)
                    }
                }
            }
        }
    }
}