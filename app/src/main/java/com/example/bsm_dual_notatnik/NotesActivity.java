package com.example.bsm_dual_notatnik;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class NotesActivity extends AppCompatActivity {
    private List<Note> noteList;
    private LinearLayout notesContainer;
    private static final String SHARED_FILE_NAME = "Notes";
    private static final String KEY_NAME = "MyKey1";
    Button buttonLogout, buttonAddNote, buttonChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        noteList = new ArrayList<>();
        notesContainer = findViewById(R.id.notesContainer);

        try {
            loadNotesFromPreferencesToList();
        } catch (Exception e){
            Log.e("Error from NotepadActivity", "Caused by loadNotesFromPreferencesToList() invoked from onCreate()" + e.getMessage(), e);
        }
        displayNotes();


        buttonLogout = findViewById(R.id.buttonLogOut);
        buttonAddNote = findViewById(R.id.buttonAddNote);

        buttonLogout.setOnClickListener(view -> logOut());
        buttonAddNote.setOnClickListener(view -> showAddNoteDialog());
    }

    private void logOut(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showAddNoteDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.create_note_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(dialogView);
        builder.setTitle("Create new note");

        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            EditText noteTitleEditText = dialogView.findViewById(R.id.noteTitleEditText);
            EditText noteContentEditText = dialogView.findViewById(R.id.noteContentEditText);

            String title = noteTitleEditText.getText().toString();
            String content = noteContentEditText.getText().toString();

            if(!title.isEmpty() && !content.isEmpty()){
                Note note = new Note();
                note.setTitle(title);
                note.setContent(content);

                noteList.add(note);

                try {
                    saveNotesToPreferences("add");
                } catch (GeneralSecurityException | IOException e) {
                    Log.e("Error from NotepadActivity", "Caused by saveNotesToPreferences() invoked from showAddNoteDialog()" + e.getMessage(), e);
                }

                createNoteView(note);
                Toast.makeText(getApplicationContext(), "Note saved!", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEditNoteDialog(Note note){
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.create_note_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Edit note");

        EditText noteTitleEditText = dialogView.findViewById(R.id.noteTitleEditText);
        EditText noteContentEditText = dialogView.findViewById(R.id.noteContentEditText);
        noteTitleEditText.setText(note.getTitle());
        noteContentEditText.setText(note.getContent());

        builder.setPositiveButton("Save", (dialogInterface, i) -> {
            String title = noteTitleEditText.getText().toString();
            String content = noteContentEditText.getText().toString();

            if (!title.isEmpty() && !content.isEmpty()){

                deleteNoteAndRefresh(note);

                note.setTitle(title);
                note.setContent(content);

                noteList.add(note);
                createNoteView(note);

                try {
                    saveNotesToPreferences("add");
                } catch (GeneralSecurityException | IOException e) {
                    Log.e("Error from NotepadActivity", "Caused by saveNotesToPreferences() invoked from showEditNoteDialog()" + e.getMessage(), e);
                }

            }else {
                Toast.makeText(getApplicationContext(), "Enter title and content!", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveNotesToPreferences(String mode) throws GeneralSecurityException, IOException {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (mode.equals("del")){
            int noteCount = sharedPreferences.getInt("_notecount_", 0);
            for(int i=0; i<noteCount; i++){
                editor.remove(i + "_title_");
                editor.remove(i + "_content_");
            }
        }

        IvParameterSpec iv = Utility.generateIv();
        String ivString = ivToString(iv);
        saveIvStringToShared(ivString);

        SecretKey secretKey = Utility.generateRandomKey(KEY_NAME);

        editor.putInt("_notecount_", noteList.size());
        for(int i=0; i<noteList.size(); i++){
            Note note = noteList.get(i);

            String encryptedTitle = Utility.encrypt("AES/CBC/PKCS7Padding", note.getTitle(), secretKey, iv);
            String encryptedContent = Utility.encrypt("AES/CBC/PKCS7Padding", note.getContent(), secretKey, iv);

            editor.putString(i + "_title_", encryptedTitle);
            editor.putString(i + "_content_", encryptedContent);
        }

        editor.apply();


    }

    private void loadNotesFromPreferencesToList() throws Exception {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        int noteCount = sharedPreferences.getInt("_notecount_", 0);

        if (noteCount!=0){
            String ivString = getIVStringFromShared();
            IvParameterSpec iv = stringToIv(ivString);

            SecretKey secretKey = Utility.retrieveKey(KEY_NAME);

            for(int i=0; i<noteCount; i++){
                String title = sharedPreferences.getString(i + "_title_", "");
                String content = sharedPreferences.getString(i + "_content_", "");

                String decryptedTitle = Utility.decrypt("AES/CBC/PKCS7Padding", title, secretKey, iv);
                String decryptedContent = Utility.decrypt("AES/CBC/PKCS7Padding", content, secretKey, iv);

                Note note = new Note();
                note.setTitle(decryptedTitle);
                note.setContent(decryptedContent);

                noteList.add(note);
            }
        }
    }

    private void saveIvStringToShared(String ivString){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("_iv_", ivString);
        editor.apply();
    }

    private String getIVStringFromShared(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        return sharedPreferences.getString("_iv_", "err");
    }

    private static IvParameterSpec stringToIv(String ivString) {
        byte[] ivBytes = Base64.getDecoder().decode(ivString);
        return new IvParameterSpec(ivBytes);
    }

    private static String ivToString(IvParameterSpec ivParameterSpec) {
        byte[] ivBytes = ivParameterSpec.getIV();
        return Base64.getEncoder().encodeToString(ivBytes);
    }

    private void createNoteView(final Note note){
        @SuppressLint("InflateParams") View noteView = getLayoutInflater().inflate(R.layout.note_item, null);
        TextView noteTitleTextView = noteView.findViewById(R.id.noteTitleTextView);
        TextView noteContentTextView = noteView.findViewById(R.id.noteContentTextView);
        Button deleteNoteButton = noteView.findViewById(R.id.btnDeleteNote);

        noteTitleTextView.setText(note.getTitle());
        noteContentTextView.setText(note.getContent());

        deleteNoteButton.setOnClickListener(view -> showDeleteDialog(note));

        noteView.setOnLongClickListener(view -> {
            showEditNoteDialog(note);
            return true;
        });

        notesContainer.addView(noteView);
    }

    private void refreshNotesView(){
        notesContainer.removeAllViews();
        displayNotes();
    }

    private void displayNotes(){
        for(Note note : noteList){
            createNoteView(note);
        }
    }

    private void deleteNoteAndRefresh(Note note){
        noteList.remove(note);
        try {
            saveNotesToPreferences("del");
        } catch (GeneralSecurityException | IOException e) {
            Log.e("Error from NotepadActivity", "Caused by saveNotesToPreferences() invoked from deleteNoteAndRefresh()" + e.getMessage(), e);

        }
        refreshNotesView();
    }

    private void showDeleteDialog(final Note note){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this note");
        builder.setMessage("Are you sure you want to delete it?");
        builder.setPositiveButton("Delete", (dialogInterface, i) -> deleteNoteAndRefresh(note));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }




}