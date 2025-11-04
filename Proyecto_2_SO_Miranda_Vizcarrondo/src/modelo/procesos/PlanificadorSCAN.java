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
 * Planificador de disco SCAN (elevador).
 */

public class PlanificadorSCAN implements PlanificadorDisco {

    private boolean direccionArriba = true; // true = hacia posiciones mayores

    @Override
    public SolicitudES seleccionarSiguiente(ColaSolicitudesES cola, int posicionCabezal) {
        if (cola.estaVacia()) {
            return null;
        }

        ListaEnlazada<SolicitudES> lista = cola.getLista();

        SolicitudES mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;

        int n = lista.size();

        // 1) Intentamos encontrar en la dirección actual
        for (int i = 0; i < n; i++) {
            SolicitudES s = lista.get(i);
            int pos = s.getPosicion();
            int dist = Math.abs(pos - posicionCabezal);

            if (direccionArriba) {
                // solo consideramos solicitudes con pos >= cabezal
                if (pos >= posicionCabezal && dist < mejorDistancia) {
                    mejorDistancia = dist;
                    mejor = s;
                }
            } else {
                // solo consideramos solicitudes con pos <= cabezal
                if (pos <= posicionCabezal && dist < mejorDistancia) {
                    mejorDistancia = dist;
                    mejor = s;
                }
            }
        }

        // 2) Si no encontramos ninguna en esa dirección, cambiamos de sentido
        if (mejor == null) {
            direccionArriba = !direccionArriba;
            // Volvemos a buscar, ahora en la nueva dirección
            for (int i = 0; i < n; i++) {
                SolicitudES s = lista.get(i);
                int pos = s.getPosicion();
                int dist = Math.abs(pos - posicionCabezal);

                if (direccionArriba) {
                    if (pos >= posicionCabezal && dist < mejorDistancia) {
                        mejorDistancia = dist;
                        mejor = s;
                    }
                } else {
                    if (pos <= posicionCabezal && dist < mejorDistancia) {
                        mejorDistancia = dist;
                        mejor = s;
                    }
                }
            }
        }

        if (mejor != null) {
            lista.eliminar(mejor);
        }

        return mejor;
    }
}

