package rompecabezas;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class PantallaInicio extends JFrame {
    // Componentes de navegación por tarjetas
    private JPanel contenedorPrincipal;
    private CardLayout cardLayout;
    private JPanel panelMenuPrincipal, panelSeleccionNiveles, panelSeleccionDificultad, panelLogros, panelRanking, panelDetalleLogro;
    private VentanaJuego panelJuego; 
    private float hue = 0.0f; // Control de color para el fondo animado
    private final Color COLOR_TITULO = new Color(255, 215, 0);
    private int nivelSeleccionado = 1;

    public PantallaInicio() {
        setTitle("Maravillas de México");
        setSize(1450, 950); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        cardLayout = new CardLayout();
        contenedorPrincipal = new JPanel(cardLayout);

        crearPanelesBase();

        // Añadir todos los paneles al manejador de tarjetas
        contenedorPrincipal.add(panelMenuPrincipal, "MENU");
        contenedorPrincipal.add(panelSeleccionNiveles, "NIVELES");
        contenedorPrincipal.add(panelSeleccionDificultad, "DIFICULTAD");
        contenedorPrincipal.add(panelLogros, "LOGROS");
        contenedorPrincipal.add(panelRanking, "RANKING");
        contenedorPrincipal.add(panelDetalleLogro, "DETALLE_LOGRO");

        add(contenedorPrincipal);
        
        // Animación constante del fondo
        new Timer(50, e -> {
            hue += 0.002f;
            if (hue > 1.0f) hue = 0.0f;
            repaint();
        }).start();
    }

    // Inicializa las pantallas secundarias
    private void crearPanelesBase() {
        panelMenuPrincipal = crearPanelEstilizado();
        panelSeleccionNiveles = crearPanelEstilizado();
        panelSeleccionDificultad = crearPanelEstilizado();
        panelLogros = crearPanelEstilizado();
        panelRanking = crearPanelEstilizado();
        panelDetalleLogro = crearPanelEstilizado();
        configurarMenuPrincipal();
    }

    // Crea paneles con el degradado dinámico
    private JPanel crearPanelEstilizado() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setPaint(new GradientPaint(0, 0, Color.getHSBColor(hue, 0.4f, 0.15f), getWidth(), getHeight(), Color.getHSBColor(hue + 0.05f, 0.5f, 0.05f)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setLayout(new GridBagLayout());
        return p;
    }

    // Diseño del menú principal
    private void configurarMenuPrincipal() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; gbc.gridx = 0;
        JLabel lbl = new JLabel("MARAVILLAS DE MÉXICO");
        lbl.setFont(new Font("Verdana", Font.BOLD, 65)); lbl.setForeground(COLOR_TITULO);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 80, 0); 
        panelMenuPrincipal.add(lbl, gbc);

        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.gridy = 1;
        panelMenuPrincipal.add(crearBtnMenu("INICIAR EXPEDICIÓN", new Color(46, 204, 113), e -> { actualizarBotonesNiveles(); animarCambioPantalla("NIVELES"); }), gbc);
        gbc.gridy = 2; 
        panelMenuPrincipal.add(crearBtnMenu("GALERÍA DE LOGROS", new Color(155, 89, 182), e -> { actualizarPanelLogros(); animarCambioPantalla("LOGROS"); }), gbc);
        gbc.gridy = 3; 
        panelMenuPrincipal.add(crearBtnMenu("RANKING", new Color(52, 152, 219), e -> { actualizarPanelRanking(); animarCambioPantalla("RANKING"); }), gbc);
        gbc.gridy = 4; 
        panelMenuPrincipal.add(crearBtnMenu("SALIR", new Color(231, 76, 60), e -> System.exit(0)), gbc);
    }

    // Cambia la vista al tablero de juego
    public void mostrarJuego(int filas, boolean hardcore) {
        if (panelJuego != null) contenedorPrincipal.remove(panelJuego);
        panelJuego = new VentanaJuego(nivelSeleccionado, filas, hardcore, this);
        contenedorPrincipal.add(panelJuego, "JUEGO");
        animarCambioPantalla("JUEGO");
    }

    public void volverAlMenu() {
        animarCambioPantalla("MENU");
    }

    // Transición visual con parpadeo negro
    public void animarCambioPantalla(String nombreCard) {
        JPanel flash = new JPanel(); flash.setBackground(Color.BLACK);
        contenedorPrincipal.add(flash, "FLASH");
        cardLayout.show(contenedorPrincipal, "FLASH");
        Timer t = new Timer(150, e -> {
            cardLayout.show(contenedorPrincipal, nombreCard);
            contenedorPrincipal.remove(flash);
        });
        t.setRepeats(false); t.start();
    }

    // Pantalla de tabla de puntajes
    private void actualizarPanelRanking() {
        panelRanking.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; gbc.gridx = 0;
        JLabel lbl = new JLabel(">>> TOP EXPEDICIONARIOS <<<");
        lbl.setFont(new Font("Verdana", Font.BOLD, 45)); lbl.setForeground(COLOR_TITULO);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 60, 0);
        panelRanking.add(lbl, gbc);

        List<String[]> datos = GestorRanking.obtenerRegistrosRanking();
        for (int i = 0; i < Math.min(datos.size(), 5); i++) {
            String[] r = datos.get(i);
            JLabel lEntry = new JLabel((i+1) + ". " + r[1].toUpperCase() + " - " + r[0] + " PTS");
            lEntry.setForeground(i == 0 ? COLOR_TITULO : Color.WHITE);
            lEntry.setFont(new Font("Monospaced", Font.BOLD, 26));
            gbc.gridy = i + 1; gbc.insets = new Insets(10, 0, 10, 0);
            panelRanking.add(lEntry, gbc);
        }
        gbc.gridy = 7; gbc.insets = new Insets(60, 0, 0, 0);
        panelRanking.add(crearBtnMenu("VOLVER AL MENÚ", Color.GRAY, e -> animarCambioPantalla("MENU")), gbc);
        panelRanking.revalidate();
    }

    // Pantalla de visualización de trofeos
    private void actualizarPanelLogros() {
        panelLogros.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; gbc.gridx = 0;
        JLabel lbl = new JLabel("TROFEOS DE EXPLORACIÓN");
        lbl.setFont(new Font("Verdana", Font.BOLD, 40)); lbl.setForeground(COLOR_TITULO);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 40, 0);
        panelLogros.add(lbl, gbc);

        String raw = GestorRanking.obtenerLogrosIDs_Static();
        Set<Integer> obtenidos = new HashSet<>();
        if (!raw.isEmpty()) for (String v : raw.split(",")) obtenidos.add(Integer.parseInt(v.trim()));

        for (int i = 0; i < GestorRanking.NOMBRES_LOGROS.length; i++) {
            boolean tiene = obtenidos.contains(i);
            final int idx = i;
            String prefijo = tiene ? "[ok] " : "[X] ";
            JButton btn = crearBtnMenu(prefijo + GestorRanking.NOMBRES_LOGROS[i], tiene ? new Color(0, 100, 0) : new Color(60, 60, 60), e -> { mostrarDetalleLogro(idx, tiene); animarCambioPantalla("DETALLE_LOGRO"); });
            gbc.gridy = i + 1; gbc.insets = new Insets(5, 0, 5, 0);
            panelLogros.add(btn, gbc);
        }
        gbc.gridy = 11; gbc.insets = new Insets(30, 0, 0, 0);
        panelLogros.add(crearBtnMenu("VOLVER", Color.GRAY, e -> animarCambioPantalla("MENU")), gbc);
        panelLogros.revalidate();
    }

    // Detalle individual de cada trofeo
    private void mostrarDetalleLogro(int idx, boolean tiene) {
        panelDetalleLogro.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; gbc.gridx = 0;
        JLabel lblT = new JLabel(GestorRanking.NOMBRES_LOGROS[idx].toUpperCase());
        lblT.setFont(new Font("Verdana", Font.BOLD, 40)); lblT.setForeground(COLOR_TITULO);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 50, 0);
        panelDetalleLogro.add(lblT, gbc);
        JLabel lblReq = new JLabel("<html><center><div style='width:600px; text-align:center;'>REQUISITO:<br><br>" + GestorRanking.REQUISITOS_LOGROS[idx] + "</div></center></html>");
        lblReq.setFont(new Font("SansSerif", Font.PLAIN, 26)); lblReq.setForeground(Color.WHITE);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 40, 0);
        panelDetalleLogro.add(lblReq, gbc);
        
        String estadoTexto = tiene ? "ESTADO: DESBLOQUEADO (V)" : "ESTADO: BLOQUEADO (X)";
        JLabel lblE = new JLabel(estadoTexto);
        lblE.setFont(new Font("SansSerif", Font.BOLD, 24)); lblE.setForeground(tiene ? Color.GREEN : Color.RED);
        gbc.gridy = 2; panelDetalleLogro.add(lblE, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(60, 0, 0, 0);
        panelDetalleLogro.add(crearBtnMenu("VOLVER A LOGROS", Color.GRAY, e -> animarCambioPantalla("LOGROS")), gbc);
        panelDetalleLogro.revalidate();
    }

    // Pantalla de selección de niveles del 1 al 10
    private void actualizarBotonesNiveles() {
        panelSeleccionNiveles.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; gbc.gridx = 0;
        JLabel lbl = new JLabel("SELECCIONA TU DESTINO");
        lbl.setFont(new Font("Verdana", Font.BOLD, 40)); lbl.setForeground(COLOR_TITULO);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 30, 0);
        panelSeleccionNiveles.add(lbl, gbc);

        int max = GestorRanking.obtenerNivelMaximo();
        String[] nombresNivelesArr = { "Castillo de Chapultepec", "Chichén Itzá", "Teotihuacán", "Monte Albán", "Uxmal", "Dolores Hidalgo", "Cenotes de Yucatán", "Pico de Orizaba", "San Juan de Ulúa", "Palacio Nacional" };
        for (int i = 0; i < nombresNivelesArr.length; i++) {
            final int n = i + 1; boolean bloqueado = n > max; boolean medalla = GestorRanking.tieneMedalla(n);
            String textoBtn = bloqueado ? "LOCKED [X]" : (medalla ? "(*) " : "") + nombresNivelesArr[i];
            JButton btn = crearBtnMenu(textoBtn, bloqueado ? Color.DARK_GRAY : (medalla ? new Color(218, 165, 32) : new Color(52, 152, 219)), e -> { nivelSeleccionado = n; actualizarPanelDificultad(); animarCambioPantalla("DIFICULTAD"); });
            if (bloqueado) btn.setEnabled(false);
            gbc.gridy = i + 1; gbc.insets = new Insets(4, 0, 4, 0);
            panelSeleccionNiveles.add(btn, gbc);
        }
        gbc.gridy = 12; gbc.insets = new Insets(20, 0, 0, 0);
        panelSeleccionNiveles.add(crearBtnMenu("VOLVER", Color.GRAY, e -> animarCambioPantalla("MENU")), gbc);
        panelSeleccionNiveles.revalidate();
    }

    // Selección de tamaño de rejilla y modo de juego
    private void actualizarPanelDificultad() {
        panelSeleccionDificultad.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER; gbc.gridx = 0;
        JLabel lblSub = new JLabel("NIVEL DE DESAFÍO");
        lblSub.setFont(new Font("Verdana", Font.BOLD, 45)); lblSub.setForeground(COLOR_TITULO);
        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 100, 0); 
        panelSeleccionDificultad.add(lblSub, gbc);

        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.gridy = 1; panelSeleccionDificultad.add(crearBtnDif("Fácil (3x3)", 3, false, new Color(46, 204, 113)), gbc);
        gbc.gridy = 2; panelSeleccionDificultad.add(crearBtnDif("Normal (4x4)", 4, false, new Color(52, 152, 219)), gbc);
        gbc.gridy = 3; panelSeleccionDificultad.add(crearBtnDif("Difícil (6x6)", 6, false, new Color(155, 89, 182)), gbc);
        gbc.gridy = 4; panelSeleccionDificultad.add(crearBtnDif("(!) HARDCORE (1:30)", 6, true, new Color(241, 196, 15)), gbc);
        gbc.gridy = 5; gbc.insets = new Insets(50, 0, 0, 0); 
        panelSeleccionDificultad.add(crearBtnMenu("VOLVER A DESTINOS", Color.GRAY, e -> animarCambioPantalla("NIVELES")), gbc);
        panelSeleccionDificultad.revalidate();
    }

    private JButton crearBtnDif(String t, int f, boolean h, Color c) {
        return crearBtnMenu(t, c, e -> mostrarJuego(f, h));
    }

    // Generador de botones estandarizados para el menú
    private JButton crearBtnMenu(String t, Color b, ActionListener al) {
        JButton btn = new JButton(t); btn.setPreferredSize(new Dimension(550, 50)); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20)); btn.setForeground(Color.WHITE);
        btn.setBackground(b.darker()); btn.setBorder(new LineBorder(b, 2));
        btn.setFocusPainted(false); btn.setContentAreaFilled(false); btn.setOpaque(true);
        if (al != null) btn.addActionListener(al);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(b); }
            public void mouseExited(MouseEvent e) { if(btn.isEnabled()) btn.setBackground(b.darker()); }
        });
        return btn;
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new PantallaInicio().setVisible(true)); }
}