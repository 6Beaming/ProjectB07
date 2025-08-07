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

        presenter = new LoginPresenter(this, new LoginModel());
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
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void navigateAndFinish(Class<?> Activity) { // it's used to redirect to Main/PinSetup
        startActivity(new Intent(this, Activity));
        finish();
    }

    public void navigate(Class<?> Activity) { // used to redirect to SignUp/Reset
        startActivity(new Intent(this, Activity));
    }

}