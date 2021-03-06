package it.polimi.ingsw.PSP13.network.client_dispatching.behavior;


import it.polimi.ingsw.PSP13.network.client_dispatching.MessageFromControllerToView;
import it.polimi.ingsw.PSP13.view.Input;


public class ClientTurnEndedBehavior extends ClientDispatcherBehavior {

    @Override
    public void behavior(MessageFromControllerToView messageCV) {
        input.turnEnded();
    }

    public ClientTurnEndedBehavior(Input input)
    {
        super(input);
    }
}
