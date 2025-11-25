/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo.procesos;

/**
 *
 * @author Nathaly
 */

public class Proceso {

    private static int contadorIds = 1;

    private final int id;
    private EstadoProceso estado;
    private TipoOperacionFS tipoOperacion;

    private String rutaObjetivo;   // p.ej. /root/Documentos/a.txt  (o dir)
    private int tamanoEnBloques;   // solo aplica al crear archivo

    // NUEVO: para RENOMBRAR (nombre destino)
    private String nuevoNombre;    // null si no aplica
    
    private int posicionES = -1;

    public int getPosicionES() {
        return posicionES;
    }

    public void setPosicionES(int pos) {
        this.posicionES = pos;
    }

    public Proceso(TipoOperacionFS tipoOperacion, String rutaObjetivo, int tamanoEnBloques) {
        this.id = contadorIds++;
        this.estado = EstadoProceso.NUEVO;
        this.tipoOperacion = tipoOperacion;
        this.rutaObjetivo = rutaObjetivo;
        this.tamanoEnBloques = tamanoEnBloques;
    }

    // Constructor útil para operaciones que no usan tamaño
    public Proceso(TipoOperacionFS tipoOperacion, String rutaObjetivo) {
        this(tipoOperacion, rutaObjetivo, 0);
    }

    // Constructor para RENOMBRAR
    public Proceso(TipoOperacionFS tipoOperacion, String rutaObjetivo, String nuevoNombre) {
        this(tipoOperacion, rutaObjetivo, 0);
        this.nuevoNombre = nuevoNombre;
    }

    public int getId() { return id; }
    public EstadoProceso getEstado() { return estado; }
    public void setEstado(EstadoProceso estado) { this.estado = estado; }
    public TipoOperacionFS getTipoOperacion() { return tipoOperacion; }
    public String getRutaObjetivo() { return rutaObjetivo; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public String getNuevoNombre() { return nuevoNombre; }

    @Override
    public String toString() {
        String extra = (nuevoNombre != null) ? (", nuevoNombre=" + nuevoNombre) : "";
        return "P" + id + " [" + tipoOperacion + ", " + estado + ", " + rutaObjetivo + extra + "]";
    }
}
