/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author Nathaly
 */

import javax.swing.table.AbstractTableModel;
import modelo.fs.EntradaTablaAsignacion;
import modelo.fs.ListaEnlazada;
import modelo.fs.SistemaArchivos;

/**
 * TableModel para mostrar la tabla de asignación de archivos en un JTable.
 */
public class ModeloTablaAsignacion extends AbstractTableModel {

    private final String[] columnas = { "ID", "Nombre", "Bloques", "Primer bloque" };

    private SistemaArchivos sistemaArchivos;

    public ModeloTablaAsignacion(SistemaArchivos sistemaArchivos) {
        this.sistemaArchivos = sistemaArchivos;
    }

    public void setSistemaArchivos(SistemaArchivos sa) {
        this.sistemaArchivos = sa;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        if (sistemaArchivos == null) return 0;
        ListaEnlazada<EntradaTablaAsignacion> tabla = sistemaArchivos.getTablaAsignacion();
        return tabla.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnas[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ListaEnlazada<EntradaTablaAsignacion> tabla = sistemaArchivos.getTablaAsignacion();
        EntradaTablaAsignacion entrada = tabla.get(rowIndex);

        switch (columnIndex) {
            case 0: return entrada.getIdArchivo();
            case 1: return entrada.getNombreArchivo();
            case 2: return entrada.getTamanoEnBloques();
            case 3: return entrada.getPrimerBloque();
            default: return "";
        }
    }
}

