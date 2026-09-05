'use strict';

const http = require('http');
const crypto = require('crypto');
const { URL } = require('url');
const ds = require('./src/dataset');
const sim = require('./src/previsoes');

const PORTA = Number(process.env.PORT || 3000);
const HOST = process.env.HOST || '0.0.0.0';
const BASE = '/api/v1';

const AVISO_DADOS = 'Dados fictícios para fins acadêmicos. Não representam a operação real da Auto Viação Chapecó.';

function envelope(dados, extra) {
  return Object.assign({
    atualizadoEm: new Date().toISOString(),
    fonte: AVISO_DADOS,
    dados
  }, extra || {});
}

function responder(res, status, corpo) {
  const json = JSON.stringify(corpo);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    'Access-Control-Allow-Origin': '*'
  });
  res.end(json);
}

function erro(res, status, mensagem) {
  responder(res, status, { erro: mensagem, status });
}

function numero(valor, padrao) {
  const n = Number(valor);
  return Number.isFinite(n) ? n : padrao;
}

function sentidoValido(linha, bruto) {
  const s = (bruto || 'IDA').toUpperCase();
  if (linha.circular) return 'IDA';
  return s === 'VOLTA' ? 'VOLTA' : 'IDA';
}

function resumoLinha(linha) {
  return {
    id: linha.id,
    numero: linha.numero,
    nome: linha.nome,
    cor: linha.cor,
    circular: linha.circular,
    acessivel: linha.acessivel,
    empresa: ds.EMPRESA,
    sentidoIda: linha.sentidoIda,
    sentidoVolta: linha.sentidoVolta,
    primeiraPartida: linha.primeiraPartida,
    ultimaPartida: linha.ultimaPartida,
    intervaloMin: linha.intervaloMin,
    totalPontos: linha.itinerario.length,
    duracaoMin: linha.temposMin[linha.temposMin.length - 1]
  };
}


const catalogoCompleto = (() => {
  const itinerarios = [];
  const horarios = [];
  for (const linha of ds.LINHAS) {
    for (const sentido of ds.sentidosDaLinha(linha)) {
      itinerarios.push(...ds.itinerarioDaLinha(linha, sentido));
      horarios.push(...ds.horariosDaLinha(linha, sentido));
    }
  }
  return { itinerarios, horarios };
})();

// Assinatura do catalogo estatico: permite ao aplicativo pular o download
// completo quando nada mudou desde a ultima sincronizacao (RNF04/RNF06).
const VERSAO_DADOS = crypto
  .createHash('sha1')
  .update(JSON.stringify([ds.LINHAS, ds.PONTOS, ds.INFORMACOES]))
  .digest('hex')
  .slice(0, 12);

