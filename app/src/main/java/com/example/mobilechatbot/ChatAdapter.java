package com.example.mobilechatbot;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
public class ChatAdapter
        extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private final List<Message> messages = new ArrayList<>();
    public void submitMessages(List<Message> items) {
        messages.clear();
        messages.addAll(items);
        notifyDataSetChanged();
    }
    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        final TextView tvMessage, tvTime;
        public MessageViewHolder(View view) {
            super(view);
            tvMessage = view.findViewById(R.id.tvMessage);
            tvTime = view.findViewById(R.id.tvTime);
        }
    }
    @Override public int getItemViewType(int position) {
        return messages.get(position).isUser() ? 1 : 0;
    }
    @Override public MessageViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.item_user_message
                : R.layout.item_bot_message;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new MessageViewHolder(view);
    }
    @Override public void onBindViewHolder(MessageViewHolder holder,
                                           int position) {
        Message message = messages.get(position);
        holder.tvMessage.setText(message.getContent());
        holder.tvTime.setText(message.getFormattedTime());
    }
    @Override public int getItemCount() { return messages.size(); }
}