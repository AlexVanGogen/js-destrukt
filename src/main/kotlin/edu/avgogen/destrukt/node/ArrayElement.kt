package edu.avgogen.destrukt.node

import com.google.javascript.rhino.Node

data class ArrayElement(
    val arrayIdentifier: Node,
    val index: Int
): AssignableNode