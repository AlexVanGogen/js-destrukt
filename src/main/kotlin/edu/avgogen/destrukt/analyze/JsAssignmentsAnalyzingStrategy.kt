package edu.avgogen.destrukt.analyze

import edu.avgogen.destrukt.JsAssignment

interface JsAssignmentsAnalyzingStrategy {
    fun analyze(assignments: List<JsAssignment>): StrategySuggestedReplacements
}