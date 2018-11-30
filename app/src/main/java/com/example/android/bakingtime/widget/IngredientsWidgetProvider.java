package com.example.android.bakingtime.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.data.Constant;
import com.example.android.bakingtime.ui.select_recipe.SelectRecipeActivity;
import com.example.android.bakingtime.ui.select_recipe_step.SelectRecipeStepActivity;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.FragmentState;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.example.android.bakingtime.data.Constant.EXTRA_FRAGMENT_STATE;
import static com.example.android.bakingtime.data.Constant.EXTRA_RECIPE_ID;
import static com.example.android.bakingtime.data.Constant.INVALID_RECIPE_ID;

/**
 * Implementation of App Widget functionality.
 */
public class IngredientsWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                                int recipeId, String recipeName, String recipeIngredientsString) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_ingredients);
        Intent intent;
        PendingIntent pendingIntent;

        if(recipeId == INVALID_RECIPE_ID) {
            views.setViewVisibility(R.id.ll_widget_ingredients_content, View.INVISIBLE);
            views.setViewVisibility(R.id.tv_widget_before_first_load, View.VISIBLE);
            intent = new Intent(context, SelectRecipeActivity.class);
            pendingIntent =PendingIntent.getActivity(context,0,intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

        } else {
            views.setViewVisibility(R.id.ll_widget_ingredients_content, View.VISIBLE);
            views.setViewVisibility(R.id.tv_widget_before_first_load, View.INVISIBLE);

            views.setTextViewText(R.id.tv_widget_recipe_name, recipeName);
            views.setTextViewText(R.id.tv_widget_ingredients_list, recipeIngredientsString);

            intent = new Intent(context, SelectRecipeStepActivity.class);
            intent.putExtra(EXTRA_RECIPE_ID, recipeId);
            intent.putExtra(EXTRA_FRAGMENT_STATE,
                    FragmentState.IngredientsList);

            // Start an Activity from a Notification
            // https://developer.android.com/training/notify-user/navigation
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(intent);

            // Get the PendingIntent containing the entire back stack
            pendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        views.setOnClickPendingIntent(R.id.fl_widget_main_layout, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        IngredientsWidgetService.startActionUpdateIngredientsWidget(context);
    }

    public static void updateIngredientWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds,
                                               int recipe_id, String recipeName, String recipeIngredientsString) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId,
                    recipe_id, recipeName, recipeIngredientsString);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

