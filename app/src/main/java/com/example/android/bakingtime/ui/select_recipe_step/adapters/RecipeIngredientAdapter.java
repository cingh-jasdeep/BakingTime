package com.example.android.bakingtime.ui.select_recipe_step.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bakingtime.R;
import com.example.android.bakingtime.db.model.IngredientEntry;
import com.example.android.bakingtime.db.model.StepEntry;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.bakingtime.data.Constant.INGREDIENT_QUANTITY_NUMBER_FORMAT;


public class RecipeIngredientAdapter extends RecyclerView.Adapter<RecipeIngredientAdapter.RecipeIngredientAdapterViewHolder>{

    private List<IngredientEntry> mIngredientsData;
    private Context mContext;
    private int mShortAnimationDuration;

    public RecipeIngredientAdapter(Context context) {

        mContext = context;
        mShortAnimationDuration = mContext.getResources().getInteger(
                android.R.integer.config_longAnimTime);
    }


    /**
     * Viewholder class to hold views for rv
     * we only use one viewType and one xml recipe_item.xml
     *
     * **/
    public class RecipeIngredientAdapterViewHolder extends RecyclerView.ViewHolder{

        public final TextView mRecipeIngredientSerialNumberTextView;
        public final TextView mRecipeIngredientNameTextView;
        public final TextView mRecipeIngredientQuantityTextView;
        public final TextView mRecipeIngredientMeasureTextView;



        public final View mItemView;
        public RecipeIngredientAdapterViewHolder(View itemView) {
            super(itemView);
            mRecipeIngredientSerialNumberTextView = itemView.findViewById(R.id.tv_ingredient_serial_number);
            mRecipeIngredientNameTextView = itemView.findViewById(R.id.tv_recipe_ingredient_name);
            mRecipeIngredientQuantityTextView = itemView.findViewById(R.id.tv_quantity);
            mRecipeIngredientMeasureTextView = itemView.findViewById(R.id.tv_measure);

            mItemView = itemView;
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
    public RecipeIngredientAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.recipe_ingredient_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutForItem, parent, false);
        view.setVisibility(View.GONE);
        return new RecipeIngredientAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeIngredientAdapterViewHolder holder, int position) {
        IngredientEntry ingredient = mIngredientsData.get(position);
        if(ingredient!=null) {

            holder.mRecipeIngredientSerialNumberTextView.setText(String.format(
            mContext.getString(R.string.formatted_recipe_ingredient_serial_number),
            String.valueOf(position + 1)));

            holder.mRecipeIngredientNameTextView.setText(ingredient.getIngredient());
            //https://stackoverflow.com/questions/11826439/show-decimal-of-a-double-only-when-needed

            holder.mRecipeIngredientQuantityTextView.setText(String.format(
                    mContext.getString(R.string.formatted_recipe_ingredient_measure),
                    INGREDIENT_QUANTITY_NUMBER_FORMAT.format(ingredient.getQuantity())));

            holder.mRecipeIngredientMeasureTextView.setText(ingredient.getMeasure());

            fadeInItem(holder);
        }
    }

    private void fadeInItem(@NonNull RecipeIngredientAdapterViewHolder holder) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        holder.mItemView.setAlpha(0f);
        holder.mItemView.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        holder.mItemView.animate()
                .alpha(1f)
                .setDuration(mShortAnimationDuration)
                .setListener(null);
    }

    @Override
    public int getItemCount() {
        if(mIngredientsData == null) { return 0; }
        return mIngredientsData.size();
    }

    /**
     * @param ingredients list of ingredient objects to be added to rv
     * **/
    public void setIngredientsData(List<IngredientEntry> ingredients) {
        if(mIngredientsData == null) { mIngredientsData = new ArrayList<>();}
        mIngredientsData.clear();
        //check argument
        if (ingredients != null && ingredients.size() != 0) {
            mIngredientsData.addAll(ingredients);
        }
        notifyDataSetChanged();
    }

}

