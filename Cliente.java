import javax.swing.*;
import java.io.*;
import java.net.*;

public class Cliente {
    private static Socket socket;
    
    public static void main(String[] args) {
        try {
            // Tenta conectar ao servidor na porta 5050
            System.out.println("Tentando conectar ao servidor...");
            socket = new Socket("localhost", 5050);
            System.out.println("Conectado ao servidor com sucesso!");
            
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String symbol = in.readUTF();
            System.out.println("Você é o jogador: " + symbol);

            SwingUtilities.invokeLater(() -> {
                Interface board = new Interface(symbol, in, out, socket);
                board.setVisible(true);
            });
            
            // Adicionar um gancho de encerramento para fechar o socket quando o programa terminar
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                        System.out.println("Socket fechado com sucesso.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            
        } catch (ConnectException e) {
            System.out.println("ERRO: Não foi possível conectar ao servidor.");
            System.out.println("Certifique-se de que o servidor está em execução antes de iniciar o cliente.");
            System.out.println("Execute 'java Servidor' em um terminal separado primeiro.");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: " + e.getMessage());
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(1);
        }
    }
}
