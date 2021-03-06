package it.polimi.ingsw.PSP13.view.GUI;

import it.polimi.ingsw.PSP13.network.Client;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Lobby implements Initializable{

    private GuiInput guiInput;
    private boolean nicknameSent = false;

    @FXML
    private AnchorPane container;
    @FXML
    private AnchorPane parent;
    @FXML
    private AnchorPane slide;
    @FXML
    private AnchorPane slide1;
    @FXML
    private TextField portText;
    @FXML
    private TextField serverText;
    @FXML
    private TextField nicknameText;
    @FXML
    private Button ok;
    @FXML
    private Button okFirst;
    @FXML
    private Button okSlide;
    @FXML
    private Label errorLabel;
    @FXML
    private Label nicknameError;
    @FXML
    private Label waitLabel;
    @FXML
    private Spinner<Integer> spinner;



    /**
     * The lobby is full and an error is printed
     */
    public void FullLobbyWaitMsg() {
        nicknameError.setText("Players limit has been reached for this match,\nyou can wait in queue or disconnect.\nyour priority is hold.");
        errorLabel.setText("Players limit has been reached for this match,\nyou can wait in queue or disconnect.\nyour priority is hold.");
        waitLabel.setText("Players limit has been reached for this match,\nyou can wait in queue or disconnect.\nyour priority is hold.");

        nicknameError.setVisible(true);
        errorLabel.setVisible(true);
        waitLabel.setVisible(true);

    }

    /**
     * The nickname chosen isn't suitable and error is printed
     */
    public void nicknameError(){
        nicknameError.setText("The nickname you have chosen\nis not available for this match,\nplease insert another nickname");
        nicknameError.setVisible(true);
        nicknameSent = false;
    }


    /**
     * Initializes the spinner element
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        SpinnerValueFactory<Integer> numbers = new SpinnerValueFactory.IntegerSpinnerValueFactory(2,3,3);
        spinner.setValueFactory(numbers);

    }

    /**
     * Utility function that swap back the scene to the nickname scene
     */
    public void goBacktoNicknameSceneChange() {

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Insert your nickname");
        popup.setMinWidth(300);

        Label label = new Label("The nickname you have chosen\nis not available for this match,\nplease insert another nickname");
        TextField field = new TextField();
        Button send = new Button("OK");
        send.setOnAction(e -> {
            if(!field.getText().equals("")){
                guiInput.getController().notifyNickname(field.getText());
                popup.close();
            }
        });

        VBox vBox = new VBox();
        vBox.getChildren().addAll(label,field,send);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        popup.setScene(scene);
        popup.showAndWait();

    }

    /**
     * Sets the scene to the god selection scene
     * @param godsList list of the gods
     * @param godsNumber number of selectable gods
     * @param isChallenger true if the player is the challenger, false otherwise
     * @throws IOException
     */
    public void sceneChangeGodSelection(List<String> godsList, int godsNumber, boolean isChallenger) throws IOException {

        if (guiInput.getGodDispatcher() != null) return;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Lobby.class.getResource("/godSelection.fxml"));
        AnchorPane pane = loader.<AnchorPane>load();

        GodDispatcher godDispatcher = loader.<GodDispatcher>getController();
        GodSelection godSelection = new GodSelection(godDispatcher);
        godDispatcher.setGodHandler(godSelection);
        godSelection.setGodsList(godsList);
        godSelection.setGodsNumber(godsNumber);
        godSelection.setIsChallenger(isChallenger);
        godSelection.clear();
        godDispatcher.upload();
        godDispatcher.setGuiInput(guiInput);
        guiInput.setGodDispatcher(godDispatcher);

        Scene selectionScene = new Scene(pane);
        selectionScene.getStylesheets().add("god_selection.css");

        Stage stage = (Stage) (parent.getScene().getWindow());
        stage.setScene(selectionScene);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    /**
     * Sends the nickname to the server and eventually makes the user decide the player number
     * @throws Exception
     */
    @FXML
    public void gameStart() throws Exception
    {
        okSlide.setDisable(true);
        if(nicknameSent)
            return;
        nicknameError.setVisible(false);
        String nickname = nicknameText.getText();
        guiInput.getController().notifyNickname(nickname);
        nicknameSent=true;


        if(guiInput.isFirst())
            choosePlayerNum();
        else
        {
            nicknameError.setText("Please wait\nuntil a match is found...");
            nicknameError.setVisible(true);
        }

    }

    /**
     * Disables a button and style it properly
     * @param button the button to be disabled
     */
    private void clearButton(Button button) {
        button.getStyleClass().clear();
        button.getStyleClass().add("textEmpty");
        button.setDisable(true);
    }

    /**
     * Enables a button and styles it properly
     * @param button the button to be enabled
     */
    private void highlightButton(Button button) {
        button.getStyleClass().remove("textEmpty");
        button.getStyleClass().add("textFilled");
        button.setDisable(false);
    }

    /**
     * This method checks if the textfield is empty and
     * style a button in accordance with its status
     * @param textField to check the emptiness
     * @param button the button to style
     */
    private void textCheck(TextField textField, Button button) {
        if(textField.getText().equals(""))
            clearButton(button);
        else
            highlightButton(button);
    }

    /**
     * Checks if the nickname textbox is empty
     * @param event event related to the user writing on the textbox
     */
    @FXML
    public void textCheckNickname(KeyEvent event)
    {
        textCheck(nicknameText, okSlide);
    }

    /**
     * Checks if the nickname textbox is empty
     * @param event event related to the user writing on the textbox
     */
    @FXML
    public void textCheckServer(KeyEvent event)
    {
        if(serverText.getText().equals("") || portText.getText().equals(""))
            clearButton(ok);
        else
            highlightButton(ok);
    }

    /**
     * Utility function that sends the user choice of players number to the server
     */
    @FXML
    public void sendPlayerNum()
    {   okFirst.setDisable(true);
        guiInput.getController().notifyPlayersNumber((Integer)spinner.getValue());
        waitLabel.setText("Please wait\nuntil a match is found...");
        waitLabel.setVisible(true);
    }

    /**
     * Scene change to choose player number section
     * @throws Exception
     */
    public void choosePlayerNum() throws Exception{
        Parent root = FXMLLoader.load(Lobby.class.getResource("/lobby.fxml"));
        Scene scene = parent.getScene();

        root.translateXProperty().set(scene.getWidth());
        parent.getChildren().add(root);

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(slide.translateXProperty(),230, Interpolator.EASE_IN);

        KeyFrame kf = new KeyFrame(Duration.seconds(1), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(event1 -> {
            slide1.setVisible(true);
        });
        timeline.play();

    }

    /**
     * Handles rematch
     * @throws IOException
     */
    public void rematch() throws IOException {
        Parent root = FXMLLoader.load(Lobby.class.getResource("/lobby.fxml"));
        Scene scene = parent.getScene();

        root.translateXProperty().set(scene.getWidth());
        parent.getChildren().add(root);

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(container.translateXProperty(),230, Interpolator.EASE_IN);

        KeyFrame kf = new KeyFrame(Duration.seconds(1), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(event1 -> {
            slide.setVisible(true);
        });
        timeline.play();

        okSlide.setDisable(true);
        nicknameText.setText("***");
        nicknameError.setText("Please wait\nuntil a match is found...");
        nicknameError.setVisible(true);
        nicknameSent = true;
    }

    /**
     * Connects the client to the server
     * @param event
     * @throws IOException
     */
    @FXML
    public void connect(ActionEvent event) throws IOException {
        ok.setDisable(true);
        errorLabel.setVisible(false);

        Pattern p = Pattern.compile("((\\d{1,3}[.]){3}\\d{1,3})");
        String text = serverText.getText();
        String port = portText.getText();
        try {
            Client.PORT = Integer.parseInt(port);
        }
        catch(NumberFormatException e) {
            errorLabel.setText("Invalid format,\ninsert a port number!");
            errorLabel.setVisible(true);
            return;
        }
        if(!p.matcher(text).matches())
        {
            errorLabel.setText("Invalid format,\ninsert an IP address!");
            errorLabel.setVisible(true);
            return;
        }

        guiInput = new GuiInput();
        guiInput.setLoginController(this);
        try {
            guiInput.connectToServer(serverText.getText());
        }catch (IOException e){
            errorLabel.setText("Cannot establish a connection,\nyou may be offline!\n(Or the server could... \uD83E\uDD14 )");
            errorLabel.setVisible(true);
            return;
        }


        Parent root = FXMLLoader.load(Lobby.class.getResource("/lobby.fxml"));
        Scene scene = ((Node)event.getSource()).getScene();

        root.translateXProperty().set(scene.getWidth());
        parent.getChildren().add(root);

        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(container.translateXProperty(),230, Interpolator.EASE_IN);

        KeyFrame kf = new KeyFrame(Duration.seconds(1), kv);
        timeline.getKeyFrames().add(kf);
        timeline.setOnFinished(event1 -> {
            slide.setVisible(true);
        });
        timeline.play();


    }

    public boolean isNicknameSent() {
        return nicknameSent;
    }

    public void setGuiInput(GuiInput guiInput) {
        this.guiInput = guiInput;
    }

    public GuiInput getGuiInput() {
        return guiInput;
    }

}