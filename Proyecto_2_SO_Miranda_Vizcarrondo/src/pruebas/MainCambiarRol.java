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
import modelo.procesos.RolUsuario;

import java.util.Scanner;

public class MainCambiarRol {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        GestorSistema gestor = new GestorSistema(20);

        // preparar /root/Documentos
        SistemaArchivos sa = gestor.getSistemaArchivos();
        Directorio root = sa.getRaiz();
        sa.crearDirectorio(root, "Documentos");

        while (true) {
            System.out.println("\nRol actual: " + gestor.getRolActual());
            System.out.println("1) Cambiar a ADMIN");
            System.out.println("2) Cambiar a USUARIO");
            System.out.println("3) Crear archivo (permitido a ambos)");
            System.out.println("4) Renombrar archivo (solo ADMIN)");
            System.out.println("5) Eliminar archivo (solo ADMIN)");
            System.out.println("6) Mostrar FS");
            System.out.println("0) Salir");
            System.out.print("Opción: ");

            String in = sc.nextLine().trim();
            if (in.equals("0")) break;

            switch (in) {
                case "1":
                    gestor.setRolActual(RolUsuario.ADMIN);
                    System.out.println("→ Rol cambiado a ADMIN");
                    break;
                case "2":
                    gestor.setRolActual(RolUsuario.USUARIO);
                    System.out.println("→ Rol cambiado a USUARIO");
                    break;
                case "3": {
                    System.out.print("Nombre archivo: ");
                    String nom = sc.nextLine().trim();
                    System.out.print("Tamaño en bloques: ");
                    int tam = Integer.parseInt(sc.nextLine().trim());
                    gestor.solicitarCrearArchivo("/root/Documentos/" + nom, tam);
                    gestor.ejecutarPaso();
                    break;
                }
                case "4": {
                    System.out.print("Archivo a renombrar: ");
                    String nom = sc.nextLine().trim();
                    System.out.print("Nuevo nombre: ");
                    String nuevo = sc.nextLine().trim();
                    gestor.solicitarRenombrar("/root/Documentos/" + nom, nuevo);
                    gestor.ejecutarPaso();
                    break;
                }
                case "5": {
                    System.out.print("Archivo a eliminar: ");
                    String nom = sc.nextLine().trim();
                    gestor.solicitarEliminarArchivo("/root/Documentos/" + nom);
                    gestor.ejecutarPaso();
                    break;
                }
                case "6":
                    sa.imprimirEstructura();
                    sa.imprimirTablaAsignacion();
                    sa.imprimirEstadoDisco();
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
        sc.close();
    }
}

