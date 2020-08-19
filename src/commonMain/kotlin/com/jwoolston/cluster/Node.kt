package com.jwoolston.cluster

import kotlin.math.pow
import kotlin.math.sqrt

class Node<T : Number>(private val data: Array<T>) {

    private fun checkNodesCompatible(other: Node<T>) {
        if (other.length() != length()) {
            throw IllegalArgumentException("Nodes must have the same length. This(${length()}) != Other(${other.length()}")
        }
    }

    fun length(): Int {
        return data.size
    }

    fun distance(other: Node<T>): Double {
        checkNodesCompatible(other)

        val sumOfSquares = data.foldIndexed(0.0, { i, acc, left ->
            acc + (left.toDouble() - other.data[i].toDouble()).pow(2.0)
        })
        return sqrt(sumOfSquares)
    }

    override fun toString(): String {
        return "Node(data=${data.contentToString()})"
    }
}
