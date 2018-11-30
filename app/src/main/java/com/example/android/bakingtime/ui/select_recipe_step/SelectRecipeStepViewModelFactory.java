package com.example.android.bakingtime.ui.select_recipe_step;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.android.bakingtime.ui.select_recipe.SelectRecipeViewModel;

public class SelectRecipeStepViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    /**
     * Creates a {@code AndroidViewModelFactory}
     */

    private final Application mApplication;
    private final Bundle mBundle;

    public SelectRecipeStepViewModelFactory(@NonNull Application application, Bundle bundle) {
        super(application);
        mApplication = application;
        mBundle = bundle;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new SelectRecipeStepViewModel(mApplication, mBundle);
    }
}
