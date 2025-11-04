/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.fs;

import modelo.disco.Disco;
import modelo.disco.Bloque;

/**
 *
 * @author Nathaly
 */

/**
 * Maneja la estructura de directorios y archivos y se conecta con el disco.
 */

public class SistemaArchivos {

    private Directorio raiz;
    private Disco disco;

    // Tabla de asignación de archivos
    private ListaEnlazada<EntradaTablaAsignacion> tablaAsignacion;

    public SistemaArchivos(int cantidadBloquesDisco) {
        // La raíz se llamará "root"
        this.raiz = new Directorio("root", null);
        this.disco = new Disco(cantidadBloquesDisco);
        this.tablaAsignacion = new ListaEnlazada<>();
    }

    public Directorio getRaiz() {
        return raiz;
    }

    public Disco getDisco() {
        return disco;
    }

    public ListaEnlazada<EntradaTablaAsignacion> getTablaAsignacion() {
        return tablaAsignacion;
    }

    /**
     * Crea un subdirectorio dentro de un directorio padre dado.
     */
    public Directorio crearDirectorio(Directorio padre, String nombre) {
        if (padre == null) {
            throw new IllegalArgumentException("El directorio padre no puede ser null");
        }
        Directorio nuevo = new Directorio(nombre, padre);
        padre.agregarHijo(nuevo);
        return nuevo;
    }

    /**
     * Crea un archivo dentro de un directorio padre dado.
     * Además, intenta asignar bloques en el disco y actualiza la tabla de asignación.
     * @return el archivo creado, o null si no hubo espacio en el disco.
     */
    public Archivo crearArchivo(Directorio padre, String nombre, int tamanoEnBloques) {
        if (padre == null) {
            throw new IllegalArgumentException("El directorio padre no puede ser null");
        }

        // Intentamos asignar los bloques en el disco
        int primerBloque = disco.asignarBloques(tamanoEnBloques);
        if (primerBloque == Bloque.NULO) {
            System.out.println("No hay espacio suficiente en el disco para el archivo " + nombre);
            return null;
        }

        Archivo archivo = new Archivo(nombre, padre, tamanoEnBloques);
        archivo.setPrimerBloque(primerBloque);
        padre.agregarHijo(archivo);

        // Agregar entrada en la tabla de asignación
        EntradaTablaAsignacion entrada =
                new EntradaTablaAsignacion(archivo.getId(), archivo.getNombre(),
                        archivo.getTamanoEnBloques(), archivo.getPrimerBloque());
        tablaAsignacion.agregar(entrada);

        return archivo;
    }

    /**
     * Elimina un archivo.
     * Libera los bloques del disco asociados a él y actualiza la tabla de asignación.
     */
    public boolean eliminarArchivo(Directorio padre, Archivo archivo) {
        if (padre == null || archivo == null) {
            return false;
        }
        // Liberar bloques en el disco
        int primerBloque = archivo.getPrimerBloque();
        if (primerBloque != Bloque.NULO) {
            disco.liberarCadenaBloques(primerBloque);
        }

        // Eliminar de la tabla de asignación
        eliminarEntradaTablaPorIdArchivo(archivo.getId());

        // Eliminar del directorio
        return padre.eliminarHijo(archivo);
    }

    /**
     * Elimina la entrada en la tabla de asignación asociada a un archivo.
     */
    private void eliminarEntradaTablaPorIdArchivo(int idArchivo) {
        final EntradaTablaAsignacion[] aEliminar = { null };
        tablaAsignacion.forEach(ent -> {
            if (ent.getIdArchivo() == idArchivo) {
                aEliminar[0] = ent;
            }
        });

        if (aEliminar[0] != null) {
            tablaAsignacion.eliminar(aEliminar[0]);
        }
    }

    /**
     * Imprime en consola la estructura del sistema de archivos.
     * Esto nos ayuda a probar rápidamente.
     */
    public void imprimirEstructura() {
        imprimirDirectorio(raiz, 0);
    }

    private void imprimirDirectorio(Directorio dir, int nivel) {
        imprimirIndentacion(nivel);
        System.out.println("[DIR] " + dir.getNombre());

        dir.getHijos().forEach(hijo -> {
            if (hijo.esDirectorio()) {
                imprimirDirectorio((Directorio) hijo, nivel + 1);
            } else {
                imprimirIndentacion(nivel + 1);
                Archivo archivo = (Archivo) hijo;
                System.out.println("[FILE] " + archivo.getNombre() +
                        " (" + archivo.getTamanoEnBloques() + " bloques, primerBloque=" +
                        archivo.getPrimerBloque() + ", id=" + archivo.getId() + ")");
            }
        });
    }

    private void imprimirIndentacion(int nivel) {
        for (int i = 0; i < nivel; i++) {
            System.out.print("  ");
        }
    }

    /**
     * Imprime un resumen del uso del disco.
     */
    public void imprimirEstadoDisco() {
        boolean[] mapa = disco.mapaOcupacion();
        System.out.println("Estado del disco (O = ocupado, . = libre):");
        for (int i = 0; i < mapa.length; i++) {
            System.out.print(mapa[i] ? "O" : ".");
        }
        System.out.println();
        System.out.println("Bloques libres: " + disco.contarBloquesLibres() +
                " de " + disco.getCantidadBloques());
    }

    /**
     * Imprime la tabla de asignación de archivos en consola.
     * Luego esta misma info la mostraremos en un JTable.
     */
    public void imprimirTablaAsignacion() {
        System.out.println("\n=== Tabla de asignación de archivos ===");
        System.out.println("ID\tNombre\t\tBloques\tPrimerBloque");
        tablaAsignacion.forEach(ent -> {
            System.out.println(
                    ent.getIdArchivo() + "\t" +
                    ent.getNombreArchivo() + "\t\t" +
                    ent.getTamanoEnBloques() + "\t" +
                    ent.getPrimerBloque()
            );
        });
    }
}
