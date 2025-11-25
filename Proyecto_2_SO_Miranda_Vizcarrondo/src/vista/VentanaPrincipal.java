/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package vista;

/**
 *
 * @author Nathaly
 */

import control.GestorSistema;
import modelo.fs.*;
import modelo.procesos.*;// RolUsuario, planificadores, etc.

import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import javax.swing.JOptionPane;
import java.util.Random;
import java.awt.BorderLayout;

import javax.swing.JFileChooser;
import java.io.File;
import java.io.IOException;



public class VentanaPrincipal extends javax.swing.JFrame {

    private GestorSistema gestor;
    private ModeloTablaAsignacion modeloTabla;
    private PanelDisco panelDiscoDibujo;
    private ModeloColaProcesos modeloColaProcesos;
    
    /**
     * Creates new form VentanaPrincipal
     */
    public VentanaPrincipal() {
    initComponents();

    setTitle("Simulador de Sistema de Archivos");

    // Backend
    gestor = new GestorSistema(50);
    inicializarFSInicial();  // crea DocsA, DocsB y 10 archivos aleatorios

    // Árbol
    treeFS.setModel(construirModeloArbol());

    // Tabla de asignación
    modeloTabla = new ModeloTablaAsignacion(gestor.getSistemaArchivos());
    tableAsignacion.setModel(modeloTabla);

    // Panel de disco (dibujo)
    panelDisco.setLayout(new BorderLayout());
    panelDiscoDibujo = new PanelDisco(gestor.getSistemaArchivos());
    panelDisco.add(panelDiscoDibujo, BorderLayout.CENTER);
    
    modeloColaProcesos = new ModeloColaProcesos(gestor);
    tableCola.setModel(modeloColaProcesos);

    // Ajuste de divisores
    splitFS.setDividerLocation(200);       // pixels
    splitDerechaFS.setDividerLocation(200);

    // === Inicializar combos ===
    comboRol.removeAllItems();
    comboRol.addItem("ADMIN");
    comboRol.addItem("USUARIO");
    comboRol.setSelectedItem(gestor.getRolActual().name());


    comboPlanificador.removeAllItems();
    comboPlanificador.addItem("FIFO");
    comboPlanificador.addItem("SSTF");
    comboPlanificador.addItem("SCAN");
    comboPlanificador.addItem("C-SCAN");
    comboPlanificador.setSelectedIndex(0); // FIFO por defecto

    // === Listeners de combos ===
    comboRol.addActionListener(e -> {
        String seleccion = (String) comboRol.getSelectedItem();
        gestor.setRolActual(RolUsuario.valueOf(seleccion));
        actualizarHabilitadoPorRol();
    });

    comboPlanificador.addActionListener(e -> {
        String sel = (String) comboPlanificador.getSelectedItem();
        if ("FIFO".equals(sel)) {
            gestor.setPlanificador(new PlanificadorFIFO());
        } else if ("SSTF".equals(sel)) {
            gestor.setPlanificador(new PlanificadorSSTF());
        } else if ("SCAN".equals(sel)) {
            gestor.setPlanificador(new PlanificadorSCAN());
        } else if ("C-SCAN".equals(sel)) {
            gestor.setPlanificador(new PlanificadorCSCAN());
        }
    });

    // === Listeners de botones ===
    btnCrearArchivo.addActionListener(e -> accCrearArchivo());
    btnCrearDirectorio.addActionListener(e -> accCrearDirectorio());
    btnEliminar.addActionListener(e -> accEliminar());
    btnRenombrar.addActionListener(e -> accRenombrar());
    btnEjecutarPaso.addActionListener(e -> {
        gestor.ejecutarPaso();
        refrescarTodo();
        
        String error = gestor.getUltimoError();
        if (error != null) {
            javax.swing.JOptionPane.showMessageDialog(
                this,
                error,
                "Error al ejecutar proceso",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }  
    });
    btnGuardar.addActionListener(e -> accGuardar());
    btnCargar.addActionListener(e -> accCargar());
    btnRefrescar.addActionListener(e -> refrescarTodo());

    // Ajustar qué puede hacer cada rol
    actualizarHabilitadoPorRol();
}


    // ============ MÉTODO: estado inicial del FS ============
    private void inicializarFSInicial() {
    SistemaArchivos sa = gestor.getSistemaArchivos();
    Directorio root = sa.getRaiz();

    sa.crearDirectorio(root, "DocsA");
    sa.crearDirectorio(root, "DocsB");

    String[] extensiones = { ".txt", ".log", ".dat", ".csv", ".jpg", ".bin" };
    Random rnd = new Random();

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
}


    // ============ MÉTODOS: construir modelo del árbol ============
    private TreeModel construirModeloArbol() {
    SistemaArchivos sa = gestor.getSistemaArchivos();
    Directorio raiz = sa.getRaiz();

    DefaultMutableTreeNode nodoRoot = construirNodoRec(raiz, "/root");
    return new DefaultTreeModel(nodoRoot);
}

private DefaultMutableTreeNode construirNodoRec(Directorio dir, String rutaActual) {
    DefaultMutableTreeNode nodoDir = new DefaultMutableTreeNode(rutaActual);

    ListaEnlazada<NodoFS> hijos = dir.getHijos();
    for (int i = 0; i < hijos.size(); i++) {
        NodoFS hijo = hijos.get(i);
        if (hijo.esDirectorio()) {
            Directorio sub = (Directorio) hijo;
            String rutaHijo = rutaActual + "/" + sub.getNombre();
            nodoDir.add(construirNodoRec(sub, rutaHijo));
        } else {
            Archivo a = (Archivo) hijo;
            String rutaArchivo = rutaActual + "/" + a.getNombre();
            nodoDir.add(new DefaultMutableTreeNode(rutaArchivo));
        }
    }
    return nodoDir;
}

    // Ruta completa del nodo seleccionado en el árbol (archivo o directorio)
    private String obtenerRutaSeleccionadaNodo() {
        javax.swing.tree.TreePath path = treeFS.getSelectionPath();
        if (path == null) return null;
        Object last = path.getLastPathComponent();
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) last;
        return nodo.getUserObject().toString();
    }

