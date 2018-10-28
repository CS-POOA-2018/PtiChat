package fr.centralesupelec.ptichatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fr.centralesupelec.ptichatapp.PODS.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private List<Message> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessageTime;
        private TextView mMessageSender;
        private TextView mMessageContent;
        private MyViewHolder(View itemView) {
            super(itemView);
            mMessageTime = itemView.findViewById(R.id.messageTime);
            mMessageSender = itemView.findViewById(R.id.messageSender);
            mMessageContent = itemView.findViewById(R.id.messageContent);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    MessageAdapter(List<Message> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public MessageAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.message_row, parent, false);
        return new MyViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Message message = mDataset.get(position);
        holder.mMessageTime.setText(Utils.dateToString(message.getDate(), "HH:mm"));
        holder.mMessageSender.setText(message.getSenderId());
        holder.mMessageContent.setText(message.getContent());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
