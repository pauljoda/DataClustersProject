package com.pauldavis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class ClusteredDatabase {

    // List of clusters
    List<Cluster> clusters;

    /**
     * Generate a clusted database, will create random initial points
     * @param rawData Raw input
     * @param clusterCount How many clusters to create
     */
    public ClusteredDatabase(double[][] rawData, int clusterCount) {
        clusters = new ArrayList<>();
        List<Integer> randomIndices = new ArrayList<>();
        Random rand = new Random();
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
            if(randomIndices.contains(i))
                continue;
            
            double closestClusterDist = Double.MAX_VALUE;
            Cluster closestCluster = null;
            for(Cluster cluster : clusters) {
                double tempDistance = cluster.distanceFromCentroid(rawData[i]);
                if(tempDistance < closestClusterDist) {
                    closestClusterDist = tempDistance;
                    closestCluster = cluster;
                }
            }
            closestCluster.addChild(rawData[i]);
        }
    }

    /**
     * Rebuilds clusters
     */
    public void rebuildClusters() {
        List<double[]> data = new ArrayList<>();
        // Grab non centroids and clear
        for(Cluster cluster : clusters) {
            data.addAll(cluster.children);
            cluster.clearChildren();
        }

        // Assign to closest cluster
        for (double[] datum : data) {
            double closestClusterDist = Double.MAX_VALUE;
            Cluster closestCluster = null;
            for (Cluster cluster : clusters) {
                double tempDistance = cluster.distanceFromCentroid(datum);
                if (tempDistance < closestClusterDist) {
                    closestClusterDist = tempDistance;
                    closestCluster = cluster;
                }
            }
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

    /**
     * Data structure for clusters
     */
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
        }

        /**
         * Balances centroid to middle of cluster
         */
        public void recalculateCentroid() {
            children.add(centroid);

            // Find middle of cluster
            double[] meanValues = new double[centroid.length];
            for(double[] point : children)
                for(int i = 0; i < point.length; i++)
                    meanValues[i] += point[i];
            for(int i = 0; i < meanValues.length; i ++) {
                double sum = meanValues[i];
                meanValues[i] = sum / children.size();
            }

            centroid = meanValues;
            // Find closest point
            double closestDist = Double.MAX_VALUE;
            int closestIndex = 0;
            for(int i = 0; i < children.size(); i ++) {
                double tempDist = distanceFromCentroid(children.get(i));
                if(tempDist < closestDist) {
                    closestDist = tempDist;
                    closestIndex = i;
                }
            }
            centroid = children.get(closestIndex);
            children.remove(closestIndex);
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
        public double distanceFromCentroid(double[] point) {
            double distance = 0;
            for(int i = 0; i < point.length; i ++) {
                distance += calculateSingleError(centroid[i], point[i]);
            }
            return distance;
        }

        /**
         * Calculates the Squared Error for this cluster
         * @return Squared Error
         */
        public double calculateSquaredError() {
            double error = 0;
            for(double[] point : children) {
                for(int i = 0; i < point.length; i ++) {
                    error += calculateSingleError(centroid[i], point[i]);
                }
            }
            return error;
        }
    }
}
