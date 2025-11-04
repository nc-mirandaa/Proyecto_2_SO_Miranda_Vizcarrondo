/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

/**
 *
 * @author Nathaly
 */

import control.GestorSistema;
import modelo.fs.SistemaArchivos;
import modelo.fs.Directorio;

public class MainPruebaGestor {

    public static void main(String[] args) {

        GestorSistema gestor = new GestorSistema(20);

        // Creamos /root/Documentos manualmente (por ahora)
        SistemaArchivos sa = gestor.getSistemaArchivos();
        Directorio root = sa.getRaiz();
        Directorio docs = sa.crearDirectorio(root, "Documentos");

        // Pedimos crear archivos mediante procesos
        gestor.solicitarCrearArchivo("/root/Documentos/tarea1.txt", 3);
        gestor.solicitarCrearArchivo("/root/Documentos/tarea2.txt", 5);

        // Simulamos dos "ticks" del sistema
        gestor.ejecutarPaso();
        gestor.ejecutarPaso();

        System.out.println("\n=== FS DESPUÉS DE CREAR ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();

        // Ahora probamos eliminar vía proceso
        gestor.solicitarEliminarArchivo("/root/Documentos/tarea1.txt");
        gestor.ejecutarPaso();

        System.out.println("\n=== FS DESPUÉS DE ELIMINAR tarea1.txt ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();
    }
}

