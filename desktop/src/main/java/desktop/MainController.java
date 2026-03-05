package desktop;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.io.IOException;

public class MainController {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private VBox LoginForm;
    @FXML
    private VBox SignupForm;
    @FXML
    private Label SignupText;
    @FXML
    private VBox ForgotPasswordForm;

    @FXML
    public void handleOnboardingClick() {
        System.out.println("Button was clicked");
        try {
            UserInterface.getInstance().showMainUI(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void ShowSignup() {
        System.out.println("Showing Signup Form");
        LoginForm.setVisible(false);
        LoginForm.setManaged(false);
        SignupForm.setVisible(true);
        SignupForm.setManaged(true);
        SignupText.setVisible(true);
        SignupText.setManaged(true);
        ForgotPasswordForm.setVisible(false);
        ForgotPasswordForm.setManaged(false);
    }

    @FXML
    public void ShowLogin() {
        System.out.println("Showing Login Form");
        SignupForm.setVisible(false);
        SignupForm.setManaged(false);
        LoginForm.setVisible(true);
        LoginForm.setManaged(true);
        SignupText.setVisible(false);
        SignupText.setManaged(false);
        ForgotPasswordForm.setVisible(false);
        ForgotPasswordForm.setManaged(false);
    }

    @FXML
    public void ShowForgotPassword() {
        System.out.println("Showing Forgot Password Form");
        LoginForm.setVisible(false);
        LoginForm.setManaged(false);
        ForgotPasswordForm.setVisible(true);
        ForgotPasswordForm.setManaged(true);
    }
}
