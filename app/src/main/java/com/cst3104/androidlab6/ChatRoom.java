package com.cst3104.androidlab6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cst3104.androidlab6.databinding.ActivityChatRoomBinding;
import com.cst3104.androidlab6.databinding.MessagesBinding;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatRoom extends AppCompatActivity {
    ActivityChatRoomBinding binding;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    ArrayList<ChatMessage> messagesReceive = new ArrayList<>();
    ChatRoomViewModel chatModel ;
    private RecyclerView.Adapter myAdapter;
    private ChatMessageDAO mDAO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "database-name").build();
        mDAO = db.cmDAO();

        if (messages == null)
        {
            chatModel.messages.setValue(messages = new ArrayList<>());
            Executor thread = Executors.newSingleThreadExecutor();
            thread.execute(() ->
            {
                messages.addAll(mDAO.getAllMessages()); // Once you get the data from database
                runOnUiThread(() -> binding.recycleView.setAdapter(myAdapter)); // You can then load the RecyclerView
            });
        }

        super.onCreate(savedInstanceState);
        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        messages = chatModel.messages.getValue();
        messagesReceive = chatModel.messagesReceive.getValue();

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));
        if (messages == null) {
            chatModel.messages.postValue(new ArrayList<ChatMessage>());
        }
        if (messagesReceive == null) {
            chatModel.messagesReceive.postValue(new ArrayList<ChatMessage>());
        }

        // Send Button
        binding.button.setOnClickListener(click -> {
            if (messages == null) {
                chatModel.messages.postValue(messages = new ArrayList<>());
            }

            String messageText = binding.editText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
            String currentTime = sdf.format(new Date());

            ChatMessage chatMessage = new ChatMessage(messageText, currentTime, true);
            binding.editText.setText("");
            messages.add(chatMessage);
            myAdapter.notifyItemInserted(messages.size() - 1);

        });

        // Receive Button
        binding.button2.setOnClickListener(click -> {
            if (messagesReceive == null) {
                chatModel.messagesReceive.postValue(messagesReceive = new ArrayList<ChatMessage>());
            }

            String messageText = binding.editText.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd-MMM-yyyy hh-mm-ss a");
            String currentTime = sdf.format(new Date());

            ChatMessage chatMessage = new ChatMessage(messageText, currentTime, false);
            binding.editText.setText("");

            messagesReceive.add(chatMessage);
            messages.add(chatMessage); // Add to both lists to keep them in sync

            myAdapter.notifyItemInserted(messagesReceive.size() - 1);
        });

        binding.recycleView.setAdapter(myAdapter = new RecyclerView.Adapter<MyRowHolder>() {
            @NonNull
            @Override
            public MyRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                MessagesBinding binding = MessagesBinding.inflate(getLayoutInflater());
                return new MyRowHolder(binding.getRoot(), mDAO, messages, myAdapter);
            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                ChatMessage message = messages.get(position);

                if (message.isSentButton()) {
                    // Sent message
                    Log.d("MyAdapter", "Binding sent message at position " + position);
                    holder.timeText.setText(message.getTimeSent());
                    holder.messageText.setText(message.getMessage());

                    holder.messageText.setVisibility(View.VISIBLE);
                    holder.timeText.setVisibility(View.VISIBLE);

                    // Hide views related to received messages
                    holder.messageText1.setVisibility(View.GONE);
                    holder.timeText1.setVisibility(View.GONE);
                } else {
                    // Received message
                    Log.d("MyAdapter", "Binding received message at position " + position);
                    holder.timeText1.setText(message.getTimeSent());
                    holder.messageText1.setText(message.getMessage());

                    holder.messageText.setVisibility(View.VISIBLE);
                    holder.timeText1.setVisibility(View.VISIBLE);

                    holder.messageText.setVisibility(View.GONE);
                    holder.timeText.setVisibility(View.GONE);
                }
            }

            public int getItemViewType(int position) {
                ChatMessage message = messages.get(position);
                if (message.isSentButton()) {
                    return 0;
                }
                else return 1;
            }

            @Override
            public int getItemCount() {
                if (messages != null) {
                    return messages.size();
                } else {
                    return 0;
                }
            }
        });
    }
}

class MyRowHolder extends RecyclerView.ViewHolder {
    TextView messageText;
    TextView timeText;
    TextView messageText1;
    TextView timeText1;
    List<ChatMessage> messages;
    private RecyclerView.Adapter<MyRowHolder> myAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());

    public MyRowHolder(@NonNull View itemView, ChatMessageDAO mDAO, List<ChatMessage> messages, RecyclerView.Adapter<MyRowHolder> myAdapter) {
        super(itemView);
        this.messages = messages;
        this.myAdapter = myAdapter;

        itemView.setOnClickListener(clk -> {

            int position = getAbsoluteAdapterPosition();

            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("Question: ");
            builder.setMessage("Do you want to delete the messages: " + messageText.getText());
            builder.setMessage("Do you want to delete the messages: " + messageText1.getText());

            // Yes Button
            builder.setPositiveButton("Yes", (dialog, cl) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    ChatMessage m = messages.get(position);
                    mDAO.deleteMessage(m);

                    handler.post(() -> {
                        ChatMessage removedMessage = messages.get(position);
                        messages.remove(position);
                        myAdapter.notifyItemRemoved(position);

                        Snackbar.make(messageText, "You deleted message #" + position, Snackbar.LENGTH_LONG)
                                .setAction("Undo", clw -> {
                                    messages.add(position, removedMessage);
                                    myAdapter.notifyItemInserted(position);
                                })
                                .show();
                    });
                });
            });

            // No Button
            builder.setNegativeButton("No", (dialog, cl) -> {
            });
            builder.create().show();
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        messageText = itemView.findViewById(R.id.textMessage);
        timeText = itemView.findViewById(R.id.timeMessage);

        messageText1 = itemView.findViewById(R.id.textMessage1);
        timeText1 = itemView.findViewById(R.id.timeMessage1);
    }
}