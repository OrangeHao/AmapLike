package com.orange.amaplike.pickpoi;

import com.amap.api.services.core.PoiItem;

/**
 * created by czh on 2018-01-09
 */

public class PoiItemEvent {

    private PoiItem item;
    private int from;

    public PoiItemEvent(PoiItem ite,int from){
        this.from = from;
        item=ite;
    }

    public PoiItem getItem() {
        return item;
    }

    public void setItem(PoiItem item) {
        this.item = item;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }
}
