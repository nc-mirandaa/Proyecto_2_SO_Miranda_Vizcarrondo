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

/**
 * Planificador de disco SSTF (Shortest Seek Time First).
 * Selecciona la solicitud cuya posición está más cerca del cabezal actual.
 */

public class PlanificadorSSTF implements PlanificadorDisco {

    @Override
    public SolicitudES seleccionarSiguiente(ColaSolicitudesES cola, int posicionCabezal) {
        if (cola.estaVacia()) {
            return null;
        }

        ListaEnlazada<SolicitudES> lista = cola.getLista();

        SolicitudES mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;

        int n = lista.size();
        for (int i = 0; i < n; i++) {
            SolicitudES s = lista.get(i);
            int dist = Math.abs(s.getPosicion() - posicionCabezal);
            if (dist < mejorDistancia) {
                mejorDistancia = dist;
                mejor = s;
            }
        }

        if (mejor != null) {
            // Sacamos esa solicitud de la cola
            lista.eliminar(mejor);
        }

        return mejor;
    }
}

