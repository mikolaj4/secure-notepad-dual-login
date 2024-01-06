package com.example.bsm_dual_notatnik;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private static final String SHARED_FILE_NAME = "Notes";
    Button btnLogInPassword, btnLogInBiometry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstLaunchPasswordSetup();

        btnLogInPassword = findViewById(R.id.btnLogInPassword);
        btnLogInBiometry = findViewById(R.id.btnLogInBiometry);

        btnLogInPassword.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LogInPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void firstLaunchPasswordSetup(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_FILE_NAME, MODE_PRIVATE);
        if (!sharedPreferences.contains("email_hash")){
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }
}