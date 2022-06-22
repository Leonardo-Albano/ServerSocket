package br.edu.up.SocketServer;

import br.edu.up.SocketServer.Model.MessageModel;
import br.edu.up.SocketServer.Server.Server;

import java.net.ServerSocket;

public class App {
    public static void main(String[] args) throws Exception {
        MessageModel message = new MessageModel();
        message.Identificador = "Alan Legal";
        message.Mensagem = "Ola, eu sou o Alan";
        message.Data = "21/06/2022 21:08";

        System.out.println(message);


        ServerSocket serverSocket = new ServerSocket(4545);
        Server server = new Server(serverSocket);
        server.startServer();
    }
}
