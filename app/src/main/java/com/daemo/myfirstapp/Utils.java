package com.daemo.myfirstapp;

/**
 * Created by admin on 25/06/2016.
 */
public class Utils {

    public static String getTag(Object inst){
        return inst.getClass().getSimpleName();
    }
}
