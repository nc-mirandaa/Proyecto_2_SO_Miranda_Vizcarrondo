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
 * Representa un archivo en el sistema de archivos.
 * Más adelante se conectará con el disco (bloques).
 */

public class Archivo extends NodoFS {

    private static int contadorIds = 1;

    private final int id;
    private int tamanoEnBloques;

    // índice del primer bloque en el disco (asignación encadenada)
    private int primerBloque = -1;

    public Archivo(String nombre, Directorio padre, int tamanoEnBloques) {
        super(nombre, padre);
        this.tamanoEnBloques = tamanoEnBloques;
        this.id = contadorIds++;
    }

    @Override
    public boolean esDirectorio() {
        return false;
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    public void setTamanoEnBloques(int tamanoEnBloques) {
        this.tamanoEnBloques = tamanoEnBloques;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }

    public void setPrimerBloque(int primerBloque) {
        this.primerBloque = primerBloque;
    }

    public int getId() {
        return id;
    }
}
