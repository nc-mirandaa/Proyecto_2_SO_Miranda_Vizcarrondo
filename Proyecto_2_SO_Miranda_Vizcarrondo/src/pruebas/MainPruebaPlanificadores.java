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

public class MainPruebaPlanificadores {

    public static void main(String[] args) {

        // Misma cola de solicitudes para probar los distintos algoritmos
        ColaSolicitudesES cola = new ColaSolicitudesES();

        Proceso p1 = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, "/root/a.txt", 3);
        Proceso p2 = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, "/root/b.txt", 3);
        Proceso p3 = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, "/root/c.txt", 3);
        Proceso p4 = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, "/root/d.txt", 3);

        // Posiciones de ejemplo
        cola.encolar(new SolicitudES(p1, 10));
        cola.encolar(new SolicitudES(p2, 4));
        cola.encolar(new SolicitudES(p3, 15));
        cola.encolar(new SolicitudES(p4, 1));

        probarConPlanificador("FIFO", new PlanificadorFIFO(), cola, 8);
        probarConPlanificador("SSTF", new PlanificadorSSTF(), cola, 8);
        probarConPlanificador("SCAN", new PlanificadorSCAN(), cola, 8);
        probarConPlanificador("CSCAN", new PlanificadorCSCAN(), cola, 8);
    }

    private static void probarConPlanificador(String nombre, PlanificadorDisco planificador,
                                              ColaSolicitudesES colaOriginal, int posInicial) {

        System.out.println("\n==== Probando " + nombre + " ====");

        // Clon simplón: volvemos a crear la cola
        ColaSolicitudesES cola = new ColaSolicitudesES();
        colaOriginal.getLista().forEach(s -> {
            cola.encolar(new SolicitudES(s.getProceso(), s.getPosicion()));
        });

        int pos = posInicial;
        while (!cola.estaVacia()) {
            SolicitudES s = planificador.seleccionarSiguiente(cola, pos);
            if (s == null) break;
            System.out.println("Cabezal en " + pos + " -> atiende " + s.getPosicion());
            pos = s.getPosicion();
        }
    }
}

