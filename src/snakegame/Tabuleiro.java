package snakegame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class Tabuleiro extends JFrame {
    private JPanel painel;
    private String direcao = "direita";
    private int incremento = 10;
    private ArrayList<Quadrado> cobra;
    private int larguraTabuleiro = 800;
    private int alturaTabuleiro = 600;
    private boolean rodando = false;
    private boolean pausado = false;
    private Timer timer;
    private int pontuacao = 0;
    private Quadrado maca;
    private boolean colideBorda = true;
    private Random random = new Random();
    private JButton botaoPausar;
    private ImageIcon imagemFundo = new ImageIcon(getClass().getResource("snakeimg.jpg"));

    public Tabuleiro() {
        setTitle("Jogo da Cobrinha");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Tela cheia
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel externo com GridBagLayout para centralizar o painel de jogo
        JPanel wrapper = new JPanel(new GridBagLayout());
        painel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Desenha a imagem de fundo
                if (imagemFundo.getImage() != null) {
                    g.drawImage(imagemFundo.getImage(), 0, 0, larguraTabuleiro, alturaTabuleiro, this);
                } else {
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, larguraTabuleiro, alturaTabuleiro);
                }

                // Desenha a cobra
                for (int i = 0; i < cobra.size(); i++) {
                    Quadrado parte = cobra.get(i);
                    g.setColor(parte.cor);
                    g.fillRect(parte.x, parte.y, parte.largura, parte.altura);

                    // Desenha o olho apenas na cabeça
                    if (i == 0) {
                        g.setColor(Color.RED);
                        g.fillOval(parte.x + 2, parte.y + 2, 3, 3);
                    }
                }

                // Desenha a maçã
                g.setColor(Color.RED);
                g.fillRect(maca.x, maca.y, maca.largura, maca.altura);

                // Desenha a pontuação
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 14));
                g.drawString("Pontuação: " + pontuacao, 10, 20);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(larguraTabuleiro, alturaTabuleiro);
            }
        };
        painel.setBackground(Color.WHITE);

        // Adiciona o painel ao wrapper e centraliza
        wrapper.add(painel, new GridBagConstraints());
        add(wrapper, BorderLayout.CENTER);

        // Inicializa a cobra
        cobra = new ArrayList<>();
        cobra.add(new Quadrado(50, 50, new Color(0, 100, 0)));

        // Inicializa a maçã
        maca = gerarMaca();

        // Adiciona os botões de controle
        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new FlowLayout(FlowLayout.CENTER));

        JButton botaoIniciar = new JButton("Iniciar");
        botaoIniciar.addActionListener(e -> iniciarJogo());
        painelBotoes.add(botaoIniciar);

        botaoPausar = new JButton("Pausar");
        botaoPausar.addActionListener(e -> pausarJogo());
        painelBotoes.add(botaoPausar);

        JButton botaoReiniciar = new JButton("Reiniciar");
        botaoReiniciar.addActionListener(e -> reiniciarJogo());
        painelBotoes.add(botaoReiniciar);

        JButton botaoTrocarModo = new JButton("Trocar Modo");
        botaoTrocarModo.addActionListener(e -> trocarModo());
        painelBotoes.add(botaoTrocarModo);

        add(painelBotoes, BorderLayout.SOUTH);

        // Configura o controle de movimentação
        painel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!pausado) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            if (!direcao.equals("direita")) direcao = "esquerda";
                            break;
                        case KeyEvent.VK_RIGHT:
                            if (!direcao.equals("esquerda")) direcao = "direita";
                            break;
                        case KeyEvent.VK_UP:
                            if (!direcao.equals("baixo")) direcao = "cima";
                            break;
                        case KeyEvent.VK_DOWN:
                            if (!direcao.equals("cima")) direcao = "baixo";
                            break;
                    }
                }
            }
        });
        painel.setFocusable(true);
    }

    private void iniciarJogo() {
        if (!rodando) { // Inicia o jogo se não estiver rodando
            rodando = true;
            pontuacao = 0; // Reseta a pontuação ao iniciar
            cobra.clear();
            cobra.add(new Quadrado(50, 50, new Color(0, 100, 0)));
            maca = gerarMaca();

            // Configura a velocidade inicial
            int delay = 150; // Velocidade inicial
            timer = new Timer(delay, e -> {
                if (rodando && !pausado) {
                    moverCobra();
                    verificarColisao();
                    painel.repaint();
                }
            });
            timer.start();
            painel.requestFocusInWindow(); // Garante que o painel tenha o foco
        }
    }

    private void pausarJogo() {
        pausado = !pausado;
        if (pausado) {
            timer.stop();
            botaoPausar.setText("Despausar"); // Muda o texto do botão para "Despausar"
        } else {
            timer.start();
            botaoPausar.setText("Pausar"); // Muda o texto do botão de volta para "Pausar"
            painel.requestFocusInWindow(); // Garante que o painel tenha o foco ao despausar
        }
    }

