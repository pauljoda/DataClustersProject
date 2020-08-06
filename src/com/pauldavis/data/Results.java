package com.pauldavis.data;

import java.util.HashMap;

/**
 * Used to track the results for best runs and the value associated
 */
public class Results {

    /*******************************************************************************************************************
     * MAP IDS                                                                                                         *
     *******************************************************************************************************************/

    public static final String BEST_INITIAL_SSE_RUN = "Best_Initial_SSE_RUN";
    public static final String BEST_INITIAL_SSE = "Best_Initial_SSE";
    public static final String BEST_ENDING_SSE_RUN = "Best_Ending_SSE_Run";
    public static final String BEST_ENDING_SSE = "Best_Ending_SSE";
    public static final String BEST_RUN_RUN = "Best_Run_Run";
    public static final String BEST_RUN_COUNT = "Best_Run_Count";
    public static final String BEST_JACCARD = "Best_Jaccard";
    public static final String BEST_RAND = "Best_Rand";

    /*******************************************************************************************************************
     * Variables                                                                                                       *
     *******************************************************************************************************************/

    private static HashMap<String, HashMap<String, Double>> results = new HashMap<>();


    /*******************************************************************************************************************
     * Accessors/Mutators                                                                                              *
     *******************************************************************************************************************/

    public static HashMap<String, HashMap<String, Double>> getResults() {
        return results;
    }

    public static void setResults(HashMap<String, HashMap<String, Double>> results) {
        Results.results = results;
    }
}
