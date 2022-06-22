package br.edu.up.SocketServer.ClientHandler;

// 1. Open a socket.
// 2. Open an input stream and output stream to the socket.
// 3. Read from and write to the stream according to the server's protocol.
// 4. Close the streams.
// 5. Close the socket.

import java.io.*;
import java.net.Socket;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import br.edu.up.SocketServer.Model.ErrorModel;

/**
 * When a client connects the server spawns a thread to handle the client.
 * This way the server can handle multiple clients at the same time.
 *
 * This keyword should be used in setters, passing the object as an argument,
 * and to call alternate constructors (a constructor with a different set of
 * arguments.
 */

// Runnable is implemented on a class whose instances will be executed by a thread.
public class ClientHandler implements Runnable {

    // Array list of all the threads handling clients so each message can be sent to the client the thread is handling.
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    // Id that will increment with each new client.

    // Socket for a connection, buffer reader and writer for receiving and sending data respectively.
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String ip;

    // Creating the client handler from the socket the server passes.
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.ip = socket.getInetAddress().getHostAddress();
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            clientHandlers.add(this);
            broadcastMessage("SERVIDOR: Novo usuario conectado!");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public boolean isValidDateTime(String dateTime){                                //TO DO: ajustar verificacao de horario
        int day = Integer.parseInt(dateTime.substring(0, 2));
        int month = Integer.parseInt(dateTime.substring(3, 5));
        int year = Integer.parseInt(dateTime.substring(6, 10));
        int hour = Integer.parseInt(dateTime.substring(12, 14));
        int minute = Integer.parseInt(dateTime.substring(15, 17));
        
        DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        // System.out.println(dtf2.format(LocalDateTime.now()));
        return false;
    }

    // Everything in this method is run on a separate thread. We want to listen for messages
    // on a separate thread because listening (bufferedReader.readLine()) is a blocking operation.
    // A blocking operation means the caller waits for the callee to finish its operation.
    @Override
    public void run() {
        String messageFromClient;
        // Continue to listen for messages while a connection with the client is still established.
        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                JSONObject jsonObject = new JSONObject(messageFromClient);

                /*      identificador - id!=NULL ; id1 == id2 ;
                 *      mensagem      - msg!=NULL; limite de 10 msgs  ; 
                 *      data          - dt!=NULL ; dt == formatoCerto ; dt == 1minuto de margem de erro da data atual ;
                */

                ErrorModel errorModel = null;
                if(jsonObject.getString("Identificador") == ""){
                    errorModel = new ErrorModel("Identificador não fornecido", 2);
                }else if(jsonObject.getString("Mensagem") == ""){
                    errorModel = new ErrorModel("Mensagem não fornecida", 4);
                }else if(jsonObject.getString("Data") == "") {
                    errorModel = new ErrorModel("Data não fornecida", 3);
                }
                if (errorModel != null) {
                    this.bufferedWriter.write(errorModel.toString());
                    this.bufferedWriter.flush();
                }

                broadcastMessage(messageFromClient);
            }
            catch (JSONException e) {
                ErrorModel errorModel = new ErrorModel("Json mal formatado.", 1);
                try {
                    this.bufferedWriter.write(errorModel.toString());
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
             catch (IOException e) {
                // Close everything gracefully.
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    // Send a message through each client handler thread so that everyone gets the message.
    // Basically each client handler is a connection to a client. So for any message that
    // is received, loop through each connection and send it down it.
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                // You don't want to broadcast the message to the user who sent it.
                if (!clientHandler.ip.equals(ip)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                // Gracefully close everything.
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    // If the client disconnects for any reason remove them from the list so a message isn't sent down a broken connection.
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVIDOR: O " + ip + " saiu do chat!");
    }

    // Helper method to close everything so you don't have to repeat yourself.
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        // Note you only need to close the outer wrapper as the underlying streams are closed when you close the wrapper.
        // Note you want to close the outermost wrapper so that everything gets flushed.
        // Note that closing a socket will also close the socket's InputStream and OutputStream.
        // Closing the input stream closes the socket. You need to use shutdownInput() on socket to just close the input stream.
        // Closing the socket will also close the socket's input stream and output stream.
        // Close the socket after closing the streams.

        // The client disconnected or an error occurred so remove them from the list so no message is broadcasted.
        removeClientHandler();
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

