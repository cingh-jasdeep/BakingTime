package com.example.android.bakingtime.ui.select_recipe_step.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.android.bakingtime.R;
import com.example.android.bakingtime.api.api_utilities.Status;
import com.example.android.bakingtime.databinding.FragmentRecipeStepDetailBinding;
import com.example.android.bakingtime.db.model.RecipeEntry;
import com.example.android.bakingtime.db.model.StepEntry;
import com.example.android.bakingtime.ui.select_recipe_step.SelectRecipeStepViewModel;
import com.example.android.bakingtime.ui.utlities.ConnectionBuddyUtils;
import com.example.android.bakingtime.ui.utlities.ExoPlayerVideoHandler;
import com.google.android.exoplayer2.util.Util;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.android.bakingtime.data.Constant.DEFAULT_RECIPE_STEP_INDEX;

public class RecipeStepDetailFragment extends Fragment implements ConnectivityChangeListener {

    FragmentRecipeStepDetailBinding mBinding;
    SelectRecipeStepViewModel mViewModel;
    Toast mToast;

    private static final String TAG = RecipeStepDetailFragment.class.getSimpleName();

    // Mandatory empty constructor
    public RecipeStepDetailFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupUiComponents(inflater, container);
        return mBinding.getRoot();
    }

    private void setupUiComponents(LayoutInflater inflater, ViewGroup container) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_recipe_step_detail, container, false);


    }


    @Override
    public void onStart() {
        super.onStart();

        // initialize player for sdk > 23
        if (Util.SDK_INT > 23) {
            // initialize player
            connectViewModel();
        }
    }

