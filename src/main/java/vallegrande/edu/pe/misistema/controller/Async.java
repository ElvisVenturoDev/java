package vallegrande.edu.pe.misistema.controller;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Ejecuta el trabajo de base de datos fuera del hilo de la interfaz. */
public final class Async {
    private Async() {}

    public static <T> void run(Callable<T> trabajo, Consumer<T> alTerminar, Consumer<Throwable> alFallar) {
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return trabajo.call(); }
        };
        tarea.setOnSucceeded(e -> alTerminar.accept(tarea.getValue()));
        tarea.setOnFailed(e -> alFallar.accept(tarea.getException()));
        Thread hilo = new Thread(tarea, "db-worker");
        hilo.setDaemon(true);
        hilo.start();
    }
}
