/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.procesos;

import modelo.fs.ListaEnlazada;

/**
 *
 * @author Nathaly
 */

public class ColaSolicitudesES {

    private ListaEnlazada<SolicitudES> lista;

    public ColaSolicitudesES() {
        this.lista = new ListaEnlazada<>();
    }

    public void encolar(SolicitudES s) {
        if (s == null) return;
        lista.agregar(s);
    }

    public SolicitudES desencolarPrimero() {
        if (lista.estaVacia()) return null;
        SolicitudES primero = lista.get(0);
        lista.eliminar(primero);
        return primero;
    }

    public int size() {
        return lista.size();
    }

    public boolean estaVacia() {
        return lista.estaVacia();
    }

    public ListaEnlazada<SolicitudES> getLista() {
        return lista;
    }

    public void imprimirCola() {
        System.out.println("Cola de solicitudes E/S:");
        lista.forEach(s -> System.out.println("  " + s));
    }
}

