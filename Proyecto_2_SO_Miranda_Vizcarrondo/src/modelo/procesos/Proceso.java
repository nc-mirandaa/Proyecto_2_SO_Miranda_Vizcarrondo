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

    private String rutaObjetivo;  // p.ej. /root/Documentos/a.txt
    private int tamanoEnBloques;  // solo aplica para crear archivo

    public Proceso(TipoOperacionFS tipoOperacion, String rutaObjetivo, int tamanoEnBloques) {
        this.id = contadorIds++;
        this.estado = EstadoProceso.NUEVO;
        this.tipoOperacion = tipoOperacion;
        this.rutaObjetivo = rutaObjetivo;
        this.tamanoEnBloques = tamanoEnBloques;
    }

    public int getId() {
        return id;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public TipoOperacionFS getTipoOperacion() {
        return tipoOperacion;
    }

    public String getRutaObjetivo() {
        return rutaObjetivo;
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    @Override
    public String toString() {
        return "P" + id + " [" + tipoOperacion + ", " + estado + ", " + rutaObjetivo + "]";
    }
}

