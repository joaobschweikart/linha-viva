package br.unoesc.linhaviva.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AppExecutors {

    private static final AppExecutors INSTANCIA = new AppExecutors();

    private final ExecutorService io = Executors.newFixedThreadPool(3);
    private final Executor principal = new Handler(Looper.getMainLooper())::post;

    private AppExecutors() {
    }

    public static AppExecutors get() {
        return INSTANCIA;
    }

    public ExecutorService io() {
        return io;
    }

    public Executor principal() {
        return principal;
    }
}
