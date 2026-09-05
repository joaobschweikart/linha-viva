'use strict';

// Gera os arquivos de carga inicial embarcados no app (assets/seed).
// Garantem que o Linha Viva abra com dados uteis mesmo sem nunca ter acessado a API.

const fs = require('fs');
const path = require('path');
const ds = require('./src/dataset');

const destino = path.resolve(__dirname, '..', 'android', 'app', 'src', 'main', 'assets', 'seed');
fs.mkdirSync(destino, { recursive: true });

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

const itinerarios = [];
const horarios = [];
for (const linha of ds.LINHAS) {
  for (const sentido of ds.sentidosDaLinha(linha)) {
    itinerarios.push(...ds.itinerarioDaLinha(linha, sentido));
    horarios.push(...ds.horariosDaLinha(linha, sentido));
  }
}

const arquivos = {
  'linhas.json': ds.LINHAS.map(resumoLinha),
  'pontos.json': ds.PONTOS,
  'itinerarios.json': itinerarios,
  'horarios.json': horarios,
  'avisos.json': ds.AVISOS,
  'informacoes.json': ds.INFORMACOES
};

for (const [nome, conteudo] of Object.entries(arquivos)) {
  const caminho = path.join(destino, nome);
  fs.writeFileSync(caminho, JSON.stringify(conteudo), 'utf8');
  console.log(`${nome}: ${conteudo.length} registros (${(fs.statSync(caminho).size / 1024).toFixed(1)} KB)`);
}
console.log(`\nGravado em ${destino}`);
