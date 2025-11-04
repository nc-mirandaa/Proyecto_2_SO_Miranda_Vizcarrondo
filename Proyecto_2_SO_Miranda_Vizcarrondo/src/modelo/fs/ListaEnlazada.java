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
 * Implementación sencilla de una lista enlazada genérica.
 * No usa ArrayList ni ninguna colección de java.util.
 */

public class ListaEnlazada<T> {

    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> cabeza;
    private int size;

    public void agregar(T elemento) {
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo;
        }
        size++;
    }

    public boolean eliminar(T elemento) {
        if (cabeza == null) return false;

        // si está en la cabeza
        if (cabeza.dato.equals(elemento)) {
            cabeza = cabeza.siguiente;
            size--;
            return true;
        }

        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.equals(elemento)) {
                actual.siguiente = actual.siguiente.siguiente;
                size--;
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + index);
        }
        Nodo<T> actual = cabeza;
        int i = 0;
        while (i < index) {
            actual = actual.siguiente;
            i++;
        }
        return actual.dato;
    }

    public boolean estaVacia() {
        return size == 0;
    }

    /**
     * Recorre la lista con una función "consumidora"
     */
    public void forEach(Visitor<T> visitor) {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            visitor.visitar(actual.dato);
            actual = actual.siguiente;
        }
    }

    public interface Visitor<T> {
        void visitar(T elemento);
    }
}

