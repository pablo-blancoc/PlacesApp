package com.example.places.ui.liked;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.places.R;
import com.example.places.databinding.FragmentHomeBinding;
import com.example.places.databinding.LikedFragmentBinding;
import com.example.places.ui.home.HomeViewModel;

public class LikedFragment extends Fragment {

    private LikedViewModel likedViewModel;
    private LikedFragmentBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        likedViewModel = new ViewModelProvider(this).get(LikedViewModel.class);

        binding = LikedFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
