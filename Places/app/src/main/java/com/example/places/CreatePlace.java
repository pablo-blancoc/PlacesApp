package com.example.places;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.places.databinding.ActivityCreatePlaceBinding;
import com.example.places.databinding.ActivityLoginBinding;

public class CreatePlace extends AppCompatActivity {

    // Constants
    private final static String TAG = "CreatePlace";

    // Attributes
    ActivityCreatePlaceBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_place);

        // Setup ViewBinding
        this.binding = ActivityCreatePlaceBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

    }
}