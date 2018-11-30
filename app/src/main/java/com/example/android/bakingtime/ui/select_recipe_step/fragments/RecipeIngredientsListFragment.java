package com.example.android.bakingtime.ui.select_recipe_step.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.databinding.FragmentRecipeIngredientsListBinding;
import com.example.android.bakingtime.ui.select_recipe_step.SelectRecipeStepViewModel;
import com.example.android.bakingtime.ui.select_recipe_step.adapters.RecipeIngredientAdapter;

import java.util.Objects;

public class RecipeIngredientsListFragment extends Fragment {

    FragmentRecipeIngredientsListBinding mBinding;
    RecipeIngredientAdapter mAdapter;
    SelectRecipeStepViewModel mViewModel;

    // Mandatory empty constructor
    public RecipeIngredientsListFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setupUiComponents(inflater, container);
        connectViewModel();
        showSubtitleActionBar();

        return mBinding.getRoot();
    }

    private void showSubtitleActionBar() {
        AppCompatActivity fragmentActivity = ((AppCompatActivity) getActivity());
        if (fragmentActivity != null) {
            ActionBar actionBar = fragmentActivity.getSupportActionBar();
            if (actionBar != null)
                actionBar.setSubtitle(R.string.txt_ingredients_actionbar_subtitle);
        }
    }

    private void connectViewModel() {
        if(getActivity() != null) {
            mViewModel = ViewModelProviders.of(getActivity())
                    .get(SelectRecipeStepViewModel.class);
            mViewModel.recipeWithData.observe(this, recipeEntryWithData -> {
                if(recipeEntryWithData!=null) {
                    mAdapter.setIngredientsData(recipeEntryWithData.ingredients);
                }
            });
        }


    }

    private void setupUiComponents(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_recipe_ingredients_list, container, false);

        mAdapter = new RecipeIngredientAdapter(getContext());
        mBinding.rvRecipeIgredients.setAdapter(mAdapter);
        mBinding.rvRecipeIgredients.setLayoutManager(new LinearLayoutManager(getContext()));
        mBinding.rvRecipeIgredients.addItemDecoration(new DividerItemDecoration(
                mBinding.rvRecipeIgredients.getContext(), DividerItemDecoration.VERTICAL));

    }
}
