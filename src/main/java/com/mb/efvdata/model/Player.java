package com.mb.efvdata.model;

public class Player {
    public String name;
    public String team;
    public double owned;

    public Player(String name, String team, double owned) {
        this.name = name;
        this.team = team;
        this.owned = owned;
    }
}