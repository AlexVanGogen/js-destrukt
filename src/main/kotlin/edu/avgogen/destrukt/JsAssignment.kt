package edu.avgogen.destrukt

import com.google.javascript.rhino.Node

class JsAssignment(
    val node: Node,
    val assignee: Node,
    val expression: Node
)