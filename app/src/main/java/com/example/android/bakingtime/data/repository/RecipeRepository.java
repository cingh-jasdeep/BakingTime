package com.example.android.bakingtime.data.repository;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.bakingtime.AppExecutors;
import com.example.android.bakingtime.api.RecipeJsonApiService;
import com.example.android.bakingtime.api.RecipeJsonApiServiceClient;
import com.example.android.bakingtime.api.api_utilities.Resource;
import com.example.android.bakingtime.api.response_models.ApiResponse;
import com.example.android.bakingtime.db.AppDatabase;
import com.example.android.bakingtime.db.model.IngredientEntry;
import com.example.android.bakingtime.db.model.RecipeEntry;
import com.example.android.bakingtime.db.model.StepEntry;

import java.util.List;

public class RecipeRepository {

    private static final String TAG = RecipeRepository.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static RecipeRepository sInstance;
    private final AppDatabase mDb;
    private final RecipeJsonApiService mRecipeJsonApiService;

    private RecipeRepository(AppDatabase db) {
        mDb = db;
        mRecipeJsonApiService = RecipeJsonApiServiceClient.create();
    }

    public synchronized static RecipeRepository getInstance(
            AppDatabase db) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new RecipeRepository(db);
                Log.d(TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    public LiveData<Resource<List<RecipeEntry>>> loadRecipes(){

        AppExecutors appExecutors = AppExecutors.getInstance();

        return new NetworkBoundResource<List<RecipeEntry>, List<RecipeEntry>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<RecipeEntry> apiRecipeListing) {

                //if no recipes fetched from network even after successful network transaction
                if (apiRecipeListing.size() == 0) {
                    RecipeRepository.this.deleteAllRecipes();
                    return;
                }

                fixIdStepsAndIngredients(apiRecipeListing);

                //update recipes in database
                mDb.beginTransaction();
                try {
                    //clear old movies
                    replaceAllRecipes(apiRecipeListing);
                    replaceAllIngredientsAndStepsData(apiRecipeListing);

                    mDb.setTransactionSuccessful();
                } finally {
                    mDb.endTransaction();
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable List<RecipeEntry> data) {
                return (data == null || data.size() == 0);
            }

            @NonNull
            @Override
            protected LiveData<List<RecipeEntry>> loadFromDb() {
                final MutableLiveData<List<RecipeEntry>> mutableRecipeList = new MutableLiveData<>();
                LiveData<List<RecipeEntry>> liveRecipeList = getRecipes();

                return Transformations.switchMap(liveRecipeList, new Function<List<RecipeEntry>, LiveData<List<RecipeEntry>>>() {
                    @Override
                    public LiveData<List<RecipeEntry>> apply(List<RecipeEntry> recipeEntries) {
                        mutableRecipeList.setValue(getRecipesWithIngredientsAndStepsFromDb(recipeEntries));
                        return mutableRecipeList;
                    }
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<RecipeEntry>>> createCall() {
                return mRecipeJsonApiService.getRecipes();
            }

        }.asLiveData();

    }

    private LiveData<List<RecipeEntry>> getRecipes() {
        return mDb.recipeDao().getRecipes();
    }

    private List<RecipeEntry> getRecipesWithIngredientsAndStepsFromDb(List<RecipeEntry> recipeEntries) {

        for (RecipeEntry recipe:
                recipeEntries) {

            recipe.setIngredients(getIngredientsByRecipeId(recipe));
            recipe.setSteps(getStepsByRecipeId(recipe));

        }
        return  recipeEntries;
    }

    private List<StepEntry> getStepsByRecipeId(RecipeEntry recipe) {
        return mDb.ingredientandStepsDao().getStepsByRecipeId(recipe.getId());
    }

    private List<IngredientEntry> getIngredientsByRecipeId(RecipeEntry recipe) {
        return mDb.ingredientandStepsDao().getIngredientsByRecipeId(recipe.getId());
    }

    private void replaceAllIngredientsAndStepsData(List<RecipeEntry> apiRecipeListing) {

        deleteAllIngredients();
        deleteAllSteps();

        for (RecipeEntry recipe:
                apiRecipeListing) {

            insertAllSteps(recipe);
            insertAllIngredients(recipe);
        }
    }

    private void insertAllIngredients(RecipeEntry recipe) {
        mDb.ingredientandStepsDao().insertAllIngredients(recipe.getIngredients());
    }

    private void insertAllSteps(RecipeEntry recipe) {
        mDb.ingredientandStepsDao().insertAllSteps(recipe.getSteps());
    }

    private void deleteAllSteps() {
        mDb.ingredientandStepsDao().deleteAllSteps();
    }

    private void deleteAllIngredients() {
        mDb.ingredientandStepsDao().deleteAllIngredients();
    }

    private void replaceAllRecipes(@NonNull List<RecipeEntry> apiRecipeListing) {
        mDb.recipeDao().replaceAllRecipes(apiRecipeListing);
    }

    private void insertAllRecipes(List<RecipeEntry> recipeEntries) {
        mDb.recipeDao().insertAllRecipes(recipeEntries);
    }


    public void deleteAllRecipes() {
        mDb.recipeDao().deleteAllRecipes();
    }


    private void fixIdStepsAndIngredients(List<RecipeEntry> apiRecipeListing) {

        for (RecipeEntry recipe:
                apiRecipeListing) {
            int recipeId = recipe.getId();

            for (StepEntry step:
                 recipe.getSteps()) {
                step.setRecipeId(recipeId);
            }

            for (IngredientEntry ingredient:
                    recipe.getIngredients()) {
                ingredient.setRecipeId(recipeId);
            }
        }

    }


}
