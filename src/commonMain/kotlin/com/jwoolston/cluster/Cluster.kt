package com.jwoolston.cluster

import kotlin.math.min

class Cluster<T : Number> {

    val elements: MutableSet<Node<T>> = mutableSetOf()

    constructor(node: Node<T>) {
        elements.add(node)
    }

    constructor(nodes: Collection<Node<T>>) {
        elements.addAll(nodes)
    }

    constructor(clusters: Set<Cluster<T>>) {
        for (cluster in clusters) {
            elements.addAll(cluster.elements)
        }
    }

    /**
     * Returns the minimum inter-cluster distance based on raw node distances.
     */
    fun distance(other: Cluster<T>): Double {
        return elements.fold(Double.MAX_VALUE, { acc_i, left_i ->
            val dMin = other.elements.fold(acc_i, { acc_j, left_j ->
                val d = left_i.distance(left_j)
                min(acc_j, d)
            })
            min(acc_i, dMin)
        })
    }

    override fun toString(): String {
        return "Cluster(elements=$elements)"
    }
}
