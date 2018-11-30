package com.example.android.bakingtime.ui.select_recipe;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.android.bakingtime.api.api_utilities.Resource;
import com.example.android.bakingtime.data.repository.RecipeRepository;
import com.example.android.bakingtime.db.AppDatabase;
import com.example.android.bakingtime.db.model.RecipeEntry;
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;

import java.util.List;

public class SelectRecipeViewModel extends AndroidViewModel {

    private static final String TAG = SelectRecipeViewModel.class.getSimpleName();

    private LiveData<Resource<List<RecipeWithIngredientsAndSteps>>> recipeListResourceData;
    private RecipeRepository mRepo;

    public SelectRecipeViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(application);
        mRepo = RecipeRepository.getInstance(db);

    }

    public void loadRecipes(boolean forceRefresh) {
        recipeListResourceData = mRepo.loadRecipes(forceRefresh);
    }

    public LiveData<Resource<List<RecipeWithIngredientsAndSteps>>> getRecipeListResourceData() {
        return recipeListResourceData;
    }



}
