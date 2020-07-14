package com.pauldavis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Data Structure to hold a group of clusters and points
 */
public class ClusteredDatabase {

    // List of clusters
    List<Cluster> clusters;

    /**
     * Generate a cluster database, will create random initial points
     * @param rawData Raw input
     * @param clusterCount How many clusters to create
     */
    public ClusteredDatabase(double[][] rawData, int clusterCount) {
        // Initialize
        clusters = new ArrayList<>();
        List<Integer> randomIndices = new ArrayList<>();
        Random rand = new Random();

        // Generate random points, making sure no duplicates
        for(int i = 0; i < clusterCount; i ++) {
            int test = rand.nextInt(rawData.length);
            while(randomIndices.contains(test))
                test = rand.nextInt(rawData.length);
            randomIndices.add(test);
        }

        // Find random clusters
        for(int point : randomIndices) {
            clusters.add(new Cluster(rawData[point]));
        }
        
        // Assign to clusters
        for(int i = 0; i < rawData.length; i++) {
            // Don't want to check centroids, better logic after all made just check these for now
            if(randomIndices.contains(i))
                continue;

            // Values to track best cluster
            double closestClusterDist = Double.MAX_VALUE;
            Cluster closestCluster = null;

            // Loop Clusters
            for(Cluster cluster : clusters) {
                // Test distance to this cluster
                double tempDistance = cluster.errorFromCentroid(rawData[i]);

                // If better than anything seen, this is best option
                if(tempDistance < closestClusterDist) {
                    closestClusterDist = tempDistance;
                    closestCluster = cluster;
                }
            }

            // Assign point to what was found to be closest
            closestCluster.addChild(rawData[i]);
        }
    }

    /**
     * Rebuilds clusters
     */
    public void rebuildClusters() {
        // List to hold all non centroids points, dumped from clusters
        List<double[]> data = new ArrayList<>();

        // Grab non centroids and clear
        for(Cluster cluster : clusters) {
            data.addAll(cluster.children);
            cluster.clearChildren();
        }

        // Assign to closest cluster
        for (double[] datum : data) {

            // Tracking for closest cluster
            double closestClusterDist = Double.MAX_VALUE;
            Cluster closestCluster = null;

            // Loop clusters
            for (Cluster cluster : clusters) {
                // Test for distance
                double tempDistance = cluster.errorFromCentroid(datum);

                // If this is closer, remember
                if (tempDistance < closestClusterDist) {
                    closestClusterDist = tempDistance;
                    closestCluster = cluster;
                }
            }

            // Add to what was found to be closest
            closestCluster.addChild(datum);
        }
    }

    /**
     * Re-balance centroids of clusters
     */
    public void balanceCentroids() {
        for(Cluster cluster : clusters)
            cluster.recalculateCentroid();
    }

    /**
     * Calculates the SumSquaredError (SSE)
     * @return SSE
     */
    public double calculateSumSquaredError() {
        double error = 0;
        for(Cluster cluster : clusters)
            error += cluster.calculateSquaredError();
        return error;
    }

    /*******************************************************************************************************************
     * Data structure for clusters                                                                                     *
     *******************************************************************************************************************/
    private static class Cluster {

        double[] centroid;
        List<double[]> children;

        /**
         * Creates a cluster with given initial cluster
         * @param initialPoint First centroid
         */
        public Cluster(double[] initialPoint) {
            centroid = initialPoint;
            children = new ArrayList<>();
            children.add(initialPoint);
        }

        /**
         * Balances centroid to middle of cluster
         */
        public void recalculateCentroid() {
            // Find middle of cluster
            double[] meanValues = new double[centroid.length];
            for(double[] point : children)
                for(int i = 0; i < point.length; i++)
                    meanValues[i] += point[i];
            for(int i = 0; i < meanValues.length; i ++) {
                double sum = meanValues[i];
                meanValues[i] = sum / children.size();
            }

            // We assign values to perfect center, then find closest points to this "false" centroid
            centroid = meanValues;
        }

        /**
         * Adds child to cluster
         * @param child The child to add
         */
        public void addChild(double[] child) {
            children.add(child);
        }

        /**
         * Clear out the children for new assignment
         */
        public void clearChildren() {
            children = new ArrayList<>();
        }

        /**
         * Calculates single squared error of two points
         * @param x X
         * @param y Y
         * @return Squared distance between
         */
        public double calculateSingleError(double x, double y) {
            return (x - y) * (x - y);
        }

        /**
         * Calculate the squared distance between two points
         * @param point Input point
         * @return Squared distance between points
         */
        public double errorFromCentroid(double[] point) {
            double distance = 0;
            for(int i = 0; i < point.length; i ++)
                distance += calculateSingleError(centroid[i], point[i]);
            return distance;
        }

        /**
         * Calculates the Squared Error for this cluster
         * @return Squared Error
         */
        public double calculateSquaredError() {
            double error = 0;
            for(double[] point : children)
                error += errorFromCentroid(point);
            return error;
        }
    }
}
