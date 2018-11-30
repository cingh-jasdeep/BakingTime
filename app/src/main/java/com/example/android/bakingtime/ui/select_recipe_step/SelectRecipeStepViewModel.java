package com.example.android.bakingtime.ui.select_recipe_step;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.android.bakingtime.data.Constant;
import com.example.android.bakingtime.data.repository.RecipeRepository;
import com.example.android.bakingtime.db.AppDatabase;
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;
import com.example.android.bakingtime.db.model.StepEntry;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.FragmentState;
import com.example.android.bakingtime.ui.utlities.ExoPlayerVideoHandler;

import java.util.List;

import static com.example.android.bakingtime.data.Constant.DEFAULT_RECIPE_STEP_INDEX;
import static com.example.android.bakingtime.data.Constant.DEFAULT_TWO_PANE;
import static com.example.android.bakingtime.data.Constant.INVALID_RECIPE_ID;
import static com.example.android.bakingtime.data.Constant.INVALID_RECIPE_STEP_INDEX;

public class SelectRecipeStepViewModel extends AndroidViewModel {

    // Save fragment screen state in a mutable live data
    // https://stackoverflow.com/a/44757469/10030480
    private RecipeRepository mRepo;
    private boolean mTwoPane = false;
    private MutableLiveData<FragmentState> mState = new MutableLiveData<>();
    private MutableLiveData<Integer> mRecipeId = new MutableLiveData<>();
    private MutableLiveData<Integer> mRecipeStepIndex = new MutableLiveData<>();

    // used for storing exoPlayer video player state
    private ExoPlayerVideoHandler videoHandler = new ExoPlayerVideoHandler();

    public LiveData<RecipeWithIngredientsAndSteps> recipeWithData =
            Transformations.switchMap(mRecipeId, new Function<Integer, LiveData<RecipeWithIngredientsAndSteps>>() {
                @Override
                public LiveData<RecipeWithIngredientsAndSteps> apply(Integer recipeId) {
                    return mRepo.loadRecipeByIdWithIngredientsAndSteps(recipeId);
                }
            });

    public LiveData<StepEntry> step =
            Transformations.map(mRecipeStepIndex, stepId -> {
                if(recipeWithData == null || recipeWithData.getValue() == null) return null;
                else {
                    List<StepEntry>  stepEntries = recipeWithData.getValue().steps;
                    if(stepEntries.get(stepId) == null) return null;
                    else return stepEntries.get(stepId);
                }
            });


    public SelectRecipeStepViewModel(@NonNull Application application, Bundle bundle) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(application);
        mRepo = RecipeRepository.getInstance(db);

        readFrom(bundle);
    }

    private void readFrom(Bundle bundle) {
        if(bundle != null) {
            int recipeId = bundle.getInt(Constant.EXTRA_RECIPE_ID, INVALID_RECIPE_ID);
            if(recipeId != INVALID_RECIPE_ID) {
                mRecipeId.setValue(recipeId);

                int stepId = bundle.getInt(Constant.EXTRA_RECIPE_STEP_INDEX, INVALID_RECIPE_STEP_INDEX);
                if(stepId == INVALID_RECIPE_STEP_INDEX) mRecipeStepIndex.setValue(DEFAULT_RECIPE_STEP_INDEX);
                else mRecipeStepIndex.setValue(stepId);

                FragmentState state = (FragmentState) bundle.get(Constant.EXTRA_FRAGMENT_STATE);
                if(state == null) mState.setValue(FragmentState.StepList);//default value
                else mState.setValue(state);

                mTwoPane = bundle.getBoolean(Constant.EXTRA_TWO_PANE, DEFAULT_TWO_PANE);

            }
        }
    }

    // save instance state with viewmodels
    // https://proandroiddev.com/customizing-the-new-viewmodel-cf28b8a7c5fc
    public void writeTo(Bundle bundle) {
        if (bundle != null) {
            bundle.putSerializable(Constant.EXTRA_FRAGMENT_STATE, mState.getValue());
            if(mRecipeStepIndex.getValue() != null)
                bundle.putInt(Constant.EXTRA_RECIPE_STEP_INDEX, mRecipeStepIndex.getValue());
            if(mRecipeId.getValue() != null)
                bundle.putInt(Constant.EXTRA_RECIPE_ID, mRecipeId.getValue());
            bundle.putBoolean(Constant.EXTRA_TWO_PANE, mTwoPane);
        }
    }

    public LiveData<FragmentState> getState() {
        return mState;
    }

    public void setState(FragmentState state) {
        if(state != null)
            mState.setValue(state);
    }

    public void setRecipeStepIndex(int recipeStepIndex) {
        if(recipeWithData.getValue()!=null) {
            if(recipeStepIndex >= DEFAULT_RECIPE_STEP_INDEX &&
                    recipeStepIndex < recipeWithData.getValue().steps.size()) {
                mRecipeStepIndex.setValue(recipeStepIndex);
            }
        }
    }

    public Integer getRecipeStepIndex() {
        return mRecipeStepIndex.getValue();
    }

    public boolean isTwoPane() {
        return mTwoPane;
    }

    public void setTwoPane(boolean mTwoPane) {
        this.mTwoPane = mTwoPane;
    }

    public ExoPlayerVideoHandler getVideoHandler() {
        return videoHandler;
    }

    @Override
    protected void onCleared() {
        if(videoHandler != null) {
            videoHandler.releaseVideoPlayer();
        }
        super.onCleared();
    }
}
