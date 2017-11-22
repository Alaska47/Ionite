package com.akotnana.ionite.utils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anees on 10/27/2017.
 */

public class DayType {
    @SerializedName("name")
    public String name;
    @SerializedName("special")
    public boolean special;
    @SerializedName("blocks")
    public Blocks[] blocks;
}
