package vallegrande.edu.pe.misistema.model;

/** Totales del apartado Inicio. Un valor -1 significa que no se pudo contar. */
public record Resumen(int productos, int stockBajo, int clientes, int pedidos, int mensajes) {}
