package com.example.places.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.places.PlaceDetailActivity;
import com.example.places.ProfileActivity;
import com.example.places.R;
import com.example.places.models.Promotion;
import com.example.places.models.SearchResult;
import com.parse.ParseException;

import java.util.List;

public class PromotionsAdapter extends RecyclerView.Adapter<PromotionsAdapter.ViewHolder> {

    // Attributes
    private Context context;
    private List<Promotion> promotions;

    public PromotionsAdapter(Context context, List<Promotion> promotions) {
        this.context = context;
        this.promotions = promotions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_promotions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Promotion promotion = this.promotions.get(position);
        try {
            holder.bind(promotion);
        } catch (ParseException e) {
            Log.e("PromotionsAdapter", "Promotion not added: " + position, e);
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return this.promotions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        // Attributes
        private TextView tvName, tvSent, tvViewed, tvClicked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find items on View
            this.tvName = itemView.findViewById(R.id.tvName);
            this.tvSent = itemView.findViewById(R.id.tvSent);
            this.tvClicked = itemView.findViewById(R.id.tvClicked);
            this.tvViewed = itemView.findViewById(R.id.tvViewed);
        }

        /**
         * Bind the information from the result to it's view
         * @param promotion: The result gotten
         */
        public void bind(Promotion promotion) throws ParseException {
            this.tvName.setText(promotion.getPlace().fetchIfNeeded().getString("name"));
            this.tvSent.setText(String.format("%d", promotion.getSent()));
            this.tvClicked.setText(String.format("%d", promotion.getClicked()));
            this.tvViewed.setText(String.format("%d", promotion.getViewed()));
        }

    }

}
