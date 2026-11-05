package vallegrande.edu.pe.misistema.model;

public final class Sesion {
    private static Usuario actual;

    private Sesion() {}

    public static void iniciar(Usuario u) { actual = u; }
    public static void cerrar()           { actual = null; }
    public static Usuario getActual()     { return actual; }
    public static boolean activa()        { return actual != null; }
}