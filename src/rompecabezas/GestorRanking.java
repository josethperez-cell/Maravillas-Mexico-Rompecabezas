package rompecabezas;

import java.io.*;
import java.util.*;

public class GestorRanking {
    // Rutas de archivos de persistencia
    private static final String RUTA_ARCHIVO = "res/ranking.txt";
    private static final String RUTA_PROGRESO = "res/progreso.txt";
    
    // Nombres de los logros disponibles en el juego
    public static final String[] NOMBRES_LOGROS = {
        "Rápido como un rayo", "Precisión de cirujano", "Arquitecto Maestro",
        "Ojo de águila", "Puntaje de leyenda", "Gran explorador",
        "Explorador principiante", "Maestro del rompecabezas", "Gran maestro de la precisión"
    };

    // Requerimientos textuales para cada logro
    public static final String[] REQUISITOS_LOGROS = {
        "Completar un nivel en difícil (6x6) en menos de 2:00 minutos.", 
        "Completar un nivel en difícil (6x6) sin usar pistas ni modo fantasma.",
        "Completar cualquier nivel en dificultad difícil (6x6).",
        "Completar un nivel en difícil usando el modo fantasma máximo 2 veces.",
        "Obtener más de 10,000 puntos en una sola partida.",
        "Desbloquear los 10 niveles del juego.",
        "Completar tu primer nivel en cualquier dificultad.",
        "Completar todos los niveles (del 1 al 10) en dificultad difícil (6x6).",
        "Completar todos los niveles en difícil (6x6) sin solicitar pistas."
    };

    // Variables globales para rastrear el uso de ayudas en la sesión actual
    // ENCAPSULAMIENTO
    private static int usosAyuda = 0;
    private static int usosFantasma = 0;
    private static int penalizacionFantasmaTotal = 0;

    // Métodos para registrar y resetear estadísticas de la partida
    public static void resetearPartida() { usosAyuda = 0; usosFantasma = 0; penalizacionFantasmaTotal = 0; }
    public static void registrarUsoAyuda() { usosAyuda++; }
    
    // SOBRECARGA 
    public static void registrarUsoAyuda(int n) { usosAyuda += n; }

    public static void registrarPenalizacionFantasma() { usosFantasma++; penalizacionFantasmaTotal += 200; }

    // MANEJO DE EXCEPCIONES ESPECÍFICO
    public static int obtenerNivelMaximo() { 
        try {
            return Integer.parseInt(leerLinea(0, "1")); 
        } catch (NumberFormatException e) {
            System.err.println("Error: Formato de nivel maximo invalido.");
            return 1;
        }
    }
    
    public static String obtenerLogrosIDs_Static() { return leerLinea(1, ""); }
    private static String obtenerNivelesDificil() { return leerLinea(2, ""); }
    private static String obtenerNivelesSinAyuda() { return leerLinea(3, ""); }

    // Comprobación de medallas de oro para los botones de niveles
    public static boolean tieneMedalla(int nivelID) {
        Set<Integer> difs = cargarSet(obtenerNivelesDificil());
        return difs.contains(nivelID);
    }

