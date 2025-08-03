package com.group15.b07project;

public class LoginPresenter implements LoginContract.Presenter {

    private final LoginContract.View view;
    private final LoginContract.Model model;

    public LoginPresenter(LoginContract.View view) {
        this.view = view;
        this.model = new LoginModel();
    }

    @Override
    public void loginClicked(String email, String password) {
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
    public void SignUpClicked() {
        view.navigateToSignUp();
    }

    @Override
    public void ForgotClicked() {
        view.navigateToForgotPassword();
    }
}