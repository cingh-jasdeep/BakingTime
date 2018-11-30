package com.example.android.bakingtime.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Constant {

    /*
     * DB related constants
     */

    public static final String APP_DATABASE_NAME = "baking_time";

    /*
     * API Related constants
     */

    public static final String RECIPE_JSON_BASE_SIMPLE_URL = "http://go.udacity.com/";
    public static final String RECIPE_JSON_BASE_URL = "https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/";


    /*
     * Recipe Step Constants
     */

    public static final int INVALID_RECIPE_ID = -1;

    public static final int DEFAULT_RECIPE_STEP_INDEX = 0;
    public static final int INVALID_RECIPE_STEP_INDEX = -1;

    public static final int INTRODUCTION_STEPS = 1;

    public static final boolean DEFAULT_TWO_PANE = false;

    public static final NumberFormat INGREDIENT_QUANTITY_NUMBER_FORMAT = new DecimalFormat("##.##");


    public static final String EXTRA_RECIPE_STEP_INDEX = "recipe_step_index";
    public static final String EXTRA_FRAGMENT_STATE = "fragment_state";
    public static final String EXTRA_RECIPE_ID = "recipe_id";
    public static final String EXTRA_TWO_PANE = "two_pane";
}
