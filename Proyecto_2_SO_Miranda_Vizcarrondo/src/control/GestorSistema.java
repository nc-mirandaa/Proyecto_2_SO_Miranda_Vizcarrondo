/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import modelo.fs.*;
import modelo.procesos.*;

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

    private RolUsuario rolActual = RolUsuario.ADMIN; // por defecto admin

    public GestorSistema(int bloquesDisco) {
        this.sistemaArchivos = new SistemaArchivos(bloquesDisco);
        this.colaProcesos = new ColaProcesos();
        this.colaES = new ColaSolicitudesES();
        this.planificador = new PlanificadorFIFO();
        this.posicionCabezal = 0;
    }

    // Permitir reemplazar el FS (para cargar desde persistencia)
    public void setSistemaArchivos(SistemaArchivos fs) {
        this.sistemaArchivos = fs;
    }

    public SistemaArchivos getSistemaArchivos() {
        return sistemaArchivos;
    }

    public void setPlanificador(PlanificadorDisco planificador) {
        this.planificador = planificador;
    }

    public void setRolActual(RolUsuario rol) {
        this.rolActual = rol;
    }

    public RolUsuario getRolActual() {
        return rolActual;
    }

    // =========================================================
    // API de alto nivel: solicitudes
    // =========================================================

    public void solicitarCrearArchivo(String rutaCompleta, int tamanoEnBloques) {
        if (!tienePermiso()) return;
        Proceso p = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, rutaCompleta, tamanoEnBloques);
        colaProcesos.encolar(p);
    }

    public void solicitarEliminarArchivo(String rutaCompleta) {
        if (!tienePermiso()) return;
        Proceso p = new Proceso(TipoOperacionFS.ELIMINAR_ARCHIVO, rutaCompleta, 0);
        colaProcesos.encolar(p);
    }

    public void solicitarCrearDirectorio(String rutaCompleta) {
        if (!tienePermiso()) return;
        Proceso p = new Proceso(TipoOperacionFS.CREAR_DIRECTORIO, rutaCompleta, 0);
        colaProcesos.encolar(p);
    }

    public void solicitarEliminarDirectorio(String rutaCompleta) {
        if (!tienePermiso()) return;
        Proceso p = new Proceso(TipoOperacionFS.ELIMINAR_DIRECTORIO, rutaCompleta, 0);
        colaProcesos.encolar(p);
    }

    public void solicitarRenombrar(String rutaCompleta, String nuevoNombre) {
        if (!tienePermiso()) return;
        Proceso p = new Proceso(TipoOperacionFS.RENOMBRAR, rutaCompleta, 0);
        p.setNuevoNombre(nuevoNombre);
        colaProcesos.encolar(p);
    }

    private boolean tienePermiso() {
        if (rolActual != RolUsuario.ADMIN) {
            System.out.println("❌ Permiso denegado. Solo el ADMIN puede realizar esta operación.");
            return false;
        }
        return true;
    }

    // =========================================================
    // Ejecución de operaciones simuladas
    // =========================================================

    public void ejecutarPaso() {
        if (colaProcesos.estaVacia()) {
            System.out.println("No hay procesos pendientes.");
            return;
        }

        Proceso p = colaProcesos.desencolar();
        System.out.println("\n=== Ejecutando " + p + " ===");

        SolicitudES sol = new SolicitudES(p, 0);
        colaES.encolar(sol);

        SolicitudES siguiente = planificador.seleccionarSiguiente(colaES, posicionCabezal);
        if (siguiente == null) return;

        posicionCabezal = siguiente.getPosicion();
        ejecutarOperacionFS(p);
    }

    private void ejecutarOperacionFS(Proceso p) {
        switch (p.getTipoOperacion()) {
            case CREAR_ARCHIVO -> ejecutarCrearArchivo(p);
            case ELIMINAR_ARCHIVO -> ejecutarEliminarArchivo(p);
            case CREAR_DIRECTORIO -> ejecutarCrearDirectorio(p);
            case ELIMINAR_DIRECTORIO -> ejecutarEliminarDirectorio(p);
            case RENOMBRAR -> ejecutarRenombrar(p);
            default -> System.out.println("Operación no implementada: " + p.getTipoOperacion());
        }
    }

    private void ejecutarCrearArchivo(Proceso p) {
        String ruta = p.getRutaObjetivo();
        int tamBloques = p.getTamanoEnBloques();
        String rutaDir = obtenerRutaDirectorio(ruta);
        String nombreArchivo = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaDir);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado.");
            return;
        }

        Archivo archivo = sistemaArchivos.crearArchivo(dirPadre, nombreArchivo, tamBloques);
        if (archivo != null)
            System.out.println("Archivo creado correctamente.");
    }

    private void ejecutarEliminarArchivo(Proceso p) {
        String ruta = p.getRutaObjetivo();
        String rutaDir = obtenerRutaDirectorio(ruta);
        String nombreArchivo = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaDir);
        if (dirPadre == null) return;

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreArchivo);
        if (nodo == null || nodo.esDirectorio()) {
            System.out.println("Archivo no encontrado o es directorio.");
            return;
        }

        sistemaArchivos.eliminarArchivo(dirPadre, (Archivo) nodo);
        System.out.println("Archivo eliminado correctamente.");
    }

    private void ejecutarCrearDirectorio(Proceso p) {
        String ruta = p.getRutaObjetivo();
        String rutaPadre = obtenerRutaDirectorio(ruta);
        String nombreDir = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaPadre);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado.");
            return;
        }

        sistemaArchivos.crearDirectorio(dirPadre, nombreDir);
        System.out.println("Directorio creado correctamente.");
    }

    private void ejecutarEliminarDirectorio(Proceso p) {
        String ruta = p.getRutaObjetivo();
        String rutaPadre = obtenerRutaDirectorio(ruta);
        String nombreDir = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaPadre);
        if (dirPadre == null) return;

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreDir);
        if (nodo == null || !nodo.esDirectorio()) {
            System.out.println("Directorio no encontrado.");
            return;
        }

        sistemaArchivos.eliminarDirectorioRecursivo(dirPadre, (Directorio) nodo);
        System.out.println("Directorio eliminado recursivamente.");
    }

    private void ejecutarRenombrar(Proceso p) {
        String ruta = p.getRutaObjetivo();
        String nuevoNombre = p.getNuevoNombre();
        String rutaPadre = obtenerRutaDirectorio(ruta);
        String nombreViejo = obtenerNombreFinal(ruta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaPadre);
        if (dirPadre == null) return;

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreViejo);
        if (nodo == null) {
            System.out.println("Elemento no encontrado.");
            return;
        }

        sistemaArchivos.renombrarNodo(nodo, nuevoNombre);
        System.out.println("Elemento renombrado correctamente.");
    }

    // =========================================================
    // Utilidades de ruta
    // =========================================================
    private Directorio buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || "/".equals(ruta)) {
            return sistemaArchivos.getRaiz();
        }

        String limpia = ruta.startsWith("/") ? ruta.substring(1) : ruta;
        String[] partes = limpia.split("/");

        Directorio actual = sistemaArchivos.getRaiz();
        int i = partes[0].equals(actual.getNombre()) ? 1 : 0;

        for (; i < partes.length; i++) {
            NodoFS hijo = actual.buscarHijoPorNombre(partes[i]);
            if (hijo == null || !hijo.esDirectorio()) return null;
            actual = (Directorio) hijo;
        }
        return actual;
    }

    private String obtenerRutaDirectorio(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx <= 0) return "/root";
        return rutaCompleta.substring(0, idx);
    }

    private String obtenerNombreFinal(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        return (idx == -1) ? rutaCompleta : rutaCompleta.substring(idx + 1);
    }
}
