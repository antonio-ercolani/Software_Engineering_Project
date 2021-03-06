package it.polimi.ingsw.PSP13.view.GUI;

import it.polimi.ingsw.PSP13.immutables.MapVM;
import it.polimi.ingsw.PSP13.model.player.Color;
import it.polimi.ingsw.PSP13.model.player.Coords;
import it.polimi.ingsw.PSP13.network.client_dispatching.MessageClientsInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GameMap implements Initializable {

    private GuiInput guiInput;

    private TurnStatus status;

    @FXML
    private AnchorPane mapPane;

    @FXML
    private ImageView imageInfo1;

    @FXML
    private ImageView imageInfo2;

    @FXML
    private ImageView imageInfo3;

    @FXML
    private GridPane grid;

    @FXML
    private Label textInfo;

    @FXML
    private TextFlow godName1;

    @FXML
    private TextFlow godName2;

    @FXML
    private TextFlow godName3;

    @FXML
    private Label textEffect;

    @FXML
    private Label textGodName;

    @FXML
    private ImageView enableIf2;

    private String clientGodName;
    private String clientGodEffect;
    private String opponentGod1;
    private String opponentEffect1;
    private String opponentGod2;
    private String opponentEffect2;

    private HashMap<Color, ArrayList<ImageView>> buildersImages = new HashMap<>();

    private Coords[] clientBuildersPositions;

    private Color clientColor;

    private Pane[][] panes = new Pane[5][5];

    private final int BUILDERS_IMAGES_OFFSET = 27;

    /**
     * Initialize map status and save grid's panes
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        savePane();
        TurnStatus.setMap(this);
        textInfo.setText("Please wait your turn...");

    }

    /**
     * Handles a cell click based on the map controller "status"
     * @param e the event that caused the method invocation
     */
    @FXML
    public void selectCell(MouseEvent e) {
        clear();

        Node source = (Node)e.getSource() ;
        int colIndex = ( GridPane.getColumnIndex(source) != null ? GridPane.getColumnIndex(source) : 0);
        int rowIndex = ( GridPane.getRowIndex(source) != null ? GridPane.getRowIndex(source) : 0);

        status.selectCell(new Coords(rowIndex,colIndex));
        grid.setDisable(true);
    }


    /**
     * Sets the players information (name, god, god effect, images..) in the map view
     * @param clientsInfo  message containing players information
     */
    public void setUpClientsInfo(MessageClientsInfo clientsInfo) {

        Text name1 = new Text(clientsInfo.getClientUsername());
        name1.setStyle("-fx-fill: WHITE;");
        godName1.getChildren().add(name1);
        clientGodName = clientsInfo.getClientGod();
        Image godimg1 = new Image("Cards/" +clientGodName+".png");
        imageInfo1.setImage(godimg1);
        setBuildersImages(clientsInfo.getClientColor(),clientsInfo.getClientGod());
        clientGodEffect = clientsInfo.getClientEffect();
        clientColor = clientsInfo.getClientColor();

        opponentGod1 = clientsInfo.getOpponentsGod().get(0);
        Image godimg2 = new Image("Cards/" +opponentGod1+".png");
        imageInfo2.setImage(godimg2);
        Text name2 = new Text(clientsInfo.getOpponentsUsernames().get(0));
        name2.setStyle("-fx-fill: WHITE;");
        godName2.getChildren().add(name2);
        setBuildersImages(clientsInfo.getOpponentsColors().get(0),clientsInfo.getOpponentsGod().get(0));
        opponentEffect1 = clientsInfo.getOpponentsEffects().get(0);


        if (clientsInfo.getOpponentsUsernames().size() == 2) {
            opponentGod2 = clientsInfo.getOpponentsGod().get(1);
            Image godimg3 = new Image("Cards/" +opponentGod2+".png");
            imageInfo3.setImage(godimg3);
            Text name3 = new Text(clientsInfo.getOpponentsUsernames().get(1));
            name3.setStyle("-fx-fill: WHITE;");
            godName3.getChildren().add(name3);
            setBuildersImages(clientsInfo.getOpponentsColors().get(1),clientsInfo.getOpponentsGod().get(1));
            opponentEffect2 = clientsInfo.getOpponentsEffects().get(1);

        } else {
            enableIf2.setVisible(true);
        }
    }


    /**
     * Sets workers images (ImageView) belonging to "color" player
     * @param color player's color
     * @param god player's god
     */
    public void setBuildersImages(Color color, String god) {
        ImageView i1 = new ImageView("Icons/"+god+".png");
        ImageView i2 = new ImageView("Icons/"+god+".png");
        i1.setFitHeight(50);
        i1.setFitWidth(50);
        i2.setFitHeight(50);
        i2.setFitWidth(50);
        i1.setMouseTransparent(true);
        i2.setMouseTransparent(true);
        i1.setTranslateX(BUILDERS_IMAGES_OFFSET);
        i2.setTranslateX(BUILDERS_IMAGES_OFFSET);
        ArrayList<ImageView> i = new ArrayList<>();
        i.add(i1);
        i.add(i2);
        buildersImages.put(color,i);
    }

    Boolean firstUpdateBuilders = true;

    /**
     * Updates the workers position of the "color" player in the map view
     * @param color player's color
     * @param coords updated coordinates of the workers
     */
    @FXML
    public void updateBuilders(Color color, Coords[] coords) {
        if (!firstUpdateBuilders) {
            grid.getChildren().remove(buildersImages.get(color).get(0));
            grid.getChildren().remove(buildersImages.get(color).get(1));
        } else {
            firstUpdateBuilders = false;
        }
        if (coords[0] != null && coords[1] != null) {
            grid.add(buildersImages.get(color).get(0), coords[0].getY(), coords[0].getX());
            grid.add(buildersImages.get(color).get(1), coords[1].getY(), coords[1].getX());
        }

        if (color == clientColor) {
            clientBuildersPositions = coords;
        }
    }

    String imageShowed = "";

    /**
     * Shows the effect of the god you clicked on
     * @param e the event that caused the method invocation
     */
    @FXML
    public void showEffect(MouseEvent e) {
        Node source = (Node) e.getSource();
        String id = source.getId();
        if (imageShowed.equals(id)) {
            imageShowed = "";
            textInfo.setVisible(true);
            textEffect.setVisible(false);
            textGodName.setVisible(false);
        } else {
            switch (id) {
                case "imageInfo1":
                    textGodName.setText(clientGodName);
                    textEffect.setText(clientGodEffect);
                    break;
                case "imageInfo2":
                    textGodName.setText(opponentGod1);
                    textEffect.setText(opponentEffect1);
                    break;
                case "imageInfo3":
                    textGodName.setText(opponentGod2);
                    textEffect.setText(opponentEffect2);
                    break;
            }
                imageShowed = id;
                textInfo.setVisible(false);
                textGodName.setVisible(true);
                textEffect.setVisible(true);
        }
    }


    /**
     * Update the images of blocks and domes of each cell according to the received mapVM
     * @param mapVM Immutable map sent from the model
     */
    public void updateMap(MapVM mapVM) {
        int cellHeight;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                cellHeight = mapVM.getMap()[i][j].getLevel().getHeight();
                boolean isDome = mapVM.getMap()[i][j].getDome();
                String buildingStyle = "";
                switch (cellHeight) {
                    case 0:
                        buildingStyle = isDome ? "dome" : "clear";
                        break;
                    case 1:
                        buildingStyle = isDome ? "level1Dome" : "level1";
                        break;
                    case 2:
                        buildingStyle = isDome ? "level2Dome" : "level2";
                        break;
                    case 3:
                        buildingStyle = isDome ? "level3Dome" : "level3";
                        break;
                }
                panes[i][j].getStyleClass().clear();
                panes[i][j].getStyleClass().add(buildingStyle);

            }
        }
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j].getStyleClass().removeAll("highlighted");
            }
        }
    }

    /**
     * Save the Pane object of each cell of the grid
     */
    public void savePane() {
        int i,j;
        for (Node child : grid.getChildren()) {
            i = ( GridPane.getColumnIndex(child) != null ? GridPane.getColumnIndex(child) : 0);
            j = ( GridPane.getRowIndex(child) != null ? GridPane.getRowIndex(child) : 0);
            panes[j][i] = (Pane) child;
        }
    }


    /**
     * Sets the login scene
     * @throws IOException
     */
    public void setSceneLogin() throws IOException {
        AnchorPane root = mapPane;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GameMap.class.getResource("/lobby.fxml"));
        AnchorPane pane = loader.<AnchorPane>load();

        Scene scene = new Scene(pane);
        scene.getStylesheets().add("god_selection.css");

        Stage stage = (Stage) (root.getScene().getWindow());

        stage.setTitle("Santorini");
        stage.getIcons().add(new Image("island.png"));
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    /**
     * Sets the endgame scene
     * @throws IOException
     */
    public void endgameSceneChange() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GameMap.class.getResource("/endgame.fxml"));

        AnchorPane pane = loader.<AnchorPane>load();
        Scene endgame = new Scene(pane);
        endgame.getStylesheets().add("endgame.css");

        Stage stage = (Stage) (grid.getScene().getWindow());
        stage.setScene(endgame);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

        EndGame endGame = loader.<EndGame>getController();
        endGame.setGuiInput(guiInput);

        if(guiInput.isWinner())
            endGame.win();
    }

    /**
     * Shows a popup asking if the player wants to use his god effect
     * @param god name of the god
     */
    public void askEffect(String god) throws IOException {
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GameMap.class.getResource("/effectPopup.fxml"));
        AnchorPane effectPane = loader.<AnchorPane>load();

        EffectInput effectInput = loader.<EffectInput>getController();
        effectInput.setGuiInput(guiInput);
        effectInput.upload(god);

        Scene scene = new Scene(effectPane);
        scene.getStylesheets().add("effectInput.css");

        stage.setTitle("Effect");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();

    }


    /**
     * Informs the player that he has to select a cell to remove a block and highlights the selectable cells
     * @param removableBlocks list of the cells that the player can choose to remove a block
     */
    public void removeBlock(List<Coords> removableBlocks) {
        textInfo.setText("You can remove a block only from the highlighted cells");
        highlightCells(removableBlocks);
        grid.setDisable(false);
    }

    /**
     * Removes the highlight from the cells
     */
    private void clear() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                panes[i][j].getStyleClass().removeAll("highlight");
                panes[i][j].setDisable(true);
            }
        }
    }

    /**
     * Highlights cells
     * @param cells cells that have to be highlighted
     */
    private void highlightCells(List<Coords> cells) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Coords tempCoords = new Coords(i, j);
                if (cells.contains(tempCoords)) {
                    panes[i][j].getStyleClass().add("highlight");
                    panes[i][j].setDisable(false);

                }
            }
        }
    }

    /**
     * Disables cells making them not clickable
     * @param setupCells cells that have to be disabled
     */
    private void disableCells(List<Coords> setupCells) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Coords tempCoords = new Coords(i, j);
                if (setupCells.contains(tempCoords)) {
                    panes[i][j].setDisable(false);
                }
            }
        }
    }

    /**
     * Informs the player that he has to choose a cell to build a block on it and
     * highlights the selectable cells
     * @param checkBuildCells cells among which the player can choose to build
     */
    public void build(List<Coords> checkBuildCells) {
        textInfo.setText("Now you have to build a block\nChoose a cell from the highlighted ones");
        highlightCells(checkBuildCells);
        grid.setDisable(false);
    }

    /**
     * Informs the player that he has to choose a cell to move in and
     * highlights the selectable cells
     * @param checkMoveCells cells among which the player can choose to move
     */
    public void move(List<Coords> checkMoveCells) {
        textInfo.setText("It's time to move your worker\nChoose a cell from the highlighted ones");
        highlightCells(checkMoveCells);
        grid.setDisable(false);
    }

    /**
     * Informs the player that he has to choose a builder and
     * disables the not selectable cells
     */
    public void chooseBuilder() {
        textInfo.setText("It's your turn!\nPlease select a worker");
        disableCells(Arrays.asList(clientBuildersPositions));
        grid.setDisable(false);
    }


    /**
     * Informs the player that he has place a builder and
     * disables the not selectable cells
     * @param callnumber number of the worker the player has to place (first/second)
     */
    public void builderSetup(boolean callnumber) {
        if (!callnumber)
            textInfo.setText("Choose the position of your second builder");
        else {
            textInfo.setText("It's your turn! Choose the position of your first builder");
            clear();
        }
        setupHighlight();
        grid.setDisable(false);
    }


    /**
     * Cycles all the grid panes and check if any pane has an ImageView as child. If it does checks its url
     * If the pane doesn't have an image or it's url is clear, a new Coords is created and added to checkSetupList
     */
    private void setupHighlight() {
        List<Coords> checkSetupList = new ArrayList<>();

        for(int i=0;i<5;i++) {
            for(int j=0;j<5;j++)
                checkSetupList.add(new Coords(i,j));
        }

        for(Node pane : grid.getChildren()) {
            int x = (GridPane.getColumnIndex(pane) != null ? GridPane.getColumnIndex(pane) : 0);
            int y = (GridPane.getRowIndex(pane) != null ? GridPane.getRowIndex(pane) : 0);

            if(pane instanceof ImageView) checkSetupList.remove(new Coords(y, x));
        }

        disableCells(checkSetupList);
    }

    public void setStatus(TurnStatus status) {
        this.status = status;
    }

    public GuiInput getGuiInput() {
        return guiInput;
    }

    public void setGuiInput(GuiInput guiInput) {
        this.guiInput = guiInput;
    }

    /**
     * Informs the player that his turn is finished and he has to wait
     */
    public void turnEnded() {
        textInfo.setText("Your turn is over\nPlease wait...");
    }

    /**
     * Informs the player that he has to wait for his turn
     */
    public void printWait() {
        textInfo.setText("Please wait your turn...");
    }

    /**
     * Prints the spectate message
     */
    public void printSpectateMessage() {
        textInfo.setText("You lost\n\nWait while the match is ending");
    }

}
