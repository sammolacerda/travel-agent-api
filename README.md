# Travel Agent API

API responsável por receber solicitações de viagem, utilizar um modelo de linguagem local através do Ollama e acessar ferramentas externas disponibilizadas pelo `travel-mcp-server` através do protocolo MCP.

## Stack

* Java 25
* Spring Boot
* LangChain4j
* Ollama
* `gpt-oss:20b`
* Model Context Protocol (MCP)

## Visão geral

O `travel-agent-api` funciona como o componente responsável pelo raciocínio e pela tomada de decisão.

O agente recebe uma solicitação em linguagem natural, analisa o que precisa ser feito e decide se alguma ferramenta externa precisa ser utilizada.

As ferramentas de viagem não estão implementadas diretamente nesta aplicação. Elas são disponibilizadas pelo projeto `travel-mcp-server`.

```text
Usuário
   |
   v
POST /api/travel
   |
   v
TravelController
   |
   v
TravelAgent
   |
   v
gpt-oss:20b / Ollama
   |
   | decide utilizar uma ferramenta
   v
McpToolProvider
   |
   v
MCP Client
   |
   | MCP / stdio
   v
travel-mcp-server
   |
   v
TravelTools
   |
   +-- searchFlights(...)
   |
   +-- searchHotels(...)
```

## Endpoint

```http
POST /api/travel
```

Exemplo:

```json
{
  "message": "Quero viajar de Salvador para São Paulo. Qual é o voo mais barato?"
}
```

Exemplo de resposta:

```json
{
  "response": "O voo mais barato de Salvador para São Paulo é com a GOL, custando R$ 480,00."
}
```

## Fluxo do agente

### 1. O usuário envia uma solicitação

O fluxo começa com uma requisição HTTP:

```text
POST /api/travel
```

O `TravelController` recebe a mensagem e encaminha o conteúdo para:

```java
TravelAgent.chat(...)
```

### 2. O LangChain4j envia a solicitação para o Ollama

O `TravelAgent` é criado através do `AiServices` do LangChain4j.

O modelo utilizado atualmente é:

```text
gpt-oss:20b
```

executado localmente através do Ollama.

Além da mensagem do usuário, o modelo recebe a descrição das ferramentas disponíveis.

Atualmente:

```text
searchFlights
searchHotels
```

### 3. O modelo decide se precisa utilizar uma ferramenta

Para a pergunta:

```text
Quero viajar de Salvador para São Paulo.
Qual é o voo mais barato?
```

o modelo identifica que não possui os preços necessários para responder.

Ele então decide utilizar:

```text
searchFlights
```

com os argumentos:

```text
origin      = Salvador
destination = São Paulo
```

Essa decisão é realizada pelo modelo.

Não existe código como:

```java
if (message.contains("voo")) {
    searchFlights();
}
```

O modelo escolhe a ferramenta com base na solicitação do usuário e nas descrições das tools disponíveis.

## 4. LangChain4j executa a Tool Call

Quando o modelo retorna uma solicitação de tool:

```text
searchFlights(
    origin = "Salvador",
    destination = "São Paulo"
)
```

o LangChain4j utiliza o:

```text
McpToolProvider
```

para localizar a ferramenta.

O `McpToolProvider` utiliza o `McpClient` configurado para conversar com o:

```text
travel-mcp-server
```

## 5. Comunicação via MCP

O `travel-agent-api` inicia o `travel-mcp-server` como um subprocesso local.

A comunicação ocorre através de:

```text
stdin
stdout
```

utilizando MCP/JSON-RPC.

O fluxo é:

```text
travel-agent-api
        |
        | tools/call
        v
travel-mcp-server
        |
        v
TravelTools.searchFlights(...)
```

## 6. O MCP Server executa a ferramenta

Nesta primeira versão, `searchFlights()` retorna dados fixos:

```text
GOL    R$ 480
LATAM  R$ 530
AZUL   R$ 610
```

Esses dados são retornados através do MCP para o `travel-agent-api`.

## 7. O resultado volta para o modelo

Depois da execução da ferramenta, o LangChain4j envia novamente ao modelo:

* mensagem original do usuário;
* tool call realizada;
* resultado retornado pelo MCP.

Conceitualmente:

```text
User:
Qual é o voo mais barato?

Assistant:
Preciso utilizar searchFlights.

Tool:
GOL    480
LATAM  530
AZUL   610
```

O modelo agora possui informação suficiente para produzir a resposta final.

## 8. Segunda inferência do modelo

O `gpt-oss:20b` analisa o resultado da ferramenta e determina que:

```text
480 < 530 < 610
```

Portanto, a opção mais barata é a GOL.

O modelo gera então a resposta final:

```text
O voo mais barato de Salvador para São Paulo
é com a GOL, custando R$ 480,00.
```

## Fluxo completo

```text
User Request
     |
     v
TravelController
     |
     v
TravelAgent
     |
     v
LLM - gpt-oss:20b
     |
     | reasoning
     |
     | "preciso consultar voos"
     v
Tool Call
searchFlights(...)
     |
     v
McpToolProvider
     |
     v
McpClient
     |
     | MCP
     v
travel-mcp-server
     |
     v
TravelTools.searchFlights()
     |
     v
Flight results
     |
     | MCP
     v
TravelAgent
     |
     v
LLM - gpt-oss:20b
     |
     | analisa os resultados
     v
Final Answer
```

## Loop agentic

O ponto principal deste projeto é observar um fluxo agentic simples.

```text
Request
   |
   v
Reasoning
   |
   v
Tool Selection
   |
   v
Tool Execution
   |
   v
Observation
   |
   v
Reasoning
   |
   v
Final Answer
```

O modelo não apenas responde à solicitação inicial.

Ele pode identificar que não possui informação suficiente, utilizar uma ferramenta para obter novos dados e continuar o processamento com base no resultado recebido.

## Tempo de execução

Uma solicitação que utiliza ferramentas normalmente envolve pelo menos duas inferências do modelo:

```text
LLM #1
 |
 | decide utilizar uma tool
 v
MCP / Tool
 |
 v
LLM #2
 |
 v
Resposta final
```

Com o `gpt-oss:20b` executado localmente, o tempo total pode ser maior do que uma chamada convencional a uma API REST.

No teste inicial, o fluxo completo levou aproximadamente:

```text
~46 segundos
```

A maior parte desse tempo foi utilizada pelas inferências do modelo, e não pela comunicação MCP.

## Responsabilidades

### travel-agent-api

Responsável por:

```text
receber a solicitação
        +
interpretar a intenção
        +
decidir quais tools utilizar
        +
executar tools via MCP
        +
interpretar os resultados
        +
gerar a resposta final
```

### travel-mcp-server

Responsável por:

```text
publicar ferramentas
        +
executar ferramentas
        +
retornar resultados
```

O MCP Server não decide qual ferramenta deve ser utilizada.

Essa decisão pertence ao agente.
