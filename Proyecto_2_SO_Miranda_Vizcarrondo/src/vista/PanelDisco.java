/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;

/**
 *
 * @author Nathaly
 */

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Dimension;

import modelo.fs.SistemaArchivos;
import modelo.disco.Disco;

/**
 * Panel que dibuja los bloques del disco: ocupado/libre.
 */
public class PanelDisco extends JPanel {

    private SistemaArchivos sistemaArchivos;

    public PanelDisco(SistemaArchivos sistemaArchivos) {
        this.sistemaArchivos = sistemaArchivos;
        setPreferredSize(new Dimension(400, 120));
    }

    public void setSistemaArchivos(SistemaArchivos sa) {
        this.sistemaArchivos = sa;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (sistemaArchivos == null) return;

        Disco disco = sistemaArchivos.getDisco();
        boolean[] mapa = disco.mapaOcupacion();

        int n = mapa.length;
        int cols = 25;      // cuadros por fila
        int size = 14;      // tamaño de cada bloque
        int padding = 2;

        for (int i = 0; i < n; i++) {
            int fila = i / cols;
            int col = i % cols;

            int x = col * (size + padding);
            int y = fila * (size + padding);

            g.drawRect(x, y, size, size); // borde

            if (mapa[i]) {
                g.fillRect(x + 1, y + 1, size - 1, size - 1); // ocupado
            }
        }
    }
}

