/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.procesos;

/**
 *
 * @author Nathaly
 */

public class SolicitudES {

    private Proceso proceso;
    private int posicion; // número de bloque

    public SolicitudES(Proceso proceso, int posicion) {
        this.proceso = proceso;
        this.posicion = posicion;
    }

    public Proceso getProceso() {
        return proceso;
    }

    public int getPosicion() {
        return posicion;
    }

    @Override
    public String toString() {
        return "SolicitudES{P" + proceso.getId() + ", pos=" + posicion + "}";
    }
}

