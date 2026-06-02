# Trilhas App — Guia Completo de Integração

## Visão Geral

Aplicativo Android de rastreamento de trilhas com GPS, mapa ao vivo, banco SQLite e visualização de percursos.

---

## Arquivos e o que fazer com cada um

### Java — pasta: app/src/main/java/com/example/atividade3/

| Arquivo | O que fazer |
|---|---|
| MainActivity.java | Substituir |
| Banco.java | Substituir |
| RegistrarTrilhaActivity.java | Adicionar (novo) |
| ConsultarTrilhasActivity.java | Adicionar (novo) |
| VisualizarTrilhaActivity.java | Adicionar (novo) |
| MapsActivity.java | Manter o original |
| Config.java | Manter o original |

### Layout — pasta: app/src/main/res/layout/

| Arquivo | O que fazer |
|---|---|
| activity_registrar_trilha.xml | Adicionar (novo) |
| activity_consultar_trilhas.xml | Adicionar (novo) |
| activity_visualizar_trilha.xml | Adicionar (novo) |
| item_trilha.xml | Adicionar (novo) |
| dialog_intervalo.xml | Adicionar (novo) |
| activity_main.xml | Editar — adicionar 2 botões (ver abaixo) |

---

## Passo a Passo

### 1. AndroidManifest.xml

Dentro da tag <application>, adicione:

    <activity android:name=".RegistrarTrilhaActivity"/>
    <activity android:name=".ConsultarTrilhasActivity"/>
    <activity android:name=".VisualizarTrilhaActivity"/>

### 2. activity_main.xml — adicionar 2 botões

    <Button
        android:id="@+id/button_registrar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Registrar Trilha"
        android:backgroundTint="#1B5E20"
        android:textColor="#FFFFFF"/>

    <Button
        android:id="@+id/button_consultar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Consultar Trilhas"
        android:backgroundTint="#0D47A1"
        android:textColor="#FFFFFF"/>

### 3. Ícone do marcador — res/drawable/ic_usuario_marker.xml

    <?xml version="1.0" encoding="utf-8"?>
    <vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="48dp"
        android:height="48dp"
        android:viewportWidth="48"
        android:viewportHeight="48">
        <path android:fillColor="#FFFFFF"
            android:pathData="M24,4 C13,4 4,13 4,24 C4,35 13,44 24,44 C35,44 44,35 44,24 C44,13 35,4 24,4Z"/>
        <path android:fillColor="#1565C0"
            android:pathData="M24,8 C15,8 8,15 8,24 C8,33 15,40 24,40 C33,40 40,33 40,24 C40,15 33,8 24,8Z"/>
        <path android:fillColor="#FFFFFF"
            android:pathData="M24,13 L32,33 L24,28 L16,33 Z"/>
    </vector>

---

## Funcionalidades

### Registrar Trilha (RegistrarTrilhaActivity)

Mapa ao vivo com:
- Marcador personalizado (circulo azul com seta) na posicao do usuario
- Circulo semitransparente azul indicando a acuracia do GPS
- Polilinha vermelha desenhando o percurso em tempo real
- Camera seguindo o usuario automaticamente (zoom 17)

Overlay superior mostrando:
- Botao Voltar (bloqueado durante gravacao)
- Cronometro HH:MM:SS
- Indicador GRAVANDO / PARADO
- Velocidade instantanea em km/h
- Velocidade maxima em km/h
- Distancia total em metros ou km

Botoes inferiores:
- MODO SIMULACAO — ativa/desativa o modo de teste
- INICIAR TRILHA — comeca a gravacao
- FINALIZAR — encerra e salva

Modo Simulacao:
- Ao abrir a tela busca a posicao real do GPS silenciosamente
- Ao iniciar gera uma rota em forma de "U" a partir da sua posicao atual
- Se o GPS nao retornar nada usa a UCSAL como ponto base
- 41 pontos, um a cada 2 segundos (~80 segundos de trilha)
- Percurso: vai ~110m para leste, desce ~110m, volta para oeste, sobe ate a origem
- Velocidade calculada pela distancia real entre pontos consecutivos
- Finaliza e salva sozinho ao completar o percurso

