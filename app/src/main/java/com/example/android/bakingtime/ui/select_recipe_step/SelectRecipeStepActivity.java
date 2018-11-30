package com.example.android.bakingtime.ui.select_recipe_step;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.data.Constant;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.FragmentState;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.RecipeIngredientsListFragment;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.RecipeStepDetailFragment;
import com.example.android.bakingtime.ui.select_recipe_step.fragments.SelectRecipeStepListFragment;

import static com.example.android.bakingtime.data.Constant.INVALID_RECIPE_ID;

public class SelectRecipeStepActivity extends AppCompatActivity {


    private SelectRecipeStepViewModel mViewModel;

    // true if two pane
    private boolean mTwoPane;
//    private RecipeStepDetailFragment mCurrDetailFragment;
    private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupUiComponents();

        int recipeId;

        //check saved instance state for recipeWithData id
        if (savedInstanceState != null && savedInstanceState.containsKey(Constant.EXTRA_RECIPE_ID)) {
            recipeId = savedInstanceState.getInt(Constant.EXTRA_RECIPE_ID, INVALID_RECIPE_ID);
            if(recipeId != INVALID_RECIPE_ID) {
                //recipeWithData id is valid and load data
                connectViewModel(savedInstanceState);
            }
        } else {
            //check intent for recipeWithData id
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(Constant.EXTRA_RECIPE_ID)) {
                recipeId = intent.getIntExtra(Constant.EXTRA_RECIPE_ID, INVALID_RECIPE_ID);
                //there is no recipeWithData id in intent
                if(recipeId == INVALID_RECIPE_ID) closeOnError();

                //recipeWithData id is valid from intent and load data
                Bundle initBundle = new Bundle();
                initBundle.putInt(Constant.EXTRA_RECIPE_ID, recipeId);
                initBundle.putBoolean(Constant.EXTRA_TWO_PANE, mTwoPane);


                FragmentState intentStateExtra = (FragmentState) intent.getSerializableExtra(Constant.EXTRA_FRAGMENT_STATE);
                if(intentStateExtra != null) {
                    initBundle.putSerializable(Constant.EXTRA_FRAGMENT_STATE, intentStateExtra);
                }

                connectViewModel(initBundle);

            } else {
                closeOnError();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mViewModel!=null) {
            mViewModel.writeTo(outState);
        }
    }

    /**
     * used when no recipeid data is given
     */
    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.message_no_show_recipe_step, Toast.LENGTH_SHORT).show();
    }

    private void setupUiComponents() {
        setContentView(R.layout.activity_select_recipe_step);
        mTwoPane = ( findViewById(R.id.fl_fragment_master_container) != null );

        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    private void connectViewModel(Bundle savedInstanceState) {

        SelectRecipeStepViewModelFactory viewModelFactory =
                new SelectRecipeStepViewModelFactory(getApplication(), savedInstanceState);

        mViewModel = ViewModelProviders.of(this, viewModelFactory).get(SelectRecipeStepViewModel.class);

        //switch fragments according to current state
        mViewModel.getState().observe(this, fragmentState -> {
            if(fragmentState!=null) {
                switch (fragmentState) {
                    case StepList:
                        showStepListFragment();
                        break;
                    case IngredientsList:
                        showIngredientsListFragment();
                        break;
                    case StepDetail:
                        showStepDetailFragment();
                        break;
                }
            }
        });

        mViewModel.recipeWithData.observe(this, recipeEntryWithData -> {
            if(recipeEntryWithData!=null) {
                ActionBar actionBar = getSupportActionBar();
                if(actionBar != null) actionBar.setTitle(recipeEntryWithData.recipe.getName());
            }
        });

    }

    private void showStepListFragment() {
        if(mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragment_master_container, new SelectRecipeStepListFragment(),
                            "visible_fragment")
                    .replace(R.id.fl_fragment_container, new RecipeIngredientsListFragment())
                    .commit();


        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_fragment_container, new SelectRecipeStepListFragment(),
                            "visible_fragment")
                    .commit();
        }
        if(mOnBackStackChangedListener == null) {
            mOnBackStackChangedListener = new SelectRecipeStepOnBackStackChangedListener();
            getSupportFragmentManager().addOnBackStackChangedListener(
                    mOnBackStackChangedListener);
        }

    }

    private void showIngredientsListFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, new RecipeIngredientsListFragment(),
                        "visible_fragment")
                .addToBackStack(null)
                .commit();
        if (mOnBackStackChangedListener == null) {
            mOnBackStackChangedListener = new SelectRecipeStepOnBackStackChangedListener();
            getSupportFragmentManager().addOnBackStackChangedListener(
                    mOnBackStackChangedListener);
        }
    }

    private void showStepDetailFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_fragment_container, new RecipeStepDetailFragment(),
                        "visible_fragment")
                .addToBackStack(null)
                .commit();
        if (mOnBackStackChangedListener == null) {
            mOnBackStackChangedListener = new SelectRecipeStepOnBackStackChangedListener();
            getSupportFragmentManager().addOnBackStackChangedListener(
                    mOnBackStackChangedListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Learn how to use the OnBackStackChangedListener to get the current Fragment
    // https://why-android.com/2016/03/29/learn-how-to-use-the-onbackstackchangedlistener/
    private class SelectRecipeStepOnBackStackChangedListener
            implements FragmentManager.OnBackStackChangedListener {
        @Override
        public void onBackStackChanged() {
            Fragment currentBackStackFragment =
                    getSupportFragmentManager().findFragmentByTag("visible_fragment");
            if(!(currentBackStackFragment instanceof RecipeIngredientsListFragment)
                    && !(currentBackStackFragment instanceof RecipeStepDetailFragment)){
                mViewModel.setState(FragmentState.StepList);
                RecipeStepDetailFragment.showSystemUI(getWindow().getDecorView());
                RecipeStepDetailFragment.showActionBar(getSupportActionBar());
            }
        }
    }

}
