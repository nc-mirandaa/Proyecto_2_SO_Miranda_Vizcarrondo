/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import modelo.fs.SistemaArchivos;
import modelo.fs.Directorio;
import modelo.fs.Archivo;
import modelo.fs.NodoFS;
import modelo.procesos.*;

/**
 *
 * @author Nathaly
 */

/**
 * Orquesta procesos, solicitudes de E/S, planificación de disco
 * y operaciones sobre el Sistema de Archivos.
 */

public class GestorSistema {

    private SistemaArchivos sistemaArchivos;
    private ColaProcesos colaProcesos;
    private ColaSolicitudesES colaES;
    private PlanificadorDisco planificador;
    private int posicionCabezal;

    public GestorSistema(int bloquesDisco) {
        this.sistemaArchivos = new SistemaArchivos(bloquesDisco);
        this.colaProcesos = new ColaProcesos();
        this.colaES = new ColaSolicitudesES();
        this.planificador = new PlanificadorFIFO(); // de momento FIFO
        this.posicionCabezal = 0;
    }

    public SistemaArchivos getSistemaArchivos() {
        return sistemaArchivos;
    }

    public void setPlanificador(PlanificadorDisco planificador) {
        this.planificador = planificador;
    }

    // =========================================================
    // API de alto nivel: aquí "el usuario" pide operaciones
    // =========================================================

    /**
     * Crea un proceso para CREAR_ARCHIVO y lo mete a la cola.
     * Ejemplo ruta: "/root/Documentos/tarea1.txt"
     */
    public void solicitarCrearArchivo(String rutaCompleta, int tamanoEnBloques) {
        Proceso p = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, rutaCompleta, tamanoEnBloques);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
    }

    /**
     * Crea un proceso para ELIMINAR_ARCHIVO y lo mete a la cola.
     */
    public void solicitarEliminarArchivo(String rutaCompleta) {
        Proceso p = new Proceso(TipoOperacionFS.ELIMINAR_ARCHIVO, rutaCompleta, 0);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
    }

    // Más adelante: CREAR_DIRECTORIO, ELIMINAR_DIRECTORIO, RENOMBRAR...

    // =========================================================
    // Simulación de un "paso" del sistema
    // =========================================================

    /**
     * Simula un paso:
     * - Saca un proceso de la cola.
     * - Genera solicitud de E/S.
     * - Planificador elige cuál atender.
     * - Ejecuta la operación sobre el FS.
     */
    public void ejecutarPaso() {
        if (colaProcesos.estaVacia()) {
            System.out.println("No hay procesos en la cola.");
            return;
        }

        Proceso p = colaProcesos.desencolar();
        if (p == null) return;

        System.out.println("\n=== Ejecutando " + p + " ===");

        p.setEstado(EstadoProceso.LISTO);

        // 1) Generar una solicitud de E/S (simplificada)
        // Luego, si quieres, aquí puedes hacer algo más realista
        int posicionObjetivo = 0; // por ahora fijo
        SolicitudES sol = new SolicitudES(p, posicionObjetivo);
        colaES.encolar(sol);

        // 2) Planificador elige la siguiente solicitud
        SolicitudES siguiente = planificador.seleccionarSiguiente(colaES, posicionCabezal);
        if (siguiente == null) {
            System.out.println("No hay solicitudes de E/S.");
            return;
        }

        System.out.println("Planificador seleccionó: " + siguiente);
        p.setEstado(EstadoProceso.EJECUTANDO);

        // Movemos el "cabezal" virtual
        posicionCabezal = siguiente.getPosicion();

        // 3) Ejecutar operación de FS asociada al proceso
        ejecutarOperacionFS(p);

        p.setEstado(EstadoProceso.TERMINADO);
        System.out.println("Proceso " + p.getId() + " TERMINADO.");
    }

    // =========================================================
    // Ejecución de las operaciones de FS
    // =========================================================

    private void ejecutarOperacionFS(Proceso p) {
        switch (p.getTipoOperacion()) {
            case CREAR_ARCHIVO:
                ejecutarCrearArchivo(p);
                break;
            case ELIMINAR_ARCHIVO:
                ejecutarEliminarArchivo(p);
                break;
            default:
                System.out.println("Operación no implementada aún: " + p.getTipoOperacion());
        }
    }

    private void ejecutarCrearArchivo(Proceso p) {
        String ruta = p.getRutaObjetivo();
        int tamBloques = p.getTamanoEnBloques();

        String rutaDir = obtenerRutaDirectorio(ruta);
        String nombreArchivo = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaDir);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado para ruta: " + rutaDir);
            return;
        }

        Archivo archivo = sistemaArchivos.crearArchivo(dirPadre, nombreArchivo, tamBloques);
        if (archivo == null) {
            System.out.println("No se pudo crear el archivo (sin espacio en disco).");
        } else {
            System.out.println("Archivo creado: " + archivo.getNombre() +
                    " en " + dirPadre.getRutaCompleta() +
                    " (primerBloque=" + archivo.getPrimerBloque() + ")");
        }
    }

    private void ejecutarEliminarArchivo(Proceso p) {
        String ruta = p.getRutaObjetivo();
        String rutaDir = obtenerRutaDirectorio(ruta);
        String nombreArchivo = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaDir);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado para ruta: " + rutaDir);
            return;
        }

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreArchivo);
        if (nodo == null || nodo.esDirectorio()) {
            System.out.println("Archivo no encontrado o es un directorio: " + nombreArchivo);
            return;
        }

        Archivo archivo = (Archivo) nodo;
        boolean ok = sistemaArchivos.eliminarArchivo(dirPadre, archivo);
        System.out.println(ok
                ? "Archivo eliminado: " + ruta
                : "No se pudo eliminar el archivo: " + ruta);
    }

    // =========================================================
    // Utilidades para manejar rutas tipo "/root/Documentos/a.txt"
    // =========================================================

    /**
     * Busca un directorio por ruta absoluta, p.ej. "/root/Documentos/Sub".
     */
    private Directorio buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || "/".equals(ruta)) {
            return sistemaArchivos.getRaiz();
        }

        String limpia = ruta.trim();
        if (limpia.startsWith("/")) {
            limpia = limpia.substring(1);
        }

        String[] partes = limpia.split("/");

        Directorio actual = sistemaArchivos.getRaiz();
        int i = 0;
        if (partes.length > 0 && partes[0].equals(actual.getNombre())) {
            i = 1; // saltamos "root"
        }

        for (; i < partes.length; i++) {
            String nombreDir = partes[i];
            NodoFS hijo = actual.buscarHijoPorNombre(nombreDir);
            if (hijo == null || !hijo.esDirectorio()) {
                return null;
            }
            actual = (Directorio) hijo;
        }
        return actual;
    }

    private String obtenerRutaDirectorio(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx <= 0) {
            return "/root";
        }
        return rutaCompleta.substring(0, idx);
    }

    private String obtenerNombreFinal(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx == -1 || idx == rutaCompleta.length() - 1) {
            return rutaCompleta;
        }
        return rutaCompleta.substring(idx + 1);
    }
}
