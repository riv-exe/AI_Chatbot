package com.example.mobilechatbot;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class Message {
    private final String content;
    private final boolean user;
    private final long timestamp;
    public Message(String content, boolean user) {
        this.content = content;
        this.user = user;
        timestamp = System.currentTimeMillis();
    }
    public String getContent() { return content; }
    public boolean isUser() { return user; }
    public String getFormattedTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(timestamp));
    }
}