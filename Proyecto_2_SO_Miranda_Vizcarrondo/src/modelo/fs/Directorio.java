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
 * Representa un directorio en el sistema de archivos.
 */

public class Directorio extends NodoFS {

    private ListaEnlazada<NodoFS> hijos;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.hijos = new ListaEnlazada<>();
    }

    @Override
    public boolean esDirectorio() {
        return true;
    }

    public void agregarHijo(NodoFS hijo) {
        hijos.agregar(hijo);
    }

    public boolean eliminarHijo(NodoFS hijo) {
        return hijos.eliminar(hijo);
    }

    public ListaEnlazada<NodoFS> getHijos() {
        return hijos;
    }

    public NodoFS buscarHijoPorNombre(String nombreBuscado) {
        final NodoFS[] encontrado = { null };
        hijos.forEach(hijo -> {
            if (hijo.getNombre().equals(nombreBuscado)) {
                encontrado[0] = hijo;
            }
        });
        return encontrado[0];
    }
}

