package com.android_lab_2.Adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android_lab_2.Interface.ItemClickListener;
import com.android_lab_2.R;
import com.android_lab_2.model.TrimmedRSSObject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView title;
    public TextView date;
    public TextView content;
    // for debugging
    public TextView link;
    public ImageView image;

    private ItemClickListener itemClickListener;

    public FeedViewHolder(@NonNull View itemView) {
        super(itemView);

        title = itemView.findViewById(R.id.text_card_title);
        date = itemView.findViewById(R.id.text_card_date);
        content = itemView.findViewById(R.id.text_card_content);
        // for debugging
        link = itemView.findViewById(R.id.text_card_link);
        image = itemView.findViewById(R.id.text_card_image);

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
    private static final String TAG = "FeedAdapter";
    private Context context;
    private LayoutInflater layoutInflater;
    private List<TrimmedRSSObject> trimmedRSSObject;
    private ImageLoader imageLoader = ImageLoader.getInstance(); // Get singleton instance

    public FeedAdapter(Context context,List<TrimmedRSSObject> trimmedRSSObject) {
        this.context = context;
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
       // for debugging
        feedViewHolder.link.setText(trimmedRSSObject.get(i).getLink());

        String uri = "https://gfx.nrk.no/MNSXJtYbTWLX9jZFjJZGOwwgfUF5qvgO5-H9OuyNhzuw";
        imageLoader.displayImage(trimmedRSSObject.get(i).getImageUrl(),feedViewHolder.image);


        feedViewHolder.setItemClickListener(new ItemClickListener() {

            @Override
            public void onClick(View view, int position, boolean isClick) {
                if (isClick) {

                    Uri parse;
                    try {
                        parse =  Uri.parse(trimmedRSSObject.get(position).getLink());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    if (parse.toString() == "" || parse.toString() == "") {
                        return;
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW, parse);
                    context.startActivity(intent);
                }
            }
        });
    }




    @Override
    public int getItemCount() {
       return trimmedRSSObject.size();
        //return RSSObject.items.size();
    }
}
