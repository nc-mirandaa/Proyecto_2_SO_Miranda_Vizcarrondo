/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.fs;

/**
 *
 * @author Nathaly
 */

/**
 * Maneja la estructura de directorios y archivos.
 */

public class SistemaArchivos {

    private Directorio raiz;

    public SistemaArchivos() {
        // La raíz se llamará "root"
        this.raiz = new Directorio("root", null);
    }

    public Directorio getRaiz() {
        return raiz;
    }

    /**
     * Crea un subdirectorio dentro de un directorio padre dado.
     */
    public Directorio crearDirectorio(Directorio padre, String nombre) {
        if (padre == null) {
            throw new IllegalArgumentException("El directorio padre no puede ser null");
        }
        Directorio nuevo = new Directorio(nombre, padre);
        padre.agregarHijo(nuevo);
        return nuevo;
    }

    /**
     * Crea un archivo dentro de un directorio padre dado.
     */
    public Archivo crearArchivo(Directorio padre, String nombre, int tamanoEnBloques) {
        if (padre == null) {
            throw new IllegalArgumentException("El directorio padre no puede ser null");
        }
        Archivo archivo = new Archivo(nombre, padre, tamanoEnBloques);
        padre.agregarHijo(archivo);
        return archivo;
    }

    /**
     * Elimina un nodo (archivo o directorio) del sistema.
     * OJO: si es directorio, no hace borrado recursivo todavía.
     */
    public boolean eliminarNodo(Directorio padre, NodoFS nodo) {
        if (padre == null || nodo == null) {
            return false;
        }
        return padre.eliminarHijo(nodo);
    }

    /**
     * Imprime en consola la estructura del sistema de archivos.
     * Esto nos ayuda a probar rápidamente.
     */
    public void imprimirEstructura() {
        imprimirDirectorio(raiz, 0);
    }

    private void imprimirDirectorio(Directorio dir, int nivel) {
        imprimirIndentacion(nivel);
        System.out.println("[DIR] " + dir.getNombre());

        dir.getHijos().forEach(hijo -> {
            if (hijo.esDirectorio()) {
                imprimirDirectorio((Directorio) hijo, nivel + 1);
            } else {
                imprimirIndentacion(nivel + 1);
                Archivo archivo = (Archivo) hijo;
                System.out.println("[FILE] " + archivo.getNombre() +
                        " (" + archivo.getTamanoEnBloques() + " bloques)");
            }
        });
    }

    private void imprimirIndentacion(int nivel) {
        for (int i = 0; i < nivel; i++) {
            System.out.print("  ");
        }
    }
}

