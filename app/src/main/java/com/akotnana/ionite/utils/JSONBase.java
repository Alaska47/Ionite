package com.akotnana.ionite.utils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by anees on 10/27/2017.
 */

public class JSONBase {
    @SerializedName("url")
    public String url;
    @SerializedName("date")
    public String date;
    @SerializedName("day_type")
    public DayType day_type;

    public Object[] getData() {
        Object[] ob = new Object[4];
        ob[0] = date;
        ob[1] = day_type.name;
        ob[3] = day_type.special;
        ArrayList<String[]> sched = new ArrayList<String[]>();
        Blocks[] bl = day_type.blocks;
        for (Blocks b : bl) {
            String[] s = b.start.split(":");
            String[] e = b.end.split(":");
            int s1 = Integer.parseInt(s[0]);
            int s2 = Integer.parseInt(e[0]);
            String[] te = { b.name, s1 + ":" + s[1] + " - " + s2 + ":" + e[1] };
            sched.add(te);
        }
        ob[2] = sched;
        return ob;
    }
}

