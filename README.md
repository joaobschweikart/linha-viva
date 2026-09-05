# Linha Viva

Aplicativo Android para acompanhamento do transporte coletivo urbano de **Chapecó — SC**.

Trabalho acadêmico da disciplina **Desenvolvimento Mobile** — Universidade do Oeste de Santa
Catarina (Unoesc), Curso de Análise e Desenvolvimento de Sistemas.
Implementação da proposta definida na Atividade 1.

> **Dados fictícios.** Linhas, itinerários, horários, pontos, avisos e previsões deste projeto
> são fictícios e existem apenas para demonstrar o funcionamento do aplicativo. Não representam
> a operação real da Auto Viação Chapecó, e o projeto não possui vínculo com a empresa.

---

## Objetivo

Reduzir a principal incerteza de quem usa transporte coletivo: **não saber quando o ônibus vai
passar**. Em poucos toques o aplicativo responde a três perguntas — *qual linha eu pego*,
*onde ela passa* e *quanto tempo falta*.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Plataforma | Android nativo, `minSdk 26` (Android 8.0), `targetSdk 34` |
| Build | Gradle 8.2 · Android Gradle Plugin 8.2.2 |
| Arquitetura | MVVM (View · ViewModel · Repository · DataSource) |
| Persistência local | Room 2.6.1 |
| Rede | Retrofit 2.9 + Gson + OkHttp |
| Assincronismo | `ExecutorService` + `LiveData` (nunca na Main Thread) |
| Mapa | osmdroid 6.1.18 (OpenStreetMap, sem chave de API) |
| Localização | `LocationManager` da plataforma |
| Interface | Material Components 3, ViewBinding |
| QR Code | ZXing (`zxing-android-embedded`) |
| API de demonstração | Node.js (sem dependências externas) |

---

## Arquitetura

```
┌──────────────────────── UI (Activities / Fragments) ────────────────────────┐
│  Splash · Mapa · Linhas · Detalhe da linha · Tempo real · Ponto · Favoritos │
│                              · Mais · Lista de informações                  │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │ observa LiveData, sem regra de negócio
┌───────────────────────────────────▼─────────────────────────────────────────┐
│                                ViewModels                                    │
│   filtros, busca, estados de carregamento/erro/offline, cálculo de previsão  │
└───────────────────────────────────┬─────────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼─────────────────────────────────────────┐
│                              Repositories                                    │
│  RepositorioLinhas · RepositorioPontos · RepositorioFavoritos                │
│  RepositorioConteudo · Sincronizador · CargaInicial                          │
│           (o banco local é a única fonte de verdade da interface)            │
└──────────────┬──────────────────────────────────────────┬───────────────────┘
               │                                          │
┌──────────────▼──────────────┐            ┌──────────────▼───────────────────┐
│  Room  (banco local)        │            │  Retrofit  (API REST)            │
│  linha, ponto, itinerario,  │            │  ApiLinhaViva + DTOs             │
│  horario, previsao,         │            │  ClienteApi (URL configurável)   │
│  favorito, aviso, informacao│            └──────────────────────────────────┘
└──────────────┬──────────────┘
               │ primeira execução
┌──────────────▼──────────────┐
│  assets/seed/*.json         │  carga inicial embarcada (abre útil sem rede)
└─────────────────────────────┘
```

**Fluxo de dados:** a interface **sempre** lê do Room. A rede apenas alimenta o Room. Se a
requisição falha, os dados anteriores permanecem e a tela informa o estado offline em vez de
mostrar erro. É a estratégia *offline-first* prevista na Atividade 1 (RNF04, RNF05, RNF12).

### Estrutura de pastas

```
project/
├── android/                     projeto do Android Studio
│   └── app/src/
│       ├── main/
│       │   ├── java/br/unoesc/linhaviva/
│       │   │   ├── data/
│       │   │   │   ├── local/       Room: BancoLocal, entities, daos
│       │   │   │   ├── remote/      Retrofit: ApiLinhaViva, ClienteApi, dtos
│       │   │   │   ├── repository/  repositórios, Sincronizador, CargaInicial
│       │   │   │   └── Mapeador     conversão DTO → entidade
│       │   │   ├── ui/              uma pasta por tela (+ common)
│       │   │   ├── util/            executors, formatação, GPS, rede, prefs
│       │   │   └── LinhaVivaApp
│       │   ├── assets/seed/         dados embarcados da carga inicial
│       │   └── res/                 layouts, temas, ícones, strings
│       ├── debug/res/xml/           configuração de rede do build de depuração
│       └── test/                    testes de contrato da API e de formatação
└── api/                         API REST de demonstração (Node.js)
    ├── server.js                roteamento HTTP
    ├── gerar-seed.js            gera os assets embarcados no app
    └── src/
        ├── dataset.js           linhas, pontos, itinerários, horários, avisos
        ├── previsoes.js         simulação de previsões e posição dos veículos
        └── geo.js               distância e interpolação geográfica
```

---

## Como executar

### Pré-requisitos

- **Android Studio** Hedgehog (2023.1.1) ou superior
- **JDK 17** (o Android Studio já traz o seu)
- **Node.js 16+** para a API de demonstração
- Um emulador Android **API 26 ou superior**, ou um aparelho físico

### 1. Subir a API

```bash
cd api
node server.js
```

Saída esperada:

```
Linha Viva — API de demonstração
  http://localhost:3000/api/v1
  Emulador Android: http://10.0.2.2:3000/api/v1/
```

A API **não tem dependências externas** — não é preciso rodar `npm install`.
Para usar outra porta: `PORT=8080 node server.js`.

Confira no navegador: <http://localhost:3000/api/v1/health>

### 2. Abrir o projeto no Android Studio

1. **File → Open**
2. Selecione a pasta **`android/`** (não a raiz do repositório)
3. Aguarde o *Gradle sync*
4. **Run ▶** no módulo `app`

O arquivo `local.properties` é gerado automaticamente pelo Android Studio com o caminho do
seu SDK. Também é possível compilar pela linha de comando:

```bash
cd android
./gradlew assembleDebug        # gera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # instala no dispositivo conectado
./gradlew testDebugUnitTest    # roda os testes
```

## Funcionalidades implementadas

### Telas (seguindo o protótipo da Atividade 1)

1. **Splash** — carrega o banco local e tenta sincronizar; nunca bloqueia a entrada sem rede.
2. **Mapa** — posição do usuário por GPS, pontos próximos, traçado das linhas próximas, busca,
   filtros (Linhas próximas · Pontos · Terminais) e painel com o ponto mais próximo e suas
   próximas partidas.
3. **Linhas** — listagem com busca por número, nome, sentido, ponto ou bairro; filtros
   Todas · Favoritas · Próximas · Acessíveis; previsão por linha.
4. **Detalhe da linha** — seletor de sentido e abas Itinerário (linha do tempo com a posição do
   veículo) · Horários (por dia útil, sábado e domingo) · Mapa.
5. **Tempo real** — veículo no mapa, tempo estimado até o ponto, lotação, prefixo,
   acessibilidade e aviso de aproximação.
6. **Ponto de parada** — localização, distância a pé, infraestrutura, acessibilidade, próximas
   partidas de todas as linhas e leitura de QR Code.
7. **Favoritos e informações** — linhas favoritas, pontos salvos, avisos de operação e atalhos
   para tarifas, terminais e contatos.
8. **Mais** — informações úteis, configuração do servidor, estado da sincronização e da conexão.

### Requisitos funcionais da Atividade 1

| ID | Requisito | Situação |
|---|---|---|
| RF01 | Consulta sem cadastro ou login | ✅ |
| RF02 | Mapa com posição do usuário e pontos próximos | ✅ |
| RF03 | Listar todas as linhas com número, nome e sentido | ✅ |
| RF04 | Buscar linhas e pontos por número, nome, bairro ou endereço | ✅ |
| RF05 | Itinerário completo com sequência de pontos e horários | ✅ |
| RF06 | Previsão de chegada dos próximos veículos em um ponto | ✅ simulada |
| RF07 | Acompanhar a posição do veículo no mapa em tempo real | ✅ simulada |
| RF08 | Salvar linhas e pontos como favoritos | ✅ |
| RF09 | Notificar quando o veículo estiver a N paradas | ✅ com a tela aberta |
| RF10 | Itinerários, horários e favoritos disponíveis sem conexão | ✅ |
| RF11 | Avisos de operação | ✅ |
| RF12 | Tarifas, cartão, terminais e canais de contato | ✅ |
| RF13 | Abrir o ponto pela leitura do QR Code da placa | ✅ + entrada manual |
| RF14 | Acessibilidade do veículo e do ponto, e nível de lotação | ✅ |

### Requisitos não funcionais

