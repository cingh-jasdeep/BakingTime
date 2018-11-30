package com.example.android.bakingtime.ui.select_recipe;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.android.bakingtime.R;
import com.example.android.bakingtime.db.model.IngredientEntry;
import com.example.android.bakingtime.db.model.RecipeEntry;
import com.example.android.bakingtime.db.model.RecipeWithIngredientsAndSteps;
import com.example.android.bakingtime.db.model.StepEntry;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.bakingtime.data.Constant.INTRODUCTION_STEPS;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeAdapterViewHolder>{

    private List<RecipeWithIngredientsAndSteps> mRecipesData;
    private Context mContext;
    private RecipeAdapterOnClickHandler mClickHandler;
    private int mShortAnimationDuration;

    private static final String TAG = RecipeAdapter.class.getSimpleName();

    public RecipeAdapter(Context context, RecipeAdapterOnClickHandler clickHandler) {

        mContext = context;
        mClickHandler = clickHandler;

        mShortAnimationDuration = mContext.getResources().getInteger(
                android.R.integer.config_longAnimTime);
    }

    /**
     * Inflates recipe_item layout into one item of recycler view
     * @param parent parent viewgroup to attach rv item to
     * @param viewType viewType of rv item to be attached (we only use one view type)
     * @return new MoviesAdapterViewHolder with inflated layout
     */
    @NonNull
    @Override
    public RecipeAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutForItem = R.layout.recipe_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutForItem, parent, false);
        view.setVisibility(View.GONE);
        return new RecipeAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapterViewHolder holder, int position) {
        RecipeEntry recipe = mRecipesData.get(position).recipe;

        if(recipe!=null) {
//            https://stackoverflow.com/questions/23391523/load-images-from-disk-cache-with-picasso-if-offline
            if(recipe.getImage()!=null && !recipe.getImage().equals("")) {
                Glide.with(mContext)
                        .load(recipe.getImage())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e(TAG, "onLoadFailed: ", e);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                String photoA11y = String.format(mContext.getString(R.string.a11y_recipe_photo),
                                        recipe.getName());
                                holder.mRecipePhotoImageView.setContentDescription(photoA11y);
                                return false;
                            }
                        })
                        .into(holder.mRecipePhotoImageView);
            } else {
                Glide.with(mContext)
                        .load(R.mipmap.ic_launcher)
                        .into(holder.mRecipePhotoImageView);
            }
            holder.mRecipeNameTextView.setText(recipe.getName());

            List<IngredientEntry> ingredientEntries = mRecipesData.get(position).ingredients;
            List<StepEntry> stepEntries = mRecipesData.get(position).steps;

            holder.mRecipeIngredientsNumberTextView.setText(String.valueOf(ingredientEntries.size()));
            //first step is always introduction so ommit that
            holder.mRecipeStepsNumberTextView.setText(String.valueOf(stepEntries.size() - INTRODUCTION_STEPS));

            fadeInItem(holder);


        }
    }

    private void fadeInItem(@NonNull RecipeAdapterViewHolder holder) {
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
        if(mRecipesData == null) { return 0; }
        return mRecipesData.size();
    }

    /**
     * @param recipesWithData list of recipeWithData objects to be added to rv
     * **/
    public void setRecipesData(List<RecipeWithIngredientsAndSteps> recipesWithData) {
        if(mRecipesData == null) { mRecipesData = new ArrayList<>();}
        mRecipesData.clear();
        //check argument
        if (recipesWithData != null && recipesWithData.size() != 0) {
            mRecipesData.addAll(recipesWithData);
        }
        notifyDataSetChanged();
    }

    /**
     * click handler to get movie object at a particular position
     */
    public interface RecipeAdapterOnClickHandler {
        void onClick(RecipeEntry recipe);
    }

    /**
     * Viewholder class to hold views for rv
     * we only use one viewType and one xml recipe_item.xml
     *
     * **/
    public class RecipeAdapterViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        public final ImageView mRecipePhotoImageView;
        public final TextView mRecipeNameTextView;
        public final TextView mRecipeIngredientsNumberTextView;
        public final TextView mRecipeStepsNumberTextView;

        public final View mItemView;
        public RecipeAdapterViewHolder(View itemView) {
            super(itemView);
            mRecipePhotoImageView = itemView.findViewById(R.id.iv_recipe_photo);
            mRecipeNameTextView = itemView.findViewById(R.id.tv_recipe_name);
            mRecipeIngredientsNumberTextView = itemView.findViewById(R.id.tv_number_of_ingredients);
            mRecipeStepsNumberTextView = itemView.findViewById(R.id.tv_number_of_steps);

            mItemView = itemView;
            mItemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecipesData!=null) {
                RecipeEntry recipe = mRecipesData.get(getAdapterPosition()).recipe;
                mClickHandler.onClick(recipe);
            }
        }
    }
}
