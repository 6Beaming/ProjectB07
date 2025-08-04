package com.group15.b07project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
            String email = emailEdited.getText().toString().trim();
            String password = passwordEdited.getText().toString();
            presenter.loginClicked(email, password);
        });
        findViewById(R.id.createButton).setOnClickListener(v -> presenter.SignUpClicked());
        findViewById(R.id.forgotButton).setOnClickListener(v -> presenter.ForgotClicked());
    }

    @Override
    public void showEmailError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPasswordError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loginSucceed() {
        presenter.onLoginSuccess(); // then go to the pages based on PIN status
    }

    @Override
    public void loginFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish(); // go to main, then can't go back to log in
    }

    @Override
    public void navigateToPinSetup() {
        startActivity(new Intent(this, PinSetupActivity.class));
        finish(); // go to PIN setup, then can't go back to log in
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