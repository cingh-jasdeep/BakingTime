package com.example.android.bakingtime.data.repository;

import android.arch.lifecycle.LiveData;
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
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;
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

    public LiveData<Resource<List<RecipeWithIngredientsAndSteps>>> loadRecipes
            (final boolean forceRefresh){

        AppExecutors appExecutors = AppExecutors.getInstance();

        return new NetworkBoundResource<List<RecipeWithIngredientsAndSteps>, List<RecipeEntry>>(appExecutors) {

            @Override
            protected void saveCallResult(@NonNull List<RecipeEntry> apiRecipeListing) {

                //if no recipes fetched from network even after successful network transaction
                if (apiRecipeListing.size() == 0) {
                    deleteAllRecipes();
                    return;
                }

                fixRecipeIdInStepsAndIngredients(apiRecipeListing);

                //update recipes in database
                mDb.beginTransaction();
                try {
//                    clear old recipes
                    replaceAllRecipes(apiRecipeListing);
                    replaceAllIngredientsAndStepsData(apiRecipeListing);

                    mDb.setTransactionSuccessful();
                } finally {
                    mDb.endTransaction();
                }

            }

            @Override
            protected boolean shouldFetch(@Nullable List<RecipeWithIngredientsAndSteps> data) {
                return (forceRefresh || data == null || data.size() == 0);
            }

            @NonNull
            @Override
            protected LiveData<List<RecipeWithIngredientsAndSteps>> loadFromDb() {
                return loadRecipesWithIngredientsAndSteps();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<List<RecipeEntry>>> createCall() {
                return mRecipeJsonApiService.getRecipes();
            }

        }.asLiveData();

    }

    public LiveData<RecipeWithIngredientsAndSteps> loadRecipeByIdWithIngredientsAndSteps(Integer recipeId) {
        Log.d(TAG, "Actively loading recipe by id in DataBase from Repo");
        return mDb.recipeIngredientsStepsDao().loadRecipeByIdWithIngredientsAndSteps(recipeId);
    }

    public RecipeWithIngredientsAndSteps getRecipeByIdWithIngredientsAndSteps(Integer recipeId) {
        Log.d(TAG, "Actively getting recipe by id in DataBase from Repo");
        return mDb.recipeIngredientsStepsDao().getRecipeByIdWithIngredientsAndSteps(recipeId);
    }

    public StepEntry getStep(Integer recipeId, Integer stepIndex) {
        Log.d(TAG, "Actively getting recipe step in DataBase from Repo");
        return mDb.recipeIngredientsStepsDao().getRecipeByIdWithIngredientsAndSteps(recipeId)
                .steps.get(stepIndex);
    }

    public LiveData<StepEntry> loadStep(Integer recipeId, Integer stepIndex) {
        Log.d(TAG, "Actively getting recipe step in DataBase from Repo");
        if(recipeId == null || stepIndex == null) {
            return null;
        } else {
            return mDb.ingredientandStepsDao().loadStepByRecipeIdAndRecipeListIndex(recipeId, stepIndex);
        }
    }

    private LiveData<List<RecipeWithIngredientsAndSteps>> loadRecipesWithIngredientsAndSteps() {
        Log.d(TAG, "Actively loading all recipes in DataBase from Repo");
        return mDb.recipeIngredientsStepsDao().loadRecipesWithIngredientsAndSteps();
    }

    public List<RecipeWithIngredientsAndSteps> getRecipesWithIngredientsAndSteps() {
        Log.d(TAG, "Actively loading all recipes in DataBase from Repo");
        return mDb.recipeIngredientsStepsDao().getRecipesWithIngredientsAndSteps();
    }


    private void insertAllIngredients(RecipeEntry recipe) {
        Log.d(TAG, "Actively inserting ingredients in DataBase from Repo");
        mDb.ingredientandStepsDao().insertAllIngredients(recipe.getIngredients());
    }

    private void deleteAllIngredients() {
        Log.d(TAG, "Actively deleting ingredients in DataBase from Repo");
        mDb.ingredientandStepsDao().deleteAllIngredients();
    }

    private void insertAllSteps(RecipeEntry recipe) {
        Log.d(TAG, "Actively inserting steps in DataBase from Repo");
        mDb.ingredientandStepsDao().insertAllSteps(recipe.getSteps());
    }

    private void deleteAllSteps() {
        Log.d(TAG, "Actively deleting steps in DataBase from Repo");
        mDb.ingredientandStepsDao().deleteAllSteps();
    }



    /**
     * used to add recipeid into api recipe listing steps and ingredients
     * @param apiRecipeListing api response for recipe listing
     */
    private void fixRecipeIdInStepsAndIngredients(List<RecipeEntry> apiRecipeListing) {

        for (RecipeEntry recipe:
                apiRecipeListing) {
            int recipeId = recipe.getId();

            //https://www.javatpoint.com/java-for-loop
            for (int i = 0; i < recipe.getSteps().size(); i++) {

                StepEntry step = recipe.getSteps().get(i);
                step.setRecipeId(recipeId);
                step.setRecipeListIndex(i);
            }

            for (IngredientEntry ingredient:
                    recipe.getIngredients()) {
                ingredient.setRecipeId(recipeId);
            }
        }

    }

    private void deleteAllRecipes() {
        Log.d(TAG, "Actively deleting recipes in DataBase from Repo");
        mDb.recipeDao().deleteAllRecipes();
    }

    private void replaceAllRecipes(@NonNull List<RecipeEntry> apiRecipeListing) {
        Log.d(TAG, "Actively replacing recipes in DataBase from Repo");
        mDb.recipeDao().replaceAllRecipes(apiRecipeListing);
    }

    /**
     * saves fixed recipe listing from api to db
     * @param apiRecipeListing api listing to saved to db
     */
    private void replaceAllIngredientsAndStepsData(List<RecipeEntry> apiRecipeListing) {

        deleteAllIngredients();
        deleteAllSteps();

        for (RecipeEntry recipe:
                apiRecipeListing) {

            insertAllSteps(recipe);
            insertAllIngredients(recipe);
        }
    }


}
