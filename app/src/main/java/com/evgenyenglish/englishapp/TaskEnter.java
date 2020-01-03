package com.evgenyenglish.englishapp;

import android.net.Uri;
import android.speech.SpeechRecognizer;

public class TaskEnter {

    private SpeechRecognizer speechRecognizer;
    private String fileMediaPlayer;
    private String fileImage;
    private String text;
    private String type;
    private int say;

    public String saying = "";
    public boolean gif = false;
    public boolean next = false;
    public boolean status_recognizer = false;

    public TaskEnter(String text_in, String type_in, int say_in) {
        text = text_in;
        type = type_in;
        say = say_in;
    }

    public void setFileImage(String fileImage) {
        this.fileImage = fileImage;
    }

    public String getFileImage() {
        return fileImage;
    }

    public void setFileMediaPlayer(String _file) {
        this.fileMediaPlayer = _file;
    }

    public String getFileMediaPlayer() {
        return fileMediaPlayer;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSay(int say) {
        this.say = say;
    }

    public int getSay() {
        return say;
    }
}
