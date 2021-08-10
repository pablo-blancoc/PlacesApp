package com.example.places.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Message")
public class Message extends ParseObject {
    public static final String KEY_SENDER = "sender";
    public static final String KEY_TEXT = "text";
    public static final String KEY_CHAT = "chat";

    public ParseUser getSender() {
        return getParseUser(KEY_SENDER);
    }

    public void setSender(ParseUser user) {
        put(KEY_SENDER, user);
    }

    public String getText() {
        return getString(KEY_TEXT);
    }

    public void setText(String text) {
        put(KEY_TEXT, text);
    }

    public ParseObject getChat() {
        return getParseObject(KEY_CHAT);
    }

    public void setChat(ParseObject chat) {
        put(KEY_CHAT, chat);
    }
}
