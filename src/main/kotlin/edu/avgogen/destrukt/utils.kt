package edu.avgogen.destrukt

import com.google.javascript.rhino.Node

fun Node.prettyPrint(indent: Int = 0): String {
    val builder = StringBuilder("\t".repeat(indent) + this).append("\n")
    children().forEach { builder.append(it.prettyPrint(indent + 1)) }
    return builder.toString()
}