package com.evgenyenglish.englishapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChapterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<String> chapters = new ArrayList<>();
    private Context context;

    ChapterAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_chapter, null, false);
        ChapterViewHolder chapterViewHolder = new ChapterViewHolder(itemView);
        return chapterViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ((ChapterViewHolder)(holder)).button.setText(chapters.get(position));

        ((ChapterViewHolder)(holder)).button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, LessonsActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("Number", chapters.get(position));

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public void addChapter(String str) {
        chapters.add(str);
        notifyItemInserted(chapters.size() - 1);
    }

    public class ChapterViewHolder extends  RecyclerView.ViewHolder{
        protected Button button;

        public ChapterViewHolder(View itemView) {
            super(itemView);

            Typeface tf = Typeface.createFromAsset(context.getAssets(),"font.Impact/Impact.ttf");

            button = itemView.findViewById(R.id.button_ch);

            button.setTypeface(tf);

            button.setAlpha((float) 0.8);
        }
    } // Complete
}
