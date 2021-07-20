package com.example.places;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.places.databinding.ActivityLoginBinding;
import com.example.places.models.User;
import com.onesignal.OneSignal;
import com.parse.LogInCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import static android.view.View.GONE;

public class LoginActivity extends AppCompatActivity {

    // Constants
    private final static String TAG = "LoginActivity";

    // Attributes
    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ViewBinding
        this.binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }

        // User has already logged in
        if(ParseUser.getCurrentUser() != null) {
            this.goMainActivity();
        }

        // Buttons clickListeners
        this.binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                login(username, password);
            }
        });
        this.binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = binding.etUsername.getText().toString();
                String password = binding.etPassword.getText().toString();
                signup(username, password);
            }
        });
    }

    /**
     * Attempts to login into the app using credentials given
     * @param username: The username provided
     * @param password: The password provided
     */
    private void login(String username, String password) {
        startLoading();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                finishLoading();

                if( e == null ) {
                    // Logged in
                    String text = String.format("Welcome, %s!", user.getUsername());
                    Toast.makeText(LoginActivity.this, text, Toast.LENGTH_SHORT).show();

                    try {
                        String oneSignalId = OneSignal.getDeviceState().getUserId();
                        if(!oneSignalId.isEmpty()) {
                            ParseUser.getCurrentUser().put(User.KEY_ONE_SIGNAL, oneSignalId);
                            ParseUser.getCurrentUser().saveInBackground();
                        }
                    } catch (NullPointerException ex) {
                        Log.e(TAG, "NoeSignal no UUID", ex);
                    }

                    goMainActivity();
                } else {
                    // Could not login
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error on login: ", e);
                }
            }
        });
    }

    /**
     * Signs up in the platform using credentials provided
     * @param username: The username provided
     * @param password: The password provided
     */
    private void signup(String username, String password) {
        this.startLoading();

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(username);
        user.setBio("default bio...");
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                finishLoading();

                if (e == null) {

                    // Create relations object for user
                    ParseObject object = new ParseObject("Relations");
                    object.put("user", ParseUser.getCurrentUser());
                    object.put("followerCount", 0);
                    object.put("followingCount", 0);
                    object.saveInBackground();

                    login(username, password);
                } else {
                    Toast.makeText(LoginActivity.this, "Could not signup", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Could not signup", e);
                }
            }
        });
    }

    private void goMainActivity() {
        cleanFields();

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // With finish the LoginActivity is removed from back-stack
        finish();
    }

    private void cleanFields() {
        this.binding.etPassword.setText("");
        this.binding.etUsername.setText("");
    }

    private void startLoading() {
        this.binding.btnLogin.setVisibility(View.GONE);
        this.binding.btnSignup.setVisibility(View.GONE);
        this.binding.loading.setVisibility(View.VISIBLE);
    }

    private void finishLoading() {
        this.binding.btnLogin.setVisibility(View.VISIBLE);
        this.binding.btnSignup.setVisibility(View.VISIBLE);
        this.binding.loading.setVisibility(View.GONE);
    }
}
