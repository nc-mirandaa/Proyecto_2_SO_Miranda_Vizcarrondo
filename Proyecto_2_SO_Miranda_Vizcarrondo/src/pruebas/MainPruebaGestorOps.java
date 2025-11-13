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

public class MainPruebaGestorOps {

    public static void main(String[] args) {
        GestorSistema gestor = new GestorSistema(30);

        // Preparo /root/Documentos
        SistemaArchivos sa = gestor.getSistemaArchivos();
        Directorio root = sa.getRaiz();
        sa.crearDirectorio(root, "Documentos");

        // 1) Crear directorio /root/Documentos/Sub
        gestor.solicitarCrearDirectorio("/root/Documentos/Sub");
        gestor.ejecutarPaso();

        // 2) Crear archivos en /root/Documentos y /root/Documentos/Sub
        gestor.solicitarCrearArchivo("/root/Documentos/t1.txt", 3);
        gestor.solicitarCrearArchivo("/root/Documentos/Sub/foto.jpg", 6);
        gestor.solicitarCrearArchivo("/root/Documentos/Sub/data.bin", 5);
        gestor.ejecutarPaso();
        gestor.ejecutarPaso();
        gestor.ejecutarPaso();

        System.out.println("\n=== ANTES ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();

        // 3) Renombrar archivo
        gestor.solicitarRenombrar("/root/Documentos/t1.txt", "tarea_final.txt");
        gestor.ejecutarPaso();

        // 4) Eliminar directorio recursivo
        gestor.solicitarEliminarDirectorio("/root/Documentos/Sub");
        gestor.ejecutarPaso();

        System.out.println("\n=== DESPUÉS ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();
    }
}

