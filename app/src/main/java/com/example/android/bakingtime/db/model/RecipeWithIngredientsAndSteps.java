package com.example.android.bakingtime.db.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Relation;

import java.util.List;

public class RecipeWithIngredientsAndSteps {

    @Embedded
    public RecipeEntry recipe;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    public List<IngredientEntry> ingredients;

    @Relation(parentColumn = "id", entityColumn = "recipe_id")
    public List<StepEntry> steps;

}
