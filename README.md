# Checkpoint 5 — Bug Hunt PetFiap

## Identificacao

**Grupo e integrantes:** pendentes de preenchimento pelo grupo.

| Integrante | RM | Turma |
|---|---|---|
| A preencher | A preencher | A preencher |

| Campo | Resultado |
|---|---|
| Total de bugs corrigidos | 12 / 12 |
| Total de ajustes de Clean Code | 6 / 6 |
| Total de testes novos escritos | 6 / 6 |
| Suite original | 20 testes, 9 falhas no estado recebido; 0 falhas apos as correcoes |
| Suite final | 26 testes, 0 falhas, 0 erros, 0 ignorados |

## Como executar

Requisito: JDK 17 ou superior e Maven. Execute `mvn test` na raiz.
No Eclipse, importe como **Maven > Existing Maven Projects** e rode `src/test/java` como JUnit Test.
Os testes usam JUnit e Mockito, sem Spring, rede ou banco de dados.
Para iniciar a API com Oracle, configure as credenciais apenas localmente.
O arquivo versionado preserva `SEU_RM` e `SUA_SENHA`.
Para experimentar com H2 sem alterar o arquivo, use:

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.datasource.url=jdbc:h2:mem:petfiap --spring.datasource.driver-class-name=org.h2.Driver --spring.datasource.username=sa --spring.datasource.password= --spring.jpa.hibernate.ddl-auto=create-drop"
```

## Parte 1 — Bugs encontrados

Os caminhos abaixo sao relativos a `src/main/java/br/com/fiap/petfiap`.
As linhas apontam para o codigo final; a causa original pode ser conferida no primeiro commit.

| # | Sintoma observado | Causa raiz (arquivo e linha aproximada) | Correcao aplicada | Conceito da disciplina |
|---|---|---|---|---|
| bug01 | O Builder devolvia nome do pet nulo, apesar de comPet receber o nome. | `builder/AtendimentoBuilder.java:24`: O parametro era atribuido a si mesmo (petNome = petNome). | Usar this.petNome para preencher o campo. | Encapsulamento, this e sombreamento de variaveis |
| bug02 | construir aceitava nome ou porte ausentes. | `builder/AtendimentoBuilder.java:41`: Nao existia validacao dos campos obrigatorios no Builder. | Recusar null, vazio e apenas espacos no nome ou porte antes da Factory. | Builder e invariantes do objeto |
| bug03 | Pedir TOSA criava um Banho. | `factory/AtendimentoFactory.java:17`: O case TOSA instanciava a subclasse errada. | Instanciar Tosa nesse case. | Factory, heranca e polimorfismo |
| bug04 | Consulta criada perdia pet, porte, tutor, protocolo, data e status. | `model/ConsultaVeterinaria.java:17`: O construtor chamava super() e descartava os argumentos. | Chamar o construtor da superclasse com todos os dados. | Construtores e heranca |
| bug05 | getInstancia devolvia objetos diferentes e a sequencia reiniciava em 1. | `model/GeradorProtocolo.java:22`: A nova instancia nao era guardada; o acesso e incremento tambem nao eram protegidos. | Guardar a instancia e sincronizar criacao e incremento. | Singleton, estado compartilhado e concorrencia |
| bug06 | Mesmo pet e horario em objetos distintos passavam pela verificacao. | `service/AgendaService.java:33`: == comparava referencias de String e LocalDateTime, nao os valores. | Comparar os dois valores com equals, mantendo a condicao AGENDADO. | Identidade versus igualdade |
| bug07 | Busca de ID inexistente retornava null e escondia a excecao. | `service/AgendaService.java:46`: catch (Exception) capturava inclusive a excecao do orElseThrow. | Remover a captura generica e propagar AtendimentoNaoEncontradoException. | Excecoes unchecked e Optional |
| bug08 | Novo atendimento nao tinha ID nem estrategia de geracao para o JPA. | `model/Atendimento.java:15`: @Id sozinho exige que a aplicacao atribua o identificador, mas isso nao acontecia. | Configurar @GeneratedValue(strategy = GenerationType.AUTO). | JPA e identidade persistente |
| bug09 | Banho pequeno custava 100 e grande custava 60. | `model/Banho.java:26`: Os valores dos portes pequeno e grande estavam invertidos. | Restaurar pequeno 60, medio 80 e grande 100. | Regra de negocio no model |
| bug10 | Tosa durava 30 minutos quando chamada como Atendimento. | `model/Tosa.java:41`: getDuracaoMinutos(String) sobrecarregava o metodo, em vez de sobrescreve-lo. | Remover o parametro e adicionar @Override, devolvendo 60. | Sobrescrita versus sobrecarga |
| bug11 | Era possivel cancelar atendimento concluido ou ja cancelado. | `model/Atendimento.java:64`: cancelar mudava o status sem validar a transicao. | Permitir somente AGENDADO; recusar os demais sem alterar o estado. | Maquina de estados e excecoes de dominio |
| bug12 | Agendamento no passado consultava o banco e podia ser salvo. | `service/AgendaService.java:28`: Faltava validar data/hora antes de acessar o repository. | Recusar data passada ou ausente antes de qualquer consulta. | Validacao de negocio e falha antecipada |

## Parte 2 — Ajustes de Clean Code

| # | Onde estava | Principio violado | O que mudou |
|---|---|---|---|
| clean01 | AtendimentoFactory.criar | Nomes devem comunicar intencao | p, t, n, po, tu e d passaram a protocolo, tipo, petNome, petPorte, tutorNome e dataHora. |
| clean02 | AgendaService.agendar | Legibilidade de variaveis locais | doPet e a passaram a atendimentosDoPet e existente. |
| clean03 | AgendaService e AtendimentoController | Dependencias explicitas e estado imutavel | Injecao em campos foi substituida por construtores e campos final; Spring e Mockito continuam injetando as dependencias. |
| clean04 | AgendaService e GeradorProtocolo | Saida de diagnostico controlavel | System.out.println foi substituido por SLF4J em nivel debug; o service registra o protocolo sem montar um recibo com dados pessoais. SLF4J ja existe nas dependencias. |
| clean05 | AtendimentoController | Evitar numeros sem significado visivel | 201 e 409 foram substituidos por HttpStatus.CREATED e HttpStatus.CONFLICT. |
| clean06 | AtendimentoController.calcularDescontoFidelidade | Evitar codigo morto e funcionalidade especulativa | Metodo privado nunca chamado e comentarios de funcionalidades futuras removidos. |

## Parte 3 — Testes novos

Os seis metodos foram adicionados em `src/test/java/br/com/fiap/petfiap/RegrasSemCoberturaTest.java`.
Os sete arquivos dos 20 testes entregues foram preservados integralmente.
Todos os testes novos separam Arrange, Act e Assert; os cenarios do service usam mocks.

| # | Teste escrito (classe.metodo) | Regra coberta | Resultado ao escrever |
|---|---|---|---|
| teste01 | RegrasSemCoberturaTest.deveCobrarPrecoCorrespondenteQuandoPorteDoBanhoVariar | Banho: pequeno 60, medio 80, grande 100 | Vermelho: revelou bug09; verde apos o fix. |
| teste02 | RegrasSemCoberturaTest.deveDurar60MinutosQuandoAtendimentoForTosa | Tosa dura 60 minutos, inclusive por referencia Atendimento | Vermelho: revelou bug10; verde apos o fix. |
| teste03 | RegrasSemCoberturaTest.deveRecusarCancelamentoQuandoAtendimentoJaEstiverConcluido | Cancelar CONCLUIDO lanca StatusInvalidoException, mantem status e nao salva | Vermelho: revelou bug11; verde apos o fix. |
| teste04 | RegrasSemCoberturaTest.deveRecusarAgendamentoQuandoDataHoraEstiverNoPassado | Data passada lanca IllegalArgumentException antes de acessar o repository | Vermelho: revelou bug12; verde apos o fix. |
| teste05 | RegrasSemCoberturaTest.deveCustar150ReaisQuandoPorteDaConsultaVariar | Consulta custa 150 para qualquer porte | Verde de cara; protege regra ja correta. |
| teste06 | RegrasSemCoberturaTest.deveRecusarConclusaoQuandoAtendimentoEstiverCancelado | Concluir CANCELADO recusa sem mudar estado ou salvar | Verde de cara; protege regra ja correta. |

## Parte 4 — Perguntas de reflexao

### 1. A suite como contrato (Aula 15)

Comecei executando os 20 testes e confirmei as nove falhas descritas no enunciado.
No Builder, o resultado esperado era o nome recebido, mas o objeto devolvia null.
Ao ler comPet, encontrei petNome = petNome: o campo nunca recebia o argumento.
Na Factory, o teste esperava uma Tosa, mas o case TOSA criava Banho.
Corrigi as causas no codigo de producao e mantive os testes originais intactos.
A suite repete essas verificacoes automaticamente e mostra regressao sem depender de chamadas manuais com curl ou do Oracle.

### 2. Mock e injecao de dependencia (Aulas 13 a 15)

No AgendaServiceTest, @Mock faz o Mockito criar um AtendimentoRepository simulado.
@InjectMocks entrega esse objeto ao AgendaService; o Spring nao participa desse teste.
when(repository.findById(...)) define os dados que a dependencia deve devolver naquele cenario.
verify(repository, never()).save(any()) confirma que uma operacao recusada nao persiste nada.
Em producao, o Spring cria e injeta o repository real, originalmente usando @Autowired no campo.
Apos clean03, ele usa o construtor unico; o Mockito tambem usa esse construtor, por isso os testes continuam sem banco ou contexto Spring.

### 3. == vs .equals() (Aula 7)

AgendaService.agendar comparava o nome e a data com ==, que verifica identidade entre referencias.
Dois LocalDateTime criados separadamente podem representar o mesmo horario sem serem o mesmo objeto.
O teste original reproduz isso convertendo a data para texto e lendo-a novamente.
Literais como "Rex" podem compartilhar uma referencia no pool de strings, escondendo o erro por sorte.
O fix06 usa equals nas duas comparacoes, verificando o conteudo do nome e o valor da data.
A regra continua exigindo status AGENDADO e recusa o conflito antes de chamar save.

### 4. Sobrescrita vs sobrecarga (Aula 7)

Atendimento declara getDuracaoMinutos() sem argumentos e devolve 30 como duracao padrao.
Tosa declarava getDuracaoMinutos(String porte), criando outra assinatura por sobrecarga.
Por isso, chamar getDuracaoMinutos() em uma Tosa executava a implementacao herdada de 30 minutos.
O teste02 usa uma referencia Atendimento para confirmar o comportamento polimorfico esperado.
O fix10 removeu o parametro e acrescentou @Override, fazendo a chamada devolver 60.
Se @Override estivesse no metodo antigo, o compilador rejeitaria a assinatura porque ela nao sobrescrevia o metodo da superclasse.

### 5. Singleton manual vs bean do Spring (Aula 14)

GeradorProtocolo deve compartilhar uma unica instancia e uma sequencia global na aplicacao.
O metodo antigo criava um objeto novo sem guarda-lo em instancia, reiniciando contador em cada chamada.
O fix05 guarda o objeto e sincroniza tanto getInstancia quanto proximo para proteger chamadas concorrentes.
AgendaService e um @Service criado pelo Spring, cujo escopo padrao e singleton dentro do contexto.
O container gerencia sua instancia e injecao, evitando o erro manual de esquecer de guardar o objeto.
Isso nao torna qualquer estado automaticamente seguro para threads nem persiste protocolos entre reinicios; a numeracao atual continua em memoria.

### 6. Cobertura de testes: onde parar? (Aula 15)

Os testes de preco fixo da consulta e conclusao de cancelado ficaram verdes quando foram escritos.
Eu os manteria porque uma alteracao futura na consulta ou nas transicoes pode quebrar regras que hoje funcionam.
Os outros quatro mostraram falhas reais em precos, duracao, cancelamento e data passada.
Com prazo limitado, priorizaria regras de maior impacto e caminhos de erro, sem abandonar o caminho feliz.
Aqui isso inclui impedir duplicidade, nao consultar banco para data passada e nao cancelar um atendimento realizado.
Nao perseguiria 100% de linhas como objetivo isolado: o bug08 mostra que mocks podem esconder um erro de persistencia mesmo com muitos testes verdes.

## Parte 5 — Validacao e observacoes

- Primeiro commit: estado original do projeto recebido.
- Cada fix, teste novo e item de Clean Code possui commit proprio.
- Nenhuma biblioteca ou framework foi adicionada ao pom.xml.
- application.properties, pom.xml e os 20 testes originais permanecem iguais aos arquivos recebidos.
- A compilacao de verificacao usou o compilador do Eclipse com nivel Java 17, por uma restricao de normalizacao de caminhos do javac neste ambiente Windows; JUnit foi executado pelo Maven Surefire.
- Os testes unitarios dispensam o Oracle. A validacao complementar da API usa H2, ja presente no pom, com parametros de execucao e sem alterar as credenciais versionadas.
- O enunciado sugere cp5-bughunt-<nome-do-grupo>; foi usado cp05-poo-petfiap-bughunt conforme o repositorio informado.
- Identificacao do grupo e entrega do link no Teams ficam pendentes do grupo.

### Verificacao complementar da API

- API iniciou com H2 e ID inexistente respondeu 404.
- BANHO, TOSA e CONSULTA: POST 201, IDs gerados, protocolos 1/2/3, resumos corretos e conflitos 409.
- Transicoes validas 200; cancelar concluido, cancelar novamente e concluir cancelado responderam 409.
- Tipo inexistente, nome/porte vazios e data passada responderam 400.

### Evolucao da suite

| Etapa | Testes | Falhas |
|---|---:|---:|
| baseline | 20 | 9 |
| bug01 | 20 | 8 |
| bug02 | 20 | 6 |
| bug03 | 20 | 5 |
| bug04 | 20 | 4 |
| bug05 | 20 | 2 |
| bug06 | 20 | 1 |
| bug07 | 20 | 0 |
| bug08 | 20 | 0 |
| teste01 | 21 | 1 |
| bug09 | 21 | 0 |
| teste02 | 22 | 1 |
| bug10 | 22 | 0 |
| teste03 | 23 | 1 |
| bug11 | 23 | 0 |
| teste04 | 24 | 1 |
| bug12 | 24 | 0 |
| teste05 | 25 | 0 |
| teste06 | 26 | 0 |
| clean01 | 26 | 0 |
| clean02 | 26 | 0 |
| clean03 | 26 | 0 |
| clean04 | 26 | 0 |
| clean05 | 26 | 0 |
| clean06 | 26 | 0 |
| final | 26 | 0 |
