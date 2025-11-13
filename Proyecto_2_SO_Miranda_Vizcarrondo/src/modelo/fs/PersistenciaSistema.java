/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.fs;

import java.io.*;

/**
 * Utilidad para guardar y cargar el estado del sistema de archivos
 * en un archivo de texto simple.
 *
 * Formato:
 *   Primera línea:  DISCO;N
 *   Directorio:     D;/ruta/completa
 *   Archivo:        F;/ruta/completa/archivo.ext;tamanoEnBloques
 */
public class PersistenciaSistema {

    // ===================== GUARDAR =====================

    public static void guardar(SistemaArchivos sa, String nombreArchivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {

            // Línea con cantidad de bloques del disco
            bw.write("DISCO;" + sa.getDisco().getCantidadBloques());
            bw.newLine();

            // Recorrer desde la raíz
            Directorio raiz = sa.getRaiz();
            String rutaRoot = "/root";
            escribirDirectorioRec(raiz, rutaRoot, bw);
        }
    }

    // No escribimos línea "D;/root", solo sus subdirectorios y archivos
    private static void escribirDirectorioRec(Directorio dir, String rutaActual, BufferedWriter bw) throws IOException {

        // Primero, escribir este directorio si no es la raíz
        if (dir.getPadre() != null) {
            bw.write("D;" + rutaActual);
            bw.newLine();
        }

        // Luego hijos
        for (int i = 0; i < dir.getHijos().size(); i++) {
            NodoFS hijo = dir.getHijos().get(i);
            if (hijo.esDirectorio()) {
                Directorio sub = (Directorio) hijo;
                String nuevaRuta = rutaActual + "/" + sub.getNombre();
                escribirDirectorioRec(sub, nuevaRuta, bw);
            } else {
                Archivo a = (Archivo) hijo;
                String rutaArchivo = rutaActual + "/" + a.getNombre();
                bw.write("F;" + rutaArchivo + ";" + a.getTamanoEnBloques());
                bw.newLine();
            }
        }
    }

    // ===================== CARGAR =====================

    public static SistemaArchivos cargar(String nombreArchivo) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(nombreArchivo))) {

            String linea = br.readLine();
            if (linea == null || !linea.startsWith("DISCO;")) {
                throw new IOException("Archivo de estado inválido: falta línea DISCO;");
            }

            String[] partes = linea.split(";");
            int bloques = Integer.parseInt(partes[1].trim());

            SistemaArchivos sa = new SistemaArchivos(bloques);

            String l;
            while ((l = br.readLine()) != null) {
                l = l.trim();
                if (l.isEmpty()) continue;

                String[] p = l.split(";");
                if (p[0].equals("D")) {
                    String rutaDir = p[1].trim();
                    crearDirectorioPorRuta(sa, rutaDir);
                } else if (p[0].equals("F")) {
                    String rutaArchivo = p[1].trim();
                    int tam = Integer.parseInt(p[2].trim());

                    int idx = rutaArchivo.lastIndexOf('/');
                    String rutaPadre = (idx <= 0) ? "/root" : rutaArchivo.substring(0, idx);
                    String nombre = (idx == -1 || idx == rutaArchivo.length() - 1)
                            ? rutaArchivo
                            : rutaArchivo.substring(idx + 1);

                    Directorio padre = crearDirectorioPorRuta(sa, rutaPadre);
                    if (padre != null) {
                        sa.crearArchivo(padre, nombre, tam);
                    }
                }
            }

            return sa;
        }
    }

    /**
     * Garantiza que exista la ruta de directorio dada.
     * Si ya existe, la retorna; si no, crea los directorios faltantes.
     */
    private static Directorio crearDirectorioPorRuta(SistemaArchivos sa, String rutaCompleta) {
        if (rutaCompleta == null || rutaCompleta.isEmpty()
                || rutaCompleta.equals("/") || rutaCompleta.equals("/root")) {
            return sa.getRaiz();
        }

        String limpia = rutaCompleta.trim();
        if (limpia.startsWith("/")) limpia = limpia.substring(1);

        String[] partes = limpia.split("/");

        Directorio actual = sa.getRaiz();
        int i = 0;
        if (partes.length > 0 && partes[0].equals(actual.getNombre())) {
            i = 1; // saltar "root"
        }

        for (; i < partes.length; i++) {
            String nombreDir = partes[i];
            if (nombreDir.isEmpty()) continue;

            NodoFS hijo = actual.buscarHijoPorNombre(nombreDir);
            if (hijo == null || !hijo.esDirectorio()) {
                // crear nuevo directorio
                Directorio nuevo = sa.crearDirectorio(actual, nombreDir);
                actual = nuevo;
            } else {
                actual = (Directorio) hijo;
            }
        }
        return actual;
    }
}