const rotas = [
  {
    metodo: 'GET', padrao: /^\/?$/,
    handler: (req, res) => responder(res, 200, {
      servico: 'Linha Viva — API de demonstração',
      versao: '1.0.0',
      base: BASE,
      fonte: AVISO_DADOS,
      endpoints: [
        `${BASE}/health`,
        `${BASE}/versao`,
        `${BASE}/itinerarios`,
        `${BASE}/horarios`,
        `${BASE}/linhas`,
        `${BASE}/linhas/previsoes`,
        `${BASE}/linhas/{id}`,
        `${BASE}/linhas/{id}/itinerario?sentido=IDA|VOLTA`,
        `${BASE}/linhas/{id}/horarios?sentido=IDA|VOLTA`,
        `${BASE}/linhas/{id}/veiculo?sentido=IDA|VOLTA`,
        `${BASE}/veiculos`,
        `${BASE}/pontos?lat={lat}&lon={lon}&raio={metros}`,
        `${BASE}/pontos/{id}`,
        `${BASE}/pontos/{id}/previsoes`,
        `${BASE}/avisos`,
        `${BASE}/informacoes`
      ]
    })
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/health$`),
    handler: (req, res) => responder(res, 200, {
      status: 'ok',
      horaServidor: new Date().toISOString(),
      tipoDeDia: sim.tipoDeDia(sim.agoraLocal()),
      linhas: ds.LINHAS.length,
      pontos: ds.PONTOS.length
    })
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/versao$`),
    handler: (req, res) => responder(res, 200, {
      versaoDados: VERSAO_DADOS,
      linhas: ds.LINHAS.length,
      pontos: ds.PONTOS.length,
      geradoEm: new Date().toISOString(),
      fonte: AVISO_DADOS
    })
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/itinerarios$`),
    handler: (req, res) => responder(res, 200, envelope(catalogoCompleto.itinerarios, {
      total: catalogoCompleto.itinerarios.length, versaoDados: VERSAO_DADOS
    }))
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/horarios$`),
    handler: (req, res) => responder(res, 200, envelope(catalogoCompleto.horarios, {
      total: catalogoCompleto.horarios.length, versaoDados: VERSAO_DADOS
    }))
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/linhas$`),
    handler: (req, res, m, url) => {
      const busca = (url.searchParams.get('q') || '').trim().toLowerCase();
      let linhas = ds.LINHAS.map(resumoLinha);
      if (busca) {
        linhas = linhas.filter((l) =>
          l.numero.toLowerCase().includes(busca) ||
          l.nome.toLowerCase().includes(busca) ||
          l.sentidoIda.toLowerCase().includes(busca) ||
          l.sentidoVolta.toLowerCase().includes(busca));
      }
      responder(res, 200, envelope(linhas, { total: linhas.length }));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/linhas/previsoes$`),
    handler: (req, res) => responder(res, 200, envelope(sim.proximaPartidaDasLinhas(), {
      observacao: 'Próxima partida de cada linha. Previsões estimadas a partir do horário programado.'
    }))
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/linhas/([^/]+)$`),
    handler: (req, res, m) => {
      const linha = ds.LINHAS.find((l) => l.id === m[1]);
      if (!linha) return erro(res, 404, 'Linha não encontrada');
      responder(res, 200, envelope(resumoLinha(linha)));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/linhas/([^/]+)/itinerario$`),
    handler: (req, res, m, url) => {
      const linha = ds.LINHAS.find((l) => l.id === m[1]);
      if (!linha) return erro(res, 404, 'Linha não encontrada');
      const sentido = sentidoValido(linha, url.searchParams.get('sentido'));
      responder(res, 200, envelope(ds.itinerarioDaLinha(linha, sentido), { linhaId: linha.id, sentido }));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/linhas/([^/]+)/horarios$`),
    handler: (req, res, m, url) => {
      const linha = ds.LINHAS.find((l) => l.id === m[1]);
      if (!linha) return erro(res, 404, 'Linha não encontrada');
      const sentido = sentidoValido(linha, url.searchParams.get('sentido'));
      responder(res, 200, envelope(ds.horariosDaLinha(linha, sentido), { linhaId: linha.id, sentido }));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/linhas/([^/]+)/veiculo$`),
    handler: (req, res, m, url) => {
      const linha = ds.LINHAS.find((l) => l.id === m[1]);
      if (!linha) return erro(res, 404, 'Linha não encontrada');
      const sentido = sentidoValido(linha, url.searchParams.get('sentido'));
      const veiculo = sim.veiculoDaLinha(linha.id, sentido);
      if (!veiculo) return responder(res, 200, envelope(null, { emOperacao: false, motivo: 'Nenhum veículo em circulação neste horário.' }));
      responder(res, 200, envelope(veiculo, { emOperacao: true }));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/veiculos$`),
    handler: (req, res) => responder(res, 200, envelope(sim.veiculosAtivos()))
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/pontos$`),
    handler: (req, res, m, url) => {
      const lat = url.searchParams.get('lat');
      const lon = url.searchParams.get('lon');
      const busca = (url.searchParams.get('q') || '').trim().toLowerCase();

      let pontos;
      if (lat !== null && lon !== null) {
        const raio = numero(url.searchParams.get('raio'), 1500);
        const limite = numero(url.searchParams.get('limite'), 20);
        pontos = sim.pontosProximos(numero(lat, 0), numero(lon, 0), raio, limite);
      } else {
        pontos = ds.PONTOS.slice();
      }
      if (busca) {
        pontos = pontos.filter((p) =>
          p.nome.toLowerCase().includes(busca) ||
          p.endereco.toLowerCase().includes(busca) ||
          p.bairro.toLowerCase().includes(busca));
      }
      responder(res, 200, envelope(pontos, { total: pontos.length }));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/pontos/([^/]+)$`),
    handler: (req, res, m) => {
      const ponto = ds.pontosPorId.get(m[1]);
      if (!ponto) return erro(res, 404, 'Ponto não encontrado');
      const linhas = ds.linhasQuePassamEm(ponto.id)
        .map((p) => ({ linhaId: p.linha.id, numero: p.linha.numero, nome: p.linha.nome, sentido: p.sentido }));
      responder(res, 200, envelope(Object.assign({}, ponto, { linhas })));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/pontos/([^/]+)/previsoes$`),
    handler: (req, res, m, url) => {
      const ponto = ds.pontosPorId.get(m[1]);
      if (!ponto) return erro(res, 404, 'Ponto não encontrado');
      const limite = numero(url.searchParams.get('limite'), 8);
      responder(res, 200, envelope(sim.previsoesDoPonto(ponto.id, null, limite), {
        pontoId: ponto.id,
        observacao: 'Previsões estimadas a partir do horário programado. Não há integração com rastreamento real da frota.'
      }));
    }
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/avisos$`),
    handler: (req, res) => responder(res, 200, envelope(ds.AVISOS))
  },
  {
    metodo: 'GET', padrao: new RegExp(`^${BASE}/informacoes$`),
    handler: (req, res) => responder(res, 200, envelope(ds.INFORMACOES))
  }
];

const servidor = http.createServer((req, res) => {
  if (req.method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type'
    });
    return res.end();
  }

  const url = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
  const caminho = decodeURIComponent(url.pathname.replace(/\/+$/, '')) || '/';

  for (const rota of rotas) {
    if (rota.metodo !== req.method) continue;
    const m = caminho.match(rota.padrao);
    if (m) {
      try {
        return rota.handler(req, res, m, url);
      } catch (e) {
        console.error('[erro]', e);
        return erro(res, 500, 'Erro interno na API de demonstração');
      }
    }
  }
  erro(res, 404, `Recurso não encontrado: ${caminho}`);
});

servidor.listen(PORTA, HOST, () => {
  console.log(`Linha Viva — API de demonstração`);
  console.log(`  http://localhost:${PORTA}${BASE}`);
  console.log(`  Emulador Android: http://10.0.2.2:${PORTA}${BASE}/`);
  console.log(`  ${AVISO_DADOS}`);
});
