package com.example.android.bakingtime.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.data.repository.RecipeRepository;
import com.example.android.bakingtime.db.AppDatabase;
import com.example.android.bakingtime.db.model.IngredientEntry;
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.example.android.bakingtime.data.Constant.INGREDIENT_QUANTITY_NUMBER_FORMAT;
import static com.example.android.bakingtime.data.Constant.INVALID_RECIPE_ID;

public class IngredientsWidgetService extends IntentService {

    private static final String TAG = IngredientsWidgetService.class.getSimpleName();
    public static final String ACTION_UPDATE_INGREDIENTS_WIDGET =
            "com.example.android.bakingtime.action.update_ingredients_widget";

    public IngredientsWidgetService() {
        super(TAG);
    }

    public static void startActionUpdateIngredientsWidget(Context context) {
        Intent intent = new Intent(context, IngredientsWidgetService.class);
        intent.setAction(ACTION_UPDATE_INGREDIENTS_WIDGET);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_INGREDIENTS_WIDGET.equals(action)) {
                handleUpdateIngredientsWidget();
            }
        }
    }

    private void handleUpdateIngredientsWidget() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, IngredientsWidgetProvider.class));
        RecipeRepository repo = RecipeRepository.getInstance(AppDatabase.getInstance(this));
        String recipeName = null;
        StringBuilder recipeIngredientsString = new StringBuilder();


        int recipeId = sp.getInt(getString(R.string.pref_recipe_id_key), INVALID_RECIPE_ID);
        if(recipeId != INVALID_RECIPE_ID) {
            RecipeWithIngredientsAndSteps recipeWithData = repo.getRecipeByIdWithIngredientsAndSteps(recipeId);

            if( recipeWithData != null) {

                recipeName = recipeWithData.recipe.getName();
                buildRecipeIngredientsString(recipeIngredientsString, recipeWithData);
            }
        } else {
            List<RecipeWithIngredientsAndSteps> recipeWithIngredientsAndStepsList =
                    repo.getRecipesWithIngredientsAndSteps();

            if(recipeWithIngredientsAndStepsList.size() !=0 ) {

                RecipeWithIngredientsAndSteps recipeWithData = recipeWithIngredientsAndStepsList.get(0);

                recipeId = recipeWithData.recipe.getId();
                recipeName = recipeWithData.recipe.getName();

                buildRecipeIngredientsString(recipeIngredientsString, recipeWithIngredientsAndStepsList.get(0));

            }
        }

        IngredientsWidgetProvider.updateIngredientWidgets(this, appWidgetManager, appWidgetIds,
                recipeId, recipeName, recipeIngredientsString.toString());
    }

    private void buildRecipeIngredientsString(StringBuilder recipeIngredientsString, RecipeWithIngredientsAndSteps recipeWithData) {
        for (IngredientEntry ingredient:
             recipeWithData.ingredients) {

            String ingredientMeasure = ingredient.getMeasure();

            ingredientMeasure =  ingredientMeasure.substring(0,1).toUpperCase() +
                    ingredientMeasure.substring(1).toLowerCase();

            recipeIngredientsString.append("-\t")
                    .append(INGREDIENT_QUANTITY_NUMBER_FORMAT.format(ingredient.getQuantity()))
                    .append(" ").append(ingredientMeasure)
                    .append(",\t").append(ingredient.getIngredient()).append("\n");
        }
    }
}
