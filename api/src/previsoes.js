'use strict';

const ds = require('./dataset');
const { interpolar, distanciaMetros } = require('./geo');

// A operadora nao disponibiliza rastreamento publico da frota. As previsoes e posicoes
// abaixo sao SIMULADAS a partir da tabela de horarios programados, com um desvio
// deterministico por viagem, apenas para demonstrar o comportamento do aplicativo.

const JANELA_TEMPO_REAL_MIN = 25;

function agoraLocal(referencia) {
  const d = referencia ? new Date(referencia) : new Date();
  const utc = d.getTime() + d.getTimezoneOffset() * 60000;
  return new Date(utc - 3 * 3600000);
}

function minutosDoDia(data) {
  return data.getHours() * 60 + data.getMinutes() + data.getSeconds() / 60;
}

function tipoDeDia(data) {
  const dia = data.getDay();
  if (dia === 0) return 'DOMINGO';
  if (dia === 6) return 'SABADO';
  return 'UTIL';
}

function desvioDaViagem(linhaId, sentido, partidaMin) {
  const semente = `${linhaId}${sentido}${partidaMin}`
    .split('')
    .reduce((acc, c) => (acc * 31 + c.charCodeAt(0)) % 100003, 7);
  return ((semente % 9) - 3) / 2;
}

function lotacaoDaViagem(linhaId, partidaMin, data) {
  const hora = data.getHours();
  const pico = (hora >= 6 && hora <= 8) || (hora >= 17 && hora <= 19);
  const semente = (partidaMin + linhaId.charCodeAt(0)) % 3;
  if (pico) return semente === 0 ? 'ALTA' : 'MEDIA';
  return semente === 2 ? 'MEDIA' : 'BAIXA';
}

function viagensDaLinha(linha, sentido, data) {
  const diaTipo = tipoDeDia(data);
  const horarios = ds.horariosDaLinha(linha, sentido).filter((h) => h.diaTipo === diaTipo);
  return horarios.map((h) => {
    const partidaMin = ds.horaParaMinutos(h.hora);
    return {
      partidaMin,
      partidaHora: h.hora,
      desvioMin: desvioDaViagem(linha.id, sentido, partidaMin),
      prefixo: `${1000 + ((partidaMin * 7 + linha.id.charCodeAt(2)) % 90)}`
    };
  });
}

function previsoesDoPonto(pontoId, referencia, limite) {
  const data = agoraLocal(referencia);
  const agoraMin = minutosDoDia(data);
  const resultado = [];

  for (const passagem of ds.linhasQuePassamEm(pontoId)) {
    const { linha, sentido, tempoAcumuladoMin } = passagem;
    for (const viagem of viagensDaLinha(linha, sentido, data)) {
      const chegadaMin = viagem.partidaMin + tempoAcumuladoMin + viagem.desvioMin;
      const faltamMin = chegadaMin - agoraMin;
      if (faltamMin < -1 || faltamMin > 120) continue;

      const emCirculacao = agoraMin >= viagem.partidaMin;
      resultado.push({
        linhaId: linha.id,
        linhaNumero: linha.numero,
        linhaNome: linha.nome,
        sentido,
        sentidoDescricao: sentido === 'VOLTA' ? linha.sentidoVolta : linha.sentidoIda,
        pontoId,
        minutos: Math.max(0, Math.round(faltamMin)),
        horaPrevista: ds.minutosParaHora(Math.round(chegadaMin)),
        horaProgramada: ds.minutosParaHora(viagem.partidaMin + tempoAcumuladoMin),
        tempoReal: emCirculacao && faltamMin <= JANELA_TEMPO_REAL_MIN,
        origem: emCirculacao && faltamMin <= JANELA_TEMPO_REAL_MIN ? 'SIMULADO_TEMPO_REAL' : 'HORARIO_PROGRAMADO',
        prefixoVeiculo: emCirculacao ? viagem.prefixo : null,
        acessivel: linha.acessivel,
        lotacao: emCirculacao ? lotacaoDaViagem(linha.id, viagem.partidaMin, data) : null,
        partidaTerminal: viagem.partidaHora
      });
      break;
    }
  }

  resultado.sort((a, b) => a.minutos - b.minutos);
  return limite ? resultado.slice(0, limite) : resultado;
}

function posicaoNaRota(itinerario, minutosDecorridos) {
  if (minutosDecorridos <= 0) return { ...itinerario[0], fracao: 0 };
  const ultimo = itinerario[itinerario.length - 1];
  if (minutosDecorridos >= ultimo.tempoAcumuladoMin) return { ...ultimo, fracao: 1 };

  for (let i = 0; i < itinerario.length - 1; i++) {
    const a = itinerario[i];
    const b = itinerario[i + 1];
    if (minutosDecorridos >= a.tempoAcumuladoMin && minutosDecorridos <= b.tempoAcumuladoMin) {
      const span = b.tempoAcumuladoMin - a.tempoAcumuladoMin || 1;
      const fracao = (minutosDecorridos - a.tempoAcumuladoMin) / span;
      const ponto = interpolar(a, b, fracao);
      return { ...ponto, proximoPontoOrdem: b.ordem, fracao: minutosDecorridos / ultimo.tempoAcumuladoMin };
    }
  }
  return { ...ultimo, fracao: 1 };
}

