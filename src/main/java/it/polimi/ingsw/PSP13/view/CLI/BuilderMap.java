package it.polimi.ingsw.PSP13.view.CLI;

import it.polimi.ingsw.PSP13.immutables.BuilderVM;
import it.polimi.ingsw.PSP13.model.player.Color;
import it.polimi.ingsw.PSP13.model.player.Coords;


import java.util.HashMap;
import java.util.Map;

public class BuilderMap {

    private final Map<Color, BuilderVM> map;

    public Coords[] getCoords(Color color){
        return map.get(color).getBuilders();
    }

    /**
     * Creates an immutable builder
     */
    public BuilderMap()
    {
        map = new HashMap<>();
        map.put(Color.Blue,null);
        map.put(Color.Red,null);
        map.put(Color.Yellow,null);

    }

    /**
     * Updates the position of the builders identified by color
     * @param builders the new values
     */
    public void updateBuilder(BuilderVM builders)
    {
        map.put(builders.getColor(),builders);
    }

    /**
     * Check if there is a builder on coordinates (x,y)
     * @param x x coordinate
     * @param y y coordinate
     * @return the color of the builder that lies on coordinates (x,y), null if there is no builder
     */
    public Color checkBuilder(int x, int y)
    {
        Coords par = new Coords(x,y);
        Coords[] coords = null;

        for(Map.Entry entry : map.entrySet())
        {
            if(entry.getValue() != null)
                coords = ((BuilderVM)entry.getValue()).getBuilders();
            if(coords != null && coords[0] != null && coords[1] != null)
            {
                if(coords[0].equals(par) || coords[1].equals(par))
                    return (Color)entry.getKey();
            }
        }

        return null;
    }

    /**
     * Removes all data from BuilderMap
     */
    public void clear() {
        map.clear();
    }





}
