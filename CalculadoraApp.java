import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.math.BigDecimal;
import java.math.MathContext;
import javax.swing.*;

/**
 * Classe responsável pela lógica das operações básicas.
 * (Fica no mesmo arquivo, mas separada da interface gráfica.)
 */
class Calculadora {

    public double somar(double a, double b) {
        return a + b;
    }

    public double subtrair(double a, double b) {
        return a - b;
    }

    public double multiplicar(double a, double b) {
        return a * b;
    }

    public double dividir(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Divisão por zero não é permitida.");
        }
        return a / b;
    }
}

/**
 * Interface gráfica da calculadora (Swing).
 * Funciona com o mouse e com o teclado. As contas são feitas pela classe Calculadora.
 */
class CalculadoraApp extends JFrame {

    // Símbolos dos botões
    private static final String SOMA = "+";
    private static final String SUBTRACAO = "\u2212"; // −
    private static final String MULTIPLICACAO = "\u00d7"; // ×
    private static final String DIVISAO = "\u00f7"; // ÷
    private static final String APAGAR = "\u232b"; // ⌫
    private static final String INVERTER = "\u00b1"; // ±

    // Cores
    private static final Color FUNDO = Color.BLACK;
    private static final Color FUNDO_BOTAO = new Color(38, 38, 40);
    private static final Color FUNDO_PRESSIONADO = new Color(70, 70, 74);
    private static final Color LARANJA = new Color(255, 159, 10);
    private static final Color TEXTO = new Color(245, 245, 245);
    private static final Color TEXTO_AUXILIAR = new Color(150, 150, 155);

    private final Calculadora calc = new Calculadora();
    private final JLabel expressao = new JLabel(" ", SwingConstants.RIGHT); // ex.: "12 ×"
    private final JLabel visor = new JLabel("0", SwingConstants.RIGHT);

    private String entrada = "0";     // número que está sendo digitado
    private double acumulado = 0;     // primeiro número da conta
    private String operador = null;   // operação escolhida (ou null)
    private boolean novoNumero = true; // próximo dígito começa um número novo?

