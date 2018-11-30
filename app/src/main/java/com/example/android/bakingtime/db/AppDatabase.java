package com.example.android.bakingtime.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.example.android.bakingtime.db.dao.IngredientandStepsDao;
import com.example.android.bakingtime.db.dao.RecipeDao;
import com.example.android.bakingtime.db.dao.RecipeIngredientsStepsDao;
import com.example.android.bakingtime.db.model.IngredientEntry;
import com.example.android.bakingtime.db.model.RecipeEntry;
import com.example.android.bakingtime.db.model.StepEntry;

import static com.example.android.bakingtime.data.Constant.APP_DATABASE_NAME;

@Database(entities = {RecipeEntry.class,
        IngredientEntry.class,
        StepEntry.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = APP_DATABASE_NAME;
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract RecipeDao recipeDao();
    public abstract IngredientandStepsDao ingredientandStepsDao();
    public abstract RecipeIngredientsStepsDao recipeIngredientsStepsDao();


}