private void moverCobra() {
    Quadrado cabeca = cobra.get(0);
    int novaX = cabeca.x;
    int novaY = cabeca.y;

    switch (direcao) {
        case "esquerda":
            novaX -= incremento;
            break;
        case "direita":
            novaX += incremento;
            break;
        case "cima":
            novaY -= incremento;
            break;
        case "baixo":
            novaY += incremento;
            break;
    }

    // Lógica para atravessar as bordas da tela
    if (!colideBorda) {
        if (novaX < 0) novaX = larguraTabuleiro - incremento; // Sai pela esquerda, volta pela direita
        else if (novaX >= larguraTabuleiro) novaX = 0; // Sai pela direita, volta pela esquerda
        if (novaY < 0) novaY = alturaTabuleiro - incremento; // Sai por cima, volta por baixo
        else if (novaY >= alturaTabuleiro) novaY = 0; // Sai por baixo, volta por cima
    } else {
        // Modo original: colisão com borda
        if (novaX < 0 || novaX >= larguraTabuleiro || novaY < 0 || novaY >= alturaTabuleiro) {
            rodando = false;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over! Você bateu na borda.");
            return;
        }
    }

    cobra.add(0, new Quadrado(novaX, novaY, new Color(0, 100, 0)));
    if (!comeuMaca()) {
        cobra.remove(cobra.size() - 1);
    }
}


    private void verificarColisao() {
        Quadrado cabeca = cobra.get(0);

        for (int i = 1; i < cobra.size(); i++) {
            Quadrado parte = cobra.get(i);
            if (cabeca.x == parte.x && cabeca.y == parte.y) {
                rodando = false;
                timer.stop(); // Para o timer em caso de colisão
                JOptionPane.showMessageDialog(this, "Game Over! Você colidiu com o corpo.");
                return;
            }
        }
    }

    private boolean comeuMaca() {
        Quadrado cabeca = cobra.get(0);
        if (cabeca.x == maca.x && cabeca.y == maca.y) {
          maca = gerarMaca();
          pontuacao++;
          aumentarDificuldade(); // Aumenta a dificuldade a cada maçã comida
            return true;
        }
        return false;
    }

   private void aumentarDificuldade() {
    // Aumenta a velocidade do jogo diminuindo o delay do Timer
    int novoDelay = Math.max(50, timer.getDelay() - 10);  // Diminui 10ms a cada maçã comida
    timer.setDelay(novoDelay);
}


    private Quadrado gerarMaca() {
        int x = random.nextInt(larguraTabuleiro / incremento) * incremento;
        int y = random.nextInt(alturaTabuleiro / incremento) * incremento;
        return new Quadrado(x, y, Color.RED);
    }

    private void reiniciarJogo() {
        rodando = false; // Para o jogo ao reiniciar
        pausado = false;
        pontuacao = 0;
        direcao = "direita";
        cobra.clear();
        cobra.add(new Quadrado(50, 50, new Color(0, 100, 0)));
        maca = gerarMaca();
        painel.repaint();
        painel.requestFocusInWindow(); // Garante que o painel tenha o foco ao reiniciar
    }
    
  private void trocarModo() {
    colideBorda = !colideBorda; // Alterna entre colisão com borda e atravessar as bordas
    painel.requestFocusInWindow(); // Garante que o painel tenha o foco para detectar teclas
}
  
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tabuleiro tabuleiro = new Tabuleiro();
            tabuleiro.setVisible(true);
        });
    }
}
