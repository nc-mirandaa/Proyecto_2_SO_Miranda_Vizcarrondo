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
 * Planificador de disco C-SCAN (Circular SCAN).
 */

public class PlanificadorCSCAN implements PlanificadorDisco {

    @Override
    public SolicitudES seleccionarSiguiente(ColaSolicitudesES cola, int posicionCabezal) {
        if (cola.estaVacia()) {
            return null;
        }

        ListaEnlazada<SolicitudES> lista = cola.getLista();

        SolicitudES mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;

        int n = lista.size();

        // 1) Buscar solicitudes con pos >= cabezal (hacia "arriba")
        for (int i = 0; i < n; i++) {
            SolicitudES s = lista.get(i);
            int pos = s.getPosicion();
            if (pos >= posicionCabezal) {
                int dist = pos - posicionCabezal; // solo hacia arriba
                if (dist < mejorDistancia) {
                    mejorDistancia = dist;
                    mejor = s;
                }
            }
        }

        // 2) Si no había ninguna hacia arriba, "damos la vuelta" y
        // elegimos la solicitud con menor posición (como si
        // saltáramos al inicio del disco).
        if (mejor == null) {
            int mejorPos = Integer.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                SolicitudES s = lista.get(i);
                int pos = s.getPosicion();
                if (pos < mejorPos) {
                    mejorPos = pos;
                    mejor = s;
                }
            }
        }

        if (mejor != null) {
            lista.eliminar(mejor);
        }

        return mejor;
    }
}

