package com.jwoolston.cluster

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RankOrderClusterTest {

    val data = listOf(
        Node(arrayOf(1.0)),
        Node(arrayOf(1.1)),
        Node(arrayOf(3.0)),
        Node(arrayOf(12.0)),
        Node(arrayOf(12.1)),
        Node(arrayOf(12.2)),
        Node(arrayOf(30.0))
    )

    @Test
    fun clusterOrderedListTest() {
        val clusters = data.map { Cluster(it) }
        val ordered0 = clusterOrderedList(clusters[0], clusters)
        assertEquals(data[0], ordered0[0].elements.first(), "Cluster 0 ordered list index 0 mismatch")
        assertEquals(data[1], ordered0[1].elements.first(), "Cluster 0 ordered list index 1 mismatch")
        assertEquals(data[2], ordered0[2].elements.first(), "Cluster 0 ordered list index 2 mismatch")
        assertEquals(data[3], ordered0[3].elements.first(), "Cluster 0 ordered list index 3 mismatch")
        assertEquals(data[4], ordered0[4].elements.first(), "Cluster 0 ordered list index 4 mismatch")
        assertEquals(data[5], ordered0[5].elements.first(), "Cluster 0 ordered list index 5 mismatch")
        assertEquals(data[6], ordered0[6].elements.first(), "Cluster 0 ordered list index 6 mismatch")

        val ordered5 = clusterOrderedList(clusters[5], clusters)
        assertEquals(data[5], ordered5[0].elements.first(), "Cluster 5 ordered list index 0 mismatch")
        assertEquals(data[4], ordered5[1].elements.first(), "Cluster 5 ordered list index 1 mismatch")
        assertEquals(data[3], ordered5[2].elements.first(), "Cluster 5 ordered list index 2 mismatch")
        assertEquals(data[2], ordered5[3].elements.first(), "Cluster 5 ordered list index 3 mismatch")
        assertEquals(data[1], ordered5[4].elements.first(), "Cluster 5 ordered list index 4 mismatch")
        assertEquals(data[0], ordered5[5].elements.first(), "Cluster 5 ordered list index 5 mismatch")
        assertEquals(data[6], ordered5[6].elements.first(), "Cluster 5 ordered list index 6 mismatch")
    }

    @Test
    fun asymmetricRankOrderDistanceTest() {
        val clusters = data.map { Cluster(it) }
        val ordered0 = clusterOrderedList(clusters[0], clusters)
        val ordered1 = clusterOrderedList(clusters[1], clusters)
        val distance = asymmetricRankOrderDistance(clusters[1], ordered0, ordered1)
        assertEquals(1, distance, "Incorrect asymmetric rank order distance.")

        val ordered5 = clusterOrderedList(clusters[5], clusters)
        val distance5 = asymmetricRankOrderDistance(clusters[5], ordered0, ordered5)
        assertEquals(15, distance5, "Incorrect asymmetric rank order distance.")
    }

    @Test
    fun clusterRankOrderDistanceTest() {
        val clusters = data.map { Cluster(it) }
        val ordered0 = clusterOrderedList(clusters[0], clusters)
        val ordered5 = clusterOrderedList(clusters[5], clusters)
        val distance = clusterRankOrderDistance(clusters[0], clusters[5], ordered0, ordered5)
        assertEquals(6.0, distance, "Incorrect cluster rank order distance.")
    }

    @Test
    fun kNeighborAverageTest() {
        val clusters = data.map { Cluster(it) }
        val ordered0 = clusterOrderedList(clusters[0], clusters)
        val ordered5 = clusterOrderedList(clusters[5], clusters)
        val kNeighborAvg = kNeighborAverage(clusters[0], clusters[5], data.toSet(), 20)
        assertEquals(0.3508771929824562, kNeighborAvg, "Incorrect k neighbor average.")
    }

    @Test
    fun transitiveMergeTest() {
        val input = setOf(
            setOf('A', 'B'),
            setOf('B', 'A'),
            setOf('B', 'C'),
            setOf('D', 'E'),
            setOf('F'),
            setOf('G')
        )

        val expectedABC = setOf('A', 'B', 'C')
        val expectedDE = setOf('D', 'E')
        val expectedF = setOf('F')
        val expectedG = setOf('G')
        val output = transitiveMerge(input)
        assertTrue(output.contains(expectedABC), "Missing set ABC")
        assertTrue(output.contains(expectedDE), "Missing set DE")
        assertTrue(output.contains(expectedF), "Missing set F")
        assertTrue(output.contains(expectedG), "Missing set G")
    }

    @Test
    fun clusterTest() {
        val clusterer = RankOrderCluster<Double>(3.1, 20)
        val clusters = clusterer.addNodes(data)
        assertNotNull(clusters, "Updating clusterer must not return a null value")
        assertEquals(3, clusters.size, "Incorrect number of clusters.")
        clusters.forEachIndexed { index, cluster ->
            when (index) {
                0 -> {
                    assertEquals(3, cluster.elements.size, "Cluster 0 incorrect number of elements")
                    assertTrue(cluster.elements.contains(data[0]), "Cluster 0 missing Node 0")
                    assertTrue(cluster.elements.contains(data[1]), "Cluster 0 missing Node 1")
                    assertTrue(cluster.elements.contains(data[2]), "Cluster 1 missing Node 2")
                }
                1 -> {
                    assertEquals(3, cluster.elements.size, "Cluster 2 incorrect number of elements")
                    assertTrue(cluster.elements.contains(data[3]), "Cluster 2 missing Node 3")
                    assertTrue(cluster.elements.contains(data[4]), "Cluster 2 missing Node 4")
                    assertTrue(cluster.elements.contains(data[5]), "Cluster 2 missing Node 5")
                }
                2 -> {
                    assertEquals(1, cluster.elements.size, "Cluster 3 incorrect number of elements")
                    assertTrue(cluster.elements.contains(data[6]), "Cluster 3 missing Node 6")
                }
            }
        }
    }
}
