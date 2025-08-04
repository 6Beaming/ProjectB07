package com.group15.b07project;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;
    private final LoginContract.Model model;

    public LoginPresenter(LoginContract.View view) {
        this.view = view;
        this.model = new LoginModel();
    }

    @Override
    public void loginClicked(String email, String password) {
        if (email.isEmpty()) {
            view.showEmailError("Please enter email");
            return;
        }
        if (password.isEmpty()) {
            view.showPasswordError("Please enter password");
            return;
        }
        model.startLogin(email, password, new LoginContract.Model.loginFinishedListener() {
            @Override
            public void succeed() {
                view.loginSucceed();
            }

            @Override
            public void failed(String message) {
                view.loginFailed(message);
            }
        });
    }

    @Override
    public void onLoginSuccess() {
        // Presenter decides where to go after successfully log in
        // Now that log in is on success, uid can't be null
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        PinManager pinManager = new PinManager((Context) view);
        if (pinManager.hasPin(uid)) {
            view.navigateToMain();
        } else {
            view.navigateToPinSetup();
        }
    }

    @Override
    public void SignUpClicked() {
        view.navigateToSignUp();
    }

    @Override
    public void ForgotClicked() {
        view.navigateToForgotPassword();
    }
}