function veiculoDaLinha(linhaId, sentido, referencia) {
  const linha = ds.LINHAS.find((l) => l.id === linhaId);
  if (!linha) return null;

  const data = agoraLocal(referencia);
  const agoraMin = minutosDoDia(data);
  const itinerario = ds.itinerarioDaLinha(linha, sentido);
  const duracao = itinerario[itinerario.length - 1].tempoAcumuladoMin;

  const emCurso = viagensDaLinha(linha, sentido, data)
    .filter((v) => agoraMin >= v.partidaMin && agoraMin <= v.partidaMin + duracao)
    .sort((a, b) => b.partidaMin - a.partidaMin)[0];

  if (!emCurso) return null;

  const decorridos = agoraMin - emCurso.partidaMin - emCurso.desvioMin;
  const posicao = posicaoNaRota(itinerario, decorridos);
  const proximo = itinerario.find((i) => i.tempoAcumuladoMin >= decorridos) || itinerario[itinerario.length - 1];

  return {
    linhaId: linha.id,
    sentido,
    prefixo: emCurso.prefixo,
    latitude: Number(posicao.latitude.toFixed(6)),
    longitude: Number(posicao.longitude.toFixed(6)),
    proximoPontoId: proximo.pontoId,
    proximoPontoNome: proximo.pontoNome,
    progresso: Number(Math.min(1, Math.max(0, posicao.fracao)).toFixed(3)),
    lotacao: lotacaoDaViagem(linha.id, emCurso.partidaMin, data),
    acessivel: linha.acessivel,
    partidaTerminal: emCurso.partidaHora,
    atualizadoEm: new Date().toISOString(),
    origem: 'SIMULADO',
    observacao: 'Posição estimada a partir do horário programado. Não há integração com a frota real.'
  };
}

function veiculosAtivos(referencia) {
  const lista = [];
  for (const linha of ds.LINHAS) {
    for (const sentido of ds.sentidosDaLinha(linha)) {
      const veiculo = veiculoDaLinha(linha.id, sentido, referencia);
      if (veiculo) lista.push(veiculo);
    }
  }
  return lista;
}

function pontosProximos(lat, lon, raioMetros, limite) {
  return ds.PONTOS
    .map((p) => ({ ...p, distanciaMetros: distanciaMetros(lat, lon, p.latitude, p.longitude) }))
    .filter((p) => p.distanciaMetros <= raioMetros)
    .sort((a, b) => a.distanciaMetros - b.distanciaMetros)
    .slice(0, limite || 20);
}


function proximaPartidaDasLinhas(referencia) {
  const data = agoraLocal(referencia);
  const agoraMin = minutosDoDia(data);
  const resultado = [];

  for (const linha of ds.LINHAS) {
    for (const sentido of ds.sentidosDaLinha(linha)) {
      const itinerario = ds.itinerarioDaLinha(linha, sentido);
      const origem = itinerario[0];
      const proxima = viagensDaLinha(linha, sentido, data)
        .map((v) => ({ v, faltamMin: v.partidaMin + v.desvioMin - agoraMin }))
        .filter((x) => x.faltamMin >= -1)
        .sort((a, b) => a.faltamMin - b.faltamMin)[0];

      if (!proxima) {
        resultado.push({
          linhaId: linha.id, sentido, minutos: -1, horaPrevista: null,
          tempoReal: false, origem: 'SEM_OPERACAO', prefixoVeiculo: null,
          pontoReferenciaId: origem.pontoId, pontoReferenciaNome: origem.pontoNome
        });
        continue;
      }

      const emCirculacao = agoraMin >= proxima.v.partidaMin;
      const tempoReal = emCirculacao && proxima.faltamMin <= JANELA_TEMPO_REAL_MIN;
      resultado.push({
        linhaId: linha.id,
        sentido,
        minutos: Math.max(0, Math.round(proxima.faltamMin)),
        horaPrevista: ds.minutosParaHora(Math.round(proxima.v.partidaMin + proxima.v.desvioMin)),
        tempoReal,
        origem: tempoReal ? 'SIMULADO_TEMPO_REAL' : 'HORARIO_PROGRAMADO',
        prefixoVeiculo: tempoReal ? proxima.v.prefixo : null,
        pontoReferenciaId: origem.pontoId,
        pontoReferenciaNome: origem.pontoNome
      });
    }
  }
  return resultado;
}

module.exports = { previsoesDoPonto, proximaPartidaDasLinhas, veiculoDaLinha, veiculosAtivos, pontosProximos, agoraLocal, tipoDeDia };
