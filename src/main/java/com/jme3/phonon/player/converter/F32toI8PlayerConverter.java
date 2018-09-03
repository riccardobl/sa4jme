package com.jme3.phonon.player.converter;

import com.jme3.phonon.utils.BitUtils;

class F32toI8PlayerConverter implements PlayerConverter {
    private static F32toI8PlayerConverter instance;

    public static F32toI8PlayerConverter instance() {
        if(instance == null)
            instance = new F32toI8PlayerConverter();
        
        return instance;
    }

    public void convert(byte[] inputBuffer, byte[] outputBuffer) {
        byte[] partInputBuffer = new byte[4];
        byte[] partOutputBuffer = new byte[1];

        for (int i = 0; i < inputBuffer.length; i += 4) {
            for(int j = 0; j < partInputBuffer.length; ++j) {
                partInputBuffer[j] = inputBuffer[i + j];
            }

            BitUtils.cnvF32leToI8le(partInputBuffer, partOutputBuffer);

            for (int j = 0; j < partOutputBuffer.length; ++j)
                outputBuffer[(i / 4) * partOutputBuffer.length + j] = partOutputBuffer[j];
        }
    }
}