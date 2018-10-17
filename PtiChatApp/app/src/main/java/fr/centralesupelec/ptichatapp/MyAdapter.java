package fr.centralesupelec.ptichatapp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import fr.centralesupelec.ptichatapp.PODS.User;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private User[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mContactName;
        public TextView mContactStatus;
        public ImageView mContactIcon;
        public MyViewHolder(View itemView) {
            super(itemView);
            mContactName = itemView.findViewById(R.id.nameContact);
            mContactStatus = itemView.findViewById(R.id.statusContact);
            mContactIcon = itemView.findViewById(R.id.iconContact);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(User[] myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.contact_row, parent, false);
        return new MyViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        User contact = mDataset[position];
        holder.mContactName.setText(contact.getPseudo());
        holder.mContactStatus.setText(String.format("- %s", contact.getStatus()));
        if (contact.isConnected()) {
            holder.mContactIcon.setImageResource(R.drawable.ic_contact_green_24dp);
        } else {
            holder.mContactIcon.setImageResource(R.drawable.ic_contact_red_24dp);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }

}
