package com.pauldavis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

        numClusters = Integer.parseInt(args[1]);
        maxIterations = Integer.parseInt(args[2]);
        convergenceThreshold = Double.parseDouble(args[3]);
        numRuns = Integer.parseInt(args[4]);

        for(int z = 0; z < numRuns; z++) {
            System.out.println("Run: " + (z + 1));
            System.out.println("-------------------------------------------");
            clusteredDatabase = new ClusteredDatabase(data, numClusters);
            double lastSSE = clusteredDatabase.calculateSumSquaredError();
            System.out.println("Iteration 1: SSE = " + lastSSE);

            int iteration = 2;
            while (iteration <= maxIterations) {
                clusteredDatabase.balanceCentroids();
                clusteredDatabase.rebuildClusters();
                double currentSSE = clusteredDatabase.calculateSumSquaredError();
                System.out.println("Iteration " + iteration + ": SSE = " + currentSSE);
                iteration += 1;
                if(((lastSSE - currentSSE) / lastSSE) < convergenceThreshold) {
                    System.out.println();
                    break;
                }
                else
                    lastSSE = currentSSE;
            }
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
