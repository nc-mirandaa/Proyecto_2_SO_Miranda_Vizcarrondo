/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Nathaly
 */


import modelo.fs.SistemaArchivos;
import modelo.fs.Directorio;
import modelo.fs.Archivo;

public class MainPruebasFS {

    public static void main(String[] args) {
        // Creamos un sistema de archivos con 20 bloques de disco
        SistemaArchivos sa = new SistemaArchivos(20);

        Directorio root = sa.getRaiz();
        Directorio docs = sa.crearDirectorio(root, "Documentos");
        Directorio img = sa.crearDirectorio(root, "Imagenes");

        Archivo a1 = sa.crearArchivo(docs, "tarea1.txt", 3);
        Archivo a2 = sa.crearArchivo(docs, "tarea2.txt", 5);
        Archivo img1 = sa.crearArchivo(img, "foto1.jpg", 10);

        System.out.println("=== Estructura del sistema de archivos ===");
        sa.imprimirEstructura();

        sa.imprimirTablaAsignacion();

        System.out.println("\n=== Estado del disco tras crear archivos ===");
        sa.imprimirEstadoDisco();

        // Ahora borramos un archivo y vemos cómo cambian las cosas
        System.out.println("\nEliminando archivo tarea1.txt ...");
        sa.eliminarArchivo(docs, a1);

        System.out.println("\n=== Estructura del sistema de archivos ===");
        sa.imprimirEstructura();

        sa.imprimirTablaAsignacion();

        System.out.println("\n=== Estado del disco tras eliminar tarea1.txt ===");
        sa.imprimirEstadoDisco();
    }
}
