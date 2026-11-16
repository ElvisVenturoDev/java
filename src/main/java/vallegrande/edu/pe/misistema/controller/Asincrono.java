package vallegrande.edu.pe.misistema.controller;

import javafx.concurrent.Task;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/** Ejecuta el trabajo de base de datos fuera del hilo de la interfaz. */
public final class Asincrono {
    private Asincrono() {}

    public static <T> void ejecutar(Callable<T> trabajo, Consumer<T> alCompletar, Consumer<Throwable> alError) {
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return trabajo.call(); }
        };
        tarea.setOnSucceeded(e -> alCompletar.accept(tarea.getValue()));
        tarea.setOnFailed(e -> alError.accept(tarea.getException()));
        Thread hilo = new Thread(tarea, "hilo-bd");
        hilo.setDaemon(true);
        hilo.start();
    }
}
