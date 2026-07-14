package com.chess.utils;

import javax.sound.sampled.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private static boolean enabled = true;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sound-thread");
        t.setDaemon(true);
        return t;
    });

    public static void setEnabled(boolean enabled) { SoundManager.enabled = enabled; }
    public static boolean isEnabled()              { return enabled; }

    public static void playMove()      { play(440, 60);  }
    public static void playCapture()   { play(220, 90);  }
    public static void playCheck()     { play(660, 120); }
    public static void playCheckmate() { play(110, 400); }
    public static void playInvalid()   { play(150, 80);  }

    /** Generates and plays a pure sine-wave tone asynchronously. */
    private static void play(int frequencyHz, int durationMs) {
        if (!enabled) return;
        executor.submit(() -> {
            try {
                float sampleRate = 44100f;
                int   numSamples = (int) (sampleRate * durationMs / 1000.0);
                byte[] buf       = new byte[numSamples * 2];

                for (int i = 0; i < numSamples; i++) {
                    double angle = 2.0 * Math.PI * frequencyHz * i / sampleRate;
                    // Apply a simple envelope to avoid clicks
                    double envelope = 1.0;
                    int fadeLen = Math.min(numSamples / 10, 500);
                    if (i < fadeLen)               envelope = (double) i / fadeLen;
                    else if (i > numSamples - fadeLen) envelope = (double) (numSamples - i) / fadeLen;

                    short sample = (short) (Short.MAX_VALUE * 0.4 * Math.sin(angle) * envelope);
                    buf[2 * i]     = (byte) (sample & 0xFF);
                    buf[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
                }

                AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
                try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
                    line.open(af);
                    line.start();
                    line.write(buf, 0, buf.length);
                    line.drain();
                }
            } catch (Exception ignored) {
                // Sound not available — silent fallback
            }
        });
    }
}
