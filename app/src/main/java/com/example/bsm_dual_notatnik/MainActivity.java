package com.example.bsm_dual_notatnik;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnLogInPassword, btnLogInBiometry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogInPassword = findViewById(R.id.btnLogInPassword);
        btnLogInBiometry = findViewById(R.id.btnLogInBiometry);

        btnLogInPassword.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), LogInPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }
}