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

public class ColaProcesos {

    private ListaEnlazada<Proceso> lista;

    public ColaProcesos() {
        this.lista = new ListaEnlazada<>();
    }

    public void encolar(Proceso p) {
        if (p == null) return;
        lista.agregar(p);
    }

    public Proceso desencolar() {
        if (lista.estaVacia()) return null;
        Proceso primero = lista.get(0);
        lista.eliminar(primero);
        return primero;
    }

    public boolean estaVacia() {
        return lista.estaVacia();
    }

    public int size() {
        return lista.size();
    }

    public void imprimirCola() {
        System.out.println("Cola de procesos:");
        lista.forEach(p -> System.out.println("  " + p));
    }
}

