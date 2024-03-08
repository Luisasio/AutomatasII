package proyecto;

/*
 * 
ent1 a,b,c;
flo2 u,i,o;
cad3 f,g,h;
a=b+c;




 */


import javax.swing.*;
import javax.swing.table.DefaultTableModel;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Automatas3 extends JFrame {

    private static final Pattern PATRON_DECLARACION_VARIABLE = Pattern.compile("\\b(ent1|flo2|cad3)\\b");
    private static final Pattern PATRON_IDENTIFICADOR = Pattern.compile("[a-z]+[0-9]?");
    private static final Pattern PATRON_OPERADOR = Pattern.compile("[+\\-*/=]");
    private static final Pattern PATRON_ENTERO_O_FLOTANTE = Pattern.compile("\\b(\\d+(\\.\\d+)?)\\b");
    private static final Pattern PATRON_CADENA = Pattern.compile("\\b(\\w+)\\s*=\\s*\"([^\"]*)\"\\s*;");


 // Nueva clase TipoDato
    class TipoDato {
        public static final String ENTERO = "ent1";
        public static final String FLOAT = "flo2";
        public static final String CADENA = "cad3";
        public static int ultimoCodigoError = 1;

        public static void reiniciarContadores() {
            ultimoCodigoError = 1;
        }
    }

    private JTextArea areaTextoCodigo;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private DefaultTableModel modeloTablaErrores;
    private JTable tablaErrores;

    private Set<String> comasAñadidas = new HashSet<>();
    private Set<String> puntosYComasAñadidos = new HashSet<>();
    private Set<String> operadoresAñadidos = new HashSet<>();
 // Conjunto para variables declaradas
    private Set<String> variablesDeclaradas = new HashSet<>();

    public Automatas3() {
        ventana();
    }
    

    private void ventana() {
        JFrame frame = new JFrame();
        frame.setResizable(false);
        frame.setTitle("Proyecto");
        frame.setLocationRelativeTo(null);
        frame.setBounds(100, 100, 907, 492);
        frame.getContentPane().setLayout(null);

        areaTextoCodigo = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(areaTextoCodigo);
        scrollPane.setBounds(10, 45, 628, 224);
        frame.getContentPane().add(scrollPane);

        modeloTabla = new DefaultTableModel();
        modeloTabla.addColumn("Lexema");
        modeloTabla.addColumn("Tipo de dato");

        tabla = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBounds(649, 45, 232, 397);
        frame.getContentPane().add(scrollTabla);

        tabla.getColumnModel().getColumn(1).setMaxWidth(116);
        tabla.getColumnModel().getColumn(0).setMaxWidth(116);

        modeloTablaErrores = new DefaultTableModel();
        modeloTablaErrores.addColumn("Token");
        modeloTablaErrores.addColumn("Lexema");
        modeloTablaErrores.addColumn("Renglon");
        modeloTablaErrores.addColumn("Descripcion");

        tablaErrores = new JTable(modeloTablaErrores);
        JScrollPane scrollTablaErrores = new JScrollPane(tablaErrores);
        scrollTablaErrores.setBounds(10, 279, 629, 163);
        frame.getContentPane().add(scrollTablaErrores);

        JButton botonAnalizar = new JButton("Compilar");
        botonAnalizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                analizar(areaTextoCodigo.getText());
            }
        });
        botonAnalizar.setBounds(10, 10, 89, 23);
        frame.getContentPane().add(botonAnalizar);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
//--------------------------------------------------------------------------------------------------------
    private void analizar(String codigoFuente) {
        modeloTabla.setRowCount(0);
        modeloTablaErrores.setRowCount(0);
        comasAñadidas.clear();
        puntosYComasAñadidos.clear();
        operadoresAñadidos.clear();
        variablesDeclaradas.clear();

        String[] lineas = codigoFuente.split("\n");
        for (int i = 0; i < lineas.length; i++) {
            procesarLinea(lineas[i], i + 1);
        }
    }
    // Método para comprobar la compatibilidad de tipos
    private boolean comprobarTipo(String tipo1, String tipo2, String operador) {
         if (tipo1.equals(TipoDato.CADENA) || tipo2.equals(TipoDato.CADENA)) {
             return false; // Incompatible con enteros y flotantes
         }
         if (operador.equals("+") || operador.equals("-")) {
             return tipo1.equals(tipo2); // Compatible solo si ambos tipos son iguales
         }
         if (operador.equals("*") || operador.equals("/")) {
             return tipo1.equals(TipoDato.ENTERO) || tipo2.equals(TipoDato.ENTERO)
                     || tipo1.equals(TipoDato.FLOAT) || tipo2.equals(TipoDato.FLOAT); // Compatible con enteros y flotantes
         }
         return false; // Operador desconocido
     }

