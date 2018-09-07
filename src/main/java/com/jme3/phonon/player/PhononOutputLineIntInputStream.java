package com.jme3.phonon.player;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.jme3.phonon.PhononOutputLine;
import com.jme3.phonon.PhononOutputLine.ChannelStatus;
import com.jme3.phonon.format.decoder.AudioDataDecoder;
import com.jme3.phonon.format.decoder.AudioDataDecoderFactory;

/**
 * PhononChanneInputStream
 */

public class PhononOutputLineIntInputStream extends InputStream {
    ChannelStatus lastStat;
    PhononOutputLine chan;
    byte floatBuffer[];
    byte tmpBuffer[];
    int tmpBufferI = 0;
    int sampleSize;
 
    private AudioDataDecoder decoder;

    public PhononOutputLineIntInputStream(PhononOutputLine chan,int sampleSize) {
        this.chan = chan;
        this.sampleSize = sampleSize;
        floatBuffer = new byte[chan.getFrameSize() * 4];
        tmpBuffer = new byte[chan.getFrameSize() * (sampleSize/8)];

        decoder = AudioDataDecoderFactory.getAudioDataDecoder(sampleSize);
    }

    @Override
    public int read() throws IOException {
        if (tmpBufferI == tmpBuffer.length) {
            if (lastStat == ChannelStatus.OVER)
                throw new EOFException(lastStat.toString());
            lastStat = chan.readNextFrameForPlayer(floatBuffer);
            if (lastStat == ChannelStatus.NODATA) {
                return -1;
            }

            decoder.decode(floatBuffer, tmpBuffer);

            tmpBufferI = 0;
        }
        int b = tmpBuffer[tmpBufferI++];
        
        b = b & 0xff; // to unsigned byte
        return b;
    }
}