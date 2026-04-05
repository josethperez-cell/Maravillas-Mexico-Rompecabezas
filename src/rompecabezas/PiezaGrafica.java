package rompecabezas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;

// ABSTRACCIÓN
abstract class ComponenteGrafico extends JLabel {
    // ATRIBUTO PROTEGIDO 
    protected boolean activo = true;
    public abstract void refrescar(); 
}

// HERENCIA 
public class PiezaGrafica extends ComponenteGrafico {
    // Coordenadas objetivo, dimensiones y estado
    // ENCAPSULAMIENTO 
    private int xObj, yObj, wR, hR, angulo = 0;
    private boolean encajada = false;
    private Point offset;
    private BufferedImage imagenOriginal;
    private Timer timerAnimacion;
    private double paso = 0;

    // El constructor se queda EXACTAMENTE igual que el tuyo para no romper nada
    public PiezaGrafica(BufferedImage img, int x, int y, int w, int h) {
        this.imagenOriginal = img; this.xObj = x; this.yObj = y; this.wR = w; this.hR = h;
        this.setBorder(null); this.angulo = 0; refrescar();
        this.setLocation(30 + xObj, 30 + yObj);

        // Control de click y rotación
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (encajada || (timerAnimacion != null && timerAnimacion.isRunning())) return;
                if (SwingUtilities.isRightMouseButton(e)) rotar();
                else { offset = e.getPoint(); if (getParent() != null) getParent().setComponentZOrder(PiezaGrafica.this, 0); }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!encajada && !SwingUtilities.isRightMouseButton(e) && (timerAnimacion == null || !timerAnimacion.isRunning())) verificar();
            }
        });

        // Arrastre con límites de ventana y colisión de tablero informativo
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!encajada && !SwingUtilities.isRightMouseButton(e) && offset != null && (timerAnimacion == null || !timerAnimacion.isRunning())) {
                    int nX = getX() + e.getX() - offset.x;
                    int nY = getY() + e.getY() - offset.y;

                    if (nX < 0) nX = 0;
                    if (nY < 0) nY = 0;
                    if (getParent() != null) {
                        if (nX + getWidth() > getParent().getWidth()) nX = getParent().getWidth() - getWidth();
                        if (nY + getHeight() > getParent().getHeight()) nY = getParent().getHeight() - getHeight();
                        
                        // Bloqueo de entrada al cuadro de datos históricos
                        Rectangle recCuadro = new Rectangle(960, 30, 430, 250);
                        Rectangle recPieza = new Rectangle(nX, nY, getWidth(), getHeight());

                        if (recPieza.intersects(recCuadro)) return; 
                    }
                    setPosicion(nX, nY); // Cambiado para usar SOBRECARGA abajo
                }
            }
        });
    }

    // SOBRECARGA DE MÉTODOS 
    public void setPosicion(int x, int y) { this.setLocation(x, y); }
    public void setPosicion(Point p) { this.setLocation(p.x, p.y); }

    // Animación suave de mezcla inicial
    public void iniciarAnimacionMezcla(int xMeta, int yMeta, int angFinal, VentanaJuego v) {
        final int xI = getX(), yI = getY();
        paso = 0;
        timerAnimacion = new Timer(20, e -> {
            paso += 0.07;
            if (paso >= 1.0) {
                setLocation(xMeta, yMeta); angulo = angFinal; refrescar();
                timerAnimacion.stop(); timerAnimacion = null; v.notificarPiezaLista();
            } else {
                double pro = 1 - Math.pow(1 - paso, 3);
                setLocation((int)(xI+(xMeta-xI)*pro), (int)(yI+(yMeta-yI)*pro));
            }
        });
        timerAnimacion.start();
    }

    // Lógica de rotación en 90 grados
    public void rotar() { if (!encajada) { angulo = (angulo + 90) % 360; refrescar(); } }

    // SOBREESCRITURA
    @Override
    public void refrescar() {
        int aC = (angulo == 90 || angulo == 270) ? hR : wR, hC = (angulo == 90 || angulo == 270) ? wR : hR;
        this.setSize(aC, hC);
        BufferedImage r = new BufferedImage(aC, hC, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = r.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform at = new AffineTransform();
        at.translate(aC/2.0, hC/2.0); at.rotate(Math.toRadians(angulo)); at.translate(-wR/2.0, -hR/2.0);
        g.drawImage(imagenOriginal, at, null); g.dispose();
        this.setIcon(new ImageIcon(r));
    }

    // Sistema de encaje automático magnético
    private void verificar() {
        if (Math.abs(getX()-(30+xObj)) < 35 && Math.abs(getY()-(30+yObj)) < 35 && angulo == 0) forzarEncaje(30, 30);
    }

    public void forzarEncaje(int oX, int oY) {
        angulo = 0; refrescar(); setPosicion(oX+xObj, oY+yObj); encajada = true;
        setBorder(BorderFactory.createLineBorder(new Color(50, 255, 50), 2));
        if (getParent() != null) getParent().setComponentZOrder(this, getParent().getComponentCount()-1);
    }

    // GETTERS/SETTERS
    public boolean isEncajada() { return encajada; }
    public int getAngulo() { return angulo; }
}