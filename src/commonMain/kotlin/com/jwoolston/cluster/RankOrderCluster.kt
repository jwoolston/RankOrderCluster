package com.jwoolston.cluster

import kotlin.math.min

fun <V : Number> clusterOrderedList(cluster: Cluster<V>, clusters: List<Cluster<V>>): List<Cluster<V>> {
    return clusters.sortedBy { cluster.distance(it) }
}

/**
 * Calculates the asymmetric rank order distance between two clusters as described in Equation 1
 */
fun <V : Number> asymmetricRankOrderDistance(
    cluster_j: Cluster<V>,
    order_i: List<Cluster<V>>,
    order_j: List<Cluster<V>>
): Int {
    val stopIndex = order_i.indexOf(cluster_j)
    var accumulator = 0
    for (i in 0 until stopIndex) {
        // We can safely skip stop index because it should always be index 0 of orderB
        val index = order_j.indexOf(order_i[i])
        accumulator += index
    }
    return accumulator
}

/**
 * Calculates the asymmetric rank order distance between two clusters as described in Equation 1
 */
fun <V : Number> clusterRankOrderDistance(
    cluster_i: Cluster<V>,
    cluster_j: Cluster<V>,
    order_i: List<Cluster<V>>,
    order_j: List<Cluster<V>>
): Double {
    // Find the asymmetric rank order distance between cluster a and b
    val asym_ij = asymmetricRankOrderDistance(cluster_j, order_i, order_j)
    val asym_ji = asymmetricRankOrderDistance(cluster_i, order_j, order_i)
    val min_cluster_order = min(order_i.indexOf(cluster_j), order_j.indexOf(cluster_i))
    return (asym_ij + asym_ji).toDouble() / min_cluster_order
}

/**
 * Calculates the inverse of the average distance of nodes in two clusters to their top K neighbors from the full
 * dataset as described in Equation 5.
 *
 * The inverse is returned to save one double precision floating point division by the consumer.
 */
fun <V : Number> kNeighborAverage(cluster_i: Cluster<V>, cluster_j: Cluster<V>, globalNodes: Set<Node<V>>, k: Int): Double {
    // The set of nodes present in either or both clusters
    val nodes = cluster_i.elements.union(cluster_j.elements)
    // Find the average distance between the K nearest nodes by finding all the distances, sorting the distances
    // then summing the K nearest distances as described in equation 5.
    val denom = nodes.fold(0.0, { acc, node ->
        val sorted = globalNodes.map { node.distance(it) }.sorted()
        val sum = sorted.subList(1, min(globalNodes.size, k)).sum()
        acc + (sum / k)
    })

    return ((cluster_i.elements.size + cluster_j.elements.size) / denom)
}

fun <V> transitiveMerge(sets: Set<Set<V>>): Set<Set<V>> {
    val copy = sets.toMutableList()
    val size = sets.size
    val consolidated = BooleanArray(size) // all false by default
    var i = 0
    while (i < size - 1) {
        if (!consolidated[i]) {
            while (true) {
                var intersects = 0
                for (j in (i + 1) until size) {
                    if (consolidated[j]) continue
                    if (copy[i].intersect(copy[j]).isNotEmpty()) {
                        copy[i] = copy[i].union(copy[j])
                        consolidated[j] = true
                        intersects++
                    }
                }
                if (intersects == 0) break
            }
        }
        i++
    }
    return (0 until size).filter { !consolidated[it] }.map { copy[it].toSet() }.toSet()
}

class RankOrderCluster<T : Number>(private val threshold: Double, private val topNeighborCount: Int) {

    private val nodes: MutableSet<Node<T>> = mutableSetOf()

    fun addNode(node: Node<T>): List<Cluster<T>> {
        nodes.add(node)
        return update()
    }

    fun addNodes(nodes: Collection<Node<T>>): List<Cluster<T>> {
        this.nodes.addAll(nodes)
        return update()
    }

    private fun update(): List<Cluster<T>> {

        // Create clusters for each node
        var clusters = nodes.map { Cluster(listOf(it)) }

        var iterate = true
        while (iterate) {
            // Compute the order lists for each cluster
            val orderLists = mutableMapOf<Cluster<T>, List<Cluster<T>>>()
            for (cluster in clusters) {
                orderLists[cluster] = clusterOrderedList(cluster, clusters)
            }

            val mergeCandidates = mutableSetOf<Set<Node<T>>>()

            // For each pair of clusters
            for (cluster_i in clusters) {
                for (cluster_j in clusters) {
                    if (cluster_i == cluster_j) continue

                    val rodCluster = clusterRankOrderDistance(cluster_i, cluster_j,
                        orderLists[cluster_i]!!, orderLists[cluster_j]!!)

                    val kNeighborAvg = kNeighborAverage(cluster_i, cluster_j, nodes, topNeighborCount)
                    val lndCluster = kNeighborAvg * cluster_i.distance(cluster_j)

                    if (rodCluster < threshold && lndCluster < 1.0) {
                        mergeCandidates.add(mutableSetOf(*cluster_i.elements.toTypedArray(), *cluster_j.elements.toTypedArray()))
                    } else {
                        mergeCandidates.add(mutableSetOf(*cluster_i.elements.toTypedArray()))
                        mergeCandidates.add(mutableSetOf(*cluster_j.elements.toTypedArray()))
                    }
                }
            }

            val merged = transitiveMerge(mergeCandidates)

            if (mergeCandidates.size == merged.size) {
                // No merge happened, this is fully clustered
                iterate = false
            }
            // Update the cluster list and  possibly iterate
            clusters = merged.map { Cluster(it) }
        }
        return clusters
    }
}
