/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.fs;


import modelo.disco.Disco;
import modelo.disco.Bloque;

/**
 * Maneja la estructura de directorios y archivos y se conecta con el disco.
 * Añadido:
 *  - Eliminación recursiva de directorios (libera bloques y tabla de asignación).
 *  - Renombrado de archivos/directorios con actualización de tabla de asignación.
 *
 * @author Nathaly
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
     * Elimina recursivamente un directorio y todo su contenido.
     * Libera bloques de archivos y elimina entradas de la tabla de asignación.
     * Retorna true si se eliminó (del padre debe haberse invocado eliminación)
     */
    private void eliminarDirectorioRecursivo(Directorio dir) {
        // Recorremos copia de los hijos para evitar modificaciones concurrentes al iterar
        ListaEnlazada<NodoFS> hijos = dir.getHijos();
        // Usamos un array temporal porque ListaEnlazada no devuelve un array directamente.
        // Recorremos por índice
        int n = hijos.size();
        // Recorremos desde el final al principio para eliminar sin romper la lista
        for (int i = n - 1; i >= 0; i--) {
            NodoFS hijo = hijos.get(i);
            if (hijo.esDirectorio()) {
                // Si es directorio, eliminamos recursivamente
                eliminarDirectorioRecursivo((Directorio) hijo);
                // luego removemos el directorio hijo del padre actual
                dir.eliminarHijo(hijo);
            } else {
                // Si es archivo, liberamos sus bloques y borramos su entrada
                Archivo a = (Archivo) hijo;
                int primer = a.getPrimerBloque();
                if (primer != Bloque.NULO) {
                    disco.liberarCadenaBloques(primer);
                }
                eliminarEntradaTablaPorIdArchivo(a.getId());
                dir.eliminarHijo(a);
            }
        }
        // Al terminar, el directorio dir estará vacío. Su eliminación (del padre) la hace el caller.
    }

    /**
     * Método público para eliminar un directorio (identificado por referencia).
     * Se encarga de la eliminación recursiva y de quitar el directorio del padre.
     * Retorna true si se eliminó correctamente, false si no (p.ej. padre null).
     */
    public boolean eliminarDirectorio(Directorio padre, Directorio dir) {
        if (padre == null || dir == null) return false;
        // No permitimos eliminar la raíz desde aquí
        if (dir == raiz) {
            System.out.println("No se puede eliminar la raíz del sistema de archivos.");
            return false;
        }

        // Eliminar contenido recursivamente
        eliminarDirectorioRecursivo(dir);

        // Finalmente eliminar el propio directorio del padre
        boolean ok = padre.eliminarHijo(dir);
        if (!ok) {
            System.out.println("No se pudo eliminar el directorio del padre: " + dir.getNombre());
        }
        return ok;
    }

    /**
     * Renombra un nodo (archivo o directorio).
     * Si es archivo, además actualiza la entrada de la tabla de asignación.
     */
    public boolean renombrarNodo(NodoFS nodo, String nuevoNombre) {
        if (nodo == null || nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            return false;
        }
        String viejo = nodo.getNombre();
        nodo.setNombre(nuevoNombre);

        // Si es archivo, actualizar la tabla de asignación
        if (!nodo.esDirectorio()) {
            Archivo a = (Archivo) nodo;
            // buscar entrada por id y actualizar nombre
            final EntradaTablaAsignacion[] match = { null };
            tablaAsignacion.forEach(ent -> {
                if (ent.getIdArchivo() == a.getId()) {
                    match[0] = ent;
                }
            });
            if (match[0] != null) {
                match[0].setNombreArchivo(nuevoNombre);
            } else {
                // Si no encontramos la entrada, logear (posible inconsistencia)
                System.out.println("Atención: no se encontró entrada de tabla para archivo renombrado: " + viejo);
            }
        }
        return true;
    }

    /**
     * Renombra un nodo dado por una ruta absoluta (ej: "/root/Dir/archivo.txt").
     * Devuelve true si se renombró correctamente.
     */
    public boolean renombrarPorRuta(String rutaCompleta, String nuevoNombre) {
        if (rutaCompleta == null || nuevoNombre == null) return false;

        String rutaDir = obtenerRutaDirectorio(rutaCompleta);
        String nombreFinal = obtenerNombreFinal(rutaCompleta);

        Directorio dirPadre = buscarDirectorioPorRuta(rutaDir);
        if (dirPadre == null) return false;

        NodoFS nodo = dirPadre.buscarHijoPorNombre(nombreFinal);
        if (nodo == null) return false;

        return renombrarNodo(nodo, nuevoNombre);
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

    // ------------------------------
    // Métodos utilitarios para trabajar con rutas (copiados del Gestor)
    // ------------------------------

    /**
     * Busca un directorio por ruta absoluta, p.ej. "/root/Documentos/Sub".
     */
    public Directorio buscarDirectorioPorRuta(String ruta) {
        if (ruta == null || ruta.isEmpty() || "/".equals(ruta)) {
            return this.getRaiz();
        }

        String limpia = ruta.trim();
        if (limpia.startsWith("/")) {
            limpia = limpia.substring(1);
        }

        String[] partes = limpia.split("/");

        Directorio actual = this.getRaiz();
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
