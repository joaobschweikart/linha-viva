package br.unoesc.linhaviva.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import java.util.List;

import br.unoesc.linhaviva.data.local.BancoLocal;
import br.unoesc.linhaviva.data.local.entity.AvisoEntity;
import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;

public class RepositorioConteudo {

    private final BancoLocal banco;

    public RepositorioConteudo(Context contexto) {
        this.banco = BancoLocal.get(contexto);
    }

    public LiveData<List<AvisoEntity>> observarAvisos() {
        return banco.avisoDao().observarTodos();
    }

    public LiveData<List<AvisoEntity>> observarAvisosDaLinha(String linhaId) {
        return banco.avisoDao().observarPorLinha(linhaId);
    }

    public LiveData<List<InformacaoEntity>> observarInformacoes() {
        return banco.informacaoDao().observarTodas();
    }

    public LiveData<List<InformacaoEntity>> observarInformacoes(String categoria) {
        return banco.informacaoDao().observarPorCategoria(categoria);
    }
}
