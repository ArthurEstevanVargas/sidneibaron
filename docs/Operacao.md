# Execução e escopo do piloto

Requisitos: JDK 17, Maven e, para a imagem, Docker. Execute `mvn clean verify`, depois `java -jar target/banco-facil-api-0.0.1-SNAPSHOT.jar`. A aplicação usa a porta 8080 por padrão. `/health` retorna `OK`; `/discount?price=200&percent=10` retorna `180.0`.

As variáveis `DB_PASSWORD`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` e `PAYMENT_GATEWAY_API_KEY` são lidas do ambiente na inicialização da classe `AppConfig`. Sem configuração, retornam `null`; não há valores secretos padrão. A classe ainda não tem consumidores na aplicação demonstrativa. Em produção, o orquestrador deve injetar essas variáveis a partir de Vault, AWS Secrets Manager ou equivalente. Não copiar valores para Dockerfile, imagem, Git ou logs. O `GITHUB_TOKEN` serve à pipeline, não à autenticação da aplicação no provedor financeiro.

O endpoint `/conta` mantém o contrato e a URL JDBC do exemplo original. A distribuição original não fornecia driver H2 nem esquema/dados de runtime; essa infraestrutura não foi inventada nesta correção. H2 foi adicionado somente com escopo `test`, para testar a consulta parametrizada contra um banco real e temporário. Portanto, a consulta está validada nos testes, mas `/conta` não está pronto para uso no JAR isolado. Preparar persistência e autorização reais é um trabalho separado; este piloto não deve ser tratado como um banco pronto para produção.

O Dockerfile recebe o JAR já compilado pelo workflow. Para reproduzir: `mvn clean verify`, `docker build -t banco-facil-api:local .` e `docker run --rm -p 8080:8080 banco-facil-api:local`. A imagem usa JRE 17 Alpine e usuário `app`, sem precisar de root. A tag explícita ainda é mutável: fixação por digest e atualização periódica são melhorias futuras.

Spring Boot foi atualizado para 3.5.16 e Tomcat para 10.1.60. O override de Tomcat corrige os achados ainda presentes na versão 10.1.55 do BOM. A versão 10.1.58 citada pela base de vulnerabilidades não foi publicada; a [documentação do Apache](https://tomcat.apache.org/security-10.html) registra que a correção foi distribuída a partir de 10.1.59. A versão 10.1.60 foi resolvida pelo Maven Central e validada localmente. Os scans devem ser repetidos quando a base de CVEs mudar.

O workflow preserva os oito jobs e os cinco gates sequenciais. O resumo somente declara LIBERADO se todos retornarem `success`. Cancelamento e etapas puladas bloqueiam a liberação. A análise SCA resolve as dependências no cache Maven antes do Trivy para reduzir consultas remotas e erros 429. Não foram adicionadas exclusões de CVEs nem reduzido o nível de bloqueio HIGH/CRITICAL.

O README é gerado pela pipeline e não foi editado manualmente. O integrante Arthur Estevan Vargas está identificado em `alunos.txt`. O repositório existente chama-se `sidneibaron`; o workflow publicará `ghcr.io/arthurestevanvargas/sidneibaron`, enquanto a atividade sugere o nome `banco-facil-api`. Essa divergência deve ser resolvida com o responsável pela entrega, sem renomear o repositório automaticamente.

Branch Protection e multi-stage são opcionais no enunciado e não foram configurados. Exigir PR em `main` também exige revisar o job que grava o README diretamente nessa branch. Link da entrega existente: [ArthurEstevanVargas/sidneibaron](https://github.com/ArthurEstevanVargas/sidneibaron).

Pesquisa: [Parte 1](Pesquisa-ShiftLeft-ShiftRight.md). Discussão: [Parte 2](Discussao-final.md). Os sete prints da entrega estão publicados em [Entrega.md](Entrega.md) e `docs/prints`. Logs locais e relatório de trabalho ficam em `docs/evidencias` e `docs/Relatorio-execucao.md`, fora do versionamento por incluírem informações da conta.
