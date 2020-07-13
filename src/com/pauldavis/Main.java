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

    // Input file name
    String inputFileName;

    // K-Means Parameters
    int numClusters, maxIterations, numRuns;
    double convergenceThreshold;

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

        System.out.println("Test");
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
