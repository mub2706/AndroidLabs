package com.cst3104.androidlab6;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ChatRoomViewModel extends ViewModel {
    public MutableLiveData<ArrayList<ChatMessage>> messages = new MutableLiveData<>();

    public MutableLiveData<ArrayList<ChatMessage>> messagesReceive = new MutableLiveData<>();
}