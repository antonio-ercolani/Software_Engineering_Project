package it.polimi.ingsw.PSP13.controller;

import it.polimi.ingsw.PSP13.model.Match;
import it.polimi.ingsw.PSP13.model.player.Coords;

/**
 * this is the controller observer class which is subscribed to the view
 */
public class ViewObserver {

    private final MatchHandler handler;

    public ViewObserver(MatchHandler matchHandler)
    {
        this.handler = matchHandler;
    }

    /**
     * check form string validation and set the value to the player instance
     * @param nickname the nickname chosen by the user
     */
    public void updateNickname(String nickname)
    {
        handler.setNick(nickname);
    }

    /**
     * sets the value of the god to the player instance and updates the list of gods left
     * @param god the god chosen by user
     */
    public void updateGod(String god)
    {
        handler.setSelectedGod(god);
    }

    /**
     * sets the gods available for this match
     * @param gods
     */
    public void updateGodSelection(String gods)
    {
        handler.setGodsReceived(gods);
    }

    /**
     * sets the initial coordinates of player builders
     * @param builder
     */
    public void updateSetupBuilder(Coords builder)
    {
        handler.setCoords(builder);
    }
}
