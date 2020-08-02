package com.pauldavis.data.database;

import com.pauldavis.data.Cluster;

import java.util.Random;

public class RandomPartitionClusteredDatabase extends AbstractClusteredDatabase {

    /*******************************************************************************************************************
     * Constructor                                                                                                     *
     *******************************************************************************************************************/

    /**
     * Generate a cluster database, will create random initial points
     *
     * @param rawData      Raw input
     * @param clusterCount How many clusters to create
     */
    public RandomPartitionClusteredDatabase(double[][] rawData, int clusterCount) {
        super(rawData, clusterCount);
    }


    /*******************************************************************************************************************
     * Abstract Implementation                                                                                         *
     *******************************************************************************************************************/

    /**
     * Will create clusters and assign random centroids then distribute
     * @param rawData Data to cluster, first is entry, second is array of attributes
     * @param clusterCount How many clusters to create
     * @return Initial SSE
     */
    @Override
    public double initialize(double[][] rawData, int clusterCount) {
        Random rand = new Random();

        // Add empty clusters
        for(int i = 0; i < clusterCount; i++)
            clusters.add(new Cluster());

        // Assign to clusters
        for (double[] rawDatum : rawData)
            clusters.get(rand.nextInt(clusterCount)).addChild(rawDatum);

        // Calculate Centroids
        for(Cluster cluster : clusters)
            cluster.recalculateCentroid();

        return calculateSumSquaredErrorInternal();
    }
}
