package com.example.places;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.places.adapters.ChatAdapter;
import com.example.places.databinding.ActivityChatBinding;
import com.example.places.databinding.ActivityFindNearbyBinding;
import com.example.places.models.Message;
import com.example.places.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;

public class ChatActivity extends AppCompatActivity {

    // Constants
    private static final String TAG = "ChatActivity";

    // Attributes
    ActivityChatBinding binding;
    ChatAdapter adapter;
    ParseUser otherUser;
    ParseObject chat;
    List<Message> messages;

    // Create a handler which can run code periodically
    static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    Handler myHandler = new android.os.Handler();

    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            myHandler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup ViewBinding
        this.binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(this.binding.getRoot());

        // Start loading
        this.binding.rvMessages.setVisibility(View.GONE);
        this.binding.loading.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        String otherUserId = intent.getStringExtra("user");
        String chatId = intent.getStringExtra("chatId");
        try {
            // Get the user
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            this.otherUser = userQuery.get(otherUserId);

            // Get the chat
            ParseQuery<ParseObject> chatQuery = ParseQuery.getQuery("Chat");
            this.chat = chatQuery.get(chatId);
        } catch (ParseException ex) {
            Log.e(TAG, "Error loading user", ex);
            Toasty.error(ChatActivity.this, "Error retrieving information", Toast.LENGTH_SHORT, true).show();
            finish();
        }

        // Set up adapter
        this.messages = new ArrayList<>();
        this.adapter = new ChatAdapter(ChatActivity.this, this.messages);
        this.binding.rvMessages.setAdapter(this.adapter);
        this.binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));

        // Get messages
        refreshMessages();

        // Stop loading
        this.binding.rvMessages.setVisibility(View.VISIBLE);
        this.binding.loading.setVisibility(View.GONE);

        this.binding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = binding.etMessage.getText().toString();
                if(text.isEmpty()) {
                    return;
                }

                Message msg = new Message();
                msg.setText(text);
                msg.setChat(chat);
                msg.setSender(ParseUser.getCurrentUser());
                msg.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null) {
                            messages.add(msg);
                            adapter.notifyItemInserted(messages.size()-1);
                        } else {
                            onToastError("Error sending message.");
                        }
                    }
                });
            }
        });

    }

    /**
     * Query messages from Parse backend
     */
    void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
        query.whereEqualTo(Message.KEY_CHAT, this.chat);
        query.orderByDescending("createdAt");
        query.findInBackground(new FindCallback<Message>() {
            @Override
            public void done(List<Message> objects, ParseException e) {
                Toast.makeText(ChatActivity.this, "MESSAGES: " + String.valueOf(objects.size()), Toast.LENGTH_SHORT).show();
                if(e == null) {
                    messages.addAll(objects);
                    adapter.notifyDataSetChanged();
                } else {
                    onToastError("Error ocurred while retrieving newest messages.");
                    Log.e(TAG, "Retrieving messages: ", e);
                }
            }
        });
    }

    private void onToastError(String text) {
        ChatActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toasty.error(ChatActivity.this, text, Toasty.LENGTH_LONG, true).show();
            }
        });
    }
}