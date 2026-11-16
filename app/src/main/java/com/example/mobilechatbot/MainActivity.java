package com.example.mobilechatbot;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.mobilechatbot.databinding.ActivityMainBinding;
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        adapter = new ChatAdapter();
        binding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChat.setAdapter(adapter);
        viewModel.getMessages().observe(this, messages -> {
            adapter.submitMessages(messages);
            if (!messages.isEmpty())
                binding.rvChat.scrollToPosition(messages.size() - 1);
        });
        viewModel.getLoading().observe(this, loading -> {
            boolean busy = Boolean.TRUE.equals(loading);
            binding.btnSend.setEnabled(!busy);
            binding.btnSend.setText(busy ? "Sending..." : "Send");
        });
        binding.btnSend.setOnClickListener(view -> {
            String message = binding.etMessage.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.sendMessage(message);
            binding.etMessage.getText().clear();
        });
    }
}