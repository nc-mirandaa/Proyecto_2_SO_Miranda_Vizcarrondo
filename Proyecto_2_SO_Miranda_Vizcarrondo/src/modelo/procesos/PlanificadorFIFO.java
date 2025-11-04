/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.procesos;

/**
 *
 * @author Nathaly
 */

public class PlanificadorFIFO implements PlanificadorDisco {

    @Override
    public SolicitudES seleccionarSiguiente(ColaSolicitudesES cola, int posicionCabezal) {
        return cola.desencolarPrimero();
    }
}