//-------------------------------------------------------------------------------------------------------------
    // Método para procesar cada línea del código fuente
    private void procesarLinea(String linea, int numeroRenglon) {
        Matcher matcherDeclaracionVariable = PATRON_DECLARACION_VARIABLE.matcher(linea);
        if (matcherDeclaracionVariable.find()) {
            String tipo = matcherDeclaracionVariable.group(1);
            modeloTabla.addRow(new Object[]{tipo, ""});
            linea = linea.substring(matcherDeclaracionVariable.end()).trim();
            String[] identificadores = linea.split("\\s*(,|;)\\s*");
            for (String identificador : identificadores) {
                if (!identificador.isEmpty()) {
                    Matcher matcherIdentificador = PATRON_IDENTIFICADOR.matcher(identificador);
                    if (matcherIdentificador.matches()) {
                        modeloTabla.addRow(new Object[]{identificador, obtenerTipoDato(tipo)});
                        variablesDeclaradas.add(identificador);  // Agregar variable al conjunto declarado
                    }
                }
            }
            if (linea.contains(",") && comasAñadidas.add(",")) {
                modeloTabla.addRow(new Object[]{",", ""});
            }
            if (linea.contains(";") && puntosYComasAñadidos.add(";")) {
                modeloTabla.addRow(new Object[]{";", ""});
            }
        } else {
        	Matcher matcherIdentificador = PATRON_IDENTIFICADOR.matcher(linea);
            while (matcherIdentificador.find()) {
                String identificador = matcherIdentificador.group();
                if (!variablesDeclaradas.contains(identificador)) {
                    // Variable utilizada antes de ser declarada, generar un error
                    String codigoError = "ert" + TipoDato.ultimoCodigoError++;
                    modeloTablaErrores.addRow(new Object[]{codigoError, identificador, numeroRenglon, "Variable indefinida"});
                }
                // Verificar si la variable ya está presente en la tabla antes de agregarla
                if (!yaEstaEnTabla(identificador)) {
                    modeloTabla.addRow(new Object[]{identificador, ""});
                }
            }
            //encontrar los operadores
            Matcher matcherOperador = PATRON_OPERADOR.matcher(linea);
            while (matcherOperador.find()) {
                String operador = matcherOperador.group();
                if (operadoresAñadidos.add(operador)) {
                    modeloTabla.addRow(new Object[]{operador, ""});
                }
            }
         // Reconocer valores literales de enteros y flotantes
            Matcher matcherEnteroOFlotante = PATRON_ENTERO_O_FLOTANTE.matcher(linea);

            while (matcherEnteroOFlotante.find()) {
                String valor = matcherEnteroOFlotante.group(1);
                // Verificar si contiene un punto para determinar si es entero o flotante
                String tipo = valor.contains(".") ? "flo2" : "ent1";

                // Agregar la combinación valor-tipo a la tabla si no estaba presente anteriormente
                if (!yaEstaliteralEnTabla(valor, tipo)) {
                    modeloTabla.addRow(new Object[]{valor, tipo});
                }
            }
            Matcher matcherExpresion = Pattern.compile("(\\w+)\\s*([+\\-/])\\s*(\\w+)").matcher(linea);
            while (matcherExpresion.find()) {
                String identificador1 = matcherExpresion.group(1);
                String operador = matcherExpresion.group(2);
                String identificador2 = matcherExpresion.group(3);

                String tipoIdentificador1 = "";
                String tipoIdentificador2 = "";
                String errorLexema = "";

                for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                    String lexemaEnTabla = (String) modeloTabla.getValueAt(i, 0);
                    if (identificador1.equals(lexemaEnTabla)) {
                        tipoIdentificador1 = (String) modeloTabla.getValueAt(i, 1);
                    }
                    if (identificador2.equals(lexemaEnTabla)) {
                        tipoIdentificador2 = (String) modeloTabla.getValueAt(i, 1);
                    }
                    if (identificador1.equals(lexemaEnTabla) && !comprobarTipo(tipoIdentificador1, tipoIdentificador2, operador)) {
                        errorLexema = identificador1;
                    }
                    if (identificador2.equals(lexemaEnTabla) && !comprobarTipo(tipoIdentificador2, tipoIdentificador1, operador)) {
                        errorLexema = identificador2;
                    }
                }

                if (!comprobarTipo(tipoIdentificador1, tipoIdentificador2, operador)) {
                    String codigoError = "ert" + TipoDato.ultimoCodigoError++;
                    modeloTablaErrores.addRow(new Object[]{codigoError, errorLexema, numeroRenglon, "Incompatibilidad de tipos: "+tipoIdentificador1, operador, tipoIdentificador2});
                }
            }

        }
    }


 // Método para verificar si una variable ya está en la tabla de símbolos
    private boolean yaEstaEnTabla(String variable) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String lexemaEnTabla = (String) modeloTabla.getValueAt(i, 0);
            if (variable.equals(lexemaEnTabla)) {
                return true;
            }
        }
        return false;
    }
    private boolean yaEstaliteralEnTabla(String valor, String tipo) {
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String lexemaEnTabla = (String) modeloTabla.getValueAt(i, 0);
            String tipoEnTabla = (String) modeloTabla.getValueAt(i, 1);
            if (valor.equals(lexemaEnTabla) && tipo.equals(tipoEnTabla)) {
                return true;
            }
        }
        return false;
    }





    private String obtenerTipoDato(String tipo) {
        switch (tipo) {
            case "ent1":
                return "ent1";
            case "flo2":
                return "flo2";
            case "cad3":
                return "cad3";
            default:
                return "";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Automatas3());
    }
}