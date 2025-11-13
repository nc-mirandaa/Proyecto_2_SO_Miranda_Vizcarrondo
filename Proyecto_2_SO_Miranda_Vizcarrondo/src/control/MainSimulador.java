/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package control;

import modelo.fs.SistemaArchivos;
import modelo.fs.Directorio;
import modelo.fs.PersistenciaSistema;
import modelo.procesos.*;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class MainSimulador {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rnd = new Random();

        // ====== Inicialización del sistema ======
        GestorSistema gestor = new GestorSistema(50);

        SistemaArchivos sa = gestor.getSistemaArchivos();
        Directorio root = sa.getRaiz();

        // Crear dos directorios iniciales
        sa.crearDirectorio(root, "DocsA");
        sa.crearDirectorio(root, "DocsB");

        // ====== Sembrar 10 archivos aleatorios ======
        String[] extensiones = {".txt", ".log", ".dat", ".csv", ".jpg", ".bin"};

        for (int i = 1; i <= 10; i++) {
            String base = "auto_" + i;
            String ext = extensiones[rnd.nextInt(extensiones.length)];
            int tam = 1 + rnd.nextInt(8); // 1 a 8 bloques

            boolean enA = rnd.nextBoolean();
            String rutaDir = enA ? "/root/DocsA" : "/root/DocsB";
            String rutaArchivo = rutaDir + "/" + base + ext;

            boolean ok = gestor.solicitarCrearArchivo(rutaArchivo, tam);
            if (ok) gestor.ejecutarPaso();
        }

        // Estado inicial tras la semilla
        System.out.println("\n=== ESTADO INICIAL ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();

        // ====== Menú ======
        String nombrePlan = "FIFO";
        gestor.setPlanificador(new PlanificadorFIFO());

        boolean salir = false;

        while (!salir) {
            System.out.println("\n================= MENÚ =================");
            System.out.println("Rol actual: " + gestor.getRolActual() + " | Planificador: " + nombrePlan);
            System.out.println("1) Planificador FIFO");
            System.out.println("2) Planificador SSTF");
            System.out.println("3) Planificador SCAN");
            System.out.println("4) Planificador C-SCAN");
            System.out.println("5) Cambiar a ADMIN");
            System.out.println("6) Cambiar a USUARIO");
            System.out.println("7) Crear archivo");
            System.out.println("8) Eliminar archivo");
            System.out.println("9) Crear directorio");
            System.out.println("10) Eliminar directorio");
            System.out.println("11) Renombrar archivo/directorio");
            System.out.println("12) Ejecutar un paso");
            System.out.println("13) Mostrar FS completo");
            System.out.println("14) Guardar estado en archivo");
            System.out.println("15) Cargar estado desde archivo");
            System.out.println("0) Salir");
            System.out.print("Opción: ");

            String option = sc.nextLine().trim();

            switch (option) {
                case "1":
                    gestor.setPlanificador(new PlanificadorFIFO());
                    nombrePlan = "FIFO";
                    System.out.println("Planificador cambiado a FIFO.");
                    break;

                case "2":
                    gestor.setPlanificador(new PlanificadorSSTF());
                    nombrePlan = "SSTF";
                    System.out.println("Planificador cambiado a SSTF.");
                    break;

                case "3":
                    gestor.setPlanificador(new PlanificadorSCAN());
                    nombrePlan = "SCAN";
                    System.out.println("Planificador cambiado a SCAN.");
                    break;

                case "4":
                    gestor.setPlanificador(new PlanificadorCSCAN());
                    nombrePlan = "C-SCAN";
                    System.out.println("Planificador cambiado a C-SCAN.");
                    break;

                case "5":
                    gestor.setRolActual(RolUsuario.ADMIN);
                    System.out.println("Rol cambiado a ADMIN.");
                    break;

                case "6":
                    gestor.setRolActual(RolUsuario.USUARIO);
                    System.out.println("Rol cambiado a USUARIO.");
                    break;

                case "7": {
                    System.out.print("Ruta directorio (ej: /root/DocsA): ");
                    String rutaDir = normaliza(sc.nextLine());

                    System.out.print("Nombre del archivo: ");
                    String nombre = sc.nextLine().trim();

                    System.out.print("Tamaño en bloques: ");
                    int tam;
                    try {
                        tam = Integer.parseInt(sc.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Tamaño inválido.");
                        break;
                    }

                    String rutaArchivo = rutaDir + "/" + nombre;

                    boolean ok = gestor.solicitarCrearArchivo(rutaArchivo, tam);
                    if (ok) gestor.ejecutarPaso();
                    break;
                }

                case "8": {
                    System.out.print("Ruta del archivo a eliminar: ");
                    String ruta = normaliza(sc.nextLine());

                    boolean ok = gestor.solicitarEliminarArchivo(ruta);
                    if (ok) gestor.ejecutarPaso();
                    break;
                }

                case "9": {
                    System.out.print("Ruta del directorio padre: ");
                    String rutaPadre = normaliza(sc.nextLine());

                    System.out.print("Nombre del nuevo directorio: ");
                    String nombre = sc.nextLine().trim();

                    String rutaDir = rutaPadre + "/" + nombre;

                    boolean ok = gestor.solicitarCrearDirectorio(rutaDir);
                    if (ok) gestor.ejecutarPaso();
                    break;
                }

                case "10": {
                    System.out.print("Ruta completa del directorio a eliminar: ");
                    String ruta = normaliza(sc.nextLine());

                    boolean ok = gestor.solicitarEliminarDirectorio(ruta);
                    if (ok) gestor.ejecutarPaso();
                    break;
                }

                case "11": {
                    System.out.print("Ruta completa del nodo a renombrar: ");
                    String ruta = normaliza(sc.nextLine());

                    if (gestor.getRolActual() != RolUsuario.ADMIN) {
                        System.out.println("Permiso denegado: solo ADMIN puede renombrar.");
                        break;
                    }

                    if (gestor.getSistemaArchivos().buscarNodoPorRuta(ruta) == null) {
                        System.out.println("Nodo no encontrado: " + ruta);
                        break;
                    }

                    System.out.print("Nuevo nombre (sin ruta): ");
                    String nuevo = sc.nextLine().trim();

                    if (nuevo.isEmpty()) {
                        System.out.println("Nombre inválido.");
                        break;
                    }

                    boolean ok = gestor.solicitarRenombrar(ruta, nuevo);
                    if (ok) gestor.ejecutarPaso();
                    break;
                }

                case "12":
                    gestor.ejecutarPaso();
                    break;

                case "13":
                    System.out.println("\n=== ESTADO ACTUAL ===");
                    gestor.getSistemaArchivos().imprimirEstructura();
                    gestor.getSistemaArchivos().imprimirTablaAsignacion();
                    gestor.getSistemaArchivos().imprimirEstadoDisco();
                    break;

                case "14": {
                    System.out.print("Nombre del archivo donde guardar (ej: estado.txt): ");
                    String nombre = sc.nextLine().trim();
                    if (nombre.isEmpty()) {
                        System.out.println("Nombre inválido.");
                        break;
                    }
                    try {
                        PersistenciaSistema.guardar(gestor.getSistemaArchivos(), nombre);
                        System.out.println("Estado guardado en " + nombre);
                    } catch (IOException e) {
                        System.out.println("Error al guardar: " + e.getMessage());
                    }
                    break;
                }

                case "15": {
                    System.out.print("Nombre del archivo desde el que cargar (ej: estado.txt): ");
                    String nombre = sc.nextLine().trim();
                    if (nombre.isEmpty()) {
                        System.out.println("Nombre inválido.");
                        break;
                    }
                    try {
                        SistemaArchivos nuevo = PersistenciaSistema.cargar(nombre);
                        gestor.setSistemaArchivos(nuevo);
                        sa = gestor.getSistemaArchivos(); // actualizar referencia local
                        System.out.println("Estado cargado desde " + nombre);
                    } catch (IOException e) {
                        System.out.println("Error al cargar: " + e.getMessage());
                    }
                    break;
                }

                case "0":
                    salir = true;
                    System.out.println("Saliendo...");
                    break;

                default:
                    System.out.println("Opción inválida.");
            }
        }

        sc.close();
    }

    private static String normaliza(String r) {
        if (r == null || r.isEmpty()) return "/root";
        r = r.trim();
        if (!r.startsWith("/")) r = "/" + r;
        return r;
    }
}


