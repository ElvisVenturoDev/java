package vallegrande.edu.pe.misistema.controller;

import vallegrande.edu.pe.misistema.model.Resumen;
import vallegrande.edu.pe.misistema.model.ResumenDAO;

import java.sql.SQLException;

public class InicioController {
    private final ResumenDAO dao = new ResumenDAO();

    public Resumen resumen() throws SQLException { return dao.obtener(); }
}
