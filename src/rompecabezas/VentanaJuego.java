package rompecabezas;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class VentanaJuego extends JPanel { 
    // Componentes del tablero y UI de información
    private JPanel tablero, cuadroTexto;
    private JLabel lblCronometro, lblNombreSitio, lblDatoCurioso; 
    private Timer crono, cronoAyuda, cronoFantasma;
    private int segundosTranscurridos, nivelActualCargado;
    private JButton btnAyuda, btnIniciar, btnRendirse, btnFantasma, btnVolver; 
    private int cooldownAyuda = 0, cooldownFantasma = 0, piezasEnMovimiento = 0; 
    private boolean mostrandoFantasma = false, enJuego = false, modoHardcore = false; 
    private String imagenActual = ""; 
    private BufferedImage imgPrevia = null; 
    private int filasActuales, columnasActuales;
    private int tiempoLimite = 90;
    private PantallaInicio parentFrame;

    // Listas de datos informativos de los destinos
    private final String[] nombresNiveles = { "Castillo de Chapultepec", "Chichén Itzá", "Teotihuacán", "Monte Albán", "Uxmal", "Dolores Hidalgo", "Cenotes de Yucatán", "Pico de Orizaba", "San Juan de Ulúa", "Palacio Nacional" };
    private final String[] datosInformativos = { "<b>Dato Histórico:</b> Único Castillo Real en América.<br><br><b>Curiosidad:</b> Fue usado como set para la película 'Romeo + Juliet' de DiCaprio.", "<b>Dato Histórico:</b> El descenso de Kukulkán ocurre en los equinoccios.<br><br><b>Curiosidad:</b> Si aplaudes frente a la pirámide, el eco suena como el ave Quetzal.", "<b>Dato Histórico:</b> 'Lugar donde los hombres se hacen dioses'.<br><br><b>Curiosidad:</b> La Pirámide del Sol se construyó sobre una cueva que creían sagrada.", "<b>Dato Histórico:</b> Capital de los zapotecos.<br><br><b>Curiosidad:</b> El cerro donde está fue nivelado a mano hace más de 1,500 años.", "<b>Dato Histórico:</b> Famosa por su arquitectura Puuc.<br><br><b>Curiosidad:</b> La Pirámide del Adivino tiene base ovalada, algo único en el mundo maya.", "<b>Dato Histórico:</b> Cuna de la Independencia.<br><br><b>Curiosidad:</b> El cura Hidalgo enseñaba a los locales alfarería y cultivo de gusanos de seda.", "<b>Dato Histórico:</b> Puertas al inframundo (Xibalbá).<br><br><b>Curiosidad:</b> Existen más de 6,000 en Yucatán, pero pocos están abiertos al público.", "<b>Dato Histórico:</b> Montaña más alta de México.<br><br><b>Curiosidad:</b> Su nombre náhuatl es Citlaltépetl, que significa 'Monte de la Estrella'.", "<b>Dato Histórico:</b> Último baluarte español.<br><br><b>Curiosidad:</b> Sus muros son de coral; la humedad era tan alta que era una prisión mortal.", "<b>Dato Histórico:</b> Sede del Poder Ejecutivo.<br><br><b>Curiosidad:</b> Diego Rivera tardó 22 años en pintar los murales de su escalinata principal." };

    public VentanaJuego(int nivelInicial, int filas, boolean hardcore, PantallaInicio parent) {
        this.nivelActualCargado = nivelInicial;
        this.filasActuales = filas;
        this.columnasActuales = filas;
        this.modoHardcore = hardcore;
        this.parentFrame = parent;

        setLayout(new BorderLayout());

        // Diseño del panel superior de control
        JPanel panelNorte = new JPanel();
        panelNorte.setBackground(new Color(20, 20, 25));
        panelNorte.setPreferredSize(new Dimension(0, 70));
        
        lblCronometro = new JLabel("TIEMPO: 00:00");
        lblCronometro.setForeground(Color.WHITE);
        lblCronometro.setFont(new Font("Monospaced", Font.BOLD, 24));
        
        btnIniciar = crearBotonModerno("INICIAR", new Color(46, 204, 113));
        btnIniciar.addActionListener(e -> empezarPartidaReal());
        btnAyuda = crearBotonModerno("PISTA", new Color(52, 152, 219));
        btnAyuda.setEnabled(false);
        btnAyuda.addActionListener(e -> usarAyuda());
        btnFantasma = crearBotonModerno("FANTASMA", new Color(155, 89, 182));
        btnFantasma.setEnabled(false);
        btnFantasma.addActionListener(e -> usarFantasma());
        btnRendirse = crearBotonModerno("RENDIRSE", new Color(231, 76, 60));
        btnRendirse.setEnabled(false);
        btnRendirse.addActionListener(e -> rendirse());
        
        btnVolver = crearBotonModerno("MENÚ", Color.GRAY);
        btnVolver.addActionListener(e -> parentFrame.volverAlMenu());

        panelNorte.add(lblCronometro); panelNorte.add(Box.createRigidArea(new Dimension(20, 0)));
        panelNorte.add(btnIniciar); panelNorte.add(btnAyuda); panelNorte.add(btnFantasma); 
        panelNorte.add(btnRendirse); panelNorte.add(btnVolver);
        add(panelNorte, BorderLayout.NORTH);

        // Tablero de renderizado
        tablero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, new Color(45, 45, 50), 0, getHeight(), new Color(20, 20, 25)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                if (imgPrevia != null) {
                    if (!enJuego) g2d.drawImage(imgPrevia, 30, 30, 900, 700, null);
                    else if (mostrandoFantasma) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                        g2d.drawImage(imgPrevia, 30, 30, 900, 700, null);
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    }
                }
                g2d.setColor(modoHardcore ? new Color(255, 0, 0, 100) : new Color(255, 255, 255, 50));
                g2d.drawRect(30, 30, 900, 700);
            }
        };
        tablero.setLayout(null); add(tablero, BorderLayout.CENTER);

        // Paneles laterales informativos
        cuadroTexto = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 15)); g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2d.setColor(new Color(255, 215, 0, 150)); g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
            }
        };
        cuadroTexto.setOpaque(false); cuadroTexto.setBounds(960, 30, 430, 280); 
        lblNombreSitio = new JLabel(nombresNiveles[nivelActualCargado-1].toUpperCase(), SwingConstants.CENTER);
        lblNombreSitio.setForeground(new Color(255, 215, 0));
        lblNombreSitio.setFont(new Font("Verdana", Font.BOLD, 24));
        lblNombreSitio.setBorder(BorderFactory.createEmptyBorder(15,0,0,0));
        cuadroTexto.add(lblNombreSitio, BorderLayout.NORTH);
        lblDatoCurioso = new JLabel("<html><body style='padding: 20px; text-align: center;'>" + datosInformativos[nivelActualCargado-1] + "</body></html>");
        lblDatoCurioso.setForeground(new Color(220, 220, 220));
        lblDatoCurioso.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        cuadroTexto.add(lblDatoCurioso, BorderLayout.CENTER);
        tablero.add(cuadroTexto);

        // Controladores de tiempo y tiempos de espera
        crono = new Timer(1000, e -> {
            if (modoHardcore) {
                tiempoLimite--;
                lblCronometro.setText(String.format("TIME: %02d:%02d", tiempoLimite/60, tiempoLimite%60));
                if (tiempoLimite <= 0) { crono.stop(); resetParaPrevia(); }
            } else {
                segundosTranscurridos++;
                lblCronometro.setText(String.format("TIME: %02d:%02d", segundosTranscurridos/60, segundosTranscurridos%60));
            }
            if (cooldownFantasma > 0) {
                cooldownFantasma--;
                if (!mostrandoFantasma) btnFantasma.setText("Espera " + cooldownFantasma + "s");
            } else if (enJuego && !mostrandoFantasma && piezasEnMovimiento == 0) {
                btnFantasma.setText("FANTASMA"); btnFantasma.setEnabled(true);
            }
            revisarVictoria();
        });
        cronoAyuda = new Timer(1000, e -> {
            if (cooldownAyuda > 0) {
                cooldownAyuda--;
                btnAyuda.setText("Espera " + cooldownAyuda + "s");
            } else if (piezasEnMovimiento == 0) { 
                btnAyuda.setText("PISTA"); btnAyuda.setEnabled(true); cronoAyuda.stop(); 
            }
        });
        cronoFantasma = new Timer(3000, e -> { mostrandoFantasma = false; tablero.repaint(); cronoFantasma.stop(); });
        
        cargarVistaPrevia(nivelActualCargado);
    }

    // Diseñador de botones con texto blanco persistente
    private JButton crearBotonModerno(String t, Color b) {
        JButton btn = new JButton(t); btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE); btn.setBackground(b.darker());
        btn.setBorder(new LineBorder(b, 2)); btn.setFocusPainted(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
                g.setColor(Color.WHITE); super.paintText(g, b, textRect, text);
            }
        });
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(b); }
            public void mouseExited(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(b.darker()); }
        });
        return btn;
    }

    private void cargarVistaPrevia(int num) {
        File fJpg = new File("res/img" + num + ".jpg"), fJpeg = new File("res/img" + num + ".jpeg");
        imagenActual = fJpg.exists() ? fJpg.getPath() : (fJpeg.exists() ? fJpeg.getPath() : "");
        try { imgPrevia = ImageIO.read(new File(imagenActual)); resetParaPrevia(); btnIniciar.setEnabled(true); } 
        catch (Exception e) { btnIniciar.setEnabled(false); }
    }

    // Limpieza total antes de una nueva partida
    private void resetParaPrevia() {
        enJuego = false; mostrandoFantasma = false; piezasEnMovimiento = 0;
        if(crono != null) crono.stop(); if(cronoAyuda != null) cronoAyuda.stop();
        segundosTranscurridos = 0; tiempoLimite = 90; cooldownAyuda = 0; cooldownFantasma = 0;
        lblCronometro.setText("TIME: 00:00");
        btnIniciar.setEnabled(!imagenActual.isEmpty());
        btnAyuda.setEnabled(false); btnFantasma.setEnabled(false); btnRendirse.setEnabled(false);
        btnAyuda.setText("PISTA"); btnFantasma.setText("FANTASMA");
        GestorRanking.resetearPartida(); tablero.removeAll(); tablero.add(cuadroTexto); tablero.repaint();
    }

    // Inicia la lógica de mezcla y animación de piezas
    private void empezarPartidaReal() {
        enJuego = true; btnIniciar.setEnabled(false); btnRendirse.setEnabled(true); 
        segundosTranscurridos = 0; GestorRanking.resetearPartida();
        tablero.removeAll(); tablero.add(cuadroTexto); 
        ArrayList<PiezaGrafica> lista = new GestorImagenes().dividirImagen(imagenActual, filasActuales, columnasActuales);
        piezasEnMovimiento = lista.size();
        Rectangle recCuadro = new Rectangle(960, 30, 430, 280);
        for (PiezaGrafica p : lista) {
            tablero.add(p);
            int dX, dY;
            do { dX = 950 + (int)(Math.random() * 450); dY = 30 + (int)(Math.random() * 800);
            } while (recCuadro.intersects(new Rectangle(dX, dY, p.getWidth(), p.getHeight())));
            p.iniciarAnimacionMezcla(dX, dY, (int)(Math.random() * 4) * 90, this);
        }
        crono.start(); tablero.repaint();
    }

    // Lógica para el botón fantasma
    private void usarFantasma() {
        if (!enJuego || cooldownFantasma > 0 || piezasEnMovimiento > 0) return;
        mostrandoFantasma = true; cooldownFantasma = 18; 
        btnFantasma.setEnabled(false); btnFantasma.setText("VISIBLE");
        GestorRanking.registrarPenalizacionFantasma(); tablero.repaint(); cronoFantasma.start();
    }

    // Lógica para el botón de pistas
    private void usarAyuda() {
        if (piezasEnMovimiento > 0) return;
        for (Component c : tablero.getComponents()) {
            if (c instanceof PiezaGrafica && !((PiezaGrafica)c).isEncajada()) {
                ((PiezaGrafica)c).forzarEncaje(30, 30);
                GestorRanking.registrarUsoAyuda();
                cooldownAyuda = 30; btnAyuda.setEnabled(false); btnAyuda.setText("Espera 30s"); 
                cronoAyuda.start();
                tablero.repaint(); revisarVictoria(); return;
            }
        }
    }

    private void rendirse() { if (JOptionPane.showConfirmDialog(this, "¿Rendirse?") == JOptionPane.YES_OPTION) resetParaPrevia(); }

    // Detección de fin de juego
    private void revisarVictoria() {
        int encajadas = 0, total = filasActuales * columnasActuales;
        for (Component c : tablero.getComponents()) if (c instanceof PiezaGrafica && ((PiezaGrafica)c).isEncajada()) encajadas++;
        if (encajadas == total && total > 0 && enJuego) {
            enJuego = false; crono.stop();
            int tiempoFinal = modoHardcore ? 90 - tiempoLimite : segundosTranscurridos;
            int puntos = GestorRanking.calcularPuntaje(total, tiempoFinal);
            mostrarPanelVictoria(puntos, tiempoFinal);
        }
    }

    // Pantalla de resultados final integrada
    private void mostrarPanelVictoria(int puntos, int tiempo) {
        tablero.removeAll(); tablero.add(cuadroTexto);
        JPanel ficha = new JPanel(new GridBagLayout());
        ficha.setBackground(new Color(25, 25, 30, 240));
        ficha.setBorder(new LineBorder(new Color(255, 215, 0), 4));
        ficha.setBounds(100, 80, 750, 580);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0;

        JLabel lblVic = new JLabel("EXPLORACION EXITOSA", SwingConstants.CENTER);
        lblVic.setFont(new Font("Verdana", Font.BOLD, 36)); lblVic.setForeground(new Color(255, 215, 0));
        gbc.gridy = 0; ficha.add(lblVic, gbc);

        String anuncioLogro = GestorRanking.obtenerAnuncioLogros(nivelActualCargado, filasActuales*columnasActuales, tiempo, puntos);
        String anuncioProgreso = GestorRanking.obtenerAnuncioProgreso(nivelActualCargado);
        boolean esRank = GestorRanking.esTopRanking(puntos);

        StringBuilder sb = new StringBuilder("<html><div style='text-align: center; color: white; font-family: Segoe UI;'>");
        sb.append("<span style='font-size: 20px;'>PUNTUACION: <b>").append(puntos).append(" PTS</b></span><br><br>");
        
        // Uso de Getters para cumplir con Encapsulamiento (tuve que modificar lo ya hecho)
        sb.append("Ayudas: ").append(GestorRanking.getUsosAyuda()).append(" (-").append(GestorRanking.getUsosAyuda() * 500).append(" pts)<br>");
        sb.append("Fantasma: ").append(GestorRanking.getUsosFantasma()).append(" (-").append(GestorRanking.getPenalizacionFantasmaTotal()).append(" pts)<br><br>");
        
        if (!anuncioProgreso.isEmpty()) sb.append("<b style='color: #3498DB;'>[!] ").append(anuncioProgreso).append("</b><br>");
        if (!anuncioLogro.isEmpty()) sb.append("<b style='color: #2ECC71;'>(*) ").append(anuncioLogro).append("</b><br>");
        if (esRank) sb.append("<br><b style='color: #F1C40F;'> >>> NUEVO RECORD EN RANKING <<< </b><br>");
        
        sb.append("</div></html>");

        JLabel lblStats = new JLabel(sb.toString(), SwingConstants.CENTER);
        gbc.gridy = 1; ficha.add(lblStats, gbc);

        JTextField txtNom = new JTextField("Explorador", 15);
        txtNom.setFont(new Font("Segoe UI", Font.BOLD, 22)); txtNom.setHorizontalAlignment(JTextField.CENTER);
        gbc.gridy = 2; ficha.add(txtNom, gbc);

        JButton btnSave = crearBotonModerno("REGISTRAR Y FINALIZAR", new Color(46, 204, 113));
        btnSave.setPreferredSize(new Dimension(300, 50));
        btnSave.addActionListener(e -> {
            String n = txtNom.getText().trim();
            GestorRanking.registrarVictoria(n.isEmpty() ? "Explorador" : n, nivelActualCargado, filasActuales * columnasActuales, tiempo);
            parentFrame.volverAlMenu();
        });
        gbc.gridy = 3; gbc.insets = new Insets(20, 20, 10, 20); ficha.add(btnSave, gbc);
        tablero.add(ficha); tablero.setComponentZOrder(ficha, 0); tablero.repaint();
    }

    public void notificarPiezaLista() {
        piezasEnMovimiento--;
        if (piezasEnMovimiento <= 0) {
            piezasEnMovimiento = 0;
            btnAyuda.setEnabled(true); btnFantasma.setEnabled(true); btnRendirse.setEnabled(true);
        }
    }
}