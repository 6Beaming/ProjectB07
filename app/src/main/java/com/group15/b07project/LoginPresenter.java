package com.group15.b07project;

import android.content.Context;

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
            view.showErrorMessage("Please enter email");
            return;
        }
        if (password.isEmpty()) {
            view.showErrorMessage("Please enter password");
            return;
        }
        model.startLogin(email, password, new LoginContract.Model.loginFinishedListener() {
            @Override
            public void succeed() {
                // Presenter decides where to go after successfully log in
                // Now that log in is on success, uid can't be null
                String uid = model.getUser().getUid();
                PinManager pinManager = new PinManager((Context) view);
                if (pinManager.hasPin(uid)) {
                    view.navigateAndFinish(MainActivity.class);
                } else {
                    view.navigateAndFinish(PinSetupActivity.class);
                }
            }

            @Override
            public void failed(String message) {
                view.showErrorMessage(message);
            }
        });
    }

    @Override
    public void SignUpClicked() {
        view.navigate(SignupActivity.class);
    }

    @Override
    public void ForgotClicked() {
        view.navigate(ResetActivity.class);
    }
}