/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import modelo.fs.*;
import modelo.procesos.*;
import modelo.fs.ListaEnlazada;

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

    // === ROLES ===
    private RolUsuario rolActual = RolUsuario.ADMIN;
    
    // Último error producido al ejecutar una operación de FS
    private String ultimoError;


    public GestorSistema(int bloquesDisco) {
        this.sistemaArchivos = new SistemaArchivos(bloquesDisco);
        this.colaProcesos = new ColaProcesos();
        this.colaES = new ColaSolicitudesES();
        this.planificador = new PlanificadorFIFO(); // default
        this.posicionCabezal = 0;
    }

    public SistemaArchivos getSistemaArchivos() { return sistemaArchivos; }
    public void setPlanificador(PlanificadorDisco planificador) { this.planificador = planificador; }

    public void setSistemaArchivos(SistemaArchivos sistemaArchivos) {
    this.sistemaArchivos = sistemaArchivos;
    }
    
    public String getUltimoError() {
    return ultimoError;
}
    
    public ListaEnlazada<Proceso> getProcesosEnCola() {
    return colaProcesos.getLista();
    }
    
    // === Roles ===
    public void setRolActual(RolUsuario rol) { this.rolActual = rol; }
    public RolUsuario getRolActual() { return this.rolActual; }

    // ======================== API de alto nivel ========================
    // Todos devuelven boolean:
    //  - true  -> se encoló el proceso
    //  - false -> se rechazó (por permisos o validación)

    // ARCHIVOS
    public boolean solicitarCrearArchivo(String rutaCompleta, int tamanoEnBloques) {
        // Permitido para ADMIN y USUARIO
        Proceso p = new Proceso(TipoOperacionFS.CREAR_ARCHIVO, rutaCompleta, tamanoEnBloques);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
        return true;
    }

    public boolean solicitarEliminarArchivo(String rutaCompleta) {
        // Solo ADMIN
        if (rolActual != RolUsuario.ADMIN) {
            System.out.println("Permiso denegado: solo ADMIN puede eliminar archivos.");
            return false;
        }
        Proceso p = new Proceso(TipoOperacionFS.ELIMINAR_ARCHIVO, rutaCompleta);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
        return true;
    }

    // DIRECTORIOS
    public boolean solicitarCrearDirectorio(String rutaCompletaDir) {
        // Permitido para ADMIN y USUARIO
        Proceso p = new Proceso(TipoOperacionFS.CREAR_DIRECTORIO, rutaCompletaDir);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
        return true;
    }

    public boolean solicitarEliminarDirectorio(String rutaCompletaDir) {
        // Solo ADMIN
        if (rolActual != RolUsuario.ADMIN) {
            System.out.println("Permiso denegado: solo ADMIN puede eliminar directorios.");
            return false;
        }
        if (rutaCompletaDir.equals("/root") || rutaCompletaDir.equals("root")) {
            System.out.println("Operación inválida: no se puede eliminar /root.");
            return false;
        }
        Proceso p = new Proceso(TipoOperacionFS.ELIMINAR_DIRECTORIO, rutaCompletaDir);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
        return true;
    }

    // RENOMBRAR
    public boolean solicitarRenombrar(String rutaCompleta, String nuevoNombre) {
        // Solo ADMIN
        if (rolActual != RolUsuario.ADMIN) {
            System.out.println("Permiso denegado: solo ADMIN puede renombrar.");
            return false;
        }
        Proceso p = new Proceso(TipoOperacionFS.RENOMBRAR, rutaCompleta, nuevoNombre);
        p.setEstado(EstadoProceso.NUEVO);
        colaProcesos.encolar(p);
        return true;
    }

    // ======================== Simulación de un paso ========================

    public void ejecutarPaso() {
        
        // limpiar error anterior
        ultimoError = null;
        
        if (colaProcesos.estaVacia()) {
            System.out.println("No hay procesos en la cola.");
            return;
        }
        Proceso p = colaProcesos.desencolar();
        if (p == null) return;

        System.out.println("\n=== Ejecutando " + p + " ===");
        p.setEstado(EstadoProceso.LISTO);

        // Generar solicitud de E/S (simple, a pos=0 por ahora)
        colaES.encolar(new SolicitudES(p, 0));

        // Elegir con el planificador
        SolicitudES siguiente = planificador.seleccionarSiguiente(colaES, posicionCabezal);
        if (siguiente == null) {
            System.out.println("No hay solicitudes de E/S.");
            return;
        }
        System.out.println("Planificador seleccionó: " + siguiente);
        p.setEstado(EstadoProceso.EJECUTANDO);
        posicionCabezal = siguiente.getPosicion();

        // Ejecutar operación real
        ejecutarOperacionFS(p);

        p.setEstado(EstadoProceso.TERMINADO);
        System.out.println("Proceso " + p.getId() + " TERMINADO.");
    }

    // ======================== Ejecución de FS ========================

    private void ejecutarOperacionFS(Proceso p) {
        switch (p.getTipoOperacion()) {
            case CREAR_ARCHIVO:
                ejecutarCrearArchivo(p); break;
            case ELIMINAR_ARCHIVO:
                ejecutarEliminarArchivo(p); break;
            case CREAR_DIRECTORIO:
                ejecutarCrearDirectorio(p); break;
            case ELIMINAR_DIRECTORIO:
                ejecutarEliminarDirectorio(p); break;
            case RENOMBRAR:
                ejecutarRenombrar(p); break;
            default:
                System.out.println("Operación no implementada: " + p.getTipoOperacion());
        }
    }

    private void ejecutarCrearArchivo(Proceso p) {
    String ruta = p.getRutaObjetivo();
    int tamBloques = p.getTamanoEnBloques();
    String rutaDir = obtenerRutaDirectorio(ruta);
    String nombreArchivo = obtenerNombreFinal(ruta);

    Directorio dirPadre = sistemaArchivos.buscarDirectorioPorRuta(rutaDir);
    if (dirPadre == null) {
        String msg = "Directorio padre no encontrado: " + rutaDir;
        System.out.println(msg);
        ultimoError = msg;
        return;
    }

    if (dirPadre.buscarHijoPorNombre(nombreArchivo) != null) {
        String msg = "Ya existe un nodo con ese nombre en: " + rutaDir;
        System.out.println(msg);
        ultimoError = msg;
        return;
    }

    Archivo archivo = sistemaArchivos.crearArchivo(dirPadre, nombreArchivo, tamBloques);

    // ⭐⭐ AQUI ESTA LA PARTE 1.4 ⭐⭐  
    if (archivo == null) {
        String msg = "No se pudo crear el archivo '" + nombreArchivo +
                "' en " + rutaDir +
                " (no hay espacio suficiente en el disco).";
        System.out.println(msg);
        ultimoError = msg;   // ← ESTA ES LA CLAVE PARA MOSTRARLO EN LA GUI
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

        Directorio dirPadre = sistemaArchivos.buscarDirectorioPorRuta(rutaDir);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado: " + rutaDir);
            return;
        }

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreArchivo);
        if (nodo == null || nodo.esDirectorio()) {
            System.out.println("Archivo no encontrado o es directorio: " + nombreArchivo);
            return;
        }

        boolean ok = sistemaArchivos.eliminarArchivo(dirPadre, (Archivo) nodo);
        System.out.println(ok ? "Archivo eliminado: " + ruta
                              : "No se pudo eliminar el archivo: " + ruta);
    }

    private void ejecutarCrearDirectorio(Proceso p) {
        String rutaCompletaDir = p.getRutaObjetivo();
        String rutaPadre = obtenerRutaDirectorio(rutaCompletaDir);
        String nombreDir = obtenerNombreFinal(rutaCompletaDir);

        Directorio dirPadre = sistemaArchivos.buscarDirectorioPorRuta(rutaPadre);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado: " + rutaPadre);
            return;
        }

        if (dirPadre.buscarHijoPorNombre(nombreDir) != null) {
            System.out.println("Ya existe un nodo con ese nombre en: " + rutaPadre);
            return;
        }

        Directorio creado = sistemaArchivos.crearDirectorio(dirPadre, nombreDir);
        System.out.println("Directorio creado: " + creado.getRutaCompleta());
    }

    private void ejecutarEliminarDirectorio(Proceso p) {
        String rutaDir = p.getRutaObjetivo();
        if (rutaDir.equals("/root") || rutaDir.equals("root")) {
            System.out.println("Operación inválida: no se puede eliminar /root.");
            return;
        }

        String rutaPadre = obtenerRutaDirectorio(rutaDir);
        String nombreDir = obtenerNombreFinal(rutaDir);

        Directorio dirPadre = sistemaArchivos.buscarDirectorioPorRuta(rutaPadre);
        if (dirPadre == null) {
            System.out.println("Directorio padre no encontrado: " + rutaPadre);
            return;
        }

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreDir);
        if (nodo == null || !nodo.esDirectorio()) {
            System.out.println("No existe ese directorio: " + rutaDir);
            return;
        }

        boolean ok = sistemaArchivos.eliminarDirectorioRecursivo(dirPadre, (Directorio) nodo);
        System.out.println(ok ? "Directorio eliminado: " + rutaDir
                              : "No se pudo eliminar el directorio: " + rutaDir);
    }

    private void ejecutarRenombrar(Proceso p) {
        String ruta = p.getRutaObjetivo();
        String nuevoNombre = p.getNuevoNombre();

        if (nuevoNombre == null || nuevoNombre.isEmpty()) {
            System.out.println("Nuevo nombre inválido.");
            return;
        }

        NodoFS nodo = sistemaArchivos.buscarNodoPorRuta(ruta);
        if (nodo == null) {
            System.out.println("Nodo no encontrado: " + ruta);
            return;
        }

        // evitar duplicado en el mismo directorio
        Directorio padre = nodo.getPadre();
        if (padre != null && padre.buscarHijoPorNombre(nuevoNombre) != null) {
            System.out.println("Ya existe un nodo con ese nombre en: " + padre.getRutaCompleta());
            return;
        }

        sistemaArchivos.renombrarNodo(nodo, nuevoNombre);
        System.out.println("Renombrado: " + ruta + " -> " + nuevoNombre);
    }

    // ======================== Utilidades de ruta ========================

    private String obtenerRutaDirectorio(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx <= 0) return "/root";
        return rutaCompleta.substring(0, idx);
    }

    private String obtenerNombreFinal(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx == -1 || idx == rutaCompleta.length() - 1) return rutaCompleta;
        return rutaCompleta.substring(idx + 1);
    }
}

