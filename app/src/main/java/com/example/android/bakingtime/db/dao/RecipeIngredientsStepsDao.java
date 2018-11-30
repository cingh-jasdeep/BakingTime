package com.example.android.bakingtime.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;

import java.util.List;

@Dao
public abstract class RecipeIngredientsStepsDao {

    @Transaction
    @Query("SELECT * from recipe")
    public abstract LiveData<List<RecipeWithIngredientsAndSteps>> loadRecipesWithIngredientsAndSteps();

    @Transaction
    @Query("SELECT * from recipe")
    public abstract List<RecipeWithIngredientsAndSteps> getRecipesWithIngredientsAndSteps();


    @Transaction
    @Query("SELECT * from recipe where id = :recipeId limit 1")
    public abstract LiveData<RecipeWithIngredientsAndSteps> loadRecipeByIdWithIngredientsAndSteps(int recipeId);

    @Transaction
    @Query("SELECT * from recipe where id = :recipeId limit 1")
    public abstract RecipeWithIngredientsAndSteps getRecipeByIdWithIngredientsAndSteps(int recipeId);

}
