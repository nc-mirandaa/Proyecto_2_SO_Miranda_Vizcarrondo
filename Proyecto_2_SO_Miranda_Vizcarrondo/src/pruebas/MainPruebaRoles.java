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

public class MainPruebaRoles {

    public static void main(String[] args) {
        GestorSistema gestor = new GestorSistema(20);

        // Crear /root/Documentos
        SistemaArchivos sa = gestor.getSistemaArchivos();
        Directorio root = sa.getRaiz();
        sa.crearDirectorio(root, "Documentos");

        // Rol USUARIO
        gestor.setRolActual(RolUsuario.USUARIO);
        System.out.println("Rol actual: " + gestor.getRolActual());

        // Usuario puede crear archivo/carpeta
        gestor.solicitarCrearDirectorio("/root/Documentos/Sub");
        gestor.solicitarCrearArchivo("/root/Documentos/a.txt", 3);
        gestor.ejecutarPaso();
        gestor.ejecutarPaso();

        // Usuario NO puede renombrar ni eliminar
        gestor.solicitarRenombrar("/root/Documentos/a.txt", "a2.txt"); // rechazado
        gestor.solicitarEliminarArchivo("/root/Documentos/a.txt");     // rechazado
        gestor.solicitarEliminarDirectorio("/root/Documentos/Sub");    // rechazado

        // Cambiamos a ADMIN
        gestor.setRolActual(RolUsuario.ADMIN);
        System.out.println("Rol actual: " + gestor.getRolActual());

        // Ahora sí puede renombrar y eliminar
        gestor.solicitarRenombrar("/root/Documentos/a.txt", "a2.txt");
        gestor.ejecutarPaso();
        gestor.solicitarEliminarDirectorio("/root/Documentos/Sub");
        gestor.ejecutarPaso();
        gestor.solicitarEliminarArchivo("/root/Documentos/a2.txt");
        gestor.ejecutarPaso();

        // Estado final
        System.out.println("\n=== ESTADO FINAL ===");
        sa.imprimirEstructura();
        sa.imprimirTablaAsignacion();
        sa.imprimirEstadoDisco();
    }
}


