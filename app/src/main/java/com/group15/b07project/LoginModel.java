package com.group15.b07project;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginModel implements LoginContract.Model {

    private final FirebaseUser user;

    public LoginModel(){
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public FirebaseUser getUser() {
        return user;
    }

    @Override
    public void startLogin(String email, String password, loginFinishedListener listener) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (user != null && user.isEmailVerified()) {
                            listener.succeed();
                        } else {
                            listener.failed("Please verify your email.");
                        }
                    } else {
                        Exception except = task.getException();
                        String message;
                        if (except instanceof FirebaseAuthInvalidUserException) {
                            message = "Account does not exist. Please sign up or try different account";
                        } else if (except instanceof FirebaseAuthInvalidCredentialsException) {
                            message = "Incorrect password";
                        } else {
                            message = "Login failed" ;
                        }
                        listener.failed(message);
                    }
                });
    }
}
