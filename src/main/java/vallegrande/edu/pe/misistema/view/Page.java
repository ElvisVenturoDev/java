package vallegrande.edu.pe.misistema.view;

import javafx.scene.Node;

/** Un apartado del menú lateral. */
public interface Page {
    Node getView();
    /** Se llama cada vez que el apartado se muestra, para traer datos frescos. */
    void cargar();
}
