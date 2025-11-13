/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.fs;

import modelo.disco.Disco;
import modelo.disco.Bloque;

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

    // ----------------------------------------------------------------------
    // CREAR / ELIMINAR ARCHIVO
    // ----------------------------------------------------------------------

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
     * Elimina un archivo: libera bloques, borra de la tabla y lo quita del directorio.
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

    // ----------------------------------------------------------------------
    // CREAR / ELIMINAR DIRECTORIO (ELIMINACIÓN RECURSIVA)
    // ----------------------------------------------------------------------

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
     * Elimina un directorio de forma RECURSIVA:
     * - Borra todos los archivos y subdirectorios descendientes.
     * - Libera bloques de TODOS los archivos.
     * - Quita el directorio objetivo de su padre.
     * @return true si se eliminó; false si no se pudo.
     */
    public boolean eliminarDirectorioRecursivo(Directorio padre, Directorio dirAEliminar) {
        if (padre == null || dirAEliminar == null) {
            return false;
        }
        // 1) Borrado recursivo del contenido
        eliminarContenidoRecursivo(dirAEliminar);
        // 2) Quitar el directorio objetivo de su padre
        return padre.eliminarHijo(dirAEliminar);
    }

    /**
     * Recorre todos los hijos de 'dir' y:
     * - Si es archivo: libera bloques + borra de tabla + lo quita del directorio.
     * - Si es directorio: baja recursivamente y al volver lo elimina.
     *
     * Nota: NO usamos ArrayList; recorremos por índice de atrás hacia adelante
     * usando nuestra ListaEnlazada<T> para evitar problemas al eliminar durante el recorrido.
     */
    private void eliminarContenidoRecursivo(Directorio dir) {
        // Primero eliminar TODOS los archivos
        for (int i = dir.getHijos().size() - 1; i >= 0; i--) {
            NodoFS hijo = dir.getHijos().get(i);
            if (!hijo.esDirectorio()) {
                Archivo archivo = (Archivo) hijo;
                // liberar bloques + borrar tabla + quitar del dir actual
                int primerBloque = archivo.getPrimerBloque();
                if (primerBloque != Bloque.NULO) {
                    disco.liberarCadenaBloques(primerBloque);
                }
                eliminarEntradaTablaPorIdArchivo(archivo.getId());
                dir.eliminarHijo(archivo);
            }
        }
        // Luego eliminar subdirectorios (post-orden)
        for (int i = dir.getHijos().size() - 1; i >= 0; i--) {
            NodoFS hijo = dir.getHijos().get(i);
            if (hijo.esDirectorio()) {
                Directorio sub = (Directorio) hijo;
                eliminarContenidoRecursivo(sub);
                // después de limpiar su contenido, quitarlo del directorio actual
                dir.eliminarHijo(sub);
            }
        }
    }

    // ----------------------------------------------------------------------
    // RENOMBRAR (actualiza tabla si es archivo)
    // ----------------------------------------------------------------------

    /**
     * Renombra un nodo (archivo o directorio).
     * Si es archivo, también actualiza su nombre en la tabla de asignación.
     */
    public void renombrarNodo(NodoFS nodo, String nuevoNombre) {
        if (nodo == null || nuevoNombre == null || nuevoNombre.isEmpty()) {
            return;
        }
        nodo.setNombre(nuevoNombre);

        if (!nodo.esDirectorio()) {
            Archivo a = (Archivo) nodo;
            // buscar su entrada en la tabla y actualizar el nombre
            final EntradaTablaAsignacion[] objetivo = { null };
            tablaAsignacion.forEach(ent -> {
                if (ent.getIdArchivo() == a.getId()) {
                    objetivo[0] = ent;
                }
            });
            if (objetivo[0] != null) {
                objetivo[0].setNombreArchivo(nuevoNombre);
            }
        }
    }

    // ----------------------------------------------------------------------
    // BÚSQUEDA POR RUTA (útil para gestor/menús)
    // ----------------------------------------------------------------------

    /**
     * Busca un directorio por ruta absoluta (p.ej. "/root/Documentos/Sub").
     * Si la ruta es "/" retorna la raíz.
     */
    public Directorio buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || "/".equals(ruta)) {
            return raiz;
        }

        String limpia = ruta.trim();
        if (limpia.startsWith("/")) limpia = limpia.substring(1);

        String[] partes = limpia.split("/");

        Directorio actual = raiz;
        int i = 0;
        if (partes.length > 0 && partes[0].equals(actual.getNombre())) {
            i = 1; // saltamos "root"
        }

        for (; i < partes.length; i++) {
            String nombreDir = partes[i];
            if (nombreDir.isEmpty()) continue;
            NodoFS hijo = actual.buscarHijoPorNombre(nombreDir);
            if (hijo == null || !hijo.esDirectorio()) {
                return null;
            }
            actual = (Directorio) hijo;
        }
        return actual;
    }

    /**
     * Busca un nodo (archivo o directorio) por ruta absoluta (p.ej. "/root/Docs/a.txt").
     * Retorna null si no existe.
     */
    public NodoFS buscarNodoPorRuta(String rutaCompleta) {
        if (rutaCompleta == null || rutaCompleta.isEmpty()) return null;
        String ruta = rutaCompleta.trim();
        if (ruta.equals("/") || ruta.equals("/root") || ruta.equals("root")) return raiz;

        // separar en (rutaDir) + (nombre final)
        int idx = ruta.lastIndexOf('/');
        String rutaDir = (idx <= 0) ? "/root" : ruta.substring(0, idx);
        String nombreFinal = (idx == -1 || idx == ruta.length() - 1) ? ruta : ruta.substring(idx + 1);

        Directorio padre = buscarDirectorioPorRuta(rutaDir);
        if (padre == null) return null;

        return padre.buscarHijoPorNombre(nombreFinal);
    }

    // ----------------------------------------------------------------------
    // IMPRESIÓN / DEBUG
    // ----------------------------------------------------------------------

    /**
     * Imprime en consola la estructura del sistema de archivos.
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

    // ----------------------------------------------------------------------
    // PRIVADOS
    // ----------------------------------------------------------------------

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
}
