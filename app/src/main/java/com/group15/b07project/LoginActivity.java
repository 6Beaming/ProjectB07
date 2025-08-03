package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private LoginContract.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        presenter = new LoginPresenter(this);
        EditText emailEdited = findViewById(R.id.editEmail);
        EditText passwordEdited = findViewById(R.id.editPassword);
        findViewById(R.id.loginButton).setOnClickListener(v -> {
            String email = emailEdited.getText().toString();
            String password = passwordEdited.getText().toString();
            presenter.loginClicked(email, password);
        });
        findViewById(R.id.createButton).setOnClickListener(v -> presenter.SignUpClicked());
        findViewById(R.id.forgotButton).setOnClickListener(v -> presenter.ForgotClicked());
    }

    @Override
    public void loginSucceed() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        PinManager pinManager = new PinManager(this);
        if (pinManager.hasPin(uid)) {
            startActivity(new Intent(this, MainActivity.class));
        } else{
            startActivity(new Intent(this, PinSetupActivity.class));
        }
        finish();
    }

    @Override
    public void loginFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToSignUp() {
        startActivity(new Intent(this, SignupActivity.class));
    }

    @Override
    public void navigateToForgotPassword() {
        startActivity(new Intent(this, ResetActivity.class));
    }


}