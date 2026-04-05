package rompecabezas;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class GestorImagenes {
    public ArrayList<PiezaGrafica> dividirImagen(String ruta, int filas, int columnas) {
        ArrayList<PiezaGrafica> piezas = new ArrayList<>();
        try {
            BufferedImage imgCargada = ImageIO.read(new File(ruta));
            
            // LIENZO FIJO: 900 x 700
            BufferedImage img = new BufferedImage(900, 700, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(imgCargada, 0, 0, 900, 700, null);
            g.dispose();

            // Cálculo dinámico del tamaño de pieza
            int wP = img.getWidth() / columnas;
            int hP = img.getHeight() / filas;

            for (int i = 0; i < filas; i++) {
                for (int j = 0; j < columnas; j++) {
                    BufferedImage sub = img.getSubimage(j * wP, i * hP, wP, hP);
                    piezas.add(new PiezaGrafica(sub, j * wP, i * hP, wP, hP));
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen: " + ruta);
        }
        return piezas;
    }
}