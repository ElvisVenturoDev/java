package vallegrande.edu.pe.controller;

import vallegrande.edu.pe.model.Producto;

public final class PoliticaInventario {

    public static final int STOCK_BAJO = 40;
    public static final int STOCK_EXCESO = 80;
    public static final double DESCUENTO = 0.15;

    private PoliticaInventario() {}

    public static String estado(int stock) {
        if (stock == 0) return "Agotado";
        if (stock < STOCK_BAJO) return "Stock bajo";
        return "En stock";
    }

    public static boolean aDescuento(Producto p) {
        return p.stock() >= STOCK_EXCESO;
    }

    public static double precioFinal(Producto p) {
        return aDescuento(p) ? p.precio() * (1 - DESCUENTO) : p.precio();
    }
}