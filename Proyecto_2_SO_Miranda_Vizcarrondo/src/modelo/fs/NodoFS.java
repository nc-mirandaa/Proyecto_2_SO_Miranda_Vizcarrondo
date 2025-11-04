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
 * Nodo genérico del sistema de archivos.
 * Puede ser un Archivo o un Directorio.
 */

public abstract class NodoFS {

    protected String nombre;
    protected Directorio padre;

    public NodoFS(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nuevoNombre) {
        this.nombre = nuevoNombre;
    }

    public Directorio getPadre() {
        return padre;
    }

    public String getRutaCompleta() {
        if (padre == null) {
            return "/" + nombre; // raíz o nodo sin padre
        }
        return padre.getRutaCompleta() + "/" + nombre;
    }

    // Para saber si es archivo o directorio desde el código
    public abstract boolean esDirectorio();
}

