package ru.pb.netchatclient.utils;


import java.util.HashMap;

public class MessageColorUtil {
    private static String[] styles = {"-fx-background-color: #00FFC450;", "-fx-background-color: #00BBC450;", "-fx-background-color: #66BB6450;", "-fx-background-color: #88996450;", "-fx-background-color: #00AA0050;", };
    private static int i=0;
    private static HashMap<Integer,String> stylesMap = new HashMap<>();

    public static String getColor(int id){
        if (!stylesMap.containsKey(id)){
            stylesMap.put(id, getNewColor());
        }
        return stylesMap.get(id);
//        return getNewColor();
    }

    private static String getNewColor(){
        String s=styles[i++];
        if (i== styles.length) i=0;
        return s;
    }
}
