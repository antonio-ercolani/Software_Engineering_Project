package it.polimi.ingsw.PSP13.view;

import it.polimi.ingsw.PSP13.immutables.BuilderVM;
import it.polimi.ingsw.PSP13.immutables.MapVM;
import it.polimi.ingsw.PSP13.model.player.Coords;
import it.polimi.ingsw.PSP13.network.client_callback.ControllerCallback;
import it.polimi.ingsw.PSP13.network.client_dispatching.MessageClientsInfo;
import it.polimi.ingsw.PSP13.network.client_dispatching.UpdateListener;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

import static it.polimi.ingsw.PSP13.network.Client.PORT;

public abstract class Input {

    protected ObservableToController controller;
    protected boolean first = false;
    protected Socket socket;
    protected UpdateListener listener;

    /**
     * Connects the client to the server socket. A listener thread is started
     */
    public abstract void setup();

    /**
     * Utility function to connect a client socket with the server socket
     * @param serverIp the server ip
     * @throws IOException if the connection fails
     */
    public void connectToServer(String serverIp) throws IOException {

        socket = new Socket(serverIp, PORT);
        ControllerCallback callback = new ControllerCallback(socket);
        controller = new ObservableToController(callback);
        listener = new UpdateListener(socket, this, callback);
        Thread thread = new Thread(listener, "listener");
        thread.start();
    }

    /**
     * Notifies the client that he can continue to spectate the match
     */
    public abstract void notifySpectate();

    /**
     * Asks the player to choose a builder to move
     * @param checkMoveCells a list of cell the builder can move on
     * @param error true if the previous input generated an error
     */
    public abstract void moveInput(List<Coords> checkMoveCells, boolean error);

    /**
     * Asks the player to choose a builder and to build a structure
     * @param checkBuildCells a list of cell the builder can build on
     */
    public abstract void buildInput(List<Coords> checkBuildCells, boolean error);

    /**
     * Asks the player to insert a nickname for this match
     * @param error true if the previous input generated an error
     */
    public abstract void nicknameInput(boolean error);

    /**
     * Asks the player which god he wants to play with
     * @param chosenGods the gods available to be chosen
     */
    public abstract void godInput(List<String> chosenGods, boolean error);

    /**
     * Asks the player the starting position of his builder
     */
    public abstract void builderSetUpInput(boolean callNumber, boolean error);

    /**
     * Asks the challenger to choose a set of gods for this match
     * @param godsList contains the name of all the gods available to choose from
     * @param godsNumber the number of gods the challenger has to choose
     */
    public abstract void godSelectionInput(List<String> godsList, int godsNumber, boolean error);

    /**
     * Asks the player if he wants to activate the effect of his god
     * @param god the name of the god related to the effect
     */
    public abstract void effectInput(String god);

    /**
     * Asks the player to select the builder he has to move
     * @param player the player who has to choose
     */
    public abstract void chooseBuilder(String player);

    /**
     * Asks the player to remove a block on a certain cell
     */
    public abstract void removeBlock(List<Coords> removableBlocks, boolean error);


    // ------------- UPDATES THE VIEW VIA PARAMETERS OBJECT -------------------

    /**
     * Update view's map
     * @param mapVM Immutable map sent from the model
     */
    public abstract void updateMap(MapVM mapVM);

    /**
     * Update view's builders of the color of BuilderVM
     * @param builderVM Immutables couple of builders sent from the model
     */
    public abstract void updateBuilders(BuilderVM builderVM);

    /**
     * Notifies the view that the client won
     */
    public abstract void notifyWin();

    /**
     * Notifies the view that the client lost
     */
    public abstract void notifyLose();

    /**
     * Updates client's CLI setting clients information (usernames, builders colors and gods)
     * @param messageClientsInfo contains clients information
     */
    public abstract void setupClientsInfo(MessageClientsInfo messageClientsInfo);

    /**
     * Informs clients that the challenger is choosing gods for the match
     * @param challenger username of the challenger
     */
    public abstract void printWaitGodsSelection(String challenger, List<String> godsList);

    /**
     * Informs clients that player is choosing his god
     * @param player player's username
     */
    public abstract void printWaitGodSelection(String player, List<String> chosenGods);

    /**
     * Inform client which god the server assigned him
     * @param assignedGod name of the assigned god
     */
    public abstract void printAssignedGod(String assignedGod);

    /**
     * Inform the client of a disconnection
     */
    public abstract void disconnectionMessage();

    /**
     * Asks the player to choose the number of players for this current match
     */
    public abstract void choosePlayerNum(boolean error);

    /**
     * Saves in MapPrinter the effect description of player's god
     * @param effect description of the effect
     */
    public abstract void setEffectDescription(String effect, List<String> godsEffect);

    public abstract void playAgain();

    /**
     * Asks the challenger to choose the starter player
     * @param error true if the previous input generated an error
     * @param usernames players' usernames
     */
    public abstract void starterInput(boolean error, List<String> usernames);

    /**
     * Informs clients that the challenger is choosing the starter player
     * @param challenger challenger's username
     */
    public abstract void printWaitStarterSelection(String challenger);

    /**
     * Prints a wait message
     */
    public abstract void lobbyWait();

    /**
     * Prints a wait message related to the lobby being full
     */
    public abstract void waitQueueMsg();


    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isFirst() {
        return first;
    }

    /**
     * Informs the client that his turn is finished
     */
    public abstract void turnEnded();

}