    public CalculadoraApp() {
        super("Calculadora");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        expressao.setForeground(TEXTO_AUXILIAR);
        expressao.setFont(new Font("SansSerif", Font.PLAIN, 22));
        visor.setForeground(TEXTO);
        visor.setFont(new Font("SansSerif", Font.PLAIN, 52));

        JPanel areaVisor = new JPanel(new GridLayout(2, 1));
        areaVisor.setBackground(FUNDO);
        areaVisor.setBorder(BorderFactory.createEmptyBorder(20, 22, 10, 22));
        areaVisor.add(expressao);
        areaVisor.add(visor);

        String[] botoes = {
            "C", APAGAR, "%", DIVISAO,
            "7", "8", "9", MULTIPLICACAO,
            "4", "5", "6", SUBTRACAO,
            "1", "2", "3", SOMA,
            INVERTER, "0", ",", "="
        };
        JPanel teclado = new JPanel(new GridLayout(5, 4, 9, 9));
        teclado.setBackground(FUNDO);
        teclado.setBorder(BorderFactory.createEmptyBorder(4, 18, 16, 18));
        for (String texto : botoes) {
            teclado.add(criarBotao(texto));
        }

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(FUNDO);
        raiz.setPreferredSize(new Dimension(370, 520));
        raiz.add(areaVisor, BorderLayout.NORTH);
        raiz.add(teclado, BorderLayout.CENTER);
        setContentPane(raiz);

        // Teclado físico: cada tecla é traduzida para o botão correspondente
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                aoDigitarTecla(e.getKeyChar());
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private JButton criarBotao(String texto) {
        JButton botao = new BotaoArredondado(texto);
        botao.setFont(new Font("SansSerif", Font.PLAIN, 24));
        botao.setForeground(texto.equals("=") ? Color.WHITE
                : texto.matches("[0-9,\u00b1]") ? TEXTO : LARANJA);
        botao.addActionListener(e -> aoClicar(texto));
        return botao;
    }

    // ================= Entrada (mouse e teclado) =================

    /** Converte a tecla digitada no texto do botão equivalente. */
    private void aoDigitarTecla(char c) {
        String t = String.valueOf(c);
        switch (c) {
            case '.': t = ","; break;
            case '-': t = SUBTRACAO; break;
            case '*': case 'x': case 'X': t = MULTIPLICACAO; break;
            case '/': t = DIVISAO; break;
            case '\n': t = "="; break;          // Enter
            case '\b': t = APAGAR; break;       // Backspace
            case 'c': case 'C': case '\u001b': t = "C"; break; // C ou Esc
        }
        aoClicar(t);
    }

    private void aoClicar(String t) {
        if (t.matches("[0-9]")) {
            digitar(t);
            return;
        }
        switch (t) {
            case ",": virgula(); break;
            case "C": limpar(); break;
            case APAGAR: apagar(); break;
            case INVERTER: inverter(); break;
            case "%": porcentagem(); break;
            case SOMA:
            case SUBTRACAO:
            case MULTIPLICACAO:
            case DIVISAO: escolherOperador(t); break;
            case "=": igual(); break;
        }
    }

    // ================= Lógica da calculadora =================

    private void digitar(String d) {
        if (novoNumero || entrada.equals("0")) {
            entrada = d;
            novoNumero = false;
        } else if (entrada.length() < 15) {
            entrada += d;
        }
        atualizar();
    }

    private void virgula() {
        if (novoNumero) {
            entrada = "0,";
            novoNumero = false;
        } else if (!entrada.contains(",")) {
            entrada += ",";
        }
        atualizar();
    }

    private void limpar() {
        entrada = "0";
        acumulado = 0;
        operador = null;
        novoNumero = true;
        atualizar();
    }

    private void apagar() {
        if (novoNumero) return;
        entrada = entrada.substring(0, entrada.length() - 1);
        if (entrada.isEmpty() || entrada.equals("-")) entrada = "0";
        atualizar();
    }

    private void inverter() {
        if (entrada.equals("0")) return;
        entrada = entrada.startsWith("-") ? entrada.substring(1) : "-" + entrada;
        atualizar();
    }

    private void porcentagem() {
        entrada = formatar(valorEntrada() / 100);
        novoNumero = false;
        atualizar();
    }

    private void escolherOperador(String op) {
        try {
            if (operador != null && !novoNumero) {
                acumulado = resolver();   // encadeia: 2 + 3 + ...
            } else if (operador == null) {
                acumulado = valorEntrada();
            }
            operador = op;
            novoNumero = true;
            atualizar();
        } catch (ArithmeticException e) {
            erro();
        }
    }

    private void igual() {
        if (operador == null || novoNumero) return;
        try {
            acumulado = resolver();
            entrada = formatar(acumulado);
            operador = null;
            novoNumero = true;
            atualizar();
        } catch (ArithmeticException e) {
            erro();
        }
    }

    /** Chama a classe Calculadora de acordo com o operador escolhido. */
    private double resolver() {
        double b = valorEntrada();
        switch (operador) {
            case SOMA: return calc.somar(acumulado, b);
            case SUBTRACAO: return calc.subtrair(acumulado, b);
            case MULTIPLICACAO: return calc.multiplicar(acumulado, b);
            default: return calc.dividir(acumulado, b);
        }
    }

    private double valorEntrada() {
        try {
            return Double.parseDouble(entrada.replace(',', '.'));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String formatar(double valor) {
        return BigDecimal.valueOf(valor).round(new MathContext(12))
                .stripTrailingZeros().toPlainString().replace('.', ',');
    }

    private void atualizar() {
        boolean pendente = operador != null;
        expressao.setText(pendente ? formatar(acumulado) + " " + operador : " ");
        visor.setText(pendente && novoNumero ? formatar(acumulado) : entrada);
    }

    private void erro() {
        limpar();
        visor.setText("Erro");
    }

    // ================= Botão com cantos arredondados =================

    private static class BotaoArredondado extends JButton {
        BotaoArredondado(String texto) {
            super(texto);
            setFocusable(false); // o foco fica na janela, para o teclado funcionar
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean igual = getText().equals("=");
            Color cor = igual ? LARANJA : FUNDO_BOTAO;
            if (getModel().isPressed()) cor = igual ? LARANJA.darker() : FUNDO_PRESSIONADO;
            g2.setColor(cor);
            g2.fill(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 24, 24));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalculadoraApp().setVisible(true));
    }
}
