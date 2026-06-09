# Sistema de Gerenciamento de Trilhas — Android

## Telas da Aplicação

O app possui 5 telas (activities):

- **MainActivity** — tela inicial com navegação
- **Config** — configurações do mapa
- **RegistrarTrilhaActivity** — gravação de trilha em tempo real
- **ConsultarTrilhasActivity** — lista de trilhas salvas
- **VisualizarTrilhaActivity** — visualização do trajeto no mapa

---

## 1. Configuração (Config)

Permite ao usuário definir:

**Tipo de mapa:**
- Vetorial (padrão)
- Satélite

**Forma de navegação:**
- North Up (padrão) — o mapa fica sempre orientado para o norte
- Course Up — o mapa gira acompanhando a direção do deslocamento do usuário

As configurações são salvas via `Intent` e aplicadas nas telas de mapa.

---

## 2. Registrar Trilha (RegistrarTrilhaActivity)

Exibe o mapa com a posição atual do usuário. Sobre o mapa são mostradas:

**Informações em tempo real:**
- Velocidade instantânea (km/h)
- Velocidade máxima desenvolvida (km/h)
- Cronômetro com o tempo decorrido desde o início
- Distância total percorrida (metros ou km)
- Indicador de status (GRAVANDO / PARADO)

**Elementos visuais no mapa:**
- Marcador personalizado na posição do usuário (ícone próprio, diferente do padrão Google)
- Círculo semitransparente em volta do marcador indicando a acurácia do GPS
- Polilinha vermelha desenhando o percurso em tempo real

**Dados salvos no banco SQLite ao finalizar:**
- Nome da trilha (gerado automaticamente com data/hora)
- Data/hora de início e fim
- Velocidade média e máxima
- Distância total em km
- Duração no formato HH:MM:SS
- Todos os pontos GPS (latitude e longitude) ao longo do percurso

**Botões:**
- ← Voltar (bloqueado durante gravação)
- ⚙ Config (abre as configurações)
- INICIAR TRILHA
- FINALIZAR

---

## 3. Consultar Trilhas (ConsultarTrilhasActivity)

Lista todas as trilhas registradas mostrando nome, data de início e distância.

**Opções por trilha:**
- **Ver mapa** — abre o VisualizarTrilhaActivity com o trajeto completo
- **Renomear** — dialog para alterar o nome da trilha
- **Apagar** — remove a trilha específica (com confirmação)

**Opções globais:**
- **Apagar por intervalo** — remove trilhas entre duas datas no formato dd/MM/yyyy
- **Apagar todas** — remove todas as trilhas (com confirmação)

---

## 4. Visualizar Trilha (VisualizarTrilhaActivity)

Exibe no mapa o trajeto completo da trilha selecionada.

**No mapa:**
- Polilinha vermelha com o percurso completo
- Marcador verde no ponto de início
- Marcador vermelho no ponto de fim
- Câmera ajustada automaticamente para enquadrar toda a trilha

**Overlay com as estatísticas da trilha:**
- Data/hora de início e fim
- Velocidade média (km/h)
- Velocidade máxima (km/h)
- Distância total (km)
- Duração total

---

## Banco de Dados SQLite (Banco.java)

Duas tabelas conforme exigido:

**Tabela `trilhas`**

| Campo | Tipo | Descrição |
|---|---|---|
| id | INTEGER | Chave primária, auto incremento |
| nome | TEXT | Nome da trilha |
| data_inicio | TEXT | dd/MM/yyyy HH:mm:ss |
| data_fim | TEXT | dd/MM/yyyy HH:mm:ss |
| vel_media | REAL | km/h |
| vel_max | REAL | km/h |
| distancia | REAL | km |
| duracao | TEXT | HH:MM:SS |

**Tabela `pontos`**

| Campo | Tipo | Descrição |
|---|---|---|
| id | INTEGER | Chave primária, auto incremento |
| trilha_id | INTEGER | Referência à trilha (FK) |
| latitude | REAL | Graus decimais |
| longitude | REAL | Graus decimais |

---

## Tecnologias Utilizadas

- **Linguagem:** Java
- **SDK:** Android
- **Mapas:** Google Maps SDK para Android
- **Localização:** FusedLocationProviderClient (Google Play Services)
- **Banco de dados:** SQLite via SQLiteOpenHelper
- **Permissão necessária:** ACCESS_FINE_LOCATION

---

## Como Testar no Emulador

1. Abrir o emulador e acessar **Extended Controls → Location**
2. Selecionar a aba **Routes**
3. Traçar um percurso no mapa clicando nos pontos
4. Definir a velocidade de deslocamento
5. Clicar em **Play Route**
6. No app, acessar **Registrar Trilha** e clicar em **INICIAR TRILHA**
7. Ao terminar, clicar em **FINALIZAR**
8. Acessar **Consultar Trilhas** para ver a trilha salva e visualizar o trajeto
