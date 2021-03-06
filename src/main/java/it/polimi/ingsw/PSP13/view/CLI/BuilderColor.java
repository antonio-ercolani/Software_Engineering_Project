package it.polimi.ingsw.PSP13.view.CLI;

public enum BuilderColor {

    Blue("\u001B[34m"), Red("\u001B[31m"), Yellow("\u001B[33m");

    private final String color;
    private final String builder = "\u26d1";
    private final String reset = "\u001b[0m";

    /**
     * Creates an Enum that represents Unicode strings for the builder in various colors
     * @param color builders' color
     */
    BuilderColor(String color)
    {
        this.color = color;
    }

    public String toString()
    {
        return color + builder + reset;
    }
}
