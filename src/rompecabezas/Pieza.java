package rompecabezas;

import java.awt.image.BufferedImage;

interface Interactuable {
    void mover(int nuevaX, int nuevaY);
}

// Clase Pieza que hereda de una posición base (Herencia simple)
public class Pieza implements Interactuable {
    private BufferedImage imagenFragmento;
    private int xActual, yActual;
    private int xObjetivo, yObjetivo; // Posición correcta donde debe encajar
    private boolean encajada;

    public Pieza(BufferedImage img, int xObj, int yObj) {
        this.imagenFragmento = img;
        this.xObjetivo = xObj;
        this.yObjetivo = yObj;
        this.encajada = false;
    }

    @Override
    public void mover(int nuevaX, int nuevaY) {
        if (!encajada) {
            this.xActual = nuevaX;
            this.yActual = nuevaY;
            verificarPosicion();
        }
    }

    private void verificarPosicion() {
        // Lógica para dejar estática la pieza si está en su lugar
        if (Math.abs(xActual - xObjetivo) < 10 && Math.abs(yActual - yObjetivo) < 10) {
            this.xActual = xObjetivo;
            this.yActual = yObjetivo;
            this.encajada = true;
        }
    }
    
    // Getters necesarios para la interfaz gráfica
    public BufferedImage getImagen() { return imagenFragmento; }
    public boolean isEncajada() { return encajada; }
}