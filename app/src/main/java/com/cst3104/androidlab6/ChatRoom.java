package com.cst3104.androidlab6;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.cst3104.androidlab6.databinding.ActivityChatRoomBinding;
import com.cst3104.androidlab6.databinding.MessagesBinding;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.item1) {
            int selectedPosition = myAdapter.getItemCount() > 0 ? 0 : RecyclerView.NO_POSITION;
            if (selectedPosition != RecyclerView.NO_POSITION) {
                showDeleteConfirmationDialog(selectedPosition);
            }
            return true;
        } else if (item.getItemId() == R.id.aboutItem) {
            // Display About toast
            Toast.makeText(this, "Version 1.0 , created by Mubarak Hassan", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);

        return true;
    }

    private void showDeleteConfirmationDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Question:");
        builder.setMessage("Do you want to delete the message: " + messages.get(position).getMessage());
        builder.setPositiveButton("Yes", (dialog, cl) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                ChatMessage selectedMessage = messages.get(position);
                mDAO.deleteMessage(selectedMessage);

                new Handler(Looper.getMainLooper()).post(() -> {
                    messages.remove(position);
                    myAdapter.notifyItemRemoved(position);

                    Snackbar.make(binding.getRoot(), "You deleted the message", Snackbar.LENGTH_LONG)
                            .setAction("Undo", clw -> {
                                // Undo deletion
                                messages.add(position, selectedMessage);
                                myAdapter.notifyItemInserted(position);
                            })
                            .show();
                });
            });
        });

        builder.setNegativeButton("No", (dialog, cl) -> {
            // Do nothing if the user chooses not to delete the message
        });

        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        chatModel = new ViewModelProvider(this).get(ChatRoomViewModel.class);
        binding = ActivityChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (chatModel.messages.getValue() == null) {
            chatModel.messages.postValue(new ArrayList<>());
        }

        MessageDatabase db = Room.databaseBuilder(getApplicationContext(), MessageDatabase.class, "database-name").build();

        mDAO = db.cmDAO();

        binding.recycleView.setLayoutManager(new LinearLayoutManager(this));

        Executor thread = Executors.newSingleThreadExecutor();
        thread.execute(() -> {
            messages.addAll(mDAO.getAllMessages());
            runOnUiThread(() -> {
                myAdapter.notifyDataSetChanged();
                binding.recycleView.setAdapter(myAdapter);
            });
        });

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