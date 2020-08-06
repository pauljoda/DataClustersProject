package com.pauldavis.data;

/**
 * Data structure for points, holds data and original indices
 */
public class Point {

    /*******************************************************************************************************************
     * Variables                                                                                                       *
     *******************************************************************************************************************/

    // Raw point data
    public double[] data;
    // Original Index
    public int index;

    /*******************************************************************************************************************
     * Constructor                                                                                                     *
     *******************************************************************************************************************/

    public Point(double[] data, int index) {
        this.data = data;
        this.index = index;
    }
}
