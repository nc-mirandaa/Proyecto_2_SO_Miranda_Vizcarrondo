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
 * Representa una entrada en la tabla de asignación de archivos.
 */

public class EntradaTablaAsignacion {

    private int idArchivo;
    private String nombreArchivo;
    private int tamanoEnBloques;
    private int primerBloque;

    public EntradaTablaAsignacion(int idArchivo, String nombreArchivo,
                                  int tamanoEnBloques, int primerBloque) {
        this.idArchivo = idArchivo;
        this.nombreArchivo = nombreArchivo;
        this.tamanoEnBloques = tamanoEnBloques;
        this.primerBloque = primerBloque;
    }

    public int getIdArchivo() {
        return idArchivo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public void setTamanoEnBloques(int tamanoEnBloques) {
        this.tamanoEnBloques = tamanoEnBloques;
    }

    public void setPrimerBloque(int primerBloque) {
        this.primerBloque = primerBloque;
    }
}

