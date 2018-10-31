package fr.centralesupelec.ptichatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fr.centralesupelec.ptichatapp.PODS.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<Message> mDataset;
    private ChatActivity mChatActivity;
    private int mMyMessageColor;
    private int mOtherMessageColor;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessageTime;
        private TextView mMessageSender;
        private TextView mSaysText;
        private TextView mMessageContent;

        private MyViewHolder(View itemView) {
            super(itemView);
            mMessageTime = itemView.findViewById(R.id.messageTime);
            mMessageSender = itemView.findViewById(R.id.messageSender);
            mSaysText = itemView.findViewById(R.id.saysText);
            mMessageContent = itemView.findViewById(R.id.messageContent);
        }

        private void setTextColor(int color) {
            mMessageTime.setTextColor(color);
            mMessageSender.setTextColor(color);
            mSaysText.setTextColor(color);
            mMessageContent.setTextColor(color);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MessageAdapter(List<Message> myDataset, ChatActivity chatActivity) {
        mDataset = myDataset;
        mChatActivity = chatActivity;
        mMyMessageColor = mChatActivity.getColor(R.color.customMyMessageColor);
        mOtherMessageColor = mChatActivity.getColor(R.color.customOtherMessageColor);
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.message_row, parent, false);
        return new MyViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // - Get element from your dataset at this position
        // - Replace the contents of the view with that element
        Message message = mDataset.get(position);
        String senderId = message.getSenderId();
        String myUserId = mChatActivity.getMyUserId();

        holder.mMessageTime.setText(Utils.dateToString(message.getDate(), "HH:mm"));
        holder.mMessageSender.setText(senderId);
        holder.mMessageContent.setText(message.getContent());
        holder.setTextColor((senderId.equals(myUserId)) ? mMyMessageColor : mOtherMessageColor);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
