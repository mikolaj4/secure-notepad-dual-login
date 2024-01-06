package com.example.bsm_dual_notatnik;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private static final String SHARED_FILE_NAME = "Notes";
    Button buttonRegister;
    EditText editTextEmail, editTextPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(view -> registerClicked());

    }


    private void registerClicked() {
        String email, password;

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        email = String.valueOf(editTextEmail.getText());
        password = String.valueOf(editTextPassword.getText());

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(), "Fill both fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validatePassword(password)){
            Toast.makeText(getApplicationContext(), "Password to short!", Toast.LENGTH_SHORT).show();
            return;
        }
        //TU PO TESTACH DODAĆ !validateEmail(email)
        if (false) {
            Toast.makeText(getApplicationContext(), "Wrong email format!", Toast.LENGTH_SHORT).show();
            return;
        }
        saveNewUser(email, password);
    }

    private boolean validatePassword(String password){
        return password.length() >= 5;
    }

    private boolean validateEmail(String email){
        final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private void saveNewUser(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.contains("email_hash")){
            Toast.makeText(getApplicationContext(), "Login credentials are already set up!", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] emailSalt = Utility.generateRandomSalt();
        String emailSaltString = Base64.getEncoder().encodeToString(emailSalt);
        String emailHash = Utility.hashCredentail(email, emailSalt);

        byte[] passwordSalt = Utility.generateRandomSalt();
        String passwordSaltString = Base64.getEncoder().encodeToString(passwordSalt);
        String passwordHash = Utility.hashCredentail(password, passwordSalt);

        editor.putString("email_salt", emailSaltString);
        editor.putString("password_salt", passwordSaltString);
        editor.putString("email_hash", emailHash);
        editor.putString("password_hash", passwordHash);
        editor.apply();

        buttonRegister.setEnabled(false);
        editTextEmail.setText("");
        editTextPassword.setText("");
        Toast.makeText(getApplicationContext(), "Account created!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }
}