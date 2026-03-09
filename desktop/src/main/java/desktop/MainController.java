package desktop;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

public class MainController {

    /* Onboarding Variables */
    @FXML
    private VBox LoginForm;
    @FXML
    private VBox SignupForm;
    @FXML
    private Label SignupText;
    @FXML
    private VBox ForgotPasswordForm;

    /* Chat Variables */
    @FXML
    private TextField ChatSearch;
    @FXML
    private Label ForgotPasswordText;
    @FXML
    private VBox chatList;

    /* Onboarding functions */
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

    /* Chat Functions */
    @FXML
    public void initialize() {
        if (chatList != null) {
            loadDummyChatList();
            setupSearchFilter();
        }
    }

    private void loadDummyChatList() {
        String[] dummyNames = {"Jasmine Joseph", "Adrian Buttowski", "Alex Burrow", "Meeting at 5!", "Besties!", "Adam <3"};
        String[] dummyMessages = {
                "Hey girl, long time no see!",
                "Sis, could I borrow ur car?",
                "Can I get the files by today?",
                "Ajax: Hey we will be start the meeting in 5 minutes..",
                "2 peoples online",
                "Sorry for your loss </3"
        };
        String[] dummyTimes = {"10:42 AM", "9:15 AM", "Yesterday", "Yesterday", "Tuesday", "Monday"};

        for (int i = 0; i < dummyNames.length; i++) {

            FontIcon profilePicture = new FontIcon("mdi2a-account");
            profilePicture.setIconSize(24);
            profilePicture.setIconColor(Color.WHITE);

            StackPane profileBg = new StackPane(profilePicture);
            profileBg.getStyleClass().add("chatProfile");

            Label nameLabel = new Label(dummyNames[i]);
            nameLabel.getStyleClass().add("nameLabel");

            Label timeLabel = new Label(dummyTimes[i]);
            timeLabel.getStyleClass().add("timeLabel");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topRow = new HBox(nameLabel, spacer, timeLabel);

            Label messageLabel = new Label(dummyMessages[i]);
            messageLabel.getStyleClass().add("messageLabel");

            VBox textContainer = new VBox(topRow, messageLabel);
            textContainer.setSpacing(5);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            HBox chatTab = new HBox(profileBg, textContainer);
            chatTab.setSpacing(15);

            chatTab.getStyleClass().add("chat-tab");
            chatTab.setMaxWidth(Double.MAX_VALUE);
            VBox.setMargin(chatTab, new javafx.geometry.Insets(5, 10, 5, 10));

            chatList.getChildren().add(chatTab);
        }
    }

    private void setupSearchFilter() {
        ChatSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            // Convert to lowercase
            String searchQuery = newValue.toLowerCase();

            for (Node node : chatList.getChildren()) {

                Label nameLabel = (Label) node.lookup(".nameLabel");
                Label messageLabel = (Label) node.lookup(".messageLabel");
                boolean matches = false;

                if (nameLabel != null && nameLabel.getText().toLowerCase().contains(searchQuery)) {
                    matches = true;
                } else if (messageLabel != null && messageLabel.getText().toLowerCase().contains(searchQuery)) {
                    matches = true;
                }

                node.setVisible(matches);
                node.setManaged(matches);
            }
        });
    }
}
