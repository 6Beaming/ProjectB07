package com.group15.b07project;

public interface LoginContract {
    interface Model {
        interface loginFinishedListener {
            void succeed();
            void failed(String message);
        }
        void startLogin(String email, String password, loginFinishedListener listener);
    }
    interface View {
        void loginSucceed();
        void loginFailed(String message);
        void navigateToSignUp();
        void navigateToForgotPassword();
        void showEmailError(String message);
        void showPasswordError(String message);
        void navigateToMain();
        void navigateToPinSetup();
    }
    interface Presenter {
        void loginClicked(String email, String password);
        void SignUpClicked();
        void ForgotClicked();
        void onLoginSuccess();
    }
}