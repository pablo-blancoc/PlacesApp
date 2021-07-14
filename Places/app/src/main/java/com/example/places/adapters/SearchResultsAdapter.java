package com.example.places.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.places.R;
import com.example.places.models.SearchResult;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    // Attributes
    private Context context;
    private List<SearchResult> results;

    public SearchResultsAdapter(Context context, List<SearchResult> results) {
        this.context = context;
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResult result = this.results.get(position);
        holder.bind(result);
    }

    @Override
    public int getItemCount() {
        return this.results.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        // Attributes
        private ImageView ivImage;
        private TextView tvName;
        private TextView tvSecondary;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find items on View
            this.ivImage = itemView.findViewById(R.id.ivImage);
            this.tvName = itemView.findViewById(R.id.tvName);
            this.tvSecondary = itemView.findViewById(R.id.tvSecondary);
        }

        /**
         * Bind the information from the result to it's view
         * @param result: The result gotten
         */
        public void bind(SearchResult result) {
            this.tvName.setText(result.name);
            if(result.isUser) {
                this.ivImage.setVisibility(View.VISIBLE);
                this.tvSecondary.setText(String.format("@%s", result.secondaryInfo));
                Glide.with(context)
                        .load(result.imageUrl)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .circleCrop()
                        .into(ivImage);
            } else {
                this.tvSecondary.setText(String.format("Category: %s", result.secondaryInfo));
                this.ivImage.setVisibility(View.GONE);
            }

        }

    }

}