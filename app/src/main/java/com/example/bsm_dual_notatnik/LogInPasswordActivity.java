package com.example.bsm_dual_notatnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Base64;

public class LogInPasswordActivity extends AppCompatActivity {
    private enum SaltOption{
        EMAIL_SALT,
        PASSWORD_SALT
    }
    private static final String SHARED_FILE_NAME = "Notes";
    ImageButton buttonBack;
    Button buttonLogin;
    EditText editTextEmail, editTextPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_password);

        buttonBack = findViewById(R.id.buttonBack);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonBack.setOnClickListener(view -> goBack());
        buttonLogin.setOnClickListener(view -> logInClicked());

    }

    private void goBack(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void logInClicked(){
        String inputEmail, inputPassword, inputEmailHash, inputPasswordHash, savedEmailHash, savedPasswordHash;

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        inputEmail = String.valueOf(editTextEmail.getText());
        inputPassword = String.valueOf(editTextPassword.getText());

        if (TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputPassword)){
            Toast.makeText(getApplicationContext(), "Enter both credentails!", Toast.LENGTH_SHORT).show();
            return;
        }

        inputEmailHash = Utility.hashCredentail(inputEmail, getSaltByteFromShared(SaltOption.EMAIL_SALT, inputEmail));
        inputPasswordHash = Utility.hashCredentail(inputPassword, getSaltByteFromShared(SaltOption.PASSWORD_SALT, inputPassword));

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        savedEmailHash = sharedPreferences.getString("email_hash", "error");
        savedPasswordHash = sharedPreferences.getString("password_hash", "error");


        assert inputEmailHash != null;
        assert inputPasswordHash != null;

        if (inputEmailHash.equals(savedEmailHash) && inputPasswordHash.equals(savedPasswordHash)){
            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getApplicationContext(), NotesActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong credentails!", Toast.LENGTH_SHORT).show();
            editTextEmail.setText("");
            editTextPassword.setText("");
        }

    }



    private byte[] getSaltByteFromShared(SaltOption saltOption, String salt){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        String saltFromFile;
        if (saltOption == SaltOption.EMAIL_SALT){
            saltFromFile = sharedPreferences.getString("email_salt", "error_es");
        } else{
            saltFromFile = sharedPreferences.getString("password_salt", "error_ps");
        }
        return Base64.getDecoder().decode(saltFromFile);
    }


}