package fr.centralesupelec.ptichatapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import fr.centralesupelec.ptichatapp.PODS.Chat;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyViewHolder> {

    private List<Chat> mDataset;
    private MainActivity mMainActivity;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mChatRow;
        private String mChatId;
        private TextView mChatName;
        private ImageView mChatIcon;

        public MyViewHolder(View itemView, final MainActivity ma) {
            super(itemView);
            mChatRow = itemView.findViewById(R.id.chatRow);
            mChatIcon = itemView.findViewById(R.id.chatIcon);
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
    public ChatAdapter(List<Chat> myDataset, MainActivity mainActivity) {
        mDataset = myDataset;
        mMainActivity = mainActivity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ChatAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.chat_row, parent, false);
        return new MyViewHolder(itemView, mMainActivity);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
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
