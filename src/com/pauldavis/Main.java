package com.pauldavis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static java.lang.System.exit;

/**
 * Project for Data Clusters class
 * Summer II - 2020
 * @author Paul Davis
 * @since 7-12-2020
 */
public class Main {

    // K-Means Parameters
    private static int numClusters, maxIterations, numRuns;
    private static double convergenceThreshold;
    private static ClusteredDatabase clusteredDatabase;

    /**
     * Main input, checks for given correct parameters:
     *  String  - F: name of the data file
     *  int     - K: number of clusters, >= 1
     *  int     - I: maximum number of iterations, positive
     *  int     - T: convergence threshold, non-negative
     *  int     - R: number of runs, positive
     * @param args
     */
    public static void main(String[] args) throws FileNotFoundException {
        // Check args
	    if(args.length != 5)
	        printUsageAndClose();

	    // Load input data
        File inputFile = new File(args[0]);
        Scanner scan = new Scanner(inputFile);
        String[] databaseParams = scan.nextLine().split(" ");

        // First two lines define size and dimensions
        int numPoints = Integer.parseInt(databaseParams[0]);
        int dimensions = Integer.parseInt(databaseParams[1]);
        System.out.println("Loading " + numPoints + " points with " + dimensions + " dimensions...");

        // Read in file
        double[][] data = new double [numPoints][dimensions];
        int i = 0;
        while(scan.hasNextLine()) {
            int j = 0;
            for(String number : scan.nextLine().split(" ")) {
                data[i][j] = Double.parseDouble(number);
                j++;
            }
            i++;
        }

        // Read the rest of the input arguments
        numClusters = Integer.parseInt(args[1]);
        maxIterations = Integer.parseInt(args[2]);
        convergenceThreshold = Double.parseDouble(args[3]);
        numRuns = Integer.parseInt(args[4]);

        // Used for tracking best run
        double lowestSSE = Double.MAX_VALUE;
        int lowestRun = -1;

        // Loop for number of runs
        for(int z = 0; z < numRuns; z++) {
            // Output formatting
            System.out.println("Run: " + (z + 1));
            System.out.println("-------------------------------------------");

            // Create the database object
            clusteredDatabase = new ClusteredDatabase(data, numClusters);
            // Used to track when done
            double lastSSE = Double.POSITIVE_INFINITY;

            // Loop for given iterations
            int iteration = 1;
            while (iteration <= maxIterations) {
                // Move centroids and reassign points to clusters
                clusteredDatabase.balanceCentroids();
                clusteredDatabase.rebuildClusters();

                // Get current SSE
                double currentSSE = clusteredDatabase.calculateSumSquaredError();
                System.out.println("Iteration " + iteration + ": SSE = " + currentSSE);
                iteration += 1;

                // Check if we should stop
                if(((lastSSE - currentSSE) / lastSSE) < convergenceThreshold) {
                    System.out.println();
                    // Find if this was best run
                    if(currentSSE < lowestSSE) {
                        lowestSSE = currentSSE;
                        lowestRun = z + 1;
                    }
                    // Leave iteration
                    break;
                }
                else // Done, update SSE for next round
                    lastSSE = currentSSE;
            }
        }

        // Print best run
        System.out.println("Best Run: " + lowestRun + " with SSE: " + lowestSSE);
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
