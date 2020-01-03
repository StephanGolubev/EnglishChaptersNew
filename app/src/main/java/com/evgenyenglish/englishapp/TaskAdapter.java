package com.evgenyenglish.englishapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.SpeechRecognizer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.eyalbira.loadingdots.LoadingDots;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.ohoussein.playpause.PlayPauseView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.supercharge.shimmerlayout.ShimmerLayout;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TEXT = 0;
    private final int IMAGE = 1;
    private final int RECOGNIZER = 2;
    private final int RECORDING = 3;
    private final int BUTTON = 4;
    private final int TEXT_RIGHT = 5;
    private final int PAUSE_BUTTON = 6;
    public NowPlaying mediaPlayer = new NowPlaying();
    public SpeechRecognizer mSpeechRecognizer;
    public Intent mSpeechRecogniserIntent;

    private List<TaskEnter> taskList = new ArrayList<>();
    private Context context;
    private int position_recognizer = -1;

    public class NowPlaying {
        public MediaPlayer mediaPlayer = new MediaPlayer();
        public int position = -1;
        public LoadingDots loadingDots;
        public TextView textView;

        public NowPlaying() {
        }
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }

        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
    }

    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();


        s1 = s1.replace("?", "");
        s1 = s1.replace("!", "");
        s1 = s1.replace(".", "");

        s2 = s2.replace("?", "");
        s2 = s2.replace("!", "");
        s2 = s2.replace(".", "");

        s1 = s1.replace("I'm", "i am");

        s2 = s2.replace("I'm", "i am");

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];

    }

    public static final Spannable getColoredString(CharSequence text, int color) {
        Spannable spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    TaskAdapter(SpeechRecognizer recognizer, Intent intent) {
        mSpeechRecognizer = recognizer;
        mSpeechRecogniserIntent = intent;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        switch (viewType) {
            case TEXT:
                View viewText = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_text, parent, false);
                TaskViewHolderText taskViewHolderText = new TaskViewHolderText(viewText);
                Log.d("Recycler", "text");
                return taskViewHolderText;
            case IMAGE:
                View viewImage = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_image, parent, false);
                TaskViewHolderImage taskViewHolderImage = new TaskViewHolderImage(viewImage, taskList.size() - 1);
                Log.d("Recycler", "image");
                return taskViewHolderImage;
            case RECOGNIZER:
                View viewRecognizer = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_listen, parent, false);
                TaskViewHolderRecognizer taskViewHolderRecognizer = new TaskViewHolderRecognizer(viewRecognizer);
                Log.d("Recycler", "recognizer");
                return taskViewHolderRecognizer;
            case RECORDING:
                View viewRecording = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_their_think, parent, false);
                TaskViewHolderRecording taskViewHolderRecording = new TaskViewHolderRecording(viewRecording, taskList.size() - 1);
                Log.d("Recycler", "recording");
                return taskViewHolderRecording;
            case BUTTON:
                View viewButton = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_button_mes, parent, false);
                TaskViewHolderButton taskViewHolderButton = new TaskViewHolderButton(viewButton, taskList.size() - 1);
                Log.d("Recycler", "button");
                return taskViewHolderButton;
            case TEXT_RIGHT:
                View viewTextRight = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_text_right, parent, false);
                TaskViewHolderTextRight taskViewHolderTextRight = new TaskViewHolderTextRight(viewTextRight);
                Log.d("Recycler", "text right");
                return taskViewHolderTextRight;
            case PAUSE_BUTTON:
                View viewPauseButton = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_listen_button, parent, false);
                TaskViewHolderPauseButton taskViewHolderPauseButton = new TaskViewHolderPauseButton(viewPauseButton);
                Log.d("Recycler", "text right");
                return taskViewHolderPauseButton;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        Log.d("Position", String.valueOf(position));
        switch (holder.getItemViewType()) {
            case TEXT:
                ((TaskViewHolderText) (holder)).textView.setText(taskList.get(position).getText());
                break;
            case RECORDING:
                if (mediaPlayer.mediaPlayer.isPlaying()) {
                    if (mediaPlayer.position == position) {
                        ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.VISIBLE);
                        ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.INVISIBLE);
                    } else {
                        ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                        ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                        if (position == taskList.size() - 1) {
                            try {
                                Log.d("MediaPlayer", "Position - 1 - prepare playing");
                                mediaPlayer.loadingDots.setVisibility(View.INVISIBLE);
                                mediaPlayer.textView.setVisibility(View.VISIBLE);

                                ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.VISIBLE);
                                ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.INVISIBLE);

                                mediaPlayer.mediaPlayer.stop();

                                mediaPlayer.loadingDots = ((TaskViewHolderRecording) (holder)).loadingDots;
                                mediaPlayer.textView = ((TaskViewHolderRecording) (holder)).textView;

                                mediaPlayer.mediaPlayer = new MediaPlayer();

                                mediaPlayer.mediaPlayer.setDataSource(taskList.get(position).getFileMediaPlayer());

                                mediaPlayer.position = position;

                                mediaPlayer.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                        Log.d("MediaPlayer", "Position - 1 - start playing");
                                    }
                                });

                                mediaPlayer.mediaPlayer.prepareAsync();

                                mediaPlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        taskList.get(position).next = true;

                                        ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                                        ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                                        Log.d("MediaPlayer", "Position - 1 - stop playing (next1 = true)");
                                    }
                                });
                            } catch (IOException ignore) {
                            }
                        }
                    }
                } else {
                    ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                    ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                    if (position == taskList.size() - 1) {
                        try {
                            Log.d("MediaPlayer", "Position - 1 - prepare playing");
                            ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.VISIBLE);
                            ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.INVISIBLE);

                            mediaPlayer.loadingDots = ((TaskViewHolderRecording) (holder)).loadingDots;
                            mediaPlayer.textView = ((TaskViewHolderRecording) (holder)).textView;

                            mediaPlayer.mediaPlayer = new MediaPlayer();

                            mediaPlayer.mediaPlayer.setDataSource(taskList.get(position).getFileMediaPlayer());

                            mediaPlayer.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mp.start();
                                    mediaPlayer.position = position;
                                    Log.d("MediaPlayer", "Position - 1 - start playing");

                                    if (!mp.isPlaying())
                                        taskList.get(position).next = true;
                                }
                            });

                            mediaPlayer.mediaPlayer.prepareAsync();

                            mediaPlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    taskList.get(position).next = true;

                                    ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                                    ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                                    Log.d("MediaPlayer", "Position - 1 - stop playing (next = true)");
                                }
                            });
                        } catch (IOException ignore) {
                        }
                    }
                }

                ((TaskViewHolderRecording) (holder)).relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.mediaPlayer.isPlaying()) {
                            ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                            ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                            mediaPlayer.mediaPlayer.stop();

                            if (mediaPlayer.position != position) {
                                mediaPlayer.loadingDots.setVisibility(View.INVISIBLE);
                                mediaPlayer.textView.setVisibility(View.VISIBLE);

                                ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.VISIBLE);
                                ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.INVISIBLE);

                                mediaPlayer.loadingDots =((TaskViewHolderRecording) (holder)).loadingDots;
                                mediaPlayer.textView=((TaskViewHolderRecording) (holder)).textView;
                                mediaPlayer.position = position;

                                try {
                                    ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.VISIBLE);
                                    ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.INVISIBLE);


                                    if (position_recognizer != -1 && !taskList.get(position_recognizer).getType().equalsIgnoreCase("pause")) {
                                        taskList.get(position_recognizer).setType("pause");
                                        notifyItemChanged(position_recognizer);
                                    }

                                    mediaPlayer.mediaPlayer = new MediaPlayer();

                                    mediaPlayer.mediaPlayer.setDataSource(taskList.get(position).getFileMediaPlayer());

                                    mediaPlayer.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            mp.start();
                                            mediaPlayer.position = position;
                                        }
                                    });

                                    mediaPlayer.mediaPlayer.prepareAsync();

                                    mediaPlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                        @Override
                                        public void onCompletion(MediaPlayer mp) {
                                            taskList.get(position).next = true;

                                            ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                                            ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                                            if (position_recognizer != -1) {
                                                taskList.get(position_recognizer).setType("listen");
                                                notifyItemChanged(position_recognizer);
                                            }
                                        }
                                    });
                                } catch (IOException ignore) {
                                }

                                mediaPlayer.loadingDots = ((TaskViewHolderRecording) (holder)).loadingDots;
                                mediaPlayer.textView = ((TaskViewHolderRecording) (holder)).textView;
                            }

                            return;
                        } else {
                            try {
                                ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.VISIBLE);
                                ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.INVISIBLE);

                                if (position_recognizer != -1 && !taskList.get(position_recognizer).getType().equalsIgnoreCase("pause")) {
                                    taskList.get(position_recognizer).setType("pause");
                                    notifyItemChanged(position_recognizer);
                                }

                                mediaPlayer.mediaPlayer = new MediaPlayer();

                                mediaPlayer.mediaPlayer.setDataSource(taskList.get(position).getFileMediaPlayer());

                                mediaPlayer.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                    @Override
                                    public void onPrepared(MediaPlayer mp) {
                                        mp.start();
                                        mediaPlayer.position = position;
                                    }
                                });

                                mediaPlayer.mediaPlayer.prepareAsync();

                                mediaPlayer.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        taskList.get(position).next = true;

                                        ((TaskViewHolderRecording) (holder)).loadingDots.setVisibility(View.INVISIBLE);
                                        ((TaskViewHolderRecording) (holder)).textView.setVisibility(View.VISIBLE);

                                        if (position_recognizer != -1) {
                                            taskList.get(position_recognizer).setType("listen");
                                            notifyItemChanged(position_recognizer);
                                        }
                                    }
                                });
                            } catch (IOException ignore) {
                            }
                        }
                    }
                });
                break;
            case RECOGNIZER:
                if (position == taskList.size() - 1) {
                    taskList.get(position).status_recognizer = false;

                    if (mediaPlayer.mediaPlayer.isPlaying()) {
                        mediaPlayer.mediaPlayer.stop();
                        mediaPlayer.loadingDots.setVisibility(View.INVISIBLE);
                        mediaPlayer.textView.setVisibility(View.VISIBLE);
                    }

                    ((TaskViewHolderRecognizer) (holder)).recognitionProgressView.setSpeechRecognizer(mSpeechRecognizer);

                    mSpeechRecognizer.startListening(mSpeechRecogniserIntent);

                    ((TaskViewHolderRecognizer) (holder)).recognitionProgressView.play();
                    ((TaskViewHolderRecognizer) (holder)).recognitionProgressView.stop();
                    ((TaskViewHolderRecognizer) (holder)).recognitionProgressView.play();

                    position_recognizer = position;

                    ((TaskViewHolderRecognizer) (holder)).recognitionProgressView.setRecognitionListener(new RecognitionListenerAdapter() {
                        @Override
                        public void onResults(Bundle results) {
                            Log.d("Recognizer", "Good");
                            ArrayList<String> matches = results
                                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                            if (matches != null) {
                                if (matches.get(0).isEmpty())
                                    notifyItemChanged(position);
                                else {
                                    taskList.get(position).saying = matches.get(0);
                                    taskList.get(position).setType("listen_stop");
                                    taskList.get(position).next = true;
                                    position_recognizer = -1;
                                    notifyItemChanged(position);
                                }
                            } else {
                                notifyItemChanged(position);
                            }
                        }

                        @Override
                        public void onError(int error) {
                            super.onError(error);
                            Log.d("Recognizer", "Error");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (!mediaPlayer.mediaPlayer.isPlaying())
                                        notifyItemChanged(position);
                                }
                            }, 300);
                        }
                    });
                }
                break;
            case BUTTON:
                ((TaskViewHolderButton) (holder)).button.setText(taskList.get(position).getText());

                if (!taskList.get(position).next) {
                    ((TaskViewHolderButton) (holder)).shimmerText.startShimmerAnimation();
                    ((TaskViewHolderButton) (holder)).button.setEnabled(true);
                    final Animation animAplha = AnimationUtils.loadAnimation(context, R.anim.alpha);
                    ((TaskViewHolderButton) (holder)).button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            taskList.get(position).next = true;
                            ((TaskViewHolderButton) (holder)).button.startAnimation(animAplha);
                            ((TaskViewHolderButton) (holder)).shimmerText.stopShimmerAnimation();
                            ((TaskViewHolderButton) (holder)).button.setEnabled(false);
                            Log.d("Adapter", "press button");
                        }
                    });
                } else {
                    ((TaskViewHolderButton) (holder)).shimmerText.stopShimmerAnimation();
                    ((TaskViewHolderButton) (holder)).button.setEnabled(false);
                }
                break;
            case IMAGE:
                if (taskList.get(position).gif) {
                    Log.d("ImageTest", taskList.get(position).getFileImage());
                    Glide.with(context)
                            .load(taskList.get(position).getFileImage())
                            .asGif()
                            .error(android.R.drawable.stat_notify_error)
                            .crossFade().diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .into(((TaskViewHolderImage) (holder)).imageView);
                } else
                    Picasso.with(context).load(taskList.get(position).getFileImage()).into(((TaskViewHolderImage) (holder)).imageView);
                break;
            case TEXT_RIGHT:
                ((TaskViewHolderTextRight) (holder)).textView.setText("");

                if (taskList.get(position).getSay() == -10) {
                    ((TaskViewHolderTextRight) (holder)).textView.setText(taskList.get(position).saying);
                    taskList.get(position).next = true;
                } else {

                    String say_said = taskList.get(position).saying;
                    String say_said_good = say_said.replace("i'm", "i am");
                    String say_perfect = say_said_good.replace("'s", " is");

                    String[] say_string = say_perfect.split(" ");
                    String[] check_string = taskList.get(position).getText().split(" ");

                    int i1, i2, length = 0;

                    try {
                        for (i1 = 0, i2 = 0; i1 < say_string.length; ) {
                            if (i1 > 0 || i2 > 0)
                                ((TaskViewHolderTextRight) (holder)).textView.append(" ");

                            if (similarity(say_string[i1], check_string[i2]) > 0.3) {
                                ((TaskViewHolderTextRight) (holder)).textView.append(getColoredString(say_string[i1], Color.BLACK));
                                length++;
                                i1++;
                                i2++;
                            } else {
                                boolean check_miss = false;
                                for (int j = i1; j < check_string.length; j++) {
                                    if (similarity(say_string[i1], check_string[j]) > 0.3) {
                                        for (int buf = i1; buf < j; buf++) {
                                            ((TaskViewHolderTextRight) (holder)).textView.append(getColoredString(check_string[buf], Color.rgb(207, 164, 101)));
                                            length++;
                                        }
                                        i2 = j;
                                        check_miss = true;
                                    } else if (check_miss)
                                        break;
                                }

                                if (!check_miss) {
                                    System.out.print(say_string[i1] + "(R) ");
                                    ((TaskViewHolderTextRight) (holder)).textView.append(getColoredString(say_string[i1], Color.rgb(207, 164, 101)));
                                    length++;
                                    i1++;
                                    i2++;
                                }
                            }
                        }
                    } catch (Exception ignore) { }

                    if (length < check_string.length) {
                        ((TaskViewHolderTextRight) (holder)).textView.append(" ");
                        for (int j = length; j < check_string.length; j++) {
                            ((TaskViewHolderTextRight) (holder)).textView.append(getColoredString(check_string[j], Color.rgb(207, 164, 101)));
                            if (j != check_string.length - 1)
                                ((TaskViewHolderTextRight) (holder)).textView.append(" ");
                        }
                    }

                    if (length < say_string.length) {
                        ((TaskViewHolderTextRight) (holder)).textView.append(" ");
                        for (int j = length; j < say_string.length; j++) {
                            ((TaskViewHolderTextRight) (holder)).textView.append(getColoredString(say_string[j], Color.rgb(207, 164, 101)));
                            if (j != check_string.length - 1)
                                ((TaskViewHolderTextRight) (holder)).textView.append(" ");

                        }
                    }
                    taskList.get(position).next = true;
                    break;
                }
                break;
            case PAUSE_BUTTON:
                mSpeechRecognizer.stopListening();

                ((TaskViewHolderPauseButton) (holder)).button.change(true);

                ((TaskViewHolderPauseButton) (holder)).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((TaskViewHolderPauseButton) (holder)).button.change(true);

                        taskList.get(position).setType("listen");

                        mediaPlayer.loadingDots.setVisibility(View.INVISIBLE);
                        mediaPlayer.textView.setVisibility(View.VISIBLE);

                        try {
                            mediaPlayer.mediaPlayer.stop();
                        } catch (Exception ignore) {}

                        notifyItemChanged(position);
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (taskList.get(position).getType().equalsIgnoreCase("text")) {
            return TEXT;
        }
        else if (taskList.get(position).getType().equalsIgnoreCase("recording")) {
            return RECORDING;
        }
        else if (taskList.get(position).getType().equalsIgnoreCase("image")) {
            return IMAGE;
        }
        else if (taskList.get(position).getType().equalsIgnoreCase("button")) {
            return BUTTON;
        }
        else if (taskList.get(position).getType().equalsIgnoreCase("listen")) {
            return RECOGNIZER;
        }
        else if (taskList.get(position).getType().equalsIgnoreCase("listen_stop")) {
            return TEXT_RIGHT;
        }
        else if (taskList.get(position).getType().equalsIgnoreCase("pause")) {
            return PAUSE_BUTTON;
        }

        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public TaskEnter getItem(int position) {
        return taskList.get(position);
    }

    public void addItems(TaskEnter task) {
        taskList.add(task);
        notifyItemInserted(taskList.size() - 1);
    }

    public void clearItems() {
        taskList.clear();
        notifyDataSetChanged();
    }

    public class TaskViewHolderText extends RecyclerView.ViewHolder {
        protected TextView textView;

        public TaskViewHolderText(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textleft);

            Typeface tf = Typeface.createFromAsset(context.getAssets(),"font/Roboto/RobotoSlab-Light.ttf");

            textView.setTypeface(tf);
            textView.setText("Text");
        }
    } // Complete

    public class TaskViewHolderTextRight extends RecyclerView.ViewHolder {
        protected TextView textView;

        public TaskViewHolderTextRight(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.txtright);

            Typeface tf = Typeface.createFromAsset(context.getAssets(),"font/Roboto/RobotoSlab-Light.ttf");

            textView.setTypeface(tf);
            textView.setText("");
        }
    } // Complete

    public class TaskViewHolderImage extends  RecyclerView.ViewHolder{
        protected ImageView imageView;

        public TaskViewHolderImage(View itemView, int position) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }
    } // Complete

    public class TaskViewHolderRecording extends  RecyclerView.ViewHolder{
        protected LoadingDots loadingDots;
        protected TextView textView;
        protected RelativeLayout relativeLayout;

        public TaskViewHolderRecording(View itemView, final int position) {
            super(itemView);

            Log.d("HolderRecording", String.valueOf(position));

            loadingDots = itemView.findViewById(R.id.progressBar);
            textView = itemView.findViewById(R.id.repeat_list);
            relativeLayout = itemView.findViewById(R.id.recording_play_again);
        }
    } // Complete

    public class TaskViewHolderRecognizer extends  RecyclerView.ViewHolder{
        protected RecognitionProgressView recognitionProgressView;
        public TaskViewHolderRecognizer(View itemView) {
            super(itemView);

            int[] colors = {
                    ContextCompat.getColor(context, R.color.color_all),
                    ContextCompat.getColor(context, R.color.color_all),
                    ContextCompat.getColor(context, R.color.color_all),
                    ContextCompat.getColor(context, R.color.color_all),
                    ContextCompat.getColor(context, R.color.color_all),
            };

            int[] heights = { 22, 26, 20, 25, 18 };

            recognitionProgressView = itemView.findViewById(R.id.button_rep);

            recognitionProgressView.setColors(colors);
            recognitionProgressView.setBarMaxHeightsInDp(heights);
            recognitionProgressView.setCircleRadiusInDp(5);
            recognitionProgressView.setSpacingInDp(5);
            recognitionProgressView.setIdleStateAmplitudeInDp(5);
            recognitionProgressView.setRotationRadiusInDp(15);
        }
    }

    public class TaskViewHolderButton extends  RecyclerView.ViewHolder{
        protected Button button;
        protected  ShimmerLayout shimmerText;

        public TaskViewHolderButton(View itemView, final int position) {
            super(itemView);

            shimmerText = (ShimmerLayout) itemView.findViewById(R.id.shimmer_text);

            button = itemView.findViewById(R.id.button2);

            Typeface tf = Typeface.createFromAsset(context.getAssets(),"font/Roboto/RobotoSlab-Light.ttf");

            button.setTypeface(tf);

            shimmerText.startShimmerAnimation();
        }
    } // Complete

    public class TaskViewHolderPauseButton extends  RecyclerView.ViewHolder{
        protected PlayPauseView button;

        public TaskViewHolderPauseButton(View itemView) {
            super(itemView);

            button = itemView.findViewById(R.id.button_rep);
            button.toggle(true);
            button.change(false);
        }
    } // Complete
}
