/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

/**
 *
 * @author Nathaly
 */

import modelo.procesos.*;

public class MainPruebaProcesos {

    public static void main(String[] args) {

        ColaProcesos colaProcesos = new ColaProcesos();
        ColaSolicitudesES colaES = new ColaSolicitudesES();
        PlanificadorDisco planificador = new PlanificadorFIFO();

        Proceso p1 = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, "/root/a.txt", 3);
        Proceso p2 = new Proceso(TipoOperacionFS.ELIMINAR_ARCHIVO, "/root/b.txt", 0);
        Proceso p3 = new Proceso(TipoOperacionFS.CREAR_DIRECTORIO, "/root/Docs", 0);

        colaProcesos.encolar(p1);
        colaProcesos.encolar(p2);
        colaProcesos.encolar(p3);

        colaProcesos.imprimirCola();

        colaES.encolar(new SolicitudES(p1, 10));
        colaES.encolar(new SolicitudES(p2, 4));
        colaES.encolar(new SolicitudES(p3, 15));

        colaES.imprimirCola();

        int posCabezal = 0;
        System.out.println("\nAtendiendo solicitudes con FIFO:");
        while (!colaES.estaVacia()) {
            SolicitudES s = planificador.seleccionarSiguiente(colaES, posCabezal);
            if (s == null) break;
            System.out.println("Atendiendo " + s + " desde pos=" + posCabezal);
            posCabezal = s.getPosicion();
        }
    }
}

