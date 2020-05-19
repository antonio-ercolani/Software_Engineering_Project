package it.polimi.ingsw.PSP13.controller;

import it.polimi.ingsw.PSP13.model.Match;
import it.polimi.ingsw.PSP13.model.Turn;
import it.polimi.ingsw.PSP13.model.gods.*;
import it.polimi.ingsw.PSP13.model.player.Builder;
import it.polimi.ingsw.PSP13.model.player.Coords;
import it.polimi.ingsw.PSP13.model.player.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MatchHandler {

    private Match match;
    private TurnHandler turnHandler;

    private List<String> disconnectedPlayers = new ArrayList<String>();
    private int numPlayers;
    boolean endGame;
    private VirtualView virtualView;
    private String godsReceived = null;
    private String selectedGod = null;
    private Coords coords = null;
    private Player challenger = null;
    private String selectedStarter = null;
    private static List<Class> gods;

    public MatchHandler () {
        match = new Match();
    }

    public void init() throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        numPlayers = match.getPlayers().size();
        turnHandler = new TurnHandler(virtualView);
        turnHandler.setMatchHandler(this);
        initializeGods();
        Turn.setMatch(match);
        Turn.setTurnHandler(turnHandler);
        match.start(virtualView);
        godSelection(virtualView);
        //virtualView.notifyClientsInfo();
        starterSelection(virtualView);
        builderSetUp(virtualView);
    }

    public void addPlayer(Player player) {
        match.addPlayer(player);
    }

    public synchronized void godSelection(VirtualView virtualView) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException, IOException {
        //TODO gestire eccezioni invece di throws
        Random r = new Random();
        challenger = match.getPlayers().get(r.nextInt(numPlayers));
        List<String> godsList = godNames();

        boolean error = false;
        List<String> godsInput;
        do {
            virtualView.godSelectionInput(challenger.getUsername(), godsList, numPlayers, error);
            virtualView.sendGodEffectDescription(godEffects());
            error = false;
            while (godsReceived == null && disconnectedPlayers.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //TODO
                }
            }
            if (!disconnectedPlayers.isEmpty()) {
                if (!disconnectedPlayers.contains(challenger.getUsername())) {
                    while (godsReceived == null && !disconnectedPlayers.contains(challenger.getUsername())) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            //TODO
                        }
                    }
                }
                virtualView.notifyDisconnection();
            }
            godsInput = new ArrayList<>(Arrays.asList(godsReceived.split("\\s*,\\s*")));
            godsReceived = null;
            if (!godsList.containsAll(godsInput)) error = true;
            if (godsInput.size() != numPlayers) error = true;
            for (String currentGod : godsInput) {
                for (String otherGod : godsInput) {
                    if (currentGod != otherGod && currentGod.equals(otherGod)) {
                        error = true;
                        break;
                    }
                }
            }
        } while(error);

        godAssignment(virtualView, challenger, godsInput);
    }

    public synchronized void godAssignment(VirtualView virtualView, Player challenger, List<String> chosenGods) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        boolean error;
        String receivedGod;
        List<Player> playerList = match.getPlayers();
        int challengerPos = playerList.indexOf(challenger);
        //sort match.getPlayers() putting challenger in the last position
        if (challengerPos != (numPlayers-1)) {
            Player app = playerList.get(numPlayers-1);
            playerList.set(numPlayers-1,playerList.get(challengerPos));
            playerList.set(challengerPos,app);
        }

        for (int i = 0; i < numPlayers; i++) {
            error = false;
            String player = match.getPlayers().get(i).getUsername();
            if (chosenGods.size() > 1) {
                do {
                    virtualView.godInput(player, chosenGods, error);
                    virtualView.sendGodEffectDescription(godEffects());
                    while (selectedGod == null && disconnectedPlayers.isEmpty()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            //TODO
                        }
                    }
                    if (!disconnectedPlayers.isEmpty()) {
                        if (!disconnectedPlayers.contains(player)) {
                            while (selectedGod == null && !disconnectedPlayers.contains(player)) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    //TODO
                                }
                            }
                        }
                        virtualView.notifyDisconnection();
                    }
                    error = false;
                    if (!chosenGods.contains(selectedGod)) error = true;
                    receivedGod = selectedGod;
                    selectedGod = null;
                } while (error);
            } else {
                virtualView.godInput(player, chosenGods, error);
                receivedGod = chosenGods.get(0);
            }
            chosenGods.remove(receivedGod);
            Class<?> clazz = Class.forName("it.polimi.ingsw.PSP13.model.gods." + receivedGod);
            Class[] c = new Class[0];
            Object[] ob = new Object[0];
            Object god = clazz.getDeclaredConstructor(c).newInstance(ob);
            Player currentPlayer = match.getPlayers().get(i);
            currentPlayer.setGod((Turn) god);
            virtualView.setGod(player,receivedGod);
            //virtualView.sendGodEffectDescription(match.getPlayers().get(i).getEffect(), godEffects());
        }
    }

    /**
     * Handles the selection of the starter player
     * by the challenger
     * @param virtualView
     * @throws IOException
     */
    public synchronized void starterSelection(VirtualView virtualView) throws IOException {
        boolean error = false;
        do {
            virtualView.starterInput(challenger.getUsername(), error);
            while (selectedStarter == null && disconnectedPlayers.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    //TODO
                }
            }
            //TODO chiedere a nanno se si gestisce così la disconnesione
            if (!disconnectedPlayers.isEmpty()) {
                if (!disconnectedPlayers.contains(challenger)) {
                    while (selectedStarter == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            //TODO
                        }
                    }
                }
                virtualView.notifyDisconnection();
            }
            error = true;
            for (Player player : match.getPlayers()) {
                if (player.getUsername().equals(selectedStarter)) {
                    error = false;
                    break;
                }
            }
            if (error) selectedStarter = null;
        } while(error);

        sortPlayers();

    }


    //TODO OTTIMIZZABILE
    /**
     * Sorts player's list shifting starterPlayer in first position,
     * keeping the shift order used up to this point
     */
    public void sortPlayers() {
        int starterIndex = match.getPlayers().indexOf(match.getPlayerByUsername(selectedStarter));
        if (starterIndex != 0) {
            if (numPlayers == 2) {
                Player app = match.getPlayers().get(0);
                match.getPlayers().set(0, match.getPlayerByUsername(selectedStarter));
                match.getPlayers().set(1, app);
            } else {  //if numPlayers = 3
                List<Player> appList = new ArrayList<>(match.getPlayers());
                match.getPlayers().set(0, appList.get(starterIndex));
                if (starterIndex == 1) {
                    match.getPlayers().set(1, appList.get(2));
                    match.getPlayers().set(2, appList.get(0));
                } else {
                    match.getPlayers().set(1, appList.get(0));
                    match.getPlayers().set(2, appList.get(1));
                }
            }
        }
    }



    /**
     * Handles the set up of the builders for each players
     * @param virtualView
     * @throws IOException
     */
    public synchronized void builderSetUp(VirtualView virtualView) throws IOException {
        Player currentPlayer;
        Coords pos1;
        Coords pos2 = null;
        Coords receivedCoords;
        Builder[] builders = new Builder[2];
        boolean firstCall, error;
        match.notifyMap();
        for (int i=0; i < numPlayers; i++) {
            firstCall = true;
            error = false;
            pos1 = null;
            currentPlayer = match.getPlayers().get(i);
            for (int numBuilder = 0; numBuilder < 2; numBuilder++) {
                do {
                    virtualView.builderSetUpInput(currentPlayer.getUsername(), firstCall, error);
                    while (coords == null && disconnectedPlayers.isEmpty()) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            //TODO
                        }
                    }
                    if (!disconnectedPlayers.isEmpty()) {
                        if (!disconnectedPlayers.contains(currentPlayer.getUsername())) {
                            while (coords == null && !disconnectedPlayers.contains(currentPlayer.getUsername())) {
                                try {
                                    wait();
                                } catch (InterruptedException e) {
                                    //TODO
                                }
                            }
                        }
                        virtualView.notifyDisconnection();
                    }
                    error = false;
                    if (match.isOccupied(coords)) error = true;
                    if (pos1 != null && pos1.equals(coords)) error = true;
                    receivedCoords = coords;
                    coords = null;
                } while (error);
                if (pos1 == null) {
                    pos1 = receivedCoords;
                } else {
                    pos2 = receivedCoords;
                }
                builders[numBuilder] = new Builder();
                firstCall = false;
            }
            currentPlayer.setBuilders(builders);
            currentPlayer.setup(builders[0], builders[1], pos1, pos2);
        }
    }

    /**
     * Controller's method that handles the progress of the game
     * @throws IOException
     */
    public synchronized void play() throws IOException {
        List<Player> players;
        List<Coords> possibleMoves, possibleBuilds;
        Builder currentBuilder;
        Coords builderPos;
        Player winner = null;
        endGame = false;
        Player currentPlayer;
        while (!endGame) {
            players = Collections.unmodifiableList(new ArrayList<>(match.getPlayers()));
            for (int i = 0; i < match.getPlayers().size(); i++) {
                currentPlayer = players.get(i);
                if (players.size() < 2) {
                    this.notifyWinners(currentPlayer.getUsername());
                    break;
                }
                builderPos = turnHandler.getInputBuilder(currentPlayer);
                currentBuilder = match.getBuilderByCoords(builderPos);
                currentPlayer.start();
                possibleMoves = currentPlayer.getCellMoves(currentBuilder);
                if (possibleMoves.isEmpty()) {
                    this.notifyLoser(currentPlayer);
                    continue;
                }
                coords = turnHandler.getInputMove(currentBuilder, possibleMoves);
                currentPlayer.move(currentBuilder, coords);
                if (endGame) {
                    winner = currentPlayer;
                    break;
                }
                possibleBuilds = currentPlayer.getCellBuilds(currentBuilder);
                if (possibleBuilds.isEmpty()) {
                    this.notifyLoser(currentPlayer);
                    continue;
                }
                coords = turnHandler.getInputBuild(currentBuilder, possibleBuilds);
                currentPlayer.build(currentBuilder, coords);
                currentPlayer.end();
            }
        }
        this.notifyWinners(winner.getUsername());
    }


    /**
     * Notifies the players that the game ended and tells them if they did win or lose.
     * @param winner username of the winner
     * @throws IOException
     */
    public void notifyWinners(String winner) throws IOException {
        endGame = true;
        virtualView.notifyWin(winner);
        for (Player player : match.getPlayers()) {
            if (!player.getUsername().equals(winner)) virtualView.notifyLose(player.getUsername());
        }
    }

    public void notifyLoser(Player loser) throws IOException {
        virtualView.notifyLose(loser.getUsername());
        match.removeBuilder(loser);
        match.getPlayers().remove(loser);
    }

    public synchronized void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
        notifyAll();
    }

    public void setVirtualView(VirtualView virtualView) {
        this.virtualView = virtualView;
    }

    public synchronized void setGodsReceived(String godsReceived) {
        this.godsReceived = godsReceived;
        notifyAll();
    }

    public synchronized void setSelectedGod(String selectedGod) {
        this.selectedGod = selectedGod;
        notifyAll();
    }

    public synchronized void setCoords(Coords coords) {
        this.coords = coords;
        notifyAll();
    }

    public synchronized void setEndGame(boolean endGame) {
        this.endGame = endGame;
        notifyAll();
    }

    public synchronized void addDisconnectedPlayer(String player) {
        disconnectedPlayers.add(player);
        notifyAll();
    }

    public Match getMatch() {
        return match;
    }

    public TurnHandler getTurnHandler() {
        return turnHandler;
    }

    public Coords getCoords() {
        return coords;
    }

    public synchronized void setSelectedStarter(String starter) {
        this.selectedStarter = starter;
        notifyAll();
    }

    private void initializeGods() {
        gods = new ArrayList<>();
        gods.add(Apollo.class);
        gods.add(Zeus.class);
        gods.add(Artemis.class);
        gods.add(Athena.class);
        gods.add(Atlas.class);
        gods.add(Minotaur.class);
        gods.add(Hephaestus.class);
        gods.add(Ares.class);
        gods.add(Hypnus.class);
        gods.add(Demeter.class);
        gods.add(Pan.class);
        gods.add(Poseidon.class);
        gods.add(Prometheus.class);
        gods.add(Hera.class);
     }

     private List<String> godNames() {
        List<String> names = new ArrayList<>();
        for (Class god : gods) {
            String[] splitted = god.toString().split("\\s* \\s*");
            String name = splitted[1].replace("it.polimi.ingsw.PSP13.model.gods.", "");
            names.add(name);
        }
        return names;
     }

     private List<String> godEffects() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
         List<String> names = new ArrayList<>();
         for (Class god : gods) {
             Turn currentGod = (Turn) god.getDeclaredConstructor().newInstance();
             String[] splitted = god.toString().split("\\s* \\s*");
             String name = splitted[1].replace("it.polimi.ingsw.PSP13.model.gods.", "");
             names.add(name + ";" + currentGod.getEffect());
         }
         return names;
     }

}