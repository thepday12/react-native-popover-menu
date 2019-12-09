package com.tokimthep.copyscript;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;


public class CopyAdapter extends  RecyclerView.Adapter<CopyAdapter.MyViewHolder> {

    private List<String> mDataset;
    private Context mContext;
    private final int MAX_LINES_FOR_CONTENT = 3;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView tvContent;
        private ImageButton btClose;

        public MyViewHolder(View view) {
            super(view);
            tvContent = view.findViewById(R.id.tvContent);
            btClose= view.findViewById(R.id.btClose);
        }

    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public CopyAdapter(List<String> dataset) {
        mDataset = dataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CopyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_copy, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        mContext = view.getContext();
        return vh;
    }



    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        String itemObject = mDataset.get(position);

        holder.tvContent.setText(itemObject);

        holder.btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDataset.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position,mDataset.size());
            }
        });

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void updateDataset(List<String> posts) {
        this.mDataset = posts;
        this.notifyDataSetChanged();
    }

    public void addNewValue(String newValue) {
        this.mDataset.add(0,newValue);
        this.notifyItemInserted(0);
//        notifyItemRangeChanged(0,mDataset.size());
    }

    public List<String> getDataset() {
        return this.mDataset;
    }
    public void clear() {
         this.mDataset= new ArrayList<>();
        this.notifyDataSetChanged();
    }
}
