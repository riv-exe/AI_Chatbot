package com.example.mobilechatbot;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import okhttp3.Response;
public class ChatViewModel extends ViewModel {
    // Local classroom demo only: never distribute a key in an APK.
    private static final String API_KEY = "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";
    private static final String MODEL = "openai/gpt-oss-20b";
    private final MutableLiveData<List<Message>> messages =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading =
            new MutableLiveData<>(false);
    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private volatile boolean cleared;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS).build();
    public LiveData<List<Message>> getMessages() { return messages; }
    public LiveData<Boolean> getLoading() { return loading; }
    public void sendMessage(String prompt) {
        if (Boolean.TRUE.equals(loading.getValue())) return;
        if (prompt.trim().isEmpty()) return;
        if (API_KEY.equals("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")) {
            append(new Message("Set your Groq API key first.", false));
            return;
        }
        append(new Message(prompt, true));
        loading.setValue(true);
        worker.execute(() -> {
            String result;
            try { result = callGroqApi(prompt); }
            catch (Exception e) {
                result = "Request failed: " + e.getMessage();
            }
            final String reply = result;
            main.post(() -> {
                if (cleared) return;
                append(new Message(reply, false));
                loading.setValue(false);
            });
        });
    }
    private String callGroqApi(String prompt) throws IOException {
        JsonObject user = new JsonObject();
        user.addProperty("role", "user");
        user.addProperty("content", prompt);
        JsonArray chat = new JsonArray();
        chat.add(user);
        JsonObject json = new JsonObject();
        json.addProperty("model", MODEL);
        json.add("messages", chat);
        json.addProperty("max_completion_tokens", 2048);
        RequestBody body = RequestBody.create(json.toString(),
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()

                .url("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + API_KEY)
                .post(body).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                int status = response.code();
                if (status == 401) throw new IOException("Invalid API key.");
                if (status == 429) throw new IOException("Rate limit. Wait.");
                throw new IOException("HTTP " + status
                        + ": check model access and Groq service.");
            }
            if (response.body() == null)
                throw new IOException("Empty response.");
            try {
                JsonObject root = JsonParser.parseString(
                        response.body().string()).getAsJsonObject();
                JsonArray choices = root.getAsJsonArray("choices");
                if (choices.size() == 0)
                    throw new IOException("No answer returned.");
                String answer = choices.get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
                if (answer.trim().isEmpty())
                    throw new IOException("Empty answer. Try again.");
                return answer;
            } catch (RuntimeException e) {
                throw new IOException("Unexpected response format.", e);
            }
        }
    }
    private void append(Message message) {
        List<Message> next = new ArrayList<>(messages.getValue());
        next.add(message);
        messages.setValue(next);
    }
    @Override protected void onCleared() {
        cleared = true;
        client.dispatcher().cancelAll();
        worker.shutdownNow();
        main.removeCallbacksAndMessages(null);
        super.onCleared();
    }
}