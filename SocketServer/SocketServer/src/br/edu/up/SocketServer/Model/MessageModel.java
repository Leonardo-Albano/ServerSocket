package br.edu.up.SocketServer.Model;

public class MessageModel {
    public String Identificador;
    public String Mensagem;
    public String Data;

    @Override
    public String toString() {
        return "{" +
                "'Identificador': " + "'"+this.Identificador+"'," +
                "'Mensagem': "+ "'"+this.Mensagem+"'," +
                "'Data': " +"'"+this.Data+"'" +
                "}";
    }
}
