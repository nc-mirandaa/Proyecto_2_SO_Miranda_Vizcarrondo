/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author Gabo
 */

import javax.swing.table.AbstractTableModel;

import control.GestorSistema;
import modelo.fs.ListaEnlazada;
import modelo.procesos.Proceso;
import modelo.procesos.TipoOperacionFS;
import modelo.procesos.EstadoProceso;

public class ModeloColaProcesos extends AbstractTableModel {

    private final String[] columnas = { "ID", "Tipo", "Ruta", "Estado" };

    private GestorSistema gestor;

    public ModeloColaProcesos(GestorSistema gestor) {
        this.gestor = gestor;
    }

    @Override
    public int getRowCount() {
        return gestor.getProcesosEnCola().size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnas[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ListaEnlazada<Proceso> cola = gestor.getProcesosEnCola();
        Proceso p = cola.get(rowIndex);

        switch (columnIndex) {
            case 0: return p.getId();
            case 1: return p.getTipoOperacion().name();
            case 2: return p.getRutaObjetivo();
            case 3: return p.getEstado().name();
            default: return "";
        }
    }

    public void refrescar() {
        fireTableDataChanged();
    }
}