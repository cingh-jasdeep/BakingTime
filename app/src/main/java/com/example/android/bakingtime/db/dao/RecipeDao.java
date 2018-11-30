package com.example.android.bakingtime.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.example.android.bakingtime.db.model.RecipeEntry;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class RecipeDao {

    @Query("SELECT * FROM recipe WHERE id = :recipeId")
    public abstract LiveData<RecipeEntry> getRecipeById(int recipeId);


    @Query("SELECT * FROM recipe")
    public abstract LiveData<List<RecipeEntry>> getRecipes();

    @Insert(onConflict = REPLACE)
    public abstract void insertAllRecipes(List<RecipeEntry> recipeEntries);

    @Query("DELETE FROM recipe")
    public abstract void deleteAllRecipes();

    @Transaction
    public void replaceAllRecipes(List<RecipeEntry> recipeEntries) {
        deleteAllRecipes();
        insertAllRecipes(recipeEntries);
    }
}