    // Ruta de directorio: si hay archivo seleccionado, devuelve la ruta del padre
    private String obtenerRutaSeleccionadaDirectorio() {
        String ruta = obtenerRutaSeleccionadaNodo();
        if (ruta == null) return null;

        NodoFS nodo = gestor.getSistemaArchivos().buscarNodoPorRuta(ruta);
        if (nodo == null) return null;

        if (nodo.esDirectorio()) {
            return ruta;
        } else {
            Directorio padre = nodo.getPadre();
            return (padre != null) ? padre.getRutaCompleta() : "/root";
        }
    }

    private void actualizarHabilitadoPorRol() {
        boolean esAdmin = (gestor.getRolActual() == RolUsuario.ADMIN);

        btnCrearArchivo.setEnabled(esAdmin);
        btnCrearDirectorio.setEnabled(esAdmin);
        btnEliminar.setEnabled(esAdmin);
        btnRenombrar.setEnabled(esAdmin);

        // Estos siempre se pueden usar
        btnEjecutarPaso.setEnabled(true);
        btnGuardar.setEnabled(true);
        btnCargar.setEnabled(true);
        btnRefrescar.setEnabled(true);
    }



    // ============ MÉTODO: refrescar toda la pestaña de FS ============
    private void refrescarTodo() {
    treeFS.setModel(construirModeloArbol());
    modeloTabla.fireTableDataChanged();
    panelDiscoDibujo.repaint();

    if (modeloColaProcesos != null) {
        modeloColaProcesos.refrescar();
    }
}

    
    private void accCrearArchivo() {
    String rutaDir = obtenerRutaSeleccionadaDirectorio();
    if (rutaDir == null) {
        JOptionPane.showMessageDialog(this, "Selecciona un directorio en el árbol.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");
    if (nombre == null || nombre.trim().isEmpty()) return;

    String tamStr = JOptionPane.showInputDialog(this, "Tamaño en bloques:");
    if (tamStr == null) return;

    int tam;
    try {
        tam = Integer.parseInt(tamStr.trim());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "Tamaño inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    String ruta = rutaDir + "/" + nombre.trim();
    boolean ok = gestor.solicitarCrearArchivo(ruta, tam);
    if (ok) {
        
        refrescarTodo();   // solo refrescamos la vista (opcional incluso)
    }
}

    private void accCrearDirectorio() {
    String rutaDir = obtenerRutaSeleccionadaDirectorio();
    if (rutaDir == null) {
        JOptionPane.showMessageDialog(this, "Selecciona un directorio en el árbol.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String nombre = JOptionPane.showInputDialog(this, "Nombre del nuevo directorio:");
    if (nombre == null || nombre.trim().isEmpty()) return;

    String ruta = rutaDir + "/" + nombre.trim();
    boolean ok = gestor.solicitarCrearDirectorio(ruta);
    if (ok) {
        // antes: gestor.ejecutarPaso();
        refrescarTodo();
    }
}


    private void accRenombrar() {
    String ruta = obtenerRutaSeleccionadaNodo();
    if (ruta == null || "/root".equals(ruta)) {
        JOptionPane.showMessageDialog(this, "Selecciona un archivo o directorio válido.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String nuevo = JOptionPane.showInputDialog(this, "Nuevo nombre (sin ruta):");
    if (nuevo == null || nuevo.trim().isEmpty()) return;

    boolean ok = gestor.solicitarRenombrar(ruta, nuevo.trim());
    if (ok) {
        refrescarTodo();
    }
}

    private void accGuardar() {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Guardar estado del sistema");

    int opcion = fc.showSaveDialog(this);
    if (opcion == JFileChooser.APPROVE_OPTION) {
        File archivo = fc.getSelectedFile();
        try {
            PersistenciaSistema.guardar(gestor.getSistemaArchivos(), archivo.getAbsolutePath());
            JOptionPane.showMessageDialog(this,
                    "Estado guardado en:\n" + archivo.getAbsolutePath(),
                    "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}


private void accCargar() {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Cargar estado del sistema");

    int opcion = fc.showOpenDialog(this);
    if (opcion == JFileChooser.APPROVE_OPTION) {
        File archivo = fc.getSelectedFile();

        try {
            SistemaArchivos nuevo = PersistenciaSistema.cargar(archivo.getAbsolutePath());

            // Actualizar el gestor con el nuevo FS
            gestor.setSistemaArchivos(nuevo);

            // Actualizar referencias visuales
            modeloTabla.setSistemaArchivos(nuevo);
            panelDiscoDibujo.setSistemaArchivos(nuevo);

            refrescarTodo();

            JOptionPane.showMessageDialog(this,
                    "Estado cargado desde:\n" + archivo.getAbsolutePath(),
                    "OK", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}



    private void accEliminar() {
    String ruta = obtenerRutaSeleccionadaNodo();
    if (ruta == null || "/root".equals(ruta)) {
        JOptionPane.showMessageDialog(this, "Selecciona un archivo o directorio válido (no /root).", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }

    NodoFS nodo = gestor.getSistemaArchivos().buscarNodoPorRuta(ruta);
    if (nodo == null) {
        JOptionPane.showMessageDialog(this, "Nodo no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    int resp = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas eliminar " + ruta + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
    if (resp != JOptionPane.YES_OPTION) return;

    boolean ok;
    if (nodo.esDirectorio()) {
        ok = gestor.solicitarEliminarDirectorio(ruta);
    } else {
        ok = gestor.solicitarEliminarArchivo(ruta);
    }

    if (ok) {
        // antes: gestor.ejecutarPaso();
        refrescarTodo();
    }
}

      


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        tabbedPrincipal = new javax.swing.JTabbedPane();
        panelFS = new javax.swing.JPanel();
        splitFS = new javax.swing.JSplitPane();
        scrollArbol = new javax.swing.JScrollPane();
        treeFS = new javax.swing.JTree();
        splitDerechaFS = new javax.swing.JSplitPane();
        scrollTablaAsignacion = new javax.swing.JScrollPane();
        tableAsignacion = new javax.swing.JTable();
        panelDisco = new javax.swing.JPanel();
        panelControles = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        comboRol = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        comboPlanificador = new javax.swing.JComboBox<>();
        btnCrearArchivo = new javax.swing.JButton();
        btnCrearDirectorio = new javax.swing.JButton();
        btnEliminar = new javax.swing.JButton();
        btnRenombrar = new javax.swing.JButton();
        btnEjecutarPaso = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnCargar = new javax.swing.JButton();
        btnRefrescar = new javax.swing.JButton();
        panelCola = new javax.swing.JPanel();
        scrollCola = new javax.swing.JScrollPane();
        tableCola = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        panelFS.setLayout(new java.awt.BorderLayout());

        scrollArbol.setViewportView(treeFS);

        splitFS.setLeftComponent(scrollArbol);

        splitDerechaFS.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        tableAsignacion.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollTablaAsignacion.setViewportView(tableAsignacion);

        splitDerechaFS.setTopComponent(scrollTablaAsignacion);

        javax.swing.GroupLayout panelDiscoLayout = new javax.swing.GroupLayout(panelDisco);
        panelDisco.setLayout(panelDiscoLayout);
        panelDiscoLayout.setHorizontalGroup(
            panelDiscoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 668, Short.MAX_VALUE)
        );
        panelDiscoLayout.setVerticalGroup(
            panelDiscoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 277, Short.MAX_VALUE)
        );

        splitDerechaFS.setRightComponent(panelDisco);

        splitFS.setRightComponent(splitDerechaFS);

        panelFS.add(splitFS, java.awt.BorderLayout.CENTER);

        tabbedPrincipal.addTab("Sistema de Archivos", panelFS);

        panelControles.setLayout(new java.awt.GridLayout(3, 4));

        jLabel3.setText("Rol: ");
        panelControles.add(jLabel3);

        comboRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelControles.add(comboRol);

        jLabel1.setText("Planificador: ");
        panelControles.add(jLabel1);

        comboPlanificador.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        panelControles.add(comboPlanificador);

        btnCrearArchivo.setText("Crear Archivo");
        panelControles.add(btnCrearArchivo);

        btnCrearDirectorio.setText("Crear Directorio");
        panelControles.add(btnCrearDirectorio);

        btnEliminar.setText("Eliminar");
        panelControles.add(btnEliminar);

        btnRenombrar.setText("Renombrar");
        panelControles.add(btnRenombrar);

        btnEjecutarPaso.setText("Ejecutar Paso");
        panelControles.add(btnEjecutarPaso);

        btnGuardar.setText("Guardar Estado");
        panelControles.add(btnGuardar);

        btnCargar.setText("Cargar Estado");
        panelControles.add(btnCargar);

        btnRefrescar.setText("Refrescar");
        panelControles.add(btnRefrescar);

        tabbedPrincipal.addTab("Controles", panelControles);

        panelCola.setLayout(new java.awt.BorderLayout());

        tableCola.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrollCola.setViewportView(tableCola);

        panelCola.add(scrollCola, java.awt.BorderLayout.CENTER);

        tabbedPrincipal.addTab("Cola de procesos", panelCola);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 704, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VentanaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VentanaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnCrearArchivo;
    private javax.swing.JButton btnCrearDirectorio;
    private javax.swing.JButton btnEjecutarPaso;
    private javax.swing.JButton btnEliminar;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnRefrescar;
    private javax.swing.JButton btnRenombrar;
    private javax.swing.JComboBox<String> comboPlanificador;
    private javax.swing.JComboBox<String> comboRol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel panelCola;
    private javax.swing.JPanel panelControles;
    private javax.swing.JPanel panelDisco;
    private javax.swing.JPanel panelFS;
    private javax.swing.JScrollPane scrollArbol;
    private javax.swing.JScrollPane scrollCola;
    private javax.swing.JScrollPane scrollTablaAsignacion;
    private javax.swing.JSplitPane splitDerechaFS;
    private javax.swing.JSplitPane splitFS;
    private javax.swing.JTabbedPane tabbedPrincipal;
    private javax.swing.JTable tableAsignacion;
    private javax.swing.JTable tableCola;
    private javax.swing.JTree treeFS;
    // End of variables declaration//GEN-END:variables
}
