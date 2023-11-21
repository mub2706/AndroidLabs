package com.cst3104.androidlab6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.cst3104.androidlab6.databinding.ActivityChatRoomBinding;
import com.cst3104.androidlab6.databinding.MessagesBinding;

import java.util.ArrayList;
import java.util.Date;

public class ChatRoom extends AppCompatActivity {

    ActivityChatRoomBinding binding;
    ArrayList<ChatMessage> messages = new ArrayList<>();
    ArrayList<ChatMessage> messagesReceive = new ArrayList<>();
    ChatRoomViewModel chatModel ;
    private RecyclerView.Adapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

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

                MessagesBinding binding = MessagesBinding   .inflate(getLayoutInflater());

                return new MyRowHolder(binding.getRoot());

            }

            @Override
            public void onBindViewHolder(@NonNull MyRowHolder holder, int position) {
                ChatMessage message = messages.get(position);

                if (message.isSentButton()) {
                    // Sent message
                    Log.d("MyAdapter", "Binding sent message at position " + position);
                    holder.timeText.setText(message.getTimeSent());
                    holder.messageText.setText(message.getMessage());

                    // Set visibility for views related to sent messages
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

                    // Set visibility for views related to received messages
                    holder.messageText.setVisibility(View.VISIBLE);
                    holder.timeText1.setVisibility(View.VISIBLE);

                    // Hide views related to sent messages
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
    public MyRowHolder(@NonNull View itemView) {
        super(itemView);

        messageText = itemView.findViewById(R.id.textMessage);
        timeText = itemView.findViewById(R.id.timeMessage);

        messageText1 = itemView.findViewById(R.id.textMessage1);
        timeText1 = itemView.findViewById(R.id.timeMessage1);
    }
}
