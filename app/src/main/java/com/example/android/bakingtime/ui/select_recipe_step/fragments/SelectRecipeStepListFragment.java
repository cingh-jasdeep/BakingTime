package com.example.android.bakingtime.ui.select_recipe_step.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.databinding.FragmentSelectRecipeStepListBinding;
import com.example.android.bakingtime.ui.select_recipe_step.SelectRecipeStepViewModel;
import com.example.android.bakingtime.ui.select_recipe_step.adapters.RecipeStepAdapter;

import java.util.Objects;

public class SelectRecipeStepListFragment extends Fragment implements RecipeStepAdapter.RecipeStepAdapterOnClickHandler, View.OnClickListener {

    FragmentSelectRecipeStepListBinding mBinding;
    RecipeStepAdapter mAdapter;
    SelectRecipeStepViewModel mViewModel;

    // Mandatory empty constructor
    public SelectRecipeStepListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        setupUiComponents(inflater, container);
        return mBinding.getRoot();
    }

    // onActivityCreated is called after onCreateView
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        connectViewModel();
        hideSubtitleActionBar();
    }

    private void setupUiComponents(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_recipe_step_list, container, false);

        mAdapter = new RecipeStepAdapter(getContext(), this);
        mBinding.rvRecipeSteps.setAdapter(mAdapter);
        mBinding.rvRecipeSteps.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvRecipeSteps.addItemDecoration(new DividerItemDecoration(
                mBinding.rvRecipeSteps.getContext(), DividerItemDecoration.VERTICAL));
        mBinding.rvRecipeSteps.setNestedScrollingEnabled(false);

        mBinding.tvRecipeIngredientsListOpener.setOnClickListener(this);

    }

    private void connectViewModel() {

        if(getActivity() != null) {
            mViewModel = ViewModelProviders.of(getActivity()).get(SelectRecipeStepViewModel.class);
            mViewModel.recipeWithData.observe(this, recipeEntryWithData -> {
                if(recipeEntryWithData!=null) {
                    mAdapter.setRecipeStepsData(recipeEntryWithData.steps);
                }
            });

            if(mViewModel.isTwoPane()) {
                mViewModel.getState().observe(this, fragmentState -> {
                    if(fragmentState!=null) {
                        switch (fragmentState) {
                            case StepList:
                                setIngredientsSelectViewBackgroundActivated(true);
                                break;
                            case IngredientsList:
                                mAdapter.clearSelectedPosition();
                                setIngredientsSelectViewBackgroundActivated(true);
                                break;
                            case StepDetail:
                                setIngredientsSelectViewBackgroundActivated(false);
                                break;
                        }
                    }
                });
            }
        }

    }
    //https://stackoverflow.com/questions/18320713/getsupportactionbar-from-inside-of-fragment-actionbarcompat
    private void hideSubtitleActionBar() {
        if(!mViewModel.isTwoPane()) {
            AppCompatActivity fragmentActivity = ((AppCompatActivity) getActivity());
            if (fragmentActivity != null) {
                ActionBar actionBar = fragmentActivity.getSupportActionBar();
                if (actionBar != null)
                    actionBar.setSubtitle(null);
            }
        }
    }


    @Override
    public void onClick(Integer stepIndex) {
        if(mViewModel!=null) {
            mViewModel.setRecipeStepIndex(stepIndex);
            mViewModel.setState(FragmentState.StepDetail);
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_recipe_ingredients_list_opener) {
            mViewModel.setState(FragmentState.IngredientsList);
            setIngredientsSelectViewBackgroundActivated(true);
        }
    }

    private void setIngredientsSelectViewBackgroundActivated(boolean isActivated) {
        if(getContext() != null){
            if(isActivated) {
                mBinding.tvRecipeIngredientsListOpener.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.colorActivated));
            } else {
                mBinding.tvRecipeIngredientsListOpener.setBackgroundColor(
                        ContextCompat.getColor(getContext(), android.R.color.transparent));
            }
        }

    }
}
