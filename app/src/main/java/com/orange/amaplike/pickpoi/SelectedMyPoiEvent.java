package com.orange.amaplike.pickpoi;

/**
 * created by czh on 2018-01-12
 */

public class SelectedMyPoiEvent {
    private int from;

    public SelectedMyPoiEvent(int from){
        this.from = from;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }
}
