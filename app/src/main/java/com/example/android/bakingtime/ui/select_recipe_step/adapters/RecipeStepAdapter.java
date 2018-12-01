package com.example.android.bakingtime.ui.select_recipe_step.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.db.model.StepEntry;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.bakingtime.data.Constant.INTRODUCTION_STEPS;
import static com.example.android.bakingtime.data.Constant.INVALID_RECIPE_STEP_INDEX;

public class RecipeStepAdapter extends RecyclerView.Adapter<RecipeStepAdapter.RecipeStepAdapterViewHolder>{

    private List<StepEntry> mStepsData;
    private Context mContext;
    private RecipeStepAdapterOnClickHandler mClickHandler;
    // highlight selected position in RV
    // https://stackoverflow.com/questions/39138315/how-to-highlight-selected-item-in-recyclerview
    private int mSelectedPosition = INVALID_RECIPE_STEP_INDEX;

    public RecipeStepAdapter(Context context, RecipeStepAdapterOnClickHandler clickHandler) {

        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * click handler to get movie object at a particular position
     */
    public interface RecipeStepAdapterOnClickHandler {
        void onClick(Integer stepIndex);
    }

    /**
     * Viewholder class to hold views for rv
     * we only use one viewType and one xml recipe_item.xml
     *
     * **/
    public class RecipeStepAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        public final TextView mRecipeStepNumberTextTextView;
        public final TextView mRecipeStepDescriptionTextView;

        public final View mItemView;
        public RecipeStepAdapterViewHolder(View itemView) {
            super(itemView);

            mRecipeStepNumberTextTextView = itemView.findViewById(R.id.tv_recipe_step_number_text);
            mRecipeStepDescriptionTextView = itemView.findViewById(R.id.tv_recipe_step_description);

            mItemView = itemView;
            mItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mStepsData !=null) {
                mClickHandler.onClick(getAdapterPosition());
                mSelectedPosition = getAdapterPosition();
                notifyDataSetChanged();
            }
        }
    }


    /**
     * Inflates recipe_item layout into one item of recycler view
     * @param parent parent viewgroup to attach rv item to
     * @param viewType viewType of rv item to be attached (we only use one view type)
     * @return new MoviesAdapterViewHolder with inflated layout
     */
    @NonNull
    @Override
    public RecipeStepAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.recipe_step_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutForItem, parent, false);
        return new RecipeStepAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeStepAdapterViewHolder holder, int position) {
        StepEntry step = mStepsData.get(position);
        if(step!=null) {
            if(mContext != null) {

                if (mSelectedPosition == position) {
                    holder.mItemView.setBackgroundColor(
                            ContextCompat.getColor(mContext, R.color.colorActivated));
                } else {
                    holder.mItemView.setBackgroundColor(
                            ContextCompat.getColor(mContext, android.R.color.transparent));

                }
                holder.mRecipeStepNumberTextTextView.setText(String.format(
                        mContext.getString(R.string.formatted_recipe_step_desc),
                        Integer.toString(position - INTRODUCTION_STEPS + 1)));
                holder.mRecipeStepDescriptionTextView.setText(step.getShortDescription());
            }

        }
    }

    @Override
    public int getItemCount() {
        if(mStepsData == null) { return 0; }
        return mStepsData.size();
    }

    /**
     * @param steps list of step objects to be added to rv
     * **/
    public void setRecipeStepsData(List<StepEntry> steps) {
        if(mStepsData == null) { mStepsData = new ArrayList<>();}
        mStepsData.clear();
        //check argument
        if (steps != null && steps.size() != 0) {
            mStepsData.addAll(steps);
        }
        notifyDataSetChanged();
    }

    /**
     *  used to clear selected position
     */
    public void clearSelectedPosition() {
        mSelectedPosition = INVALID_RECIPE_STEP_INDEX;
        notifyDataSetChanged();
    }

}