    // Lee el archivo de ranking y lo devuelve ordenado por puntaje
    public static List<String[]> obtenerRegistrosRanking() {
        List<String[]> registros = new ArrayList<>();
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists()) return registros;
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String l;
            while ((l = br.readLine()) != null) {
                String[] d = l.split(",");
                if (d.length == 3) registros.add(d);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el ranking: " + e.getMessage());
        }
        registros.sort((a, b) -> Integer.compare(Integer.parseInt(b[0]), Integer.parseInt(a[0])));
        return registros;
    }

    // Verifica si el puntaje califica para entrar al top 5
    public static boolean esTopRanking(int puntos) {
        List<String[]> lista = obtenerRegistrosRanking();
        if (lista.size() < 5) return true;
        return puntos > Integer.parseInt(lista.get(Math.min(lista.size()-1, 4))[0]);
    }

    // Procesa los logros ganados al finalizar una partida con éxito
    public static String obtenerAnuncioLogros(int nivelID, int piezas, int segundos, int puntos) {
        Set<Integer> obtenidos = cargarSet(obtenerLogrosIDs_Static());
        Set<Integer> difs = cargarSet(obtenerNivelesDificil());
        Set<Integer> sins = cargarSet(obtenerNivelesSinAyuda());
        int totalAntes = obtenidos.size();
        
        check(6, obtenidos); 
        if (piezas == 36) { 
            difs.add(nivelID);
            if (usosAyuda == 0) sins.add(nivelID);
            if (segundos < 120) check(0, obtenidos); 
            if (usosAyuda == 0 && usosFantasma == 0) check(1, obtenidos);
            check(2, obtenidos);
            if (usosFantasma > 0 && usosFantasma <= 2) check(3, obtenidos);
        }
        if (puntos >= 10000) check(4, obtenidos);
        if (obtenerNivelMaximo() >= 10) check(5, obtenidos);
        if (difs.size() >= 10) check(7, obtenidos);
        if (sins.size() >= 10) check(8, obtenidos);

        guardarTodo(obtenerNivelMaximo(), setToString(obtenidos), setToString(difs), setToString(sins));
        return (obtenidos.size() > totalAntes) ? "¡NUEVO LOGRO DESBLOQUEADO!" : "";
    }

    public static String obtenerAnuncioProgreso(int nivelCompletado) {
        int actual = obtenerNivelMaximo();
        if (nivelCompletado == actual && actual < 10) {
            guardarTodo(actual + 1, obtenerLogrosIDs_Static(), obtenerNivelesDificil(), obtenerNivelesSinAyuda());
            return "¡NIVEL " + (actual + 1) + " DESBLOQUEADO!";
        }
        return "";
    }

    public static int actualizarProgreso(int nivelCompletado) {
        int actual = obtenerNivelMaximo();
        int nuevoMax = Math.max(actual, nivelCompletado + 1);
        if (nuevoMax > actual && nuevoMax <= 10) {
            guardarTodo(nuevoMax, obtenerLogrosIDs_Static(), obtenerNivelesDificil(), obtenerNivelesSinAyuda());
            return nuevoMax;
        }
        return -1;
    }

    // GETTERS Y SETTERS 

    public static int getUsosAyuda() { return usosAyuda; }
    public static void setUsosAyuda(int n) { usosAyuda = n; }
    public static int getUsosFantasma() { return usosFantasma; }
    public static int getPenalizacionFantasmaTotal() { return penalizacionFantasmaTotal; }

    // Funciones auxiliares para manejo de archivos y conversión de datos
    private static void check(int id, Set<Integer> lista) { if (!lista.contains(id)) lista.add(id); }

    private static String leerLinea(int n, String defecto) {
        File f = new File(RUTA_PROGRESO);
        if (!f.exists()) return defecto;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String l = "";
            for (int i = 0; i <= n; i++) l = br.readLine();
            return (l != null) ? l : defecto;
        } catch (IOException e) { return defecto; }
    }

    public static void guardarTodo(int nivel, String logros, String dif, String sinA) {
        new File("res").mkdir(); 
        try (PrintWriter pw = new PrintWriter(new FileWriter(RUTA_PROGRESO))) {
            pw.println(nivel); pw.println(logros); pw.println(dif); pw.println(sinA);
        } catch (IOException e) {
            System.err.println("No se pudo guardar el progreso.");
        }
    }

    private static Set<Integer> cargarSet(String d) {
        Set<Integer> s = new HashSet<>();
        if (d != null && !d.isEmpty()) {
            for (String v : d.split(",")) {
                try {
                    if (!v.trim().isEmpty()) s.add(Integer.parseInt(v.trim()));
                } catch (NumberFormatException e) {}
            }
        }
        return s;
    }

    private static String setToString(Set<Integer> s) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : s) sb.append(i).append(",");
        return sb.toString();
    }

    // Lógica matemática para asignar puntaje según tiempo y dificultad
    public static int calcularPuntaje(int piezas, int segundos) {
        double mult = (piezas >= 36) ? 5.0 : (piezas >= 16 ? 2.5 : 1.0);
        int base = (int)((piezas * mult * 10000) / (segundos <= 0 ? 1 : segundos));
        return Math.max(0, base - (usosAyuda * 500) - penalizacionFantasmaTotal);
    }

    public static void registrarVictoria(String nombre, int nivelID, int piezas, int segundos) {
        int puntos = calcularPuntaje(piezas, segundos);
        try (PrintWriter writer = new PrintWriter(new FileWriter(RUTA_ARCHIVO, true))) {
            writer.println(puntos + "," + nombre + "," + String.format("%02d:%02d", segundos/60, segundos%60));
        } catch (IOException e) {}
    }
}