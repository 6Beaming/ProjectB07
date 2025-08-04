package com.group15.b07project;

import com.google.firebase.auth.FirebaseUser;

public interface LoginContract {
    interface Model {
        interface loginFinishedListener {
            void succeed();
            void failed(String message);
        }
        void startLogin(String email, String password, loginFinishedListener listener);
        FirebaseUser getUser();
    }
    interface View {
        void showErrorMessage(String message);
        void navigate(Class<?> Activity);
        void navigateAndFinish(Class<?> Activity);
    }
    interface Presenter {
        void loginClicked(String email, String password);
        void SignUpClicked();
        void ForgotClicked();
    }
}