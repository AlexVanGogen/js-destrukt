package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment
import edu.avgogen.destrukt.analyze.JsAssignmentsAnalyzingStrategy

/**
 * Finds out which assignments can be combined into one destructuring assignment.
 */
class JsAssignmentsAnalyzer {

    private val strategies = mutableListOf<JsAssignmentsAnalyzingStrategy>()

    fun addStrategy(strategy: JsAssignmentsAnalyzingStrategy): JsAssignmentsAnalyzer {
        strategies.add(strategy)
        return this
    }

    fun analyze(assignments: List<JsAssignment>): List<StrategyResult> {
        return strategies.map { it.analyze(assignments) }
    }
}