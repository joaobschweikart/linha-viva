'use strict';

// Dados de demonstracao ficticios. Nao representam a operacao real da Auto Viacao Chapeco.

const EMPRESA = 'Auto Viação Chapecó (dados fictícios)';

const PONTOS = [
  { id: 'P1001', nome: 'Terminal Urbano', endereco: 'Av. Getúlio Vargas, 100', bairro: 'Centro', latitude: -27.0997, longitude: -52.6180, abrigo: true, acessivel: true, terminal: true },
  { id: 'P1002', nome: 'Av. Nereu Ramos, 500', endereco: 'Av. Nereu Ramos, 500', bairro: 'Centro', latitude: -27.0980, longitude: -52.6165, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1003', nome: 'Rua Barão do Rio Branco', endereco: 'Rua Barão do Rio Branco, 320', bairro: 'Centro', latitude: -27.0975, longitude: -52.6120, abrigo: false, acessivel: true, terminal: false },
  { id: 'P1005', nome: 'Rua Quintino Bocaiúva', endereco: 'Rua Quintino Bocaiúva, 780', bairro: 'Centro', latitude: -27.0955, longitude: -52.6150, abrigo: true, acessivel: false, terminal: false },
  { id: 'P1007', nome: 'Rua Marechal Deodoro', endereco: 'Rua Marechal Deodoro, 780', bairro: 'Centro', latitude: -27.1018, longitude: -52.6178, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1012', nome: 'Av. Fernando Machado, 2100', endereco: 'Av. Fernando Machado, 2100', bairro: 'Presidente Médici', latitude: -27.1034, longitude: -52.6290, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1015', nome: 'Bairro Presidente Médici', endereco: 'Av. Fernando Machado, 4200', bairro: 'Presidente Médici', latitude: -27.1041, longitude: -52.6420, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1021', nome: 'Av. Servidão Bertaso', endereco: 'Av. Servidão Bertaso, 1500', bairro: 'Santa Maria', latitude: -27.1058, longitude: -52.6580, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1029', nome: 'Rua Uruguai, 3400', endereco: 'Rua Uruguai, 3400', bairro: 'Efapi', latitude: -27.1070, longitude: -52.6740, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1035', nome: 'Terminal Efapi', endereco: 'Av. Atílio Fontana, 6000', bairro: 'Efapi', latitude: -27.1082, longitude: -52.6902, abrigo: true, acessivel: true, terminal: true },
  { id: 'P1043', nome: 'Av. Getúlio Vargas, 1240', endereco: 'Av. Getúlio Vargas, 1240', bairro: 'Centro', latitude: -27.1005, longitude: -52.6152, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1050', nome: 'Av. Leopoldo Sander', endereco: 'Av. Leopoldo Sander, 900', bairro: 'Passo dos Fortes', latitude: -27.0900, longitude: -52.6135, abrigo: false, acessivel: true, terminal: false },
  { id: 'P1053', nome: 'Rua Padre Vitor Battistella', endereco: 'Rua Padre Vitor Battistella, 210', bairro: 'Passo dos Fortes', latitude: -27.0850, longitude: -52.6120, abrigo: true, acessivel: false, terminal: false },
  { id: 'P1057', nome: 'Praça do Passo dos Fortes', endereco: 'Rua São Pedro, 40', bairro: 'Passo dos Fortes', latitude: -27.0790, longitude: -52.6112, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1060', nome: 'Terminal Passo dos Fortes', endereco: 'Av. Leopoldo Sander, 4800', bairro: 'Passo dos Fortes', latitude: -27.0758, longitude: -52.6105, abrigo: true, acessivel: true, terminal: true },
  { id: 'P1070', nome: 'Av. Getúlio Vargas, 3300', endereco: 'Av. Getúlio Vargas Norte, 3300', bairro: 'Vila Rica', latitude: -27.0930, longitude: -52.6060, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1073', nome: 'Rua Frei Bruno', endereco: 'Rua Frei Bruno, 1120', bairro: 'São Cristóvão', latitude: -27.0885, longitude: -52.6020, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1077', nome: 'Escola São Cristóvão', endereco: 'Rua Otávio Rosa, 300', bairro: 'São Cristóvão', latitude: -27.0840, longitude: -52.5995, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1080', nome: 'Final São Cristóvão', endereco: 'Rua Otávio Rosa, 1800', bairro: 'São Cristóvão', latitude: -27.0812, longitude: -52.5975, abrigo: false, acessivel: false, terminal: true },
  { id: 'P1090', nome: 'Rua Clevelândia', endereco: 'Rua Clevelândia, 640', bairro: 'Seminário', latitude: -27.1075, longitude: -52.6110, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1093', nome: 'Seminário Nossa Senhora', endereco: 'Rua Marechal Bormann, 2200', bairro: 'Seminário', latitude: -27.1160, longitude: -52.6075, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1096', nome: 'Rodovia SC-283, km 4', endereco: 'Rodovia SC-283, km 4', bairro: 'Belvedere', latitude: -27.1210, longitude: -52.6250, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1099', nome: 'UFFS — Campus Chapecó', endereco: 'Rodovia SC-484, km 2', bairro: 'Bom Pastor', latitude: -27.1248, longitude: -52.6412, abrigo: true, acessivel: true, terminal: true },
  { id: 'P1110', nome: 'Rua Benjamin Constant', endereco: 'Rua Benjamin Constant, 450', bairro: 'Cristo Rei', latitude: -27.1055, longitude: -52.6200, abrigo: false, acessivel: true, terminal: false },
  { id: 'P1113', nome: 'Av. Atílio Fontana', endereco: 'Av. Atílio Fontana, 1900', bairro: 'Esplanada', latitude: -27.1180, longitude: -52.6245, abrigo: true, acessivel: false, terminal: false },
  { id: 'P1116', nome: 'Quedas do Palmital', endereco: 'Rua das Quedas, 700', bairro: 'Palmital', latitude: -27.1290, longitude: -52.6255, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1120', nome: 'Final Palmital', endereco: 'Rua das Quedas, 2400', bairro: 'Palmital', latitude: -27.1355, longitude: -52.6262, abrigo: true, acessivel: true, terminal: true },
  { id: 'P1130', nome: 'Hospital Regional', endereco: 'Rua Florianópolis, 1448', bairro: 'Centro', latitude: -27.1025, longitude: -52.6135, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1133', nome: 'Rodoviária de Chapecó', endereco: 'Av. Nereu Ramos, 2200', bairro: 'Centro', latitude: -27.0968, longitude: -52.6205, abrigo: true, acessivel: true, terminal: true },
  { id: 'P1140', nome: 'Rua Índios Caingangues', endereco: 'Rua Índios Caingangues, 880', bairro: 'Santa Maria', latitude: -27.1090, longitude: -52.6390, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1143', nome: 'Bairro Santa Maria', endereco: 'Av. Servidão Bertaso, 3300', bairro: 'Santa Maria', latitude: -27.1055, longitude: -52.6605, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1146', nome: 'Rua José Bernardi', endereco: 'Rua José Bernardi, 520', bairro: 'Bela Vista', latitude: -27.1120, longitude: -52.6480, abrigo: false, acessivel: true, terminal: false },
  { id: 'P1150', nome: 'Final Bela Vista', endereco: 'Rua José Bernardi, 2100', bairro: 'Bela Vista', latitude: -27.1158, longitude: -52.6528, abrigo: true, acessivel: false, terminal: true },
  { id: 'P1160', nome: 'Av. Getúlio Vargas, 3100', endereco: 'Av. Getúlio Vargas Norte, 3100', bairro: 'Universitário', latitude: -27.0930, longitude: -52.6200, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1163', nome: 'Unochapecó — Bloco A', endereco: 'Rua Senador Attílio Fontana, 591', bairro: 'Universitário', latitude: -27.0905, longitude: -52.6340, abrigo: true, acessivel: true, terminal: false },
  { id: 'P1166', nome: 'Rua Buenos Aires', endereco: 'Rua Buenos Aires, 145', bairro: 'Jardim Itália', latitude: -27.0885, longitude: -52.6420, abrigo: false, acessivel: false, terminal: false },
  { id: 'P1170', nome: 'Final Jardim Itália', endereco: 'Rua Buenos Aires, 1600', bairro: 'Jardim Itália', latitude: -27.0868, longitude: -52.6455, abrigo: true, acessivel: true, terminal: true }
];

const LINHAS = [
  {
    id: '215', numero: '215', nome: 'Efapi via Centro', cor: '#0B4DA2', circular: false, acessivel: true,
    sentidoIda: 'Terminal Urbano → Efapi', sentidoVolta: 'Efapi → Terminal Urbano',
    primeiraPartida: '05:10', ultimaPartida: '23:20', intervaloMin: 20, intervaloSabadoMin: 30, intervaloDomingoMin: 60,
    itinerario: ['P1001', 'P1043', 'P1007', 'P1012', 'P1015', 'P1021', 'P1029', 'P1035'],
    temposMin: [0, 6, 10, 17, 24, 30, 36, 43]
  },
  {
    id: '302', numero: '302', nome: 'Passo dos Fortes', cor: '#0B4DA2', circular: false, acessivel: true,
    sentidoIda: 'Terminal Urbano → P. dos Fortes', sentidoVolta: 'P. dos Fortes → Terminal Urbano',
    primeiraPartida: '05:00', ultimaPartida: '23:00', intervaloMin: 25, intervaloSabadoMin: 40, intervaloDomingoMin: 70,
    itinerario: ['P1001', 'P1002', 'P1005', 'P1050', 'P1053', 'P1057', 'P1060'],
    temposMin: [0, 4, 8, 15, 21, 27, 32]
  },
  {
    id: '118', numero: '118', nome: 'São Cristóvão', cor: '#0B4DA2', circular: false, acessivel: false,
    sentidoIda: 'Terminal Urbano → S. Cristóvão', sentidoVolta: 'S. Cristóvão → Terminal Urbano',
    primeiraPartida: '05:20', ultimaPartida: '22:40', intervaloMin: 30, intervaloSabadoMin: 45, intervaloDomingoMin: 90,
    itinerario: ['P1001', 'P1003', 'P1070', 'P1073', 'P1077', 'P1080'],
    temposMin: [0, 5, 12, 19, 25, 30]
  },
  {
    id: '407', numero: '407', nome: 'Seminário / UFFS', cor: '#0B4DA2', circular: false, acessivel: true,
    sentidoIda: 'Terminal Urbano → UFFS', sentidoVolta: 'UFFS → Terminal Urbano',
    primeiraPartida: '05:40', ultimaPartida: '23:40', intervaloMin: 30, intervaloSabadoMin: 50, intervaloDomingoMin: 0,
    itinerario: ['P1001', 'P1043', 'P1090', 'P1093', 'P1096', 'P1099'],
    temposMin: [0, 6, 13, 21, 29, 38]
  },
  {
    id: '523', numero: '523', nome: 'Palmital', cor: '#0B4DA2', circular: false, acessivel: false,
    sentidoIda: 'Terminal Urbano → Palmital', sentidoVolta: 'Palmital → Terminal Urbano',
    primeiraPartida: '05:30', ultimaPartida: '22:10', intervaloMin: 45, intervaloSabadoMin: 60, intervaloDomingoMin: 0,
    itinerario: ['P1001', 'P1007', 'P1110', 'P1113', 'P1116', 'P1120'],
    temposMin: [0, 5, 11, 19, 28, 35]
  },
  {
    id: '610', numero: '610', nome: 'Circular Centro', cor: '#0B4DA2', circular: true, acessivel: true,
    sentidoIda: 'Circular', sentidoVolta: 'Circular',
    primeiraPartida: '06:00', ultimaPartida: '20:00', intervaloMin: 15, intervaloSabadoMin: 25, intervaloDomingoMin: 0,
    itinerario: ['P1001', 'P1002', 'P1005', 'P1003', 'P1043', 'P1007', 'P1130', 'P1133'],
    temposMin: [0, 3, 6, 9, 13, 16, 19, 23]
  },
  {
    id: '812', numero: '812', nome: 'Bela Vista / Santa Maria', cor: '#0B4DA2', circular: false, acessivel: true,
    sentidoIda: 'Terminal Urbano → Bela Vista', sentidoVolta: 'Bela Vista → Terminal Urbano',
    primeiraPartida: '05:15', ultimaPartida: '22:50', intervaloMin: 35, intervaloSabadoMin: 55, intervaloDomingoMin: 90,
    itinerario: ['P1001', 'P1007', 'P1140', 'P1143', 'P1146', 'P1150'],
    temposMin: [0, 5, 14, 22, 29, 34]
  },
  {
    id: '905', numero: '905', nome: 'Universitário / Jd. Itália', cor: '#0B4DA2', circular: false, acessivel: true,
    sentidoIda: 'Terminal Urbano → Jd. Itália', sentidoVolta: 'Jd. Itália → Terminal Urbano',
    primeiraPartida: '05:45', ultimaPartida: '23:10', intervaloMin: 25, intervaloSabadoMin: 45, intervaloDomingoMin: 80,
    itinerario: ['P1001', 'P1002', 'P1160', 'P1163', 'P1166', 'P1170'],
    temposMin: [0, 4, 11, 18, 24, 29]
  }
];

const AVISOS = [
  { id: 'A01', linhaId: '302', severidade: 'ALTA', titulo: 'Desvio temporário no Centro', descricao: 'A linha 302 opera com desvio pela Rua Quintino Bocaiúva devido a obras na Av. Nereu Ramos. Previsão de normalização em 15 dias.', publicadoEm: '2026-08-28T09:00:00-03:00' },
  { id: 'A02', linhaId: '407', severidade: 'MEDIA', titulo: 'Reforço no período de aulas', descricao: 'Partidas extras da linha 407 às 18:10 e 22:50 em dias letivos da UFFS.', publicadoEm: '2026-08-30T14:30:00-03:00' },
  { id: 'A03', linhaId: null, severidade: 'BAIXA', titulo: 'Recarga do cartão pelo aplicativo', descricao: 'Recargas realizadas após as 22h são liberadas no primeiro embarque do dia seguinte.', publicadoEm: '2026-09-01T08:00:00-03:00' },
  { id: 'A04', linhaId: '523', severidade: 'MEDIA', titulo: 'Interrupção parcial aos domingos', descricao: 'A linha 523 não opera aos domingos e feriados. Utilize a linha 812 com transbordo no Terminal Urbano.', publicadoEm: '2026-08-20T10:00:00-03:00' }
];

const INFORMACOES = [
  { id: 'I01', categoria: 'TARIFA', titulo: 'Tarifa inteira', valor: 'R$ 5,25', descricao: 'Válida para pagamento em dinheiro ou cartão do usuário.', ordem: 1 },
  { id: 'I02', categoria: 'TARIFA', titulo: 'Tarifa estudante', valor: 'R$ 2,63', descricao: 'Meia tarifa mediante cartão estudantil ativo.', ordem: 2 },
  { id: 'I03', categoria: 'TARIFA', titulo: 'Integração temporal', valor: '60 min', descricao: 'Segunda viagem sem custo adicional dentro da janela de integração.', ordem: 3 },
  { id: 'I04', categoria: 'TARIFA', titulo: 'Gratuidades', valor: 'Isento', descricao: 'Pessoas com 65 anos ou mais e pessoas com deficiência cadastradas.', ordem: 4 },
  { id: 'I05', categoria: 'TERMINAL', titulo: 'Terminal Urbano', valor: 'Centro', descricao: 'Av. Getúlio Vargas, 100 — aberto das 05h às 00h.', ordem: 1 },
  { id: 'I06', categoria: 'TERMINAL', titulo: 'Terminal Efapi', valor: 'Efapi', descricao: 'Av. Atílio Fontana, 6000 — aberto das 05h às 23h30.', ordem: 2 },
  { id: 'I07', categoria: 'TERMINAL', titulo: 'Terminal Passo dos Fortes', valor: 'Passo dos Fortes', descricao: 'Av. Leopoldo Sander, 4800 — aberto das 05h às 23h.', ordem: 3 },
  { id: 'I08', categoria: 'CONTATO', titulo: 'Central de atendimento', valor: '0800 000 0000', descricao: 'Atendimento de segunda a sábado, das 07h às 19h.', ordem: 1 },
  { id: 'I09', categoria: 'CONTATO', titulo: 'Ouvidoria do transporte', valor: 'ouvidoria@exemplo.com', descricao: 'Reclamações, elogios e sugestões sobre a operação.', ordem: 2 },
  { id: 'I10', categoria: 'CONTATO', titulo: 'Achados e perdidos', valor: 'Terminal Urbano', descricao: 'Retirada no guichê 3, mediante documento com foto.', ordem: 3 }
];

const pontosPorId = new Map(PONTOS.map((p) => [p.id, p]));

function minutosParaHora(minutos) {
  const m = ((minutos % 1440) + 1440) % 1440;
  return `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;
}

function horaParaMinutos(hora) {
  const [h, m] = hora.split(':').map(Number);
  return h * 60 + m;
}

function intervaloDoDia(linha, diaTipo) {
  if (diaTipo === 'SABADO') return linha.intervaloSabadoMin;
  if (diaTipo === 'DOMINGO') return linha.intervaloDomingoMin;
  return linha.intervaloMin;
}

function partidas(linha, diaTipo) {
  const intervalo = intervaloDoDia(linha, diaTipo);
  if (!intervalo) return [];
  const inicio = horaParaMinutos(linha.primeiraPartida);
  const fim = horaParaMinutos(linha.ultimaPartida);
  const lista = [];
  for (let m = inicio; m <= fim; m += intervalo) lista.push(minutosParaHora(m));
  return lista;
}

function itinerarioDaLinha(linha, sentido) {
  const ids = sentido === 'VOLTA' && !linha.circular
    ? [...linha.itinerario].reverse()
    : linha.itinerario;
  const total = linha.temposMin[linha.temposMin.length - 1];
  const tempos = sentido === 'VOLTA' && !linha.circular
    ? [...linha.temposMin].reverse().map((t) => total - t)
    : linha.temposMin;

  return ids.map((pontoId, indice) => {
    const ponto = pontosPorId.get(pontoId);
    return {
      linhaId: linha.id,
      sentido,
      ordem: indice,
      pontoId,
      pontoNome: ponto.nome,
      bairro: ponto.bairro,
      latitude: ponto.latitude,
      longitude: ponto.longitude,
      terminal: ponto.terminal,
      tempoAcumuladoMin: tempos[indice]
    };
  });
}

function horariosDaLinha(linha, sentido) {
  const resultado = [];
  for (const diaTipo of ['UTIL', 'SABADO', 'DOMINGO']) {
    const base = partidas(linha, diaTipo);
    const deslocamento = sentido === 'VOLTA' && !linha.circular
      ? linha.temposMin[linha.temposMin.length - 1] + 5
      : 0;
    for (const hora of base) {
      resultado.push({
        linhaId: linha.id,
        sentido,
        diaTipo,
        hora: minutosParaHora(horaParaMinutos(hora) + deslocamento)
      });
    }
  }
  return resultado;
}

function sentidosDaLinha(linha) {
  return linha.circular ? ['IDA'] : ['IDA', 'VOLTA'];
}

function linhasQuePassamEm(pontoId) {
  const resultado = [];
  for (const linha of LINHAS) {
    for (const sentido of sentidosDaLinha(linha)) {
      const itinerario = itinerarioDaLinha(linha, sentido);
      const parada = itinerario.find((i) => i.pontoId === pontoId);
      if (parada) resultado.push({ linha, sentido, ordem: parada.ordem, tempoAcumuladoMin: parada.tempoAcumuladoMin });
    }
  }
  return resultado;
}

module.exports = {
  EMPRESA,
  PONTOS,
  LINHAS,
  AVISOS,
  INFORMACOES,
  pontosPorId,
  minutosParaHora,
  horaParaMinutos,
  intervaloDoDia,
  partidas,
  itinerarioDaLinha,
  horariosDaLinha,
  sentidosDaLinha,
  linhasQuePassamEm
};
