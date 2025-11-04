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
        SistemaArchivos sa = new SistemaArchivos();

        Directorio root = sa.getRaiz();
        Directorio docs = sa.crearDirectorio(root, "Documentos");
        Directorio img = sa.crearDirectorio(root, "Imagenes");

        Archivo a1 = sa.crearArchivo(docs, "tarea1.txt", 3);
        Archivo a2 = sa.crearArchivo(docs, "tarea2.txt", 5);
        Archivo img1 = sa.crearArchivo(img, "foto1.jpg", 10);

        sa.imprimirEstructura();
    }
}

