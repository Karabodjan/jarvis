package fr.karabodjan.jarvis.integration;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.OutputStream;

public class VoiceService {

    // (1) ExecutorService de 1 thread, daemon — não impede o encerramento da JVM
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "voice-thread");
        t.setDaemon(true);
        return t;
    });

    // (2) Deteção de OS feita uma só vez, na construção
    private final boolean isWindows;
    private final boolean isMac;

    public VoiceService() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        isWindows = os.contains("win");
        isMac     = os.contains("mac");
    }

    // (3) speak() submete o trabalho e devolve imediatamente
    public void speak(String text) {
        if (!isWindows && !isMac) return; // Linux: silêncio gracioso

        // (4) Escapar aspas simples ANTES de entrar na lambda — imutável e seguro
        String safe = text.replace("'", "''");

        executor.submit(() -> {
            try {
                Process p;
                if (isWindows) {
                    // (5) String[] evita tokenização errada de exec(String)
                    p = Runtime.getRuntime().exec(new String[]{
                            "powershell", "-NoProfile", "-Command",
                            "Add-Type -AssemblyName System.Speech; " +
                                    "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                    "$s.SelectVoice('Microsoft Zira Desktop'); " +
                                    "$s.Speak('" + safe + "')"
                    });
                } else {
                    p = Runtime.getRuntime().exec(new String[]{"say", "-v", "Fred", safe});
                }

                // (6) Consumir stdout e stderr para evitar deadlock de buffer
                p.getInputStream().transferTo(OutputStream.nullOutputStream());
                p.getErrorStream().transferTo(OutputStream.nullOutputStream());

                // (7) waitFor() bloqueia só a voice-thread, nunca a FX thread
                p.waitFor();

            } catch (Exception e) {
                // (8) Voz é não-crítica — logamos mas não rebentamos a app
                System.err.println("[VoiceService] Erro ao falar: " + e.getMessage());
            }
        });
    }

    // (9) Chamado pelo composition root no shutdown da app
    public void shutdown() {
        executor.shutdownNow();
    }
}