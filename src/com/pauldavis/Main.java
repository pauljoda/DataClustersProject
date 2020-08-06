package com.pauldavis;

import com.pauldavis.data.Point;
import com.pauldavis.data.Results;
import com.pauldavis.data.database.RandomCentroidClusteredDatabase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

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
        if (args.length != 4)
            printUsageAndClose();

        // Load input data
        File inputFile = new File(args[0]);
        Scanner scan = new Scanner(inputFile);
        String[] databaseParams = scan.nextLine().split(" ");

        // First two lines define size and dimensions
        int numPoints = Integer.parseInt(databaseParams[0]);
        int dimensions = (Integer.parseInt(databaseParams[1]) - 1);
        int numClusters = Integer.parseInt(databaseParams[2]);
        System.out.println("Loading " + numPoints + " points with " + dimensions + " dimensions...\n\n");

        // Read in file
        double[][] data = new double[numPoints][dimensions];
        double[] labels = new double[numPoints];
        int i = 0;
        while (scan.hasNextLine()) {
            List<String> line = Arrays.stream(scan.nextLine().split(" ")).filter(x -> !x.isEmpty()).collect(Collectors.toList());
            for (int j = 0; j < line.size(); j++) {
                if (j == dimensions)
                    labels[i] = Double.parseDouble(line.get(j));
                else
                    data[i][j] = Double.parseDouble(line.get(j));
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
        int maxIterations = Integer.parseInt(args[1]);
        double convergenceThreshold = Double.parseDouble(args[2]);
        int numRuns = Integer.parseInt(args[3]);

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
        Results.getResults().get(random_partitions).put(Results.BEST_JACCARD, 0.0);
        Results.getResults().get(random_partitions).put(Results.BEST_RAND, 0.0);

        // Loop for number of runs
        for (int z = 0; z < numRuns; z++) {
            // Output formatting
            System.out.println("Run: " + (z + 1));
            System.out.println("-------------------------------------------");

            // Create the database object
            RandomCentroidClusteredDatabase randomCentroidClusteredDatabase = new RandomCentroidClusteredDatabase(data, numClusters);
            double initialSSE = randomCentroidClusteredDatabase.getInitialSSE();
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
                randomCentroidClusteredDatabase.balanceCentroids();
                randomCentroidClusteredDatabase.rebuildClusters();

                // Get current SSE
                double currentSSE = randomCentroidClusteredDatabase.calculateSumSquaredErrorInternal();
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

                    // Calculate External Validation
                    double truePositives = 0;
                    double falseNegatives = 0;
                    double falsePositives = 0;
                    double trueNegatives = 0;
                    double[] generatedLabels = randomCentroidClusteredDatabase.generateIndexClusterLabelTable(data.length);

                    for(int pointI = 0; pointI < data.length; pointI++) {
                        for(int pointJ = 0; pointJ < data.length; pointJ++) {
                            if(pointI == pointJ) continue;

                            // Test TP and FN
                            if(labels[pointI] == labels[pointJ]) {
                                // TP
                                if(generatedLabels[pointI] == generatedLabels[pointJ])
                                    truePositives++;
                                else
                                    falseNegatives++;
                            }
                            else {
                                // FP
                                if(generatedLabels[pointI] == generatedLabels[pointJ])
                                    falsePositives++;
                                else
                                    trueNegatives++;
                            }
                        }
                    }

                    double Jaccard = ((truePositives) / (truePositives + falseNegatives + falsePositives));
                    double rand = ((truePositives + trueNegatives) / (trueNegatives + truePositives + falseNegatives + falsePositives));

                    if (Jaccard > Results.getResults().get(random_partitions).get(Results.BEST_JACCARD)) {
                        Results.getResults().get(random_partitions).put(Results.BEST_JACCARD, Jaccard);
                    }

                    if (Jaccard > Results.getResults().get(random_partitions).get(Results.BEST_RAND)) {
                        Results.getResults().get(random_partitions).put(Results.BEST_RAND, rand);
                    }

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
        System.out.println("Best Jaccard: " + Results.getResults().get(random_partitions).get(Results.BEST_JACCARD));
        System.out.println("Best Rand: " + Results.getResults().get(random_partitions).get(Results.BEST_RAND));
        System.out.println();
        System.out.println();
    }

    /**
     * Runs when improper inputs are given
     */
    private static void printUsageAndClose() {
        System.out.println("Usage:");
        System.out.println("program.java <string:fileName> " +
                "<int(positive):maxIterations> <double(non-negative):convergenceThreshold> <int(positive):maxRuns>");
        exit(1);
    }
}