Dados salvos no banco:
- Nome automatico com data/hora de inicio
- Data/hora de inicio e fim
- Velocidade media e maxima
- Distancia total em km
- Duracao HH:MM:SS
- Todos os pontos GPS na tabela pontos

### Consultar Trilhas (ConsultarTrilhasActivity)

Lista todas as trilhas mostrando nome, data de inicio e distancia.
Cada item tem 3 botoes:
- Ver mapa — abre o VisualizarTrilhaActivity
- Renomear — dialog para editar o nome
- Apagar — dialog de confirmacao antes de deletar

Atualiza a lista ao voltar de outra tela (onResume).
Mostra mensagem quando a lista esta vazia.
Botao Voltar no cabecalho.

### Visualizar Trilha (VisualizarTrilhaActivity)

Mapa com:
- Polilinha vermelha do percurso completo
- Marcador verde no inicio
- Marcador vermelho no fim
- Camera ajustada para enquadrar toda a trilha

Overlay com:
- Nome da trilha
- Data/hora de inicio e fim
- Duracao total
- Velocidade media e maxima
- Distancia total

---

## Banco de Dados (DatabaseHelper)

Tabela trilhas:
  id           INTEGER PK autoincrement
  nome         TEXT
  data_inicio  TEXT  (dd/MM/yyyy HH:mm:ss)
  data_fim     TEXT  (dd/MM/yyyy HH:mm:ss)
  vel_media    REAL  (km/h)
  vel_max      REAL  (km/h)
  distancia    REAL  (km)
  duracao      TEXT  (HH:MM:SS)

Tabela pontos:
  id           INTEGER PK autoincrement
  trilha_id    INTEGER (FK para trilhas.id)
  latitude     REAL
  longitude    REAL

Metodos disponiveis:
  inserirTrilha(...)              — cria registro inicial
  finalizarTrilha(id, ...)        — atualiza stats ao finalizar
  listarTrilhas()                 — Cursor com todas (mais recente primeiro)
  deletarTrilha(id)               — apaga trilha e seus pontos
  deletarTodasTrilhas()           — apaga tudo
  deletarTrilhasPorIntervalo(...) — apaga por intervalo dd/MM/yyyy
  editarNomeTrilha(id, nome)      — renomeia
  inserirPonto(trilhaId, lat, lng)— salva ponto GPS
  listarPontosTrilha(trilhaId)    — retorna List<LatLng> do trajeto

---

## Fluxo de Navegacao

MainActivity
 ├── Registrar Trilha  →  RegistrarTrilhaActivity
 │                            └── salva no SQLite ao finalizar
 ├── Consultar Trilhas →  ConsultarTrilhasActivity
 │                            ├── Ver mapa  →  VisualizarTrilhaActivity
 │                            ├── Renomear  →  AlertDialog
 │                            └── Apagar    →  AlertDialog
 ├── [antigo] GPS      →  LocationActivity
 └── [antigo] Mapa     →  MapsActivity → Config

---

## Erros conhecidos e correcoes

ERRO: No candidates found for method call mMap.post(...)
CAUSA: GoogleMap nao e uma View e nao tem metodo post().
CORRECAO em VisualizarTrilhaActivity:

  // Errado:
  mMap.post(() -> mMap.animateCamera(...));

  // Correto:
  mMap.setOnMapLoadedCallback(() ->
      mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
  );

---

## Observacoes

- dialog_intervalo.xml e o layout interno do AlertDialog de apagar por intervalo.
  Ele nao tem Activity propria, e inflado via setView() dentro do AlertDialog.Builder.

- Os metodos deletarTodasTrilhas() e deletarTrilhasPorIntervalo() existem no
  DatabaseHelper mas nao estao expostos na UI do Consultar Trilhas (removidos a pedido).
  Podem ser reativados futuramente se necessario.

- O marcador personalizado usa bitmapFromVector() que converte o XML drawable em
  Bitmap, compativel com qualquer versao do Android suportada pelo projeto.
