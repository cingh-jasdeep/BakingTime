package com.example.android.bakingtime.api;

import com.example.android.bakingtime.api.api_utilities.LiveDataCallAdapterFactory;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.example.android.bakingtime.data.Constant.RECIPE_JSON_BASE_SIMPLE_URL;
import static com.example.android.bakingtime.data.Constant.RECIPE_JSON_BASE_URL;


public class RecipeJsonApiServiceClient {
    //Constant used for Logs
    private static final String LOG_TAG = RecipeJsonApiServiceClient.class.getSimpleName();

    /**
     * Creates the Retrofit Service for API
     *
     * @return The {@link RecipeJsonApiService} instance
     */
    public static RecipeJsonApiService create() {

        //Building the HTTPClient with the stetho
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();

        //Returning the Retrofit service for the BASE_URL
        return new Retrofit.Builder()
                .baseUrl(RECIPE_JSON_BASE_SIMPLE_URL)
                //Using the HTTPClient setup
                .client(httpClient)
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                //GSON converter to convert the JSON elements to a POJO
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                //Creating the service for the defined API Interface
                .create(RecipeJsonApiService.class);
    }
}