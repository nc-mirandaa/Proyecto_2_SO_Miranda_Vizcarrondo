/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import modelo.fs.*;
import modelo.procesos.*;
import java.util.Scanner;

/**
 * Programa principal en consola para probar el sistema de archivos.
 * Permite crear, eliminar, renombrar, guardar y cargar estructuras.
 */
public class MainConsola {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Al iniciar, cargamos persistencia si existe
        SistemaArchivos sistema = PersistenciaSistema.cargar("sistema.txt", 50);
        GestorSistema gestor = new GestorSistema(50);
        gestor.setSistemaArchivos(sistema);

        boolean salir = false;

        while (!salir) {
            System.out.println("\n=== MENU SISTEMA DE ARCHIVOS ===");
            System.out.println("1. Crear archivo");
            System.out.println("2. Eliminar archivo");
            System.out.println("3. Crear directorio");
            System.out.println("4. Eliminar directorio");
            System.out.println("5. Renombrar archivo/directorio");
            System.out.println("6. Mostrar estructura");
            System.out.println("7. Mostrar tabla de asignación");
            System.out.println("8. Guardar en disco");
            System.out.println("9. Cargar desde disco");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            int op = sc.nextInt();
            sc.nextLine(); // limpiar buffer

            switch (op) {
                case 1 -> {
                    System.out.print("Ruta completa del archivo (ej: /root/docs/tarea.txt): ");
                    String ruta = sc.nextLine();
                    System.out.print("Tamaño en bloques: ");
                    int tam = sc.nextInt();
                    gestor.solicitarCrearArchivo(ruta, tam);
                    gestor.ejecutarPaso();
                }
                case 2 -> {
                    System.out.print("Ruta completa del archivo a eliminar: ");
                    String ruta = sc.nextLine();
                    gestor.solicitarEliminarArchivo(ruta);
                    gestor.ejecutarPaso();
                }
                case 3 -> {
                    System.out.print("Ruta del nuevo directorio (ej: /root/docs/nuevo): ");
                    String ruta = sc.nextLine();
                    gestor.solicitarCrearDirectorio(ruta);
                    gestor.ejecutarPaso();
                }
                case 4 -> {
                    System.out.print("Ruta del directorio a eliminar: ");
                    String ruta = sc.nextLine();
                    gestor.solicitarEliminarDirectorio(ruta);
                    gestor.ejecutarPaso();
                }
                case 5 -> {
                    System.out.print("Ruta del elemento a renombrar: ");
                    String ruta = sc.nextLine();
                    System.out.print("Nuevo nombre: ");
                    String nuevo = sc.nextLine();
                    gestor.solicitarRenombrar(ruta, nuevo);
                    gestor.ejecutarPaso();
                }
                case 6 -> gestor.getSistemaArchivos().imprimirEstructura();
                case 7 -> gestor.getSistemaArchivos().imprimirTablaAsignacion();
                case 8 -> PersistenciaSistema.guardar(gestor.getSistemaArchivos(), "sistema.txt");
                case 9 -> {
                    sistema = PersistenciaSistema.cargar("sistema.txt", 50);
                    gestor.setSistemaArchivos(sistema);
                }
                case 0 -> {
                    salir = true;
                    System.out.println("Saliendo del sistema...");
                }
                default -> System.out.println("Opción inválida");
            }
        }

        sc.close();
    }
}

