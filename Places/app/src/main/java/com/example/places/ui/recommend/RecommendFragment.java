package com.example.places.ui.recommend;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.places.R;
import com.example.places.databinding.LikedFragmentBinding;
import com.example.places.databinding.RecommendFragmentBinding;
import com.example.places.ui.liked.LikedViewModel;

public class RecommendFragment extends Fragment {

    private RecommendViewModel recommendViewModel;
    private RecommendFragmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recommendViewModel = new ViewModelProvider(this).get(RecommendViewModel.class);

        binding = RecommendFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}