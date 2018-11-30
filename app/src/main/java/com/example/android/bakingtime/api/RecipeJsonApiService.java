package com.example.android.bakingtime.api;

import android.arch.lifecycle.LiveData;

import com.example.android.bakingtime.api.response_models.ApiResponse;
import com.example.android.bakingtime.db.model.RecipeEntry;

import java.util.List;

import retrofit2.http.GET;

/**
 *  API end points for network calls
 */
public interface RecipeJsonApiService {
    @GET("android-baking-app-json")
    LiveData<ApiResponse<List<RecipeEntry>>> getRecipes();
}