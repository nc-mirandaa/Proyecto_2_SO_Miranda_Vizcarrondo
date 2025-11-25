/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.procesos;

import modelo.fs.ListaEnlazada;

/**
 * Cola FIFO de procesos.
 * Mantiene el orden de llegada de los procesos y permite
 * encolar, desencolar y consultar el contenido.
 * 
 * @author Nathaly
 */
public class ColaProcesos {

    private final ListaEnlazada<Proceso> lista;

    public ColaProcesos() {
        this.lista = new ListaEnlazada<>();
    }

    /**
     * Inserta un proceso al final de la cola.
     */
    public void encolar(Proceso p) {
        if (p != null) {
            lista.agregar(p);
        }
    }

    /**
     * Retira y devuelve el primer proceso de la cola.
     * 
     * @return el primer proceso o null si está vacía.
     */
    public Proceso desencolar() {
        if (lista.estaVacia()) {
            return null;
        }

        Proceso primero = lista.get(0);
        lista.eliminar(primero);
        return primero;
    }

    /**
     * Indica si la cola no tiene procesos.
     */
    public boolean estaVacia() {
        return lista.estaVacia();
    }

    /**
     * Cantidad de procesos en la cola.
     */
    public int size() {
        return lista.size();
    }

    /**
     * Imprime la cola actual en consola.
     */
    public void imprimirCola() {
        System.out.println("Cola de procesos:");
        lista.forEach(p -> System.out.println("  " + p));
    }

    /**
     * Devuelve la lista interna (solo lectura recomendada).
     */
    public ListaEnlazada<Proceso> getLista() {
        return lista;
    }
}


