package com.pauldavis.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data structure for clusters
 */
public class Cluster {

    /*******************************************************************************************************************
     * Variables                                                                                                       *
     *******************************************************************************************************************/

    // Center Point
    private double[] centroid = null;
    // ALl points, centroid included
    private List<double[]> children;


    /*******************************************************************************************************************
     * Constructor                                                                                                     *
     *******************************************************************************************************************/

    /**
     * Base Constructor, will not create an initial centroid
     */
    public Cluster() {
        children = new ArrayList<>();
    }

    /**
     * Creates a cluster with given initial cluster
     *
     * @param initialPoint First centroid
     */
    public Cluster(double[] initialPoint) {
        this();
        centroid = initialPoint;
        children.add(initialPoint);
    }


    /*******************************************************************************************************************
     * Methods                                                                                                         *
     *******************************************************************************************************************/

    /**
     * Clear out the children for new assignment
     */
    public void clearChildren() {
        children = new ArrayList<>();
    }

    /**
     * Adds child to cluster
     *
     * @param child The child to add
     */
    public void addChild(double[] child) {
        children.add(child);
    }

    /**
     * Balances centroid to middle of cluster
     */
    public void recalculateCentroid() {
        // Find middle of cluster
        double[] meanValues = new double[centroid != null ? centroid.length : children.get(0).length];
        for (double[] point : children)
            for (int i = 0; i < point.length; i++)
                meanValues[i] += point[i];
        for (int i = 0; i < meanValues.length; i++) {
            double sum = meanValues[i];
            meanValues[i] = sum / children.size();
        }

        // We assign values to perfect center, then find closest points to this "false" centroid
        centroid = meanValues;
    }

    /**
     * Calculates single squared error of two points
     *
     * @param x X
     * @param y Y
     * @return Squared distance between
     */
    public double calculateSingleError(double x, double y) {
        return (x - y) * (x - y);
    }

    /**
     * Calculate the squared distance between two points
     *
     * @param point Input point
     * @return Squared distance between points
     */
    public double errorFromCentroid(double[] point) {
        double distance = 0;
        for (int i = 0; i < point.length; i++)
            distance += calculateSingleError(centroid[i], point[i]);
        return distance;
    }

    /**
     * Calculates the Squared Error for this cluster
     *
     * @return Squared Error
     */
    public double calculateSquaredError() {
        double error = 0;
        for (double[] point : children)
            error += errorFromCentroid(point);
        return error;
    }


    /*******************************************************************************************************************
     * Accessors/Mutators                                                                                              *
     *******************************************************************************************************************/

    public double[] getCentroid() {
        return centroid;
    }

    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    public List<double[]> getChildren() {
        return children;
    }

    public void setChildren(List<double[]> children) {
        this.children = children;
    }
}