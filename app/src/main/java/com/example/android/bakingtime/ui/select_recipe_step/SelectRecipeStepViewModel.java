package com.example.android.bakingtime.ui.select_recipe_step;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.android.bakingtime.api.api_utilities.Status;
import com.example.android.bakingtime.data.Constant;
import com.example.android.bakingtime.data.repository.RecipeRepository;
import com.example.android.bakingtime.db.AppDatabase;
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;
import com.example.android.bakingtime.db.model.StepEntry;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.FragmentState;
import com.example.android.bakingtime.ui.utlities.ExoPlayerVideoHandler;

import static com.example.android.bakingtime.data.Constant.DEFAULT_RECIPE_STEP_INDEX;
import static com.example.android.bakingtime.data.Constant.DEFAULT_TWO_PANE;
import static com.example.android.bakingtime.data.Constant.DEFAULT_VIDEO_CURRENT_POSITION;
import static com.example.android.bakingtime.data.Constant.DEFAULT_VIDEO_PLAY_ON_LOAD;
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

    // variables used for storing exoPlayer video player state
    private ExoPlayerVideoHandler videoHandler = new ExoPlayerVideoHandler();
    private boolean videoPlayOnLoad = false;
    private long videoCurrentPlayingPosition = 0;


    public LiveData<RecipeWithIngredientsAndSteps> getRecipeWithData() {
        return recipeWithData;
    }

    private LiveData<RecipeWithIngredientsAndSteps> recipeWithData =
            Transformations.switchMap(mRecipeId, recipeId -> {
                if(recipeId == null || mRepo == null) return null;
                else {
                    return mRepo.loadRecipeByIdWithIngredientsAndSteps(recipeId);
                }
            });

    public LiveData<StepEntry> getCurrentStep() {
        return currentStep;
    }

    private LiveData<StepEntry> currentStep =
            Transformations.switchMap(mRecipeStepIndex, stepIndex -> {
                Integer recipeId = mRecipeId.getValue();
                if(stepIndex == null || recipeId == null || mRepo == null) return null;
                else {
                    return mRepo.loadStep(recipeId, stepIndex);
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

                videoPlayOnLoad = bundle.getBoolean(Constant.EXTRA_VIDEO_PLAY_ON_LOAD, DEFAULT_VIDEO_PLAY_ON_LOAD);

                videoCurrentPlayingPosition =
                        bundle.getLong(Constant.EXTRA_VIDEO_CURRENT_POSITION, DEFAULT_VIDEO_CURRENT_POSITION);

            }
        }
    }

    // save instance state with viewmodels
    // https://proandroiddev.com/customizing-the-new-viewmodel-cf28b8a7c5fc
    public void writeTo(Bundle bundle) {
        if (bundle != null) {
            Integer recipeId = mRecipeId.getValue();
            FragmentState state = mState.getValue();
            Integer recipeStepIndex = mRecipeStepIndex.getValue();

            if(recipeId != null) {
                bundle.putInt(Constant.EXTRA_RECIPE_ID, recipeId);
            }

            if(state != null) {
                bundle.putSerializable(Constant.EXTRA_FRAGMENT_STATE, state);
            }

            if(recipeStepIndex != null) {
                bundle.putInt(Constant.EXTRA_RECIPE_STEP_INDEX, recipeStepIndex);
            }

            bundle.putBoolean(Constant.EXTRA_TWO_PANE, mTwoPane);
            bundle.putBoolean(Constant.EXTRA_VIDEO_PLAY_ON_LOAD, isVideoPlayOnLoad());
            bundle.putLong(Constant.EXTRA_VIDEO_CURRENT_POSITION, getVideoCurrentPlayingPosition());
        }
    }

    public LiveData<FragmentState> getState() {
        return mState;
    }

    public void setState(FragmentState state) {
        if(state != null)
            mState.setValue(state);
    }

    public LiveData<Status> setRecipeStepIndexSingleEvent(int recipeStepIndex) {

        MediatorLiveData<Status> singleOperationOnLiveData = new MediatorLiveData<>();
        singleOperationOnLiveData.addSource(recipeWithData, recipeData -> {
            if(recipeData != null) {
                //perform operation only once
                singleOperationOnLiveData.removeSource(recipeWithData);

                if(recipeStepIndex >= DEFAULT_RECIPE_STEP_INDEX &&
                        recipeStepIndex < recipeData.steps.size()) {
                    mRecipeStepIndex.setValue(recipeStepIndex);
                }
                singleOperationOnLiveData.setValue(Status.SUCCESS);
            }
        });

        return singleOperationOnLiveData;
//        if(recipeWithData.getValue()!=null) {
//            if(recipeStepIndex >= DEFAULT_RECIPE_STEP_INDEX &&
//                    recipeStepIndex < recipeWithData.getValue().steps.size()) {
//                mRecipeStepIndex.setValue(recipeStepIndex);
//            }
//        }
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

    public boolean isVideoPlayOnLoad() {
        return videoPlayOnLoad;
    }

    public void setVideoPlayOnLoad(boolean videoPlayOnLoad) {
        this.videoPlayOnLoad = videoPlayOnLoad;
    }

    public long getVideoCurrentPlayingPosition() {
        return videoCurrentPlayingPosition;
    }

    public void setVideoCurrentPlayingPosition(long videoCurrentPlayingPosition) {
        this.videoCurrentPlayingPosition = videoCurrentPlayingPosition;
    }

    public void saveVideoPlayerState() {
        if(videoHandler != null) {
            setVideoPlayOnLoad(videoHandler.isVideoPlaying());
            setVideoCurrentPlayingPosition(videoHandler.getCurrentPosition());
        }
    }

    public void resetVideoPlayerState() {
        if(videoHandler != null) {
            videoHandler.releaseVideoPlayer();
        }

        setVideoCurrentPlayingPosition(0);
        // auto play on load new video
        // could be system preference?
        setVideoPlayOnLoad(true);
    }
}
