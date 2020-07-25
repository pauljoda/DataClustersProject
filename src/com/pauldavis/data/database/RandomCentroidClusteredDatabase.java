package com.pauldavis.data.database;

import com.pauldavis.data.Cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RandomCentroidClusteredDatabase extends AbstractClusteredDatabase {

    /*******************************************************************************************************************
     * Constructor                                                                                                     *
     *******************************************************************************************************************/

    /**
     * Generate a cluster database, will create random initial points
     *
     * @param rawData      Raw input
     * @param clusterCount How many clusters to create
     */
    public RandomCentroidClusteredDatabase(double[][] rawData, int clusterCount) {
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
        List<Integer> randomIndices = new ArrayList<>();

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
            assert closestCluster != null;
            closestCluster.addChild(rawData[i]);
        }

        return calculateSumSquaredError();
    }
}
