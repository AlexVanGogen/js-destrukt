package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment
import edu.avgogen.destrukt.analy.JsAssignmentsAnalyzingStrategy

class StrategyResult(
    val strategyClass: Class<JsAssignmentsAnalyzingStrategy>,
    val suggestedReplaces: List<JsAssignmentReplaceInfo>
) {
    override fun toString(): String {
        val builder = StringBuilder()
        builder
            .append(strategyClass.canonicalName ?: "<unknown qn>")
            .append(":\n")
            .append(suggestedReplaces.joinToString("\n", "=".repeat(10), "=".repeat(10)))
        return builder.toString()
    }
}