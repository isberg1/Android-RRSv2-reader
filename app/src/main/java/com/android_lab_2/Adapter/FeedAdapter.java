package com.android_lab_2.Adapter;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android_lab_2.Interface.ItemClickListener;
import com.android_lab_2.R;
import com.android_lab_2.model.RSSObject;
import com.android_lab_2.model.TrimmedRSSObject;

import java.util.List;

class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView title;
    public TextView date;
    public TextView content;

    private ItemClickListener itemClickListener;

    public FeedViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.text_card_title);
        date = itemView.findViewById(R.id.text_card_date);
        content = itemView.findViewById(R.id.text_card_content);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }


    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {

        itemClickListener.onClick(v, getAdapterPosition(),false);


    }

    @Override
    public boolean onLongClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition(),true);
        return true;
    }
}




public class FeedAdapter extends RecyclerView.Adapter<FeedViewHolder> {

    private Context context;
    private LayoutInflater layoutInflater;
    private RSSObject RSSObject;
    private List<TrimmedRSSObject> trimmedRSSObject;

    public FeedAdapter(Context context/*, RSSObject RSSObject*/,List<TrimmedRSSObject> trimmedRSSObject) {
        this.context = context;
       // this.RSSObject = RSSObject;
        this.trimmedRSSObject = trimmedRSSObject;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = layoutInflater.inflate(R.layout.row,viewGroup, false );

        return new FeedViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder feedViewHolder, int i) {

        feedViewHolder.title.setText(trimmedRSSObject.get(i).getTitle());
        feedViewHolder.date.setText(trimmedRSSObject.get(i).getPubDate());
        feedViewHolder.content.setText(trimmedRSSObject.get(i).getDescription());

        feedViewHolder.setItemClickListener(new ItemClickListener() {

            @Override
            public void onClick(View view, int position, boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trimmedRSSObject.get(position).getLink()));
                    context.startActivity(intent);
                }
            }
        });
    }


   /*
    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder feedViewHolder, int i) {

        feedViewHolder.title.setText(RSSObject.getItems().get(i).getTitle());
        feedViewHolder.date.setText(RSSObject.getItems().get(i).getPupDate());
        feedViewHolder.content.setText(RSSObject.getItems().get(i).getContent());

        feedViewHolder.setItemClickListener(new ItemClickListener() {

            @Override
            public void onClick(View view, int position, boolean isClick) {
                if (isClick) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RSSObject.getItems().get(position).getLink()));
                    context.startActivity(intent);
                }
            }
        });
    }*/

    @Override
    public int getItemCount() {
       return trimmedRSSObject.size();
        //return RSSObject.items.size();
    }
}
