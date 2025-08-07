package com.group15.b07project;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class PresenterUnitTest {
    @Mock
    private LoginContract.View view;

    @Mock
    private LoginContract.Model model;
    @Mock
    private PinManager pinManager;

    @Mock
    private FirebaseUser user;

    private LoginPresenter presenter;

    //Setup
    @Before
    public void setUp() {
        presenter = new LoginPresenter(view, model, pinManager);
    }

    @Test
    public void testEmptyEmail() {
        presenter.loginClicked("", "PASSWORD");
        verify(view).showErrorMessage("Please enter email");
    }
    @Test
    public void testEmptyPassword() {
        presenter.loginClicked("testing@qq.com", "");
        verify(view).showErrorMessage("Please enter password");
    }

    @Test
    public void testLoginWithPin() {
        String email = "testing@qq.com";
        String password = "PASSWORD";
        String uid = "12345678";

        //Mock uid
        when(model.getUser()).thenReturn(user);
        when(user.getUid()).thenReturn(uid);
        //Mock user having PIN
        when(pinManager.hasPin(uid)).thenReturn(true);
        presenter = new LoginPresenter(view, model, pinManager);
        presenter.loginClicked(email, password);
        //Capture the loginfinishedlistener that was passed to startlogin
        ArgumentCaptor<LoginContract.Model.loginFinishedListener> captor = ArgumentCaptor.forClass(LoginContract.Model.loginFinishedListener.class);
        //Check if startlogin is called, and capture the loginfinishedlistener
        verify(model).startLogin(eq(email), eq(password), captor.capture());
        captor.getValue().succeed();
        verify(view).navigateAndFinish(MainActivity.class);
    }

    @Test
    public void testLoginWithoutPin() {
        String email = "testing@qq.com";
        String password = "PASSWORD";
        String uid = "12345678";

        //mock uid
        when(model.getUser()).thenReturn(user);
        when(user.getUid()).thenReturn(uid);
        //Mock user not having PIN
        when(pinManager.hasPin(uid)).thenReturn(false);
        presenter = new LoginPresenter(view, model, pinManager);
        presenter.loginClicked(email, password);
        //Capture the loginfinishedlistener that was passed to startlogin
        ArgumentCaptor<LoginContract.Model.loginFinishedListener> captor = ArgumentCaptor.forClass(LoginContract.Model.loginFinishedListener.class);
        //Check if startlogin is called, and capture the loginfinishedlistener
        verify(model).startLogin(eq(email), eq(password), captor.capture());
        captor.getValue().succeed();
        verify(view).navigateAndFinish(PinSetupActivity.class);
    }

    @Test
    public void testLoginFailed() {
        String email = "testing@qq.com";
        String password = "IDONTKNOW";
        String errorMessage = "Login failed";

        doAnswer(invocation -> {
            //Get the loginfinishedlistener
            LoginContract.Model.loginFinishedListener listener = invocation.getArgument(2);
            listener.failed(errorMessage);
            return null;
        }).when(model).startLogin(eq(email), eq(password), any());
        presenter.loginClicked(email, password);
        verify(view).showErrorMessage(errorMessage);
    }

    @Test
    public void testSignUpClicked() {
        presenter.SignUpClicked();
        verify(view).navigate(SignupActivity.class);
    }

    @Test
    public void testForgotClicked() {
        presenter.ForgotClicked();
        verify(view).navigate(ResetActivity.class);
    }

}
