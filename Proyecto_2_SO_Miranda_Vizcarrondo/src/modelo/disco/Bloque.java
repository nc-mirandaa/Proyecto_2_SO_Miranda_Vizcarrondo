/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.disco;

/**
 *
 * @author Nathaly
 */

/**
 * Representa un bloque del disco.
 * Usaremos asignación encadenada, así que cada bloque conoce al siguiente.
 */

public class Bloque {

    public static final int NULO = -1;

    private final int indice;       // posición en el arreglo del disco
    private boolean ocupado;
    private int siguiente;          // índice del siguiente bloque en la cadena

    public Bloque(int indice) {
        this.indice = indice;
        this.ocupado = false;
        this.siguiente = NULO;
    }

    public int getIndice() {
        return indice;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }

    public int getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(int siguiente) {
        this.siguiente = siguiente;
    }

    public void liberar() {
        this.ocupado = false;
        this.siguiente = NULO;
    }
}

