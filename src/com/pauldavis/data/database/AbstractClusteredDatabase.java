package com.pauldavis.data.database;

import com.pauldavis.data.Cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Base for Clustered Databases
 */
public abstract class AbstractClusteredDatabase {

    /*******************************************************************************************************************
     * Variables                                                                                                       *
     *******************************************************************************************************************/

    // List of clusters
    protected List<Cluster> clusters;
    protected double initialSSE;


    /*******************************************************************************************************************
     * Constructor                                                                                                     *
     *******************************************************************************************************************/

    /**
     * Generate a cluster database, will create random initial points
     * @param rawData Raw input
     * @param clusterCount How many clusters to create
     */
    public AbstractClusteredDatabase(double[][] rawData, int clusterCount) {
        // Initialize
        clusters = new ArrayList<>();
        initialSSE = initialize(rawData, clusterCount);
    }


    /*******************************************************************************************************************
     * Abstract Functions                                                                                              *
     *******************************************************************************************************************/

    /**
     * Generate a database with the raw data and cluster count. Clusters should be created and points assigned by end
     * of this method. Returns the initial SSE post initialization. This will be last access to raw data, assign all
     * points by now
     * @param rawData Data to cluster, first is entry, second is array of attributes
     * @param clusterCount How many clusters to create
     * @return Initial SSE, before any re-balancing
     */
    public abstract double initialize(double[][] rawData, int clusterCount);


    /*******************************************************************************************************************
     * Methods                                                                                                         *
     *******************************************************************************************************************/

    /**
     * Rebuilds clusters
     */
    public void rebuildClusters() {
        // List to hold all non centroids points, dumped from clusters
        List<double[]> data = new ArrayList<>();

        // Grab non centroids and clear
        for(Cluster cluster : clusters) {
            data.addAll(cluster.getChildren());
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
            assert closestCluster != null;
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
     * Calculates the SumSquaredError (SSE), within cluster distance
     * @return SSE
     */
    public double calculateSumSquaredErrorInternal() {
        double error = 0;
        for(Cluster cluster : clusters)
            error += cluster.calculateSquaredError();
        return error;
    }

    /**
     * Calculates the SumSquaredError (SSE), outside cluster disance
     * @return SSE
     */
    public double calculateSumSquaredErrorExternal() {
        double error = 0;
        for(Cluster cluster : clusters)
            error += cluster.calculateSquaredErrorExternal(clusters);
        return error;
    }

    /**
     * Calculate the Silhouette Width average of all points
     * @return Silhouette Width
     */
    public double calculateSilhouetteWidth() {
        double silhouette = 0;

        for(Cluster cluster : clusters)
            silhouette += cluster.calculateSilhouette(getClosestCluster(cluster));
        silhouette /= clusters.size();

        return silhouette;
    }

    /**
     * Find cluster closest to given
     * @param cluster Cluster to find closest
     * @return Closest cluster
     */
    public Cluster getClosestCluster(Cluster cluster) {
        Cluster closest = new Cluster();
        double distance = Double.MAX_VALUE;

        for(Cluster localCluster : clusters) {
            if(localCluster == cluster) continue;

            double test = cluster.errorFromCentroid(localCluster.getCentroid());
            if(test < distance) {
                closest = localCluster;
                distance = test;
            }
        }

        return closest;
    }

    /*******************************************************************************************************************
     * Accessors/Mutators                                                                                              *
     *******************************************************************************************************************/

    public List<Cluster> getClusters() {
        return clusters;
    }

    public void setClusters(List<Cluster> clusters) {
        this.clusters = clusters;
    }

    public double getInitialSSE() {
        return initialSSE;
    }

    public void setInitialSSE(double initialSSE) {
        this.initialSSE = initialSSE;
    }
}
