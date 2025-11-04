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
 * Simula un disco con un número fijo de bloques.
 * Implementa asignación encadenada.
 */

public class Disco {

    private Bloque[] bloques;

    public Disco(int cantidadBloques) {
        if (cantidadBloques <= 0) {
            throw new IllegalArgumentException("La cantidad de bloques debe ser positiva");
        }
        bloques = new Bloque[cantidadBloques];
        for (int i = 0; i < cantidadBloques; i++) {
            bloques[i] = new Bloque(i);
        }
    }

    public int getCantidadBloques() {
        return bloques.length;
    }

    public Bloque getBloque(int indice) {
        if (indice < 0 || indice >= bloques.length) {
            throw new IndexOutOfBoundsException("Índice de bloque inválido: " + indice);
        }
        return bloques[indice];
    }

    /**
     * Asigna 'cantidad' bloques libres usando asignación encadenada.
     * @return índice del primer bloque de la cadena, o -1 si no hay suficiente espacio.
     */
    public int asignarBloques(int cantidad) {
        if (cantidad <= 0) {
            return Bloque.NULO;
        }

        // Primero verificamos si hay suficientes bloques libres
        int libres = contarBloquesLibres();
        if (libres < cantidad) {
            return Bloque.NULO;
        }

        int primero = Bloque.NULO;
        int anterior = Bloque.NULO;
        int asignados = 0;

        // Recorremos el disco buscando bloques libres
        for (int i = 0; i < bloques.length && asignados < cantidad; i++) {
            Bloque b = bloques[i];
            if (!b.isOcupado()) {
                b.setOcupado(true);
                b.setSiguiente(Bloque.NULO); // lo marcamos inicialmente como último

                if (primero == Bloque.NULO) {
                    primero = i;
                }

                if (anterior != Bloque.NULO) {
                    bloques[anterior].setSiguiente(i);
                }

                anterior = i;
                asignados++;
            }
        }

        return primero;
    }

    /**
     * Libera todos los bloques encadenados a partir de 'primerBloque'.
     */
    public void liberarCadenaBloques(int primerBloque) {
        int actual = primerBloque;
        while (actual != Bloque.NULO) {
            Bloque b = bloques[actual];
            int siguiente = b.getSiguiente();
            b.liberar();
            actual = siguiente;
        }
    }

    public int contarBloquesLibres() {
        int libres = 0;
        for (int i = 0; i < bloques.length; i++) {
            if (!bloques[i].isOcupado()) {
                libres++;
            }
        }
        return libres;
    }

    /**
     * Devuelve un arreglo booleano indicando qué bloques están ocupados.
     * Esto luego nos servirá para dibujar el disco.
     */
    public boolean[] mapaOcupacion() {
        boolean[] mapa = new boolean[bloques.length];
        for (int i = 0; i < bloques.length; i++) {
            mapa[i] = bloques[i].isOcupado();
        }
        return mapa;
    }
}

