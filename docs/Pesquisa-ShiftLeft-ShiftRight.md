# Segurança ao longo do ciclo de desenvolvimento

Autor: Arthur Estevan Vargas. Pesquisa da Parte 1 da atividade BancoFácil Digital, preparada para revisão individual. Fontes consultadas em 21/09/2026.

## 1. Shift Left e Shift Right

Em uma linha do tempo do SDLC, a esquerda representa concepção e desenvolvimento; a direita representa implantação e operação. Shift Left antecipa decisões e verificações de segurança, enquanto Shift Right observa o comportamento do sistema implantado. O SSDF recomenda incorporar práticas de segurança ao ciclo de desenvolvimento e responder continuamente às vulnerabilidades, em vez de concentrar a segurança em uma auditoria final [1].

Esquema autoral: as setas de retorno representam o aprendizado da operação alimentando requisitos e testes.

```text
                       SHIFT LEFT                                  SHIFT RIGHT
Requisitos -> Design -> Código -> Build -> Teste/staging -> Deploy -> Operação
   ameaças     revisão   SAST      SCA       DAST            |     alertas e resposta
              PoLP      segredos  lint      regressão        |     DAST controlado
                        testes    imagem                    |     reanálise de SBOM
      ^_____________________________________________________|___________|
                     incidentes geram novos requisitos e testes
```

Três controles à esquerda são modelagem de ameaças no design, SAST durante revisão de código e SCA no build: evitam incorporar ou distribuir defeitos conhecidos. À direita, detecção de comportamento anômalo, avaliação controlada da implantação e reanálise de inventários contra novas CVEs verificam riscos do ambiente real e mudanças ocorridas após o build.

As abordagens são complementares. Exemplo elaborado para a BancoFácil: o código implementa corretamente autorização, mas uma alteração exclusiva no proxy de produção expõe uma rota administrativa que deveria ser interna. Os testes anteriores, executados com a configuração de staging, não enxergam essa alteração posterior. Monitoramento e testes da implantação podem detectá-la. Isso não significa que toda falha de configuração seja impossível de detectar antes: nesse cenário, o desvio ocorreu depois da validação.

O custo depende do defeito. No design, corrigem-se decisões e especificações; no código, implementação e testes; no build, dependências e artefatos; na homologação, integrações e revalidação; em produção, somam-se resposta ao incidente, recuperação, indisponibilidade e possível perda de confiança. Essa é uma análise qualitativa, não uma escala universal. A conhecida afirmação de que corrigir depois custa sempre “100 vezes mais” não deve ser tratada como lei ou medição deste projeto. O estudo RTI/NIST de 2002 estimou perdas agregadas da economia por infraestrutura inadequada de testes; seus dados setoriais, contexto histórico e método de extrapolação não demonstram um multiplicador fixo para vulnerabilidades modernas [2]. Na BancoFácil, devem-se medir tempo de correção, incidentes, retrabalho e custo de indisponibilidade para avaliar o investimento em Shift Left.

## 2. Gestão de segredos

Secret sprawl é a dispersão de credenciais por locais sem gestão uniforme: arquivos de configuração, commits, branches, logs, cópias de trabalho, artefatos e camadas de imagens. Apagar um arquivo atual não elimina versões anteriores, clones e caches. Centralização, auditoria e ciclo de vida explícito reduzem essa dispersão [3].

Segredos de CI/build autorizam tarefas transitórias, como publicar no registry; precisam existir somente nos jobs correspondentes. Segredos de runtime autorizam a aplicação a acessar banco ou provedor de pagamentos e devem chegar na implantação/execução. O `GITHUB_TOKEN` da pipeline não substitui as credenciais do serviço financeiro. Um `ENV` literal no Dockerfile incorpora o valor aos metadados da imagem; arquivos copiados podem permanecer em camadas anteriores. Um registry privado limita quem baixa a imagem, mas não impede que leitores autorizados, cópias ou contas comprometidas recuperem o conteúdo [3, 4].

Gitleaks detecta padrões de credenciais e oferece fingerprints para reconhecer ocorrências específicas [5]. GitHub Secret Scanning procura segredos no histórico e apresenta alertas integrados ao repositório [6]. Essas ferramentas detectam exposição; não fornecem o segredo à aplicação. HashiCorp Vault centraliza o acesso e pode emitir credenciais dinâmicas [7]. AWS Secrets Manager armazena, recupera e gerencia a rotação de segredos [8]. Essas soluções cuidam da distribuição e do ciclo de vida, enquanto scanners ajudam a encontrar usos indevidos.

Rotação substitui uma credencial por outra e revoga a anterior. Remover o texto do repositório deixa uma chave já copiada ainda utilizável. Um incidente real exige revogação, investigação do uso e atualização dos consumidores. Neste laboratório, o enunciado identifica as chaves como exemplos públicos: as duas ocorrências históricas foram reconhecidas por fingerprint exato, sem permitir outras chaves, outros commits ou toda uma categoria de achados [3, 5].

## 3. Proteção, qualidade e testes

Branch Protection estabelece condições para alterar uma branch: exigir pull request, aprovação, checks bem-sucedidos, resolução de conversas e impedir force-push ou exclusão são exemplos. Disponibilidade depende do plano e da visibilidade do repositório. Administradores e identidades com bypass também precisam ser considerados ao desenhar a política [9].

Um Quality Gate é uma condição efetiva de avanço. Um relatório pode mostrar uma falha e ainda permitir publicação; um gate propaga a falha e impede a etapa seguinte. No projeto, `--error` do Semgrep, `exit-code: 1` do Trivy e as dependências entre jobs cumprem esse papel para a imagem. Para impedir merge, os checks também precisam ser obrigatórios na proteção da branch: uma pipeline de push que falha depois do merge não desfaz o merge.

