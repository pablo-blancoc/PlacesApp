package com.example.places.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.places.ProfileActivity;
import com.example.places.R;
import com.example.places.models.Place;
import com.example.places.models.SearchResult;

import java.util.List;

public class ProfilePlacesAdapter extends RecyclerView.Adapter<ProfilePlacesAdapter.ViewHolder> {

    // Attributes
    private Context context;
    private List<Place> places;

    public ProfilePlacesAdapter(Context context, List<Place> places) {
        this.context = context;
        this.places = places;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_place_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Place place = this.places.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return this.places.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        // Attributes
        private ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find items on View
            this.image = itemView.findViewById(R.id.image);
        }

        /**
         * Bind the information from the place to show
         * @param place: The result gotten
         */
        public void bind(Place place) {
            Glide.with(context)
                    .load(place.getImage().getUrl())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(image);
        }

    }

}