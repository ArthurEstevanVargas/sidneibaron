# Discussão final: completando o ciclo de segurança

Os controles implementados neste piloto atuam antes da publicação: detecção de segredos, testes, SAST, SCA e lint do Dockerfile. Na configuração atual, revisão do código, testes unitários e lint são controles de desenvolvimento (Shift Left), pois examinam alterações e artefatos antes da implantação. A sequência de gates impede publicar uma imagem quando qualquer verificação falha. A proteção da branch, opcional na atividade e ainda não configurada, complementaria essa política exigindo revisão e checks antes do merge.

SCA pode ser estendida ao Shift Right: uma SBOM por digest da imagem permite reavaliar componentes implantados quando aparecem CVEs. Segredos também exigem controle operacional, com auditoria de acesso, expiração, rotação e resposta a exposição. SAST pode ser reexecutada com regras novas sobre código já lançado, mas continua sem observar a execução real.

Para a BancoFácil fechar o ciclo, proponho DAST com ZAP em staging antes da liberação, testes controlados em produção com escopo e limites explícitos, logs sem credenciais, métricas e alertas sobre padrões anômalos. Tentativas de exploração relacionadas ao Log4j exigiriam correlação entre entrada suspeita, comportamento do processo e conexões de saída; uma string suspeita isolada não comprova comprometimento. A equipe precisaria definir responsáveis, limiares, procedimentos de contenção e critérios de rollback.

Feature flags e liberação gradual reduziriam o alcance inicial de uma alteração, acompanhando erros e indicadores de segurança por versão. Não corrigem sozinhas uma biblioteca vulnerável: é necessário reconstruir, testar e substituir a imagem afetada. Incidentes devem gerar testes de regressão e ajustes no modelo de ameaças. Isso conecta observações de produção às decisões de desenvolvimento.

Esta discussão propõe controles operacionais adicionais; não afirma que DAST, monitoramento e feature flags foram implantados. O resultado da publicação no GHCR e o link da execução correspondente são registrados pelo README automático. As evidências de cada gate documentam a transição da versão vulnerável para a versão corrigida; as validações locais complementam essa verificação remota.

Referências: [NIST SSDF](https://csrc.nist.gov/pubs/sp/800/218/final), [ZAP](https://www.zaproxy.org/docs/), [Trivy](https://trivy.dev/docs/latest/target/filesystem/) e [OWASP Secrets Management](https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html).
