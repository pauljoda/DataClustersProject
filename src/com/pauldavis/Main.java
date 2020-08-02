package com.pauldavis;

import com.pauldavis.data.Results;
import com.pauldavis.data.database.RandomPartitionClusteredDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * Project for Data Clusters class
 * Summer II - 2020
 *
 * @author Paul Davis
 * @since 7-12-2020
 */
public class Main {

    /**
     * Main input, checks for given correct parameters:
     * String  - F: name of the data file
     * int     - K: number of clusters, >= 1
     * int     - I: maximum number of iterations, positive
     * int     - T: convergence threshold, non-negative
     * int     - R: number of runs, positive
     *
     * @param args Input
     */
    public static void main(String[] args) throws FileNotFoundException {
        // Check args
        if (args.length != 5)
            printUsageAndClose();

        // Load input data
        File inputFile = new File(args[0]);
        Scanner scan = new Scanner(inputFile);
        String[] databaseParams = scan.nextLine().split(" ");

        // First two lines define size and dimensions
        int numPoints = Integer.parseInt(databaseParams[0]);
        int dimensions = Integer.parseInt(databaseParams[1]);
        System.out.println("Loading " + numPoints + " points with " + dimensions + " dimensions...\n\n");

        // Read in file
        double[][] data = new double[numPoints][dimensions];
        int i = 0;
        while (scan.hasNextLine()) {
            int j = 0;
            for (String number : scan.nextLine().split(" ")) {
                data[i][j] = Double.parseDouble(number);
                j++;
            }
            i++;
        }

        // Normalize Data
        // Array to hold min and max, first is which attribute, second is min at 0 max at 1
        double[] min = new double[dimensions];
        double[] max = new double[dimensions];

        // Set min and max to highest/lowest values for easier comparison
        for (int att = 0; att < dimensions; att++) {
            min[att] = Double.MAX_VALUE;
            max[att] = Double.MIN_VALUE;
        }

        // Find min and max
        for (int entry = 0; entry < numPoints; entry++) {
            for (int attribute = 0; attribute < dimensions; attribute++) {
                // Current Value checking
                double attributeValue = data[entry][attribute];

                // Min
                if (attributeValue < min[attribute])
                    min[attribute] = attributeValue;

                // Max
                if (attributeValue > max[attribute])
                    max[attribute] = attributeValue;
            }
        }

        // Apply Normalization
        for (int entry = 0; entry < numPoints; entry++) {
            for (int attribute = 0; attribute < dimensions; attribute++) {
                double tempMin = min[attribute];
                double tempMax = max[attribute];
                // Normalize
                data[entry][attribute] =
                        ((data[entry][attribute] - tempMin) /
                                (tempMax - tempMin == 0 ? 1 : tempMax - tempMin)); // Catch divide by zero
            }
        }

        // Read the rest of the input arguments
        // K-Means Parameters
        //int numClusters = Integer.parseInt(args[1]);
        int maxIterations = Integer.parseInt(args[2]);
        double convergenceThreshold = Double.parseDouble(args[3]);
        int numRuns = Integer.parseInt(args[4]);

        String best_k = "BEST_K";
        Results.getResults().put(best_k, new HashMap<>());
        Results.getResults().get(best_k).put(Results.BEST_K_SW, -1.0);
        Results.getResults().get(best_k).put(Results.BEST_K_VALUE_SW, Double.MIN_VALUE);

        long maxClusters = (int) Math.round(Math.sqrt(numPoints / 2.0));
        double[] CH_Values = new double[(int) maxClusters - 1];
        for (int numClusters = 2; numClusters <= maxClusters; numClusters++) {
            System.out.println("K-Means with random partitions K=" + numClusters);

            // Setup Results
            String random_partitions = "RANDOM_PARTITIONS";
            Results.getResults().put(random_partitions, new HashMap<>());
            Results.getResults().get(random_partitions).put(Results.BEST_INITIAL_SSE_RUN, -1.0);
            Results.getResults().get(random_partitions).put(Results.BEST_INITIAL_SSE, Double.MAX_VALUE);
            Results.getResults().get(random_partitions).put(Results.BEST_ENDING_SSE_RUN, -1.0);
            Results.getResults().get(random_partitions).put(Results.BEST_ENDING_SSE, Double.MAX_VALUE);
            Results.getResults().get(random_partitions).put(Results.BEST_RUN_RUN, -1.0);
            Results.getResults().get(random_partitions).put(Results.BEST_RUN_COUNT, Double.MAX_VALUE);
            Results.getResults().get(best_k).put(Results.BEST_K_VALUE_CH, Double.MIN_VALUE);

            // Loop for number of runs
            for (int z = 0; z < numRuns; z++) {
                // Output formatting
                System.out.println("Run: " + (z + 1));
                System.out.println("-------------------------------------------");

                // Create the database object
                RandomPartitionClusteredDatabase randomPartitionClusteredDatabase = new RandomPartitionClusteredDatabase(data, numClusters);
                double initialSSE = randomPartitionClusteredDatabase.getInitialSSE();
                if (initialSSE < Results.getResults().get(random_partitions).get(Results.BEST_INITIAL_SSE)) {
                    Results.getResults().get(random_partitions).put(Results.BEST_INITIAL_SSE, initialSSE);
                    Results.getResults().get(random_partitions).put(Results.BEST_INITIAL_SSE_RUN, (z + 1.0));
                }
                // Used to track when done
                double lastSSE = Double.POSITIVE_INFINITY;

                // Loop for given iterations
                int iteration = 1;
                while (iteration <= maxIterations) {
                    // Move centroids and reassign points to clusters
                    randomPartitionClusteredDatabase.balanceCentroids();
                    randomPartitionClusteredDatabase.rebuildClusters();

                    // Get current SSE
                    double currentSSE = randomPartitionClusteredDatabase.calculateSumSquaredErrorInternal();
                    System.out.println("Iteration " + iteration + ": SSE = " + currentSSE);
                    iteration += 1;

                    // Check if we should stop
                    if (((lastSSE - currentSSE) / lastSSE) < convergenceThreshold || iteration == maxIterations - 1) {
                        // Find if this was best run
                        if (currentSSE < Results.getResults().get(random_partitions).get(Results.BEST_ENDING_SSE)) {
                            Results.getResults().get(random_partitions).put(Results.BEST_ENDING_SSE, currentSSE);
                            Results.getResults().get(random_partitions).put(Results.BEST_ENDING_SSE_RUN, (z + 1.0));
                        }

                        // Check if shortest Run
                        if (iteration < Results.getResults().get(random_partitions).get(Results.BEST_RUN_COUNT)) {
                            Results.getResults().get(random_partitions).put(Results.BEST_RUN_COUNT, (double) iteration);
                            Results.getResults().get(random_partitions).put(Results.BEST_RUN_RUN, (z + 1.0));
                        }

                        // Print SW
                        double sw = randomPartitionClusteredDatabase.calculateSilhouetteWidth();
                        // Check if best K
                        if (sw > Results.getResults().get(best_k).get(Results.BEST_K_VALUE_SW)) {
                            Results.getResults().get(best_k).put(Results.BEST_K_SW, (double) numClusters);
                            Results.getResults().get(best_k).put(Results.BEST_K_VALUE_SW, sw);
                        }
                        System.out.println("SW(" + numClusters + "): " + sw);

                        // Check CH
                        double externalError = randomPartitionClusteredDatabase.calculateSumSquaredErrorExternal();
                        double CH = ((externalError / currentSSE) * ((numPoints - numClusters) / (numClusters - 1.0)));
                        if (CH > Results.getResults().get(best_k).get(Results.BEST_K_VALUE_CH)) {
                            Results.getResults().get(best_k).put(Results.BEST_K_VALUE_CH, CH);
                            CH_Values[numClusters - 2] = CH;
                        }
                        System.out.println("CH(" + numClusters + "): " + CH);

                        System.out.println();

                        // Leave iteration
                        break;
                    } else // Done, update SSE for next round
                        lastSSE = currentSSE;
                }
            }


            /***************************************************************************************************************
             * Results                                                                                                     *
             ***************************************************************************************************************/

            // Print best runs partitions
            System.out.println("\nRandom Partition results for K=" + numClusters);
            System.out.println("Best Initial SSE: " + Results.getResults().get(random_partitions).get(Results.BEST_INITIAL_SSE) +
                    " on run: " + Results.getResults().get(random_partitions).get(Results.BEST_INITIAL_SSE_RUN));
            System.out.println("Best Ending SSE: " + Results.getResults().get(random_partitions).get(Results.BEST_ENDING_SSE) +
                    " on run: " + Results.getResults().get(random_partitions).get(Results.BEST_ENDING_SSE_RUN));
            System.out.println("Lowest iteration count: " + Results.getResults().get(random_partitions).get(Results.BEST_RUN_COUNT) +
                    " on run: " + Results.getResults().get(random_partitions).get(Results.BEST_RUN_RUN));
            System.out.println();
            System.out.println();
        }

        System.out.println("\nBest K: " + Results.getResults().get(best_k).get(Results.BEST_K_SW) +
                " when using SW=" + Results.getResults().get(best_k).get(Results.BEST_K_VALUE_SW));
        for (int Z = 0; Z < CH_Values.length; Z++) {
            System.out.println("CH(" + (Z + 2) + "): " + CH_Values[Z]);
        }
    }

    /**
     * Runs when improper inputs are given
     */
    private static void printUsageAndClose() {
        System.out.println("Usage:");
        System.out.println("program.java <string:fileName> <int(>=1):numClusters> " +
                "<int(positive):maxIterations> <double(non-negative):convergenceThreshold> <int(positive):maxRuns>");
        exit(1);
    }
}
