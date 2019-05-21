package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment

/**
 * Finds out which assignments can be combined into one destructuring assignment.
 */
class JsAssignmentsAnalyzer {

    private val strategies = mutableListOf<JsAssignmentsAnalyzingStrategy>()

    fun addStrategy(strategy: JsAssignmentsAnalyzingStrategy): JsAssignmentsAnalyzer {
        strategies.add(strategy)
        return this
    }

    fun analyze(assignments: List<JsAssignment>): List<StrategySuggestedReplacements> {
        return strategies.map { it.analyze(assignments) }
    }
}