package br.unoesc.linhaviva.data.remote.dto;

public class EnvelopeDto<T> {
    public String atualizadoEm;
    public String fonte;
    public String observacao;
    public Boolean emOperacao;
    public T dados;
}
