package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment

class JsAssignArrayElementsStrategy: JsAssignmentsAnalyzingStrategy {

    override fun analyze(assignments: List<JsAssignment>): StrategyResult {
        return StrategyResult(JsAssignmentsAnalyzingStrategy::class.java, emptyList())
    }
}