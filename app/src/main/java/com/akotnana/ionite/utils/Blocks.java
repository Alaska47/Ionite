package com.akotnana.ionite.utils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by anees on 10/27/2017.
 */

public class Blocks {
    @SerializedName("order")
    public int order;
    @SerializedName("name")
    public String name;
    @SerializedName("start")
    public String start;
    @SerializedName("end")
    public String end;
}
