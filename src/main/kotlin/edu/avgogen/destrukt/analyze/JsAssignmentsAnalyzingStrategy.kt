package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment
import edu.avgogen.destrukt.analyze.StrategyResult

interface JsAssignmentsAnalyzingStrategy {
    fun analyze(assignments: List<JsAssignment>): StrategyResult
}