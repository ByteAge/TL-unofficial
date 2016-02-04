package com.example;

public class MyClass {
    public static void main(String[] args){
        log(hexToRgb(0xff61a349));

        log(getIntFromColor(74, 146, 60));

        log(Integer.toString(180224, 16));
    }




    public static String hexToRgb(int hex){
        int r = (hex & 0xFF0000) >> 16;
        int g = (hex & 0xFF00) >> 8;
        int b = (hex & 0xFF);
        return "r: " + r+ " g: " + g + " b: " + b;
    }
    public static void log(Object a){
        System.out.println(a);
    }
    public static int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.

        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
}