| ID | Requisito | Como foi atendido |
|---|---|---|
| RNF01 | Abertura em até 3 s | Splash com duração mínima de 1,2 s; a sincronização não bloqueia |
| RNF02 | GPS só em uso | `LocalizacaoLiveData` libera o GPS ao perder o último observador |
| RNF03 | Limitar atualizações em segundo plano | Consulta de veículo a cada 15 s **apenas** com a tela visível |
| RNF04 | Armazenar dados localmente e sincronizar diferenças | Room + endpoint `/versao`: o catálogo só é baixado quando muda |
| RNF05 | Modo offline informando a data da última atualização | Faixa âmbar em todas as telas com o horário da última sincronização |
| RNF06 | Volumes reduzidos de dados | Endpoints em lote (`/itinerarios`, `/horarios`), cache de tiles do mapa |
| RNF07 | Layout adaptável de telas pequenas a tablets | `values-sw600dp`, unidades relativas, `ConstraintLayout`/`LinearLayout` |
| RNF08 | Android 8.0 (API 26) ou superior | `minSdk 26` |
| RNF09 | Ações principais na metade inferior da tela | Navegação inferior fixa, painel inferior no mapa, botão fixo no rodapé |
| RNF10 | Leitores de tela, fontes ampliadas e contraste | `contentDescription` em todos os ícones, alvos de 48 dp, `sp` nos textos, tema claro/escuro |
| RNF11 | Localização não vinculada a identificação; permissão em uso | Sem login, sem identificador de usuário; permissões pedidas no uso |
| RNF12 | Degradar sem tempo real, sem exibir erro | Sem dado ao vivo a tela cai para o horário programado |

---

## A API REST de demonstração

Como a Atividade 1 registra, **não há garantia de API pública de rastreamento da frota de
Chapecó**. Para tornar o projeto executável foi criada uma API própria, com dados fictícios,
que reproduz a arquitetura cliente-servidor de um caso real.

Base: `http://localhost:3000/api/v1`

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/health` | Estado do serviço |
| GET | `/versao` | Assinatura do catálogo — permite sincronização incremental |
| GET | `/linhas` | Todas as linhas (aceita `?q=`) |
| GET | `/linhas/previsoes` | Próxima partida de cada linha |
| GET | `/linhas/{id}` | Uma linha |
| GET | `/linhas/{id}/itinerario?sentido=IDA\|VOLTA` | Sequência de pontos |
| GET | `/linhas/{id}/horarios?sentido=IDA\|VOLTA` | Horários programados |
| GET | `/linhas/{id}/veiculo?sentido=IDA\|VOLTA` | Posição simulada do veículo |
| GET | `/itinerarios` | Todos os itinerários (carga em lote) |
| GET | `/horarios` | Todos os horários (carga em lote) |
| GET | `/veiculos` | Todos os veículos em circulação |
| GET | `/pontos` | Pontos (aceita `?lat=&lon=&raio=&limite=` e `?q=`) |
| GET | `/pontos/{id}` | Um ponto e as linhas que o atendem |
| GET | `/pontos/{id}/previsoes` | Previsões de chegada no ponto |
| GET | `/avisos` | Avisos de operação |
| GET | `/informacoes` | Tarifas, terminais e contatos |

Toda resposta de coleção vem em um envelope que declara a origem dos dados:

```json
{
  "atualizadoEm": "2026-09-04T16:19:03.307Z",
  "fonte": "Dados fictícios para fins acadêmicos. Não representam a operação real da Auto Viação Chapecó.",
  "dados": [ ... ]
}
```

### Como o tempo real é simulado

A API calcula, a partir da tabela de horários programados e do relógio do servidor, quais
viagens estariam em curso e interpola a posição do veículo ao longo do itinerário, aplicando um
desvio determinístico por viagem. Cada resposta carrega `origem: "SIMULADO"` e uma observação
explícita, e o aplicativo repete esse aviso na interface. **Nenhuma tela apresenta dado
simulado como se fosse real.**

## Testes e validação

```bash
cd android
./gradlew testDebugUnitTest    # 15 testes
./gradlew lintDebug            # análise estática
```

O projeto foi compilado e **executado em emulador Android API 34**: as oito telas foram
percorridas sem falhas, a integração REST foi confirmada por log HTTP e o modo offline foi
testado com a API derrubada e a rede desligada.

| Verificação | Resultado |
|---|---|
| `assembleDebug` | APK de 7,0 MB |
| `testDebugUnitTest` | 15 testes, 0 falhas |
| `lintDebug` | 0 erros, 0 problemas fatais |
| Execução no emulador | 8 telas, nenhum crash |
| Modo offline | dados preservados, faixa âmbar com a data da última atualização |

- `ContratoApiTest` — verifica que os DTOs continuam compatíveis com as respostas reais da API,
  usando *fixtures* capturadas do servidor (`app/src/test/resources/fixtures/`).
- `FormatadorTest` — conversão de horários, distâncias e tempo relativo.

Para regenerar as *fixtures* depois de alterar a API, suba o servidor e capture as respostas dos
endpoints em `app/src/test/resources/fixtures/`.
