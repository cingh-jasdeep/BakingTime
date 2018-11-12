package com.example.android.bakingtime.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.example.android.bakingtime.db.model.IngredientEntry;
import com.example.android.bakingtime.db.model.StepEntry;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class IngredientandStepsDao {

    @Query("SELECT * FROM ingredient WHERE recipe_id = :recipeId")
    public abstract List<IngredientEntry> getIngredientsByRecipeId(int recipeId);

    @Insert(onConflict = REPLACE)
    public abstract void insertAllIngredients(List<IngredientEntry> ingredientEntries);

    @Query("DELETE FROM ingredient WHERE recipe_id = :recipeId")
    public abstract void deleteAllIngredientsByRecipeId(int recipeId);

    @Query("DELETE FROM ingredient")
    public abstract void deleteAllIngredients();

    @Transaction
    public void replaceIngredientsForRecipeId(List<IngredientEntry> ingredientEntries, int recipeId) {
        this.deleteAllIngredientsByRecipeId(recipeId);
        insertAllIngredients(ingredientEntries);
    }


    @Query("SELECT * FROM step WHERE  recipe_id = :recipeId")
    public abstract List<StepEntry> getStepsByRecipeId(int recipeId);

    @Insert(onConflict = REPLACE)
    public abstract void insertAllSteps(List<StepEntry> stepEntries);

    @Query("DELETE FROM step WHERE recipe_id = :recipeId")
    public abstract void deleteAllStepsByRecipeId(int recipeId);

    @Query("DELETE FROM step")
    public abstract void deleteAllSteps();

    @Transaction
    public void replaceStepsForRecipeId(List<StepEntry> stepEntries, int recipeId) {
        this.deleteAllStepsByRecipeId(recipeId);
        insertAllSteps(stepEntries);
    }

}

