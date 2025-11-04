/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.procesos;

/**
 *
 * @author Nathaly
 */

public interface PlanificadorDisco {

    SolicitudES seleccionarSiguiente(ColaSolicitudesES cola, int posicionCabezal);
}

