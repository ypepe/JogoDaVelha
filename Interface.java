import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Interface extends JFrame {
    private JButton[] botoes = new JButton[9];
    private String simboloOponente;
    private String simboloJogador;
    private JLabel statusLabel;
    private JPanel gamePanel;
    private JPanel statusPanel;
    private Color corFundo = new Color(240, 248, 255); // Azul claro
    private Color corX = new Color(70, 130, 180); // Azul aço
    private Color corO = new Color(220, 20, 60); // Vermelho carmesim
    private Font fonteBotao = new Font("Arial", Font.BOLD, 24);
    private Font fonteStatus = new Font("Arial", Font.BOLD, 16);
    private Socket socket;
    private boolean jogoFinalizado = false;
    
    public Interface(String simbolo, DataInputStream in, DataOutputStream out, Socket socket) {
        this.simboloJogador = simbolo;
        this.simboloOponente = simbolo.equals("X") ? "O" : "X";
        this.socket = socket;
        
        // Configuração da janela principal
        setTitle("Jogo da Velha - Jogador " + simbolo);
        setSize(400, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centraliza a janela
        setResizable(false);
        
        // Adicionar um listener para fechar o socket quando a janela for fechada
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        System.out.println("Socket fechado com sucesso.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
        // Configuração do layout
        setLayout(new BorderLayout());
        getContentPane().setBackground(corFundo);
        
        // Painel de status
        statusPanel = new JPanel();
        statusPanel.setBackground(corFundo);
        statusPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        statusLabel = new JLabel("Aguardando o seu turno...");
        statusLabel.setFont(fonteStatus);
        statusPanel.add(statusLabel);
        
        add(statusPanel, BorderLayout.NORTH);
        
        // Painel do jogo
        gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(3, 3, 5, 5));
        gamePanel.setBackground(new Color(100, 100, 100));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (int i = 0; i < 9; i++) {
            final int pos = i;
            botoes[i] = new JButton("");
            botoes[i].setFont(fonteBotao);
            botoes[i].setBackground(Color.WHITE);
            botoes[i].setFocusPainted(false);
            botoes[i].setEnabled(false);
            
            botoes[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (jogoFinalizado) return; // Ignora cliques se o jogo já terminou
                    
                    botoes[pos].setText(simboloJogador);
                    botoes[pos].setForeground(simboloJogador.equals("X") ? corX : corO);
                    botoes[pos].setEnabled(false);
                    statusLabel.setText("Aguardando jogada do oponente...");
                    
                    // Desabilita todos os botões enquanto espera a jogada do oponente
                    for (JButton botao : botoes) {
                        botao.setEnabled(false);
                    }
                    
                    try {
                        out.writeUTF(String.valueOf(pos));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            gamePanel.add(botoes[i]);
        }
        
        add(gamePanel, BorderLayout.CENTER);
        
        // Rodapé com informações
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(corFundo);
        JLabel footerLabel = new JLabel("Você é o jogador: " + simbolo);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        footerPanel.add(footerLabel);
        
        add(footerPanel, BorderLayout.SOUTH);

        new Thread(() -> {
            try {
                while (true) {
                    String comando = in.readUTF();
                    
                    if (comando.equals("SEU_TURNO")) {
                        SwingUtilities.invokeLater(() -> {
                            if (jogoFinalizado) return; // Ignora se o jogo já terminou
                            
                            statusLabel.setText("Sua vez de jogar!");
                            for (JButton botao : botoes) {
                                if (botao.getText().isEmpty()) {
                                    botao.setEnabled(true);
                                }
                            }
                        });
                    } 
                    else if (comando.equals("JOGADA")) {
                        int pos = Integer.parseInt(in.readUTF());
                        SwingUtilities.invokeLater(() -> {
                            if (jogoFinalizado) return; // Ignora se o jogo já terminou
                            
                            botoes[pos].setText(simboloOponente);
                            botoes[pos].setForeground(simboloOponente.equals("X") ? corX : corO);
                            botoes[pos].setEnabled(false);
                            statusLabel.setText("Aguardando seu turno...");
                        });
                    }
                    else if (comando.equals("VITORIA")) {
                        String vencedor = in.readUTF();
                        SwingUtilities.invokeLater(() -> {
                            jogoFinalizado = true; // Marca o jogo como finalizado
                            
                            if (vencedor.equals(simboloJogador)) {
                                statusLabel.setText("Parabéns! Você venceu!");
                                JOptionPane.showMessageDialog(Interface.this, "Você venceu!", "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                statusLabel.setText("Você perdeu!");
                                JOptionPane.showMessageDialog(Interface.this, "Você perdeu!", "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                            }
                            desabilitarTodosBotoes();
                        });
                    }
                    else if (comando.equals("EMPATE")) {
                        SwingUtilities.invokeLater(() -> {
                            jogoFinalizado = true; // Marca o jogo como finalizado
                            
                            statusLabel.setText("Empate!");
                            JOptionPane.showMessageDialog(Interface.this, "Empate!", "Fim de Jogo", JOptionPane.INFORMATION_MESSAGE);
                            desabilitarTodosBotoes();
                        });
                    }
                }
            } catch (IOException e) {
                // Evita mostrar erro quando o socket é fechado intencionalmente
                if (!socket.isClosed() && !jogoFinalizado) {
                    JOptionPane.showMessageDialog(Interface.this, "Erro de conexão. Fechando o jogo.");
                    System.exit(0);
                }
            }
        }).start();
    }
    
    private void desabilitarTodosBotoes() {
        for (JButton botao : botoes) {
            botao.setEnabled(false);
        }
    }
}
