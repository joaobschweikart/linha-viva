package br.unoesc.linhaviva.data;

import java.util.ArrayList;
import java.util.List;

import br.unoesc.linhaviva.data.local.entity.AvisoEntity;
import br.unoesc.linhaviva.data.local.entity.HorarioEntity;
import br.unoesc.linhaviva.data.local.entity.InformacaoEntity;
import br.unoesc.linhaviva.data.local.entity.ItinerarioEntity;
import br.unoesc.linhaviva.data.local.entity.LinhaEntity;
import br.unoesc.linhaviva.data.local.entity.PontoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoEntity;
import br.unoesc.linhaviva.data.local.entity.PrevisaoLinhaEntity;
import br.unoesc.linhaviva.data.remote.dto.AvisoDto;
import br.unoesc.linhaviva.data.remote.dto.HorarioDto;
import br.unoesc.linhaviva.data.remote.dto.InformacaoDto;
import br.unoesc.linhaviva.data.remote.dto.ItinerarioDto;
import br.unoesc.linhaviva.data.remote.dto.LinhaDto;
import br.unoesc.linhaviva.data.remote.dto.PontoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoDto;
import br.unoesc.linhaviva.data.remote.dto.PrevisaoLinhaDto;

public final class Mapeador {

    private Mapeador() {
    }

    public static LinhaEntity paraEntidade(LinhaDto dto, long instante) {
        LinhaEntity e = new LinhaEntity();
        e.id = dto.id;
        e.numero = dto.numero;
        e.nome = dto.nome;
        e.cor = dto.cor;
        e.circular = dto.circular;
        e.acessivel = dto.acessivel;
        e.empresa = dto.empresa;
        e.sentidoIda = dto.sentidoIda;
        e.sentidoVolta = dto.sentidoVolta;
        e.primeiraPartida = dto.primeiraPartida;
        e.ultimaPartida = dto.ultimaPartida;
        e.intervaloMin = dto.intervaloMin;
        e.totalPontos = dto.totalPontos;
        e.duracaoMin = dto.duracaoMin;
        e.atualizadoEm = instante;
        return e;
    }

    public static PontoEntity paraEntidade(PontoDto dto, long instante) {
        PontoEntity e = new PontoEntity();
        e.id = dto.id;
        e.nome = dto.nome;
        e.endereco = dto.endereco;
        e.bairro = dto.bairro;
        e.latitude = dto.latitude;
        e.longitude = dto.longitude;
        e.abrigo = dto.abrigo;
        e.acessivel = dto.acessivel;
        e.terminal = dto.terminal;
        e.atualizadoEm = instante;
        return e;
    }

    public static ItinerarioEntity paraEntidade(ItinerarioDto dto) {
        ItinerarioEntity e = new ItinerarioEntity();
        e.linhaId = dto.linhaId;
        e.sentido = dto.sentido;
        e.ordem = dto.ordem;
        e.pontoId = dto.pontoId;
        e.pontoNome = dto.pontoNome;
        e.bairro = dto.bairro;
        e.latitude = dto.latitude;
        e.longitude = dto.longitude;
        e.terminal = dto.terminal;
        e.tempoAcumuladoMin = dto.tempoAcumuladoMin;
        return e;
    }

    public static HorarioEntity paraEntidade(HorarioDto dto) {
        HorarioEntity e = new HorarioEntity();
        e.linhaId = dto.linhaId;
        e.sentido = dto.sentido;
        e.diaTipo = dto.diaTipo;
        e.hora = dto.hora;
        return e;
    }

    public static AvisoEntity paraEntidade(AvisoDto dto, long instante) {
        AvisoEntity e = new AvisoEntity();
        e.id = dto.id;
        e.linhaId = dto.linhaId;
        e.severidade = dto.severidade;
        e.titulo = dto.titulo;
        e.descricao = dto.descricao;
        e.publicadoEm = dto.publicadoEm;
        e.atualizadoEm = instante;
        return e;
    }

    public static InformacaoEntity paraEntidade(InformacaoDto dto, long instante) {
        InformacaoEntity e = new InformacaoEntity();
        e.id = dto.id;
        e.categoria = dto.categoria;
        e.titulo = dto.titulo;
        e.valor = dto.valor;
        e.descricao = dto.descricao;
        e.ordem = dto.ordem;
        e.atualizadoEm = instante;
        return e;
    }

    public static PrevisaoEntity paraEntidade(PrevisaoDto dto, String pontoId, long instante) {
        PrevisaoEntity e = new PrevisaoEntity();
        e.pontoId = pontoId;
        e.linhaId = dto.linhaId;
        e.sentido = dto.sentido;
        e.linhaNumero = dto.linhaNumero;
        e.linhaNome = dto.linhaNome;
        e.sentidoDescricao = dto.sentidoDescricao;
        e.minutos = dto.minutos;
        e.horaPrevista = dto.horaPrevista;
        e.horaProgramada = dto.horaProgramada;
        e.tempoReal = dto.tempoReal;
        e.origem = dto.origem;
        e.prefixoVeiculo = dto.prefixoVeiculo;
        e.acessivel = dto.acessivel;
        e.lotacao = dto.lotacao;
        e.atualizadoEm = instante;
        return e;
    }

    public static PrevisaoLinhaEntity paraEntidade(PrevisaoLinhaDto dto, long instante) {
        PrevisaoLinhaEntity e = new PrevisaoLinhaEntity();
        e.linhaId = dto.linhaId;
        e.sentido = dto.sentido;
        e.minutos = dto.minutos;
        e.horaPrevista = dto.horaPrevista;
        e.tempoReal = dto.tempoReal;
        e.origem = dto.origem;
        e.prefixoVeiculo = dto.prefixoVeiculo;
        e.pontoReferenciaId = dto.pontoReferenciaId;
        e.pontoReferenciaNome = dto.pontoReferenciaNome;
        e.atualizadoEm = instante;
        return e;
    }

    public static List<LinhaEntity> linhas(List<LinhaDto> dtos, long instante) {
        List<LinhaEntity> lista = new ArrayList<>();
        for (LinhaDto dto : dtos) lista.add(paraEntidade(dto, instante));
        return lista;
    }

    public static List<PontoEntity> pontos(List<PontoDto> dtos, long instante) {
        List<PontoEntity> lista = new ArrayList<>();
        for (PontoDto dto : dtos) lista.add(paraEntidade(dto, instante));
        return lista;
    }

    public static List<ItinerarioEntity> itinerarios(List<ItinerarioDto> dtos) {
        List<ItinerarioEntity> lista = new ArrayList<>();
        for (ItinerarioDto dto : dtos) lista.add(paraEntidade(dto));
        return lista;
    }

    public static List<HorarioEntity> horarios(List<HorarioDto> dtos) {
        List<HorarioEntity> lista = new ArrayList<>();
        for (HorarioDto dto : dtos) lista.add(paraEntidade(dto));
        return lista;
    }

    public static List<AvisoEntity> avisos(List<AvisoDto> dtos, long instante) {
        List<AvisoEntity> lista = new ArrayList<>();
        for (AvisoDto dto : dtos) lista.add(paraEntidade(dto, instante));
        return lista;
    }

    public static List<InformacaoEntity> informacoes(List<InformacaoDto> dtos, long instante) {
        List<InformacaoEntity> lista = new ArrayList<>();
        for (InformacaoDto dto : dtos) lista.add(paraEntidade(dto, instante));
        return lista;
    }
}
