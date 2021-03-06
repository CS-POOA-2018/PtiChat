package fr.centralesupelec.ptichatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import fr.centralesupelec.ptichatapp.PODS.Chat;


/**
 * This activity is the chat page. Here you can send and receive messages in the current chat.
 * The chat can be private (only you and another user) or not (group of users, including yourself)
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Chat> mDataset;
    private MainActivity mMainActivity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mChatRow;
        private String mChatId;
        private TextView mChatName;

        MyViewHolder(View itemView, final MainActivity ma) {
            super(itemView);
            mChatRow = itemView.findViewById(R.id.chatRow);
            mChatName = itemView.findViewById(R.id.chatName);

            mChatRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ma.onSelectChat(mChatId);
                }
            });
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    ChatAdapter(List<Chat> myDataset, MainActivity mainActivity) {
        mDataset = myDataset;
        mMainActivity = mainActivity;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.chat_row, parent, false);
        return new MyViewHolder(itemView, mMainActivity);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Chat chat = mDataset.get(position);
        holder.mChatName.setText(chat.getName());
        holder.mChatId = chat.getId();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