//initalize player code
//in sdk 24 split screen so get initialize before
    //@Override
    //public void onStart() {
    //    super.onStart();
    //    if (Util.SDK_INT > 23) {
    //        // initialize player
    //    }
    //}
    //      in 23 or older can wait for onresume to initialize
    //    @Override
    //    public void onResume() {
    //        super.onResume();
    //        if ((Util.SDK_INT <= 23 || player == null)) {
    //            // initialize player
    //        }
    //    }

    @Override
    public void onResume() {
        super.onResume();

        // initialize player for sdk <= 23
        if (Util.SDK_INT <= 23) {
            //initialize player
            connectViewModel();
        }

        ConnectionBuddyUtils.registerConnectionBuddyEvents(this, this);
        if(getActivity() !=null )
            handleOrientation(getActivity().getResources().getConfiguration());
    }





    private void connectViewModel() {

        if(getActivity() != null) {
            mViewModel = ViewModelProviders.of(getActivity())
                    .get(SelectRecipeStepViewModel.class);

            if(mViewModel.isTwoPane()) {
                //hide navigation
                mBinding.guidelineVideoDivider.setGuidelinePercent((float)0.6);
                mBinding.linearLayoutRecipeStepDetailNavigation.setVisibility(View.GONE);
            } else {
                mBinding.guidelineVideoDivider.setGuidelinePercent((float)0.5);
                mBinding.linearLayoutRecipeStepDetailNavigation.setVisibility(View.VISIBLE);
            }

            //https://stackoverflow.com/questions/3663665/how-can-i-get-the-current-screen-orientation
            handleOrientation(getResources().getConfiguration());

            mViewModel.getCurrentStep().observe(this, stepEntry -> {
                if(stepEntry != null) {

                    displayRecipeStepMedia(stepEntry);

                    displayRecipeStepInstruction(stepEntry);

                    if(!mViewModel.isTwoPane()) {

                        hidePreviousButtonForFirstScreen();

                        hideNextButtonForLastScreen();

                        setupRecipeStepNavigationButtonListeners();

                        setupNavigationStepsTextFooter();

                    }

                }
            });
        }

    }

    private void displayRecipeStepMedia(StepEntry stepEntry) {

        if(stepEntry.getVideoURL() == null || stepEntry.getVideoURL().equals("")) {

            mBinding.pvRecipeStepDetailVideoPlayer.setVisibility(View.GONE);
            mBinding.ivRecipeStepThumbnail.setVisibility(View.VISIBLE);

            if(stepEntry.getThumbnailURL() == null || stepEntry.getThumbnailURL().equals("")) {
                if(getContext() != null) {
                    Glide.with(getContext())
                            .load(R.mipmap.ic_launcher)
                            .into(mBinding.ivRecipeStepThumbnail);
                }
            } else {
                if(getContext() != null) {
                    mBinding.pbImageLoading.setVisibility(View.VISIBLE);
                    Glide.with(getContext())
                            .load(stepEntry.getThumbnailURL())
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    mBinding.pbImageLoading.setVisibility(View.INVISIBLE);

                                    if(getContext() != null) {
                                        Glide.with(getContext())
                                                .load(R.mipmap.ic_launcher)
                                                .into(mBinding.ivRecipeStepThumbnail);
                                    }

                                    Log.e(TAG, "onLoadFailed: ", e);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    mBinding.pbImageLoading.setVisibility(View.INVISIBLE);

                                    if(getContext() != null) {
                                        String photoA11y = String.format(getContext().getString(R.string.a11y_recipe_step_photo),
                                                stepEntry.getDescription());
                                        mBinding.ivRecipeStepThumbnail.setContentDescription(photoA11y);
                                    }

                                    return false;
                                }
                            })
                            .into(mBinding.ivRecipeStepThumbnail);
                }
            }

        } else {
            mBinding.pvRecipeStepDetailVideoPlayer.setVisibility(View.VISIBLE);
            mBinding.ivRecipeStepThumbnail.setVisibility(View.GONE);
            initializeVideoPlayer(stepEntry.getVideoURL());
        }

    }

    private void initializeVideoPlayer(@NonNull String videoURL) {
        ExoPlayerVideoHandler videoHandler = mViewModel.getVideoHandler();
        if(videoHandler != null) {
            videoHandler.prepareExoPlayerForUri(getContext(), Uri.parse(videoURL),
                    mViewModel.isVideoPlayOnLoad(), mViewModel.getVideoCurrentPlayingPosition(),
                    mBinding.pvRecipeStepDetailVideoPlayer);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectionBuddyUtils.clearConnectionBuddyState(savedInstanceState, this);
    }

    //save player state in onpause (safest for all api)

    @Override
    public void onPause() {

        //if video is visible
        if(mBinding.pvRecipeStepDetailVideoPlayer.getVisibility() == View.VISIBLE) {
            //save player state here in viewmodel
            mViewModel.saveVideoPlayerState();
            //release exoplayer resources for sdk <= 23
            if (Util.SDK_INT <= 23) {
                // release player
                mViewModel.getVideoHandler().releaseVideoPlayer();
            }
        }

        //unregister network change updates
        ConnectionBuddyUtils.unregisterConnectionBuddyEvents(this);
        super.onPause();
    }


//release video player code
    // no guarantee for onstop so release in onpause
    //    @Override
    //    public void onPause() {
    //        super.onPause();
    //        if (Util.SDK_INT <= 23) {
    //            // release player
    //        }
    //    }

    //    there is guarantee for onstop so release later in onstop
    //    @Override
    //    public void onStop() {
    //        super.onStop();
    //        if (Util.SDK_INT > 23) {
    //            // release player
    //        }
    //    }

    @Override
    public void onStop() {

        //if video is visible
        if(mBinding.pvRecipeStepDetailVideoPlayer.getVisibility() == View.VISIBLE) {
            //release exoplayer resources for sdk > 23
            if (Util.SDK_INT > 23) {
                // release player
                mViewModel.getVideoHandler().releaseVideoPlayer();
            }
        }
        super.onStop();
    }

    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        boolean isConnected = event.getState() == ConnectivityState.CONNECTED;
        showConnectionToast(isConnected);

    }

    private void showConnectionToast(boolean isConnected) {
        if(!isConnected) {
            mToast = Toast.makeText(getContext(),
                    R.string.message_no_internet_media_may_not_load, Toast.LENGTH_LONG);
            mToast.show();
        }
    }

    private void setupRecipeStepNavigationButtonListeners() {
        if(mBinding!=null) {

            mBinding.bRecipeStepDetailPrevButton.setOnClickListener(v -> handlePreviousButtonClick());

            mBinding.bRecipeStepDetailNextButton.setOnClickListener(v -> handleNextButtonClick());
        }
    }

    private void handlePreviousButtonClick() {
        if(mViewModel.getRecipeStepIndex() != null) {

            resetVideoAndLoadingIndicator();

            LiveData<Status> singleOperation =
                    mViewModel.setRecipeStepIndexSingleEvent(mViewModel.getRecipeStepIndex() - 1);
            singleOperation.observe(this,
                    status -> {
                        if(status != null && status.equals(Status.SUCCESS)) {
                            singleOperation.removeObservers(this);
                        }
                    });

        }
    }


    private void handleNextButtonClick() {
        if(mViewModel.getRecipeStepIndex() != null) {

            resetVideoAndLoadingIndicator();

            LiveData<Status> singleOperation =
                    mViewModel.setRecipeStepIndexSingleEvent(mViewModel.getRecipeStepIndex() + 1);
            singleOperation.observe(this,
                    status -> {
                        if(status != null && status.equals(Status.SUCCESS)) {
                            singleOperation.removeObservers(this);
                        }
                    });


        }
    }

    //used to reset video player state and loading indicator
    private void resetVideoAndLoadingIndicator() {
        mViewModel.resetVideoPlayerState();

        mBinding.pbImageLoading.setVisibility(View.INVISIBLE);
    }

    private void hideNextButtonForLastScreen() {
        mViewModel.getRecipeWithData().observe(this, recipeWithData -> {
            if(recipeWithData !=null ) {
                mViewModel.getRecipeWithData().removeObservers(this);
                RecipeEntry recipe = recipeWithData.recipe;
                recipe.setSteps(recipeWithData.steps);
                recipe.setIngredients(recipeWithData.ingredients);
                if(mViewModel.getRecipeStepIndex() != null) {
                    if (mViewModel.getRecipeStepIndex() == recipe.getSteps().size() - 1) {
                        mBinding.bRecipeStepDetailNextButton.setEnabled(false);
                        mBinding.bRecipeStepDetailNextButton.setText(R.string.txt_recipe_step_detail_button_last_step);

                    } else {
                        mBinding.bRecipeStepDetailNextButton.setEnabled(true);
                        mBinding.bRecipeStepDetailNextButton.setText(R.string.txt_recipe_step_detail_button_next_step);
                    }
                }
            }
        });


    }

    private void hidePreviousButtonForFirstScreen() {
        if(mViewModel.getRecipeStepIndex() != null) {
            if(mViewModel.getRecipeStepIndex() == DEFAULT_RECIPE_STEP_INDEX) {
                mBinding.bRecipeStepDetailPrevButton.setEnabled(false);
                mBinding.bRecipeStepDetailPrevButton.setText(R.string.txt_recipe_step_detail_button_first_step);
            }
            else {
                mBinding.bRecipeStepDetailPrevButton.setEnabled(true);
                mBinding.bRecipeStepDetailPrevButton.setText(R.string.txt_recipe_step_detail_button_previous_step);
            }
        }

    }

    private void displayRecipeStepInstruction(@NonNull StepEntry stepEntry) {
        AppCompatActivity fragmentActivity = ((AppCompatActivity)getActivity());
        if(fragmentActivity != null) {
            ActionBar actionBar = fragmentActivity.getSupportActionBar();
            if(actionBar != null) actionBar.setSubtitle(stepEntry.getShortDescription());
        }

        String descriptionText = stepEntry.getDescription();

        Matcher matcher = Pattern.compile("\\d*\\.\\s*").matcher(descriptionText);
        if(matcher.find() && matcher.start() == 0) {
            descriptionText = descriptionText.replaceFirst("\\d*\\.\\s*","");
        }

        mBinding.tvRecipeStepDetailInstruction.setText(descriptionText);
    }

    private void setupNavigationStepsTextFooter() {
        mViewModel.getRecipeWithData().observe(this, recipeWithData -> {
            if(recipeWithData !=null ) {
                mViewModel.getRecipeWithData().removeObservers(this);
                if(mViewModel.getRecipeStepIndex() != null ) {
                    mBinding.tvRecipeStepDetailNavigationPositionText.setText(
                            String.format(getString(R.string.formatted_recipe_step_position),
                                    String.valueOf(mViewModel.getRecipeStepIndex()),
                                    String.valueOf(recipeWithData.steps.size() - 1))
                    );
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        handleOrientation(newConfig);
    }

    private void handleOrientation(Configuration newConfig) {
        if(!mViewModel.isTwoPane()) {
            // Checks the orientation of the screen
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                hideSystemUI();

            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
                if(getActivity() != null) {
                    showSystemUI(getActivity().getWindow().getDecorView());
                }
            }
            if(getActivity() !=null ) {
                View decorView = getActivity().getWindow().getDecorView();
                decorView.setOnSystemUiVisibilityChangeListener
                        (visibility -> {
                            // Note that system bars will only be "visible" if none of the
                            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                resetVideoToDefaultSizeAndShowActionBar();
                            } else {
                                expandVideoToFullScreenAndHideActionBar();
                            }
                        });
            }

        }
    }

    private void resetVideoToDefaultSizeAndShowActionBar() {
        //        <!-- steps for portrait transformation-->

        //<!--1 fl_recipe_step_detail_video_frame set size equal to 0 -->
        ViewGroup.LayoutParams flRecipeStepDetailVideoFrameLayoutParams
                = mBinding.flRecipeStepDetailVideoFrame.getLayoutParams();

        flRecipeStepDetailVideoFrameLayoutParams.height = 0;
        flRecipeStepDetailVideoFrameLayoutParams.width = 0;
        mBinding.flRecipeStepDetailVideoFrame.setLayoutParams(flRecipeStepDetailVideoFrameLayoutParams);

        //<!--2 guideline_video_divider set visibility visible-->
        mBinding.guidelineVideoDivider.setVisibility(View.VISIBLE);

        //<!--3 card_view_recipe_instruction_text set layout_constraintTop_toBottomOf attribute guidelineVideoDivider-->
        ConstraintLayout.LayoutParams cardViewRecipeInstructionTextConstraintLayoutParams
                = (ConstraintLayout.LayoutParams) mBinding.cardViewRecipeInstructionText.getLayoutParams();
        cardViewRecipeInstructionTextConstraintLayoutParams.topToBottom = mBinding.guidelineVideoDivider.getId();
        mBinding.cardViewRecipeInstructionText.setLayoutParams(cardViewRecipeInstructionTextConstraintLayoutParams);

        //<!--4 fl_recipe_step_detail_video_frame add layout_constraintBottom_toTopOf attribute guidelineVideoDivider-->
        ConstraintLayout.LayoutParams flRecipeStepDetailVideoFrameConstraintLayoutParams
                = (ConstraintLayout.LayoutParams) mBinding.flRecipeStepDetailVideoFrame.getLayoutParams();
        flRecipeStepDetailVideoFrameConstraintLayoutParams.bottomToTop = mBinding.guidelineVideoDivider.getId();
        mBinding.flRecipeStepDetailVideoFrame.setLayoutParams(flRecipeStepDetailVideoFrameConstraintLayoutParams);


        //<!--5 constraint_layout_parent_view set layout_height match_parent-->
        ViewGroup.LayoutParams constraintLayoutParentViewLayoutParams
                = mBinding.constraintLayoutParentView.getLayoutParams();
        constraintLayoutParentViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mBinding.constraintLayoutParentView.setLayoutParams(constraintLayoutParentViewLayoutParams);

        //<!--6 card_view_recipe_instruction_text set layout_height 0-->
        ViewGroup.LayoutParams cardViewRecipeInstructionTextLayoutParams
                = mBinding.cardViewRecipeInstructionText.getLayoutParams();
        cardViewRecipeInstructionTextLayoutParams.height = 0;
        mBinding.cardViewRecipeInstructionText.setLayoutParams(cardViewRecipeInstructionTextLayoutParams);

        AppCompatActivity fragmentActivity = ((AppCompatActivity)getActivity());
        if(fragmentActivity != null)  showActionBar(fragmentActivity.getSupportActionBar());
    }

    public static void showActionBar(ActionBar actionBar) {
        if(actionBar != null) actionBar.show();
    }

    private void expandVideoToFullScreenAndHideActionBar() {

        //        <!-- steps for landscape transformation-->

        //<!--1 card_view_recipe_instruction_text set layout_height wrap_content-->
        ViewGroup.LayoutParams cardViewRecipeInstructionTextLayoutParams
                = mBinding.cardViewRecipeInstructionText.getLayoutParams();
        cardViewRecipeInstructionTextLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mBinding.cardViewRecipeInstructionText.setLayoutParams(cardViewRecipeInstructionTextLayoutParams);

        //<!--2 constraint_layout_parent_view set layout_height wrap_content-->
        ViewGroup.LayoutParams constraintLayoutParentViewLayoutParams
                = mBinding.constraintLayoutParentView.getLayoutParams();
        constraintLayoutParentViewLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mBinding.constraintLayoutParentView.setLayoutParams(constraintLayoutParentViewLayoutParams);

        //<!--3 fl_recipe_step_detail_video_frame remove layout_constraintBottom_toTopOf attribute-->
        ConstraintLayout.LayoutParams flRecipeStepDetailVideoFrameConstraintLayoutParams
                = (ConstraintLayout.LayoutParams) mBinding.flRecipeStepDetailVideoFrame.getLayoutParams();
        flRecipeStepDetailVideoFrameConstraintLayoutParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        mBinding.flRecipeStepDetailVideoFrame.setLayoutParams(flRecipeStepDetailVideoFrameConstraintLayoutParams);

        //<!--4 card_view_recipe_instruction_text remove layout_constraintTop_toBottomOf attribute-->
        //<!--5 card_view_recipe_instruction_text set layout_constraintTop_toBottomOf to fl_recipe_step_detail_video_frame -->
        ConstraintLayout.LayoutParams cardViewRecipeInstructionTextConstraintLayoutParams
                = (ConstraintLayout.LayoutParams) mBinding.cardViewRecipeInstructionText.getLayoutParams();
        cardViewRecipeInstructionTextConstraintLayoutParams.topToBottom = mBinding.flRecipeStepDetailVideoFrame.getId();
        mBinding.cardViewRecipeInstructionText.setLayoutParams(cardViewRecipeInstructionTextConstraintLayoutParams);

        //<!--6 guideline_video_divider set visibility gone-->
        mBinding.guidelineVideoDivider.setVisibility(View.GONE);


        //<!--7 fl_recipe_step_detail_video_frame set size equal to screen size -->
        ViewGroup.LayoutParams flRecipeStepDetailVideoFrameLayoutParams
                = mBinding.flRecipeStepDetailVideoFrame.getLayoutParams();

        //https://stackoverflow.com/questions/5959870/programmatically-set-height-on-layoutparams-as-density-independent-pixels
        if(getActivity() != null) {
            flRecipeStepDetailVideoFrameLayoutParams.height =
                    getActivity().getResources().getDisplayMetrics().heightPixels;
            mBinding.flRecipeStepDetailVideoFrame.setLayoutParams(flRecipeStepDetailVideoFrameLayoutParams);
        }


        AppCompatActivity fragmentActivity = ((AppCompatActivity)getActivity());
        if(fragmentActivity != null)  hideActionBar(fragmentActivity.getSupportActionBar());

    }

    private void hideActionBar(ActionBar actionBar) {
        if(actionBar != null) actionBar.hide();
    }

    private void hideSystemUI() {
        if(getActivity() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                View decorView = getActivity().getWindow().getDecorView();
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                // Hide the nav bar and status bar
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }
    }

    // Shows the system bars by removing all the flags
    public static void showSystemUI(@NonNull View decorView) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            decorView.setSystemUiVisibility(View.VISIBLE);
        }
    }



}