Testes unitários preservam invariantes de negócio e verificam limites, cálculos e casos negativos. Testes de segurança também devem validar entradas hostis e ausência de efeitos indevidos. Cobertura de linhas é um indicador de execução, não prova de segurança: um teste que chama a função sem verificar autorização ou resultado pode cobrir linhas e continuar inútil. Neste projeto, a regressão da consulta testa isolamento por ID e verifica que uma tentativa destrutiva não elimina a tabela. Branch Protection impede contornar a revisão, gates tornam o resultado obrigatório e testes fornecem evidências repetíveis. Essa combinação reduz a chance de regressões antes da publicação, mas não comprova ausência de toda vulnerabilidade.

## 4. SAST, DAST e SCA

SAST examina código-fonte ou representações estáticas, como bytecode, sem precisar da aplicação rodando [10]. DAST envia entradas à aplicação em execução e observa respostas; depende de ambiente, rotas e autenticação adequados [11]. SCA inventaria componentes e relaciona suas versões a vulnerabilidades conhecidas. O caso Log4Shell (CVE-2021-44228), no Log4j Core, demonstrou como uma dependência amplamente utilizada pode introduzir execução remota de código em determinadas condições; o código próprio da aplicação não precisa conter uma implementação explícita do ataque [12].

| Técnica | O que analisa | Quando | Ferramentas | Achados típicos | Limitações |
|---|---|---|---|---|---|
| SAST | Fonte/bytecode | Editor, PR, CI | Semgrep, CodeQL | Fluxos de injeção, APIs inseguras | Regras e contexto limitados; falsos positivos e negativos |
| DAST | Serviço em execução | Staging; produção controlada | ZAP | Injeção observável, cabeçalhos e configuração HTTP | Rotas não alcançadas e autenticação limitam cobertura; risco de efeitos colaterais |
| SCA | Dependências, lockfiles, artefatos/SBOM | Build e reavaliação pós-deploy | Trivy, Dependency-Check | CVEs de bibliotecas transitivas e diretas | Banco pode estar incompleto; versão afetada não prova explorabilidade |

Na pipeline estudada, SAST é Shift Left porque examina o artefato de desenvolvimento antes da implantação; não observa tráfego de produção. A palavra “puramente” é uma simplificação: a análise estática também pode ser reexecutada sobre uma versão já publicada, embora continue estática. DAST é Shift Left em staging e Shift Right no ambiente real. SCA também precisa continuar após o deploy: uma imagem aprovada ontem pode se tornar afetada por uma CVE publicada hoje. A análise do filesystem pelo Trivy é útil para dependências; não substitui a análise da imagem final e de seus pacotes de sistema [13].

## 5. Infraestrutura e contêineres

Hardening reduz privilégios, componentes e configurações desnecessárias. Más práticas incluem `latest` sem controle de versão, execução como root, base com ferramentas que o runtime não usa, inclusão de segredos, `ADD` quando basta `COPY`, contexto de build excessivo e compiladores mantidos na imagem final. Tags explícitas ajudam a controlar atualizações, mas continuam mutáveis; digest oferece referência imutável e exige um processo de atualização [4].

O princípio do menor privilégio concede somente o necessário. Root dentro do contêiner amplia o impacto de uma exploração, especialmente com volumes do host, capabilities e configuração permissiva. Contêiner compartilha o kernel do host; não é uma justificativa para ignorar privilégios. Uma necessidade pontual deve ser redesenhada ou receber a menor capability possível, em vez de conceder root a todo o serviço. Para esta API na porta 8080, não há necessidade de root.

Multi-stage usa uma etapa com compilador e outra de runtime que recebe apenas o artefato. Assim, ferramentas e arquivos intermediários não precisam integrar a imagem final, reduzindo tamanho e superfície [14]. Neste projeto, mantivemos o build Maven do workflow e uma imagem final JRE pequena: o multi-stage é opcional e não é necessário para cumprir os seis itens obrigatórios.

Hadolint verifica instruções do Dockerfile e práticas de shell; não conhece todas as CVEs dos pacotes [15]. Trivy pode analisar uma imagem construída e confrontar seus componentes com bases de vulnerabilidades [13]. São controles complementares: um Dockerfile com lint aprovado ainda pode produzir uma imagem vulnerável.

## Referências

1. NIST. [SSDF, SP 800-218](https://csrc.nist.gov/pubs/sp/800/218/final).
2. RTI/NIST. [The Economic Impacts of Inadequate Infrastructure for Software Testing, 2002](https://samate.nist.gov/docs/econImpactSumm.v23.pdf).
3. OWASP. [Secrets Management Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html).
4. Docker. [Building best practices](https://docs.docker.com/build/building/best-practices/).
5. Gitleaks. [Documentação do projeto](https://github.com/gitleaks/gitleaks).
6. GitHub. [Secret scanning](https://docs.github.com/en/code-security/concepts/secret-security/secret-scanning).
7. HashiCorp. [Vault documentation](https://developer.hashicorp.com/vault/docs).
8. AWS. [What is Secrets Manager?](https://docs.aws.amazon.com/secretsmanager/latest/userguide/intro.html).
9. GitHub. [About protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches).
10. OWASP. [Source Code Analysis Tools](https://community.owasp.org/Source_Code_Analysis_Tools).
11. ZAP. [Documentation](https://www.zaproxy.org/docs/).
12. Apache. [Logging Services Security](https://logging.apache.org/security.html).
13. Aqua Security. [Trivy filesystem scanning](https://trivy.dev/docs/latest/target/filesystem/).
14. Docker. [Multi-stage builds](https://docs.docker.com/build/building/multi-stage/).
15. Hadolint. [Documentação do projeto](https://github.com/hadolint/hadolint).
