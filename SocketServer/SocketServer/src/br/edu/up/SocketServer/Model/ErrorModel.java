package br.edu.up.SocketServer.Model;

public class ErrorModel {
    String Erro;
    int CodigoErro;

    public ErrorModel(String Erro, int CodigoErro){
        this.Erro=Erro;
        this.CodigoErro=CodigoErro;
    }

    @Override
    public String toString(){
        return "{'Erro':'"+this.Erro+"','CodigoErro':"+this.CodigoErro+"}";
    }
}
