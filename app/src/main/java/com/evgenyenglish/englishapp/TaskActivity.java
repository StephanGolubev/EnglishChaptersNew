package com.evgenyenglish.englishapp;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import static android.os.Environment.DIRECTORY_MUSIC;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class TaskActivity extends AppCompatActivity {
    private int i_recording = 1;
    private boolean complete_lesson = false;
    private int i_image = 1;
    public static final String mypreference = "mypref";
    final int[] cash_mp3 = {1};
    final int[] cash_image = {1};
    final boolean[] next_itter = {true};
    final boolean[] status_repeat = {false};

    private String say_text_check = "";
    private boolean check_open = true;
    private List<Uri> uriList = new ArrayList<>();
    public static final String LESSON = "lessonKey";
    volatile int int_lesson_global;
    private StorageReference storageReference;
    volatile int i = 0; // define as a global variable
    volatile int old_i = -1;
    Timer timer; // define as a global variable
    TaskAdapter adapter2;


    SpeechRecognizer mSpeechRecogniser;
    Intent mSpeechRecogniserIntent;

    class Loader extends AsyncTask<Void, Void, Void> {
        private AlertDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                status_repeat[0] = false;
                while (cash_mp3[0] != -1) {
                    if (next_itter[0]) {
                        next_itter[0] = false;

                        Log.d("Check download", "Get " + String.valueOf(cash_mp3[0]) + "r.mp3");

                        storageReference.child(cash_mp3[0] + "r.mp3").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                try {
                                    DownloadManager.Request request=new DownloadManager.Request(uri)// Description of the Download Notification
                                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                                            .setDestinationInExternalPublicDir(DIRECTORY_MUSIC, int_lesson_global + "_" + cash_mp3[0] + "r.mp3");// Uri of the destination file
                                    DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                                    cash_mp3[0]++;

                                    long downloadID = downloadManager.enqueue(request);

                                    next_itter[0] = true;
                                } catch (IllegalStateException ex) {
                                    ex.printStackTrace();
                                } catch (Exception ex) {
                                    // just in case, it should never be called anyway
                                    ex.printStackTrace();
                                }

                                uriList.add(uri);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                cash_mp3[0] = -1;
                                next_itter[0] = true;
                            }
                        });
                    }
                }

                while (cash_image[0] != -1) {
                    if (next_itter[0]) {
                        next_itter[0] = false;

                        storageReference.child(cash_image[0] + "i.gif").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                DownloadManager.Request request=new DownloadManager.Request(uri)// Description of the Download Notification
                                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                                        .setDestinationInExternalPublicDir(DIRECTORY_MUSIC, int_lesson_global + "_" + cash_image[0] + "i.gif");// Uri of the destination file
                                DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                                cash_image[0]++;

                                long downloadID = downloadManager.enqueue(request);

                                next_itter[0] = true;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                storageReference.child(cash_image[0] + "i.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        DownloadManager.Request request=new DownloadManager.Request(uri)// Description of the Download Notification
                                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                                                .setDestinationInExternalPublicDir(DIRECTORY_MUSIC, int_lesson_global + "_" + cash_image[0] + "i.png");// Uri of the destination file

                                        cash_image[0]++;

                                        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                                        long downloadID = downloadManager.enqueue(request);

                                        next_itter[0] = true;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        cash_image[0] = -1;
                                        next_itter[0] = true;
                                        status_repeat[0] = true;
                                    }
                                });
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final SharedPreferences sharedpreferences = getSharedPreferences(mypreference,
                Context.MODE_PRIVATE);

        int_lesson_global = (sharedpreferences.getInt(LESSON, 0));

        LayoutInflater factory = LayoutInflater.from(getApplicationContext());
        final View view = factory.inflate(R.layout.spiner, null);

        mSpeechRecogniser = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecogniserIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        mSpeechRecogniserIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        mSpeechRecogniser.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {


            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null) {
                    say_text_check = matches.get(0);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        TextView name_lesson = (TextView) findViewById(R.id.name_lesson);

        name_lesson.setTypeface(Typeface.createFromAsset(getAssets(), "font/Roboto/RobotoSlab-Light.ttf"));

        final RecyclerView listV = findViewById(R.id.list);

        final ArrayList<Task> tasks_arr = new ArrayList<>();

        adapter2 = new TaskAdapter(mSpeechRecogniser, mSpeechRecogniserIntent);
        listV.setAdapter(adapter2);
        listV.setLayoutManager(new LinearLayoutManager(this));

        listV.setItemAnimator(new DefaultItemAnimator());

        timer = new Timer();

        final int number = Objects.requireNonNull(getIntent().getExtras()).getInt("LESSON_NUMBER");

        storageReference = firebaseStorage.getReferenceFromUrl("gs://englishapp-3c0ab.appspot.com/Lesson " + String.valueOf(number) + "/");

        name_lesson.setText("Урок " + number + ". Тренер по английскому");

        final String link = "http://kurchanovenglish.ru/data/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(link)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonTask jsonTask = retrofit.create(JsonTask.class);

        Call<List<Task>> call = jsonTask.getTask(number);

        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                final List<Task> tasks = response.body();

                for (final Task task : tasks) {
                    tasks_arr.add(task);
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {

            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(TaskActivity.this, R.style.CustomDialog).setView(view).setCancelable(false).create();
        String file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_1r.mp3";

        File exist_file = new File(file);

        Log.d("FileExist", exist_file.toString());

        if (!exist_file.exists())
        {
            TextView textView = view.findViewById(R.id.textView);
            textView.setText("Загрузка урока...");
            dialog.setView(view);
            dialog.show();

            Loader loader = new Loader();
            loader.execute();
        } else status_repeat[0] = true;

        int delay = 1000; // delay for 5 sec.
        int period = 1000; // repeat every 5 secs

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Log.d("Timer", "Tick");
                try {
                    try {
                        if (adapter2.getItem(i).next)
                            i++;
                    } catch (Exception ignore) {
                    }

                    if (i < tasks_arr.size() && i != old_i && status_repeat[0]) {
                        Log.d("Ifelse", "good");
                        if (dialog.isShowing())
                            dialog.dismiss();

                        TaskEnter task_enter = new TaskEnter("null", "nullT", 0);
                        String the_type = tasks_arr.get(i).getType();
                        String the_type_text = "text";
                        String the_type_recording = "recording";
                        String the_type_button = "button";
                        String the_type_image = "image";
                        String the_type_listen = "listen";
                        String type_end = "end_lesson";

                        try {
                            Log.d("TypeMessage", the_type + "\nNumber messame = " + String.valueOf(i) + "\nNext typeMessage = " + tasks_arr.get(i + 1).getType() +
                                    "\nNumber recording = " + i_recording);
                        } catch (Exception ignore) {

                        }

                        if (!the_type.equalsIgnoreCase("listen_stop")) {
                            task_enter.setText(tasks_arr.get(i).getText());

                            if (the_type.equalsIgnoreCase("recognizer")) {
                                task_enter.setType(the_type_listen);
                                task_enter.setSay(-10);
                            } else
                                task_enter.setType(the_type);
                            if (the_type.equalsIgnoreCase(the_type_recording)) {
                                task_enter.setFileMediaPlayer(getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + i_recording++ + "r.mp3");
                            } else if (the_type.equalsIgnoreCase(the_type_image)) {
                                String file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + i_image + "i.png";

                                File exist_file_png = new File(file);

                                if (exist_file_png.exists())
                                    task_enter.setFileImage(getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + i_image++ + "i.png");
                                else {
                                    task_enter.setFileImage(getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + i_image++ + "i.gif");
                                    task_enter.gif = true;
                                }
                            } else if (the_type.equalsIgnoreCase(type_end)) {
                                try {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            TaskActivity.this.finish();
                                        }
                                    });
                                } catch (Exception ignore) {
                                }
                            }

                            final TaskEnter task_add = task_enter;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter2.addItems(task_add);
                                    try {
                                        listV.smoothScrollToPosition(adapter2.getItemCount() - 1);
                                    } catch (Exception ignore) {
                                    }
                                }
                            });

                            old_i = i;

                            if (!(task_enter.getType().equalsIgnoreCase("button") || task_enter.getType().equalsIgnoreCase("recording") || task_enter.getType().equalsIgnoreCase("listen") || task_enter.getType().equalsIgnoreCase("recognizer")))
                                i++;
                        } else i++;
                    } else if (i >= tasks_arr.size()) {
                        Log.d("StatusLesson", "End");
                        SharedPreferences.Editor editor = sharedpreferences.edit();

                        if (complete_lesson) {
                            try {
                                for (int i1 = 1; i1 >= 0; i1++) {
                                    try {
                                        String file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + String.valueOf(i1) + "r.mp3";

                                        File file1 = new File(file);

                                        boolean deleted = file1.delete();

                                        if (!deleted)
                                            i1 = -2;

                                        Log.d("Delete", String.valueOf(deleted));
                                    } catch (Exception ignore) {
                                        Log.d("Delete", ignore.getMessage());

                                        i1 = -2;
                                    }
                                }

                                for (int i1 = 1; i1 >= 0; i1++) {
                                    boolean check = false;
                                    try {
                                        String file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + String.valueOf(i1) + "i.png";

                                        try {
                                            File file1 = new File(file);

                                            boolean deleted = file1.delete();

                                            if (!deleted)
                                                check = true;
                                        } catch (Exception ignore) {}

                                        file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + String.valueOf(i1) + "i.gif";

                                        try {
                                            File file1 = new File(file);

                                            boolean deleted = file1.delete();

                                            if (!deleted && check)
                                                i1 = -2;

                                        } catch (Exception ignore) {
                                            i1 = -2;
                                        }
                                    } catch (Exception ignore) {}
                                }
                            } catch (Exception ignore) {}
                        }

                        editor.putInt("lessonKey", number + 1);
                        editor.commit();

                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TaskActivity.this.finish();
                                }
                            });
                        } catch (Exception ignore) {
                        }

                        timer.cancel();
                    } else {
                        Log.d("Circle", "Die\n" + "i = " + String.valueOf(i) + "\nold_i = " + String.valueOf(old_i) + "\nstatus_repeat = " + String.valueOf(status_repeat[0]));
                    }
                } catch (Exception ignore1) {
                    complete_lesson = true;

                    if (complete_lesson) {
                        try {
                            for (int i1 = 1; i1 >= 0; i1++) {
                                try {
                                    String file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + String.valueOf(i1) + "r.mp3";

                                    File file1 = new File(file);

                                    boolean deleted = file1.delete();

                                    if (!deleted)
                                        i1 = -2;

                                    Log.d("Delete", String.valueOf(deleted));
                                } catch (Exception ignore) {
                                    Log.d("Delete", ignore.getMessage());

                                    i1 = -2;
                                }
                            }

                            for (int i1 = 1; i1 >= 0; i1++) {
                                boolean check = false;
                                try {
                                    String file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + String.valueOf(i1) + "i.png";

                                    try {
                                        File file1 = new File(file);

                                        boolean deleted = file1.delete();

                                        if (!deleted)
                                            check = true;
                                    } catch (Exception ignore) {}

                                    file = getExternalStoragePublicDirectory(DIRECTORY_MUSIC).toString() + "/" + int_lesson_global + "_" + String.valueOf(i1) + "i.gif";

                                    try {
                                        File file1 = new File(file);

                                        boolean deleted = file1.delete();

                                        if (!deleted && check)
                                            i1 = -2;

                                    } catch (Exception ignore) {
                                        i1 = -2;
                                    }
                                } catch (Exception ignore) {}
                            }
                        } catch (Exception ignore) {}
                    }

                    Log.d("StatusLesson", "End");
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt("lessonKey", number + 1);
                    editor.commit();

                    try {
                        TaskActivity.this.finish();
                    } catch (Exception e) {}

                    timer.cancel();
                }
            }
        }, delay, period);
    }

    @Override
    protected void onStop() {
        mSpeechRecogniser.stopListening();
        super.onStop();
//        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Handle the back button
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Ask the user if they want to quit
            new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    .setIcon(R.drawable.the_warning)
                    .setTitle("Вы действительно хотите выйти?")
                    .setMessage("После выхода урок будет потерян. Продолжить?")
                    .setPositiveButton("Да", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            TaskActivity.this.finish();
                        }
                    })
                    .setNegativeButton("Нет", null)
                    .show();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    protected void onDestroy() {
        adapter2.mediaPlayer.mediaPlayer.stop();

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        check_open = true;

        try {
            adapter2.mediaPlayer.mediaPlayer.start();
        } catch (Exception ignore) { }

        super.onResume();
    }

    @Override
    protected void onPause() {
        check_open = false;
        mSpeechRecogniser.stopListening();

        if (adapter2.mediaPlayer.mediaPlayer.isPlaying())
            adapter2.mediaPlayer.mediaPlayer.pause();

        super.onPause();
    }
}