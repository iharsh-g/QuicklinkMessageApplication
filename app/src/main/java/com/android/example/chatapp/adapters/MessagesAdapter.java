package com.android.example.chatapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.example.chatapp.specificChat.ImagePreviewActivity;
import com.android.example.chatapp.databinding.ReceiverChatLayoutBinding;
import com.android.example.chatapp.databinding.SenderChatLayoutBinding;
import com.android.example.chatapp.models.Messages;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MessagesAdapter extends RecyclerView.Adapter {

    Context mContext;
    Activity mActivity;
    ArrayList<Messages> messagesArrayList;

    int ITEM_SEND = 1;
    int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<Messages> messagesArrayList, Activity activity) {
        this.mContext = context;
        this.messagesArrayList = messagesArrayList;
        this.mActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == ITEM_SEND) {
            return new SenderViewHolder(SenderChatLayoutBinding.inflate(LayoutInflater.from(mContext)));
        }
        else {
            return new ReceiverViewHolder(ReceiverChatLayoutBinding.inflate(LayoutInflater.from(mContext)));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.@NonNull ViewHolder holder, int position) {
        Messages messages = messagesArrayList.get(position);
        String data = timeStampConverter(messages.getTimestamp());

        if(holder.getClass() == SenderViewHolder.class) {

            SenderViewHolder viewHolder = (SenderViewHolder)holder;
            viewHolder.mSenderBinding.layoutForMessage.setVisibility(View.VISIBLE);
            viewHolder.mSenderBinding.cvImage.setVisibility(View.GONE);

            if(messages.isImage()) {  //image
                viewHolder.mSenderBinding.layoutForMessage.setVisibility(View.GONE);
                viewHolder.mSenderBinding.cvImage.setVisibility(View.VISIBLE);

                String decryptMsg = Decrypt(messages.getMessage(), messages.getToken());
                byte[] bytes = Base64.decode(decryptMsg, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                viewHolder.mSenderBinding.imageView.setImageBitmap(bitmap);

                viewHolder.mSenderBinding.timeOfMessage2.setText(data);

                viewHolder.mSenderBinding.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ImagePreviewActivity.class);
                        intent.putExtra("image", bytes);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                mActivity, viewHolder.mSenderBinding.imageView,
                                ViewCompat.getTransitionName(viewHolder.mSenderBinding.imageView));
                        mContext.startActivity(intent, options.toBundle());
                    }
                });
            }
            else {
                String decryptMsg = Decrypt(messages.getMessage(), messages.getToken());
                viewHolder.mSenderBinding.senderMessage.setText(decryptMsg);
                viewHolder.mSenderBinding.timeOfMessage1.setText(data);
            }


        }
        else {

            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.mReceiverBinding.layoutForMessage.setVisibility(View.VISIBLE);
            viewHolder.mReceiverBinding.cvImage.setVisibility(View.GONE);

            if(messages.isImage()) {  //image
                viewHolder.mReceiverBinding.layoutForMessage.setVisibility(View.GONE);
                viewHolder.mReceiverBinding.cvImage.setVisibility(View.VISIBLE);

                String decryptMsg = Decrypt(messages.getMessage(), messages.getToken());
                byte[] bytes = Base64.decode(decryptMsg, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                viewHolder.mReceiverBinding.imageView.setImageBitmap(bitmap);
                viewHolder.mReceiverBinding.timeOfMessage2.setText(data);

                viewHolder.mReceiverBinding.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, ImagePreviewActivity.class);
                        intent.putExtra("image", bytes);
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                mActivity, viewHolder.mReceiverBinding.imageView,
                                ViewCompat.getTransitionName(viewHolder.mReceiverBinding.imageView));
                        mContext.startActivity(intent, options.toBundle());
                    }
                });
            }
            else {
                String decryptMsg = Decrypt(messages.getMessage(), messages.getToken());
                viewHolder.mReceiverBinding.receiverMessage.setText(decryptMsg);
                viewHolder.mReceiverBinding.timeOfMessage1.setText(data);
            }
        }


    }

    private String timeStampConverter(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        return DateFormat.format("dd MMM, hh:mm a", cal).toString();
    }


    @Override
    public int getItemViewType(int position) {
        Messages messages = messagesArrayList.get(position);
        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderId())) {
            return  ITEM_SEND;
        }
        else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    class SenderViewHolder extends RecyclerView.ViewHolder {
        private SenderChatLayoutBinding mSenderBinding;

        public SenderViewHolder(SenderChatLayoutBinding binding) {
            super(binding.getRoot());
            this.mSenderBinding = binding;
        }
    }

    class ReceiverViewHolder extends RecyclerView.ViewHolder {
        private ReceiverChatLayoutBinding mReceiverBinding;

        public ReceiverViewHolder(ReceiverChatLayoutBinding binding) {
            super(binding.getRoot());
            this.mReceiverBinding = binding;
        }
    }

    public static String Decrypt(String value, String token){
        try {
            byte[] ivAndCipherText = Base64.decode(value, Base64.NO_PADDING);
            byte[] iv = Arrays.copyOfRange(ivAndCipherText, 0, 16);
            byte[] cipherText = Arrays.copyOfRange(ivAndCipherText, 16, ivAndCipherText.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(token.getBytes("utf-8"), "AES"), new IvParameterSpec(iv));

            return new String(cipher.doFinal(cipherText), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
