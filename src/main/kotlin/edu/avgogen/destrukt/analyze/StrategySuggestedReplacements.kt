package edu.avgogen.destrukt.analyze


class StrategySuggestedReplacements(
    val strategyClass: Class<JsAssignmentsAnalyzingStrategy>,
    val replacements: List<JsAssignmentReplaceInfo>
) {
    override fun toString(): String {
        val builder = StringBuilder()
        builder
            .append(strategyClass.canonicalName ?: "<unknown qn>")
            .append(":\n")
            .append(replacements.joinToString("\n", "=".repeat(10), "=".repeat(10)))
        return builder.toString()
    }
}