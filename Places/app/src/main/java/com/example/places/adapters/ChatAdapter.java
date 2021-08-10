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
import com.example.places.models.Message;
import com.example.places.models.Place;
import com.example.places.models.User;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    // Constants
    private static final int MESSAGE_OUTGOING = 1;
    private static final int MESSAGE_INCOMING = -1;

    // Attributes
    private Context context;
    private List<Message> messages;

    public ChatAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);


        if (viewType == MESSAGE_INCOMING) {
            View contactView = inflater.inflate(R.layout.item_message_other, parent, false);
            return new IncomingMessageViewHolder(contactView);
        } else if (viewType == MESSAGE_OUTGOING) {
            View contactView = inflater.inflate(R.layout.item_message_own, parent, false);
            return new OutgoingMessageViewHolder(contactView);
        } else {
            throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = this.messages.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isMe(position)) {
            return MESSAGE_OUTGOING;
        } else {
            return MESSAGE_INCOMING;
        }
    }

    /**
     * Function used to know if the sender of the message is me
     * @param position: Position of the message in the messages list
     * @return boolean
     */
    private boolean isMe(int position) {
        Message message = this.messages.get(position);
        return message.getSender().getObjectId() != null && message.getSender().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(Message message);
    }

    public class IncomingMessageViewHolder extends ViewHolder {
        TextView tvUser, tvMessage;

        public IncomingMessageViewHolder(View itemView) {
            super(itemView);
            this.tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            this.tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
        }

        @Override
        public void bindMessage(Message message) {
            try {
                this.tvUser.setText(message.getSender().fetchIfNeeded().getString("name"));
            } catch (ParseException e) {
                this.tvUser.setText("You");
            }
            this.tvMessage.setText(message.getText());
        }
    }

    public class OutgoingMessageViewHolder extends ViewHolder {
        TextView tvUser, tvMessage;

        public OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            this.tvUser = (TextView) itemView.findViewById(R.id.tvUser);
            this.tvMessage = (TextView) itemView.findViewById(R.id.tvMessage);
        }

        @Override
        public void bindMessage(Message message) {
            try {
                this.tvUser.setText(message.getSender().fetchIfNeeded().getString("name"));
            } catch (ParseException e) {
                this.tvUser.setText("Other user");
                e.printStackTrace();
            }
            this.tvMessage.setText(message.getText());
        }
    }

}