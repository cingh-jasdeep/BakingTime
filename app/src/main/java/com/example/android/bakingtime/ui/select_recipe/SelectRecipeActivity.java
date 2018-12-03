package com.example.android.bakingtime.ui.select_recipe;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.data.Constant;
import com.example.android.bakingtime.databinding.ActivitySelectRecipeBinding;
import com.example.android.bakingtime.db.model.RecipeEntry;
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;
import com.example.android.bakingtime.ui.select_recipe_step.SelectRecipeStepActivity;
import com.example.android.bakingtime.ui.utlities.ConnectionBuddyUtils;
import com.example.android.bakingtime.widget.IngredientsWidgetService;
import com.zplesac.connectionbuddy.interfaces.ConnectivityChangeListener;
import com.zplesac.connectionbuddy.models.ConnectivityEvent;
import com.zplesac.connectionbuddy.models.ConnectivityState;

import java.util.List;


public class SelectRecipeActivity extends AppCompatActivity
    implements RecipeAdapter.RecipeAdapterOnClickHandler, SwipeRefreshLayout.OnRefreshListener, ConnectivityChangeListener {

    private static final String TAG = SelectRecipeActivity.class.getSimpleName();

    private SelectRecipeViewModel mViewModel;
    private RecipeAdapter mRecipeAdapter;
    private ActivitySelectRecipeBinding mBinding;
    private MenuItem mRefreshMenuItem = null;
    private Snackbar mSnackBar;
    private Bundle mSavedInstanceState;

    @Nullable
    private CountingIdlingResource mSelectRecipeActivityIdlingResource;

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if(mSelectRecipeActivityIdlingResource == null) {
            mSelectRecipeActivityIdlingResource = new CountingIdlingResource(TAG);
        }
        return mSelectRecipeActivityIdlingResource;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_recipe);
        setupUiComponents();
        connectViewModel();
        mSavedInstanceState = savedInstanceState;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mBinding != null && !mBinding.srlRvRecipes.isRefreshing()) {
            showRefreshLoading(true);
            loadRecipeData(false, mSelectRecipeActivityIdlingResource);
        }
        ConnectionBuddyUtils.clearConnectionBuddyState(mSavedInstanceState, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectionBuddyUtils.registerConnectionBuddyEvents(this, this);

    }


    @Override
    protected void onPause() {
        super.onPause();
        ConnectionBuddyUtils.unregisterConnectionBuddyEvents(this);
    }

    /**
     * This method will get load recipeWithData data into the recyclerview adapter
     *
     */
    private void loadRecipeData(boolean forceRefresh,
                                CountingIdlingResource countingIdlingResource) {
        if(mViewModel != null) {
            if (countingIdlingResource != null) {
                countingIdlingResource.increment();
            }

            mViewModel.loadRecipes(forceRefresh);
            mViewModel.getRecipeListResourceData().observe(this, listResource -> {
                Log.d(TAG, "Updating recipes display from LiveData in ViewModel");
                if(listResource != null) {
                    switch (listResource.status) {
                        case ERROR: {
                            Log.i(TAG, "onChanged: [error] " + listResource.message);
                            showErrorToast();
                            showRecipesData(listResource.data, false);

                            if (countingIdlingResource != null) {
                                countingIdlingResource.decrement();
                            }
                            break;
                        }
                        case LOADING: {
                            showRecipesData(listResource.data, true);
                            break;
                        }
                        case SUCCESS: {
                            showRecipesData(listResource.data, false);

                            if (countingIdlingResource != null) {
                                countingIdlingResource.decrement();
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    private void connectViewModel() {
        mViewModel = ViewModelProviders.of(this).get(SelectRecipeViewModel.class);
    }

    private void showErrorToast() {
        Toast.makeText(this, getString(R.string.message_recipe_select_cannot_fetch),Toast.LENGTH_LONG)
                .show();
    }

    public void showRecipesData(List<RecipeWithIngredientsAndSteps> recipesData, boolean keepRefreshingUi) {
        showRefreshLoading(keepRefreshingUi);
        if (recipesData != null && recipesData.size() !=0 ) {
            showRecipeDataView(true);
            mRecipeAdapter.setRecipesData(recipesData);

        } else {
            showRecipeDataView(false);
            mBinding.tvErrorMessageDisplay.setText(getString(R.string.message_no_show));
        }

        //update widget to indicated there is data available
        IngredientsWidgetService.startActionUpdateIngredientsWidget(this);
    }

    private void setupUiComponents() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_recipe);

        mRecipeAdapter = new RecipeAdapter(this, this);
        mBinding.rvRecipes.setHasFixedSize(true);
        mBinding.rvRecipes.setAdapter(mRecipeAdapter);

        mBinding.srlRvRecipes.setOnRefreshListener(this);
    }

    private void showRecipeDataView(boolean show) {
        if(mBinding != null) {
            if (show){
                mBinding.rvRecipes.setVisibility(View.VISIBLE);
                mBinding.tvErrorMessageDisplay.setVisibility(View.INVISIBLE);
            }
            else {
                mBinding.tvErrorMessageDisplay.setVisibility(View.VISIBLE);
                mBinding.rvRecipes.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void showRefreshLoading(boolean show) {
        if(mBinding != null) {
            if (show) mBinding.srlRvRecipes.setRefreshing(true);
            else mBinding.srlRvRecipes.setRefreshing(false);
        }
    }

    private void enableRefreshControls(boolean enable) {
        if (mBinding != null) {
            mBinding.srlRvRecipes.setEnabled(enable);
            if(mRefreshMenuItem != null) mRefreshMenuItem.setEnabled(enable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.select_recipe, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        mRefreshMenuItem = menu.findItem(R.id.action_refresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            if(mBinding != null && !mBinding.srlRvRecipes.isRefreshing()) {
                showRefreshLoading(true);
                loadRecipeData(true, mSelectRecipeActivityIdlingResource);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(RecipeEntry recipe) {
        //send selected recipeid to viewmodel and observe selected recipeid
        Intent intentToShowRecipeSteps = new Intent(getApplicationContext(), SelectRecipeStepActivity.class);
        intentToShowRecipeSteps.putExtra(Constant.EXTRA_RECIPE_ID, recipe.getId());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        sp.edit().putInt(getString(R.string.pref_recipe_id_key), recipe.getId()).apply();

        //update widget to show selected recipe
        IngredientsWidgetService.startActionUpdateIngredientsWidget(this);
        startActivity(intentToShowRecipeSteps);

    }


    @Override
    public void onRefresh() {
        loadRecipeData(true, mSelectRecipeActivityIdlingResource);
    }

    @Override
    public void onConnectionChange(ConnectivityEvent event) {
        boolean isConnected = event.getState() == ConnectivityState.CONNECTED;
        enableRefreshControls(isConnected);
        if(isConnected) {
            if(mBinding != null && !mBinding.srlRvRecipes.isRefreshing()) {
                showRefreshLoading(true);
                loadRecipeData(false, mSelectRecipeActivityIdlingResource);
            }
        }
        showConnectionSnackBar(isConnected);
    }

    private void showConnectionSnackBar(boolean isConnected) {
        if(mBinding != null) {
            if(isConnected) {
                //hide snackbar
                if(mSnackBar != null) {
                    mSnackBar.dismiss();
                }

            } else {
                //show snackbar
                mSnackBar = Snackbar.make(mBinding.srlRvRecipes,
                        R.string.message_no_internet_refresh_disabled, Snackbar.LENGTH_INDEFINITE);
                mSnackBar.show();
            }
        }
    }
}
