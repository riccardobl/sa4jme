package com.jme3.phonon.player.converter;

public class PlayerConverterManager {
    /**
     * Factory method to initialize a proper PlayerConverter
     * 
     * 
     */

    public static PlayerConverter getPlayerConverter(int sampleSize) {
        switch(sampleSize) {
            case 8:
                return F32toI8PlayerConverter.instance();
            case 16:
                return F32toI16PlayerConverter.instance();
            case 24:
                return F32toI24PlayerConverter.instance();
            default:
                return null;
        }
    }
}