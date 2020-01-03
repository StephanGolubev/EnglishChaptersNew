package com.evgenyenglish.englishapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.nio.charset.spi.CharsetProvider;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChapterActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String URL = "http://kurchanovenglish.ru/data/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter2);

        recyclerView = findViewById(R.id.recyclerview_chapter);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        final ChapterAdapter chapterAdapter = new ChapterAdapter(getApplicationContext());

        recyclerView.setAdapter(chapterAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderChapter jsonPlaceHolderChapter = retrofit.create(JsonPlaceHolderChapter.class);

        Call<List<Chapter>> call = jsonPlaceHolderChapter.getPosts();

        call.enqueue(new Callback<List<Chapter>>() {
            @Override
            public void onResponse(Call<List<Chapter>> call, Response<List<Chapter>> response) {


                if (!response.isSuccessful()) {
                    return;
                }

                List<Chapter> chapter = response.body();

                int i = 0;
                for (final Chapter chapter1 : chapter) {
                    chapterAdapter.addChapter(String.valueOf(chapter1.getNumber()));
                }
            }

            @Override
            public void onFailure(Call<List<Chapter>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, WelcomeActivity.class);

        startActivity(intent);

        this.finish();
    }
}
