/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.fs;

import java.io.*;
import java.util.*;

/**
 * Maneja la persistencia del sistema de archivos.
 * Guarda y carga la estructura (directorios y archivos) en un archivo de texto.
 * Formato:
 *   D;/ruta/directorio
 *   F;/ruta/archivo;tamano;primerBloque
 */
public class PersistenciaSistema {

    // Guarda toda la estructura en un archivo de texto
    public static void guardar(SistemaArchivos sistema, String rutaArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            guardarDirectorioRecursivo(sistema.getRaiz(), writer);
            System.out.println("✅ Sistema de archivos guardado en: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("⚠ Error al guardar el sistema de archivos: " + e.getMessage());
        }
    }

    // Carga desde un archivo de texto
    public static SistemaArchivos cargar(String rutaArchivo, int bloquesDisco) {
        SistemaArchivos sistema = new SistemaArchivos(bloquesDisco);
        File file = new File(rutaArchivo);

        if (!file.exists()) {
            System.out.println("No existe archivo de configuración, se inicia nuevo sistema.");
            return sistema;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                procesarLinea(linea, sistema);
            }
            System.out.println("📂 Sistema de archivos cargado desde: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("⚠ Error al cargar el sistema de archivos: " + e.getMessage());
        }

        return sistema;
    }

    // Guarda recursivamente directorios y archivos
    private static void guardarDirectorioRecursivo(Directorio dir, PrintWriter writer) {
        writer.println("D;" + dir.getRutaCompleta());

        dir.getHijos().forEach(hijo -> {
            if (hijo.esDirectorio()) {
                guardarDirectorioRecursivo((Directorio) hijo, writer);
            } else {
                Archivo a = (Archivo) hijo;
                writer.println("F;" + a.getRutaCompleta() + ";" + a.getTamanoEnBloques() + ";" + a.getPrimerBloque());
            }
        });
    }

    // Procesa una línea del archivo cargado
    private static void procesarLinea(String linea, SistemaArchivos sistema) {
        String[] partes = linea.split(";");
        if (partes.length == 0) return;

        switch (partes[0]) {
            case "D" -> {
                String ruta = partes[1];
                String rutaPadre = obtenerRutaDirectorio(ruta);
                String nombreDir = obtenerNombreFinal(ruta);
                Directorio padre = buscarDirectorioPorRuta(sistema.getRaiz(), rutaPadre);
                if (padre != null) {
                    sistema.crearDirectorio(padre, nombreDir);
                }
            }
            case "F" -> {
                if (partes.length < 4) return;
                String ruta = partes[1];
                int tam = Integer.parseInt(partes[2]);
                int bloque = Integer.parseInt(partes[3]);

                String rutaPadre = obtenerRutaDirectorio(ruta);
                String nombreArchivo = obtenerNombreFinal(ruta);
                Directorio padre = buscarDirectorioPorRuta(sistema.getRaiz(), rutaPadre);
                if (padre != null) {
                    Archivo a = sistema.crearArchivo(padre, nombreArchivo, tam);
                    if (a != null) a.setPrimerBloque(bloque);
                }
            }
        }
    }

    // Utilidades
    private static String obtenerRutaDirectorio(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx <= 0) return "/root";
        return rutaCompleta.substring(0, idx);
    }

    private static String obtenerNombreFinal(String rutaCompleta) {
        int idx = rutaCompleta.lastIndexOf('/');
        if (idx == -1 || idx == rutaCompleta.length() - 1) return rutaCompleta;
        return rutaCompleta.substring(idx + 1);
    }

    private static Directorio buscarDirectorioPorRuta(Directorio raiz, String ruta) {
        if (ruta.equals("/root") || ruta.equals("/")) return raiz;

        String limpia = ruta.replaceFirst("^/", "");
        String[] partes = limpia.split("/");
        Directorio actual = raiz;

        for (int i = 1; i < partes.length; i++) {
            NodoFS hijo = actual.buscarHijoPorNombre(partes[i]);
            if (hijo == null || !hijo.esDirectorio()) return null;
            actual = (Directorio) hijo;
        }
        return actual;
    }
}