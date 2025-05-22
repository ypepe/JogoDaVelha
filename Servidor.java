import java.io.*;
import java.net.*;

public class Servidor {
    private static String[] tabuleiro = new String[9];
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket player1 = null;
        Socket player2 = null;
        
        try {
            // Inicializa o tabuleiro vazio
            for (int i = 0; i < 9; i++) {
                tabuleiro[i] = "";
            }
            
            serverSocket = new ServerSocket(5050);
            System.out.println("Aguardando os jogadores se conectarem");

            player1 = serverSocket.accept();
            System.out.println("Jogador 1 conectado.");
            DataInputStream in1 = new DataInputStream(player1.getInputStream());
            DataOutputStream out1 = new DataOutputStream(player1.getOutputStream());

            player2 = serverSocket.accept();
            System.out.println("Jogador 2 conectado.");
            DataInputStream in2 = new DataInputStream(player2.getInputStream());
            DataOutputStream out2 = new DataOutputStream(player2.getOutputStream());

            out1.writeUTF("X");
            out2.writeUTF("O");

            DataInputStream[] inputs = {in1, in2};
            DataOutputStream[] outputs = {out1, out2};
            String[] simbolos = {"X", "O"};

            int turn = 0;
            boolean jogoAtivo = true;
            
            // Informa ao primeiro jogador que é sua vez
            outputs[0].writeUTF("SEU_TURNO");
            
            while (jogoAtivo) {
                int current = turn % 2;
                int opponent = (turn + 1) % 2;

                // Aguarda a jogada do jogador atual
                String move = inputs[current].readUTF();
                int posicao = Integer.parseInt(move);
                
                // Atualiza o tabuleiro com a jogada
                tabuleiro[posicao] = simbolos[current];

                // Informa ao oponente sobre a jogada
                outputs[opponent].writeUTF("JOGADA");
                outputs[opponent].writeUTF(move);
                
                // Verifica se há um vencedor
                String vencedor = verificarVencedor();
                if (vencedor != null) {
                    // Envia mensagem de vitória para ambos os jogadores
                    outputs[0].writeUTF("VITORIA");
                    outputs[0].writeUTF(vencedor);
                    outputs[1].writeUTF("VITORIA");
                    outputs[1].writeUTF(vencedor);
                    
                    System.out.println("Jogo finalizado! Vencedor: " + vencedor);
                    jogoAtivo = false;
                } 
                // Verifica se houve empate (tabuleiro cheio)
                else if (tabuleiroCompleto()) {
                    outputs[0].writeUTF("EMPATE");
                    outputs[1].writeUTF("EMPATE");
                    
                    System.out.println("Jogo finalizado! Empate!");
                    jogoAtivo = false;
                }
                else {
                    turn++;
                    // Informa ao próximo jogador que é sua vez
                    outputs[turn % 2].writeUTF("SEU_TURNO");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fecha as conexões
            if (player1 != null && !player1.isClosed()) {
                player1.close();
            }
            if (player2 != null && !player2.isClosed()) {
                player2.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Servidor encerrado.");
        }
    }
    
    // Verifica se há um vencedor e retorna o símbolo do vencedor (X ou O) ou null se não houver
    private static String verificarVencedor() {
        // Verifica linhas
        for (int i = 0; i < 9; i += 3) {
            if (!tabuleiro[i].isEmpty() && tabuleiro[i].equals(tabuleiro[i+1]) && tabuleiro[i].equals(tabuleiro[i+2])) {
                return tabuleiro[i];
            }
        }
        
        // Verifica colunas
        for (int i = 0; i < 3; i++) {
            if (!tabuleiro[i].isEmpty() && tabuleiro[i].equals(tabuleiro[i+3]) && tabuleiro[i].equals(tabuleiro[i+6])) {
                return tabuleiro[i];
            }
        }
        
        // Verifica diagonal principal
        if (!tabuleiro[0].isEmpty() && tabuleiro[0].equals(tabuleiro[4]) && tabuleiro[0].equals(tabuleiro[8])) {
            return tabuleiro[0];
        }
        
        // Verifica diagonal secundária
        if (!tabuleiro[2].isEmpty() && tabuleiro[2].equals(tabuleiro[4]) && tabuleiro[2].equals(tabuleiro[6])) {
            return tabuleiro[2];
        }
        
        return null;
    }
    
    // Verifica se o tabuleiro está completamente preenchido (empate)
    private static boolean tabuleiroCompleto() {
        for (String celula : tabuleiro) {
            if (celula.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
