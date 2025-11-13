/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import modelo.fs.*;

/**
 *
 * @author Nathaly
 */

public class MainPruebaFS_Recursivo_Renombrar {
    public static void main(String[] args) {
        SistemaArchivos sa = new SistemaArchivos(30);
        Directorio root = sa.getRaiz();
        Directorio docs = sa.crearDirectorio(root, "Documentos");
        Directorio sub = sa.crearDirectorio(docs, "Sub");

        Archivo a1 = sa.crearArchivo(docs, "t1.txt", 3);
        Archivo a2 = sa.crearArchivo(sub,  "foto.jpg", 6);
        sa.crearArchivo(sub,  "data.bin", 5);

        System.out.println("=== ANTES ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();

        // Renombrar archivo
        sa.renombrarNodo(a1, "tarea_final.txt");

        // Borrado recursivo de /root/Documentos/Sub
        sa.eliminarDirectorioRecursivo(docs, sub);

        System.out.println("\n=== DESPUÉS ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();
    }
}
