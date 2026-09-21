# Entrega da atividade DevSecOps

Integrante: Arthur Estevan Vargas.

Repositório: [ArthurEstevanVargas/sidneibaron](https://github.com/ArthurEstevanVargas/sidneibaron).

## Documentos

- [Pesquisa teórica, referências e esquema autoral](Pesquisa-ShiftLeft-ShiftRight.md).
- [Discussão final Shift Left / Shift Right](Discussao-final.md).
- [Execução local, configuração e limites do piloto](Operacao.md).

## Sequência de evidências

As correções foram aplicadas separadamente na main, preservando as falhas reais de cada gate. Os resumos mostram que a publicação foi pulada enquanto existia falha.

| Etapa | Evidência remota | Resultado observado |
|---|---|---|
| Segredos | [Execução 35667100501](https://github.com/ArthurEstevanVargas/sidneibaron/actions/runs/35667100501) | Duas ocorrências didáticas detectadas pelo Gitleaks |
| Testes | [Execução 35667187058](https://github.com/ArthurEstevanVargas/sidneibaron/actions/runs/35667187058) | Esperado 180, obtido 198 |
| SAST | [Execução 35667402370](https://github.com/ArthurEstevanVargas/sidneibaron/actions/runs/35667402370) | Duas regras apontaram SQL Injection |
| SCA | [Execução 35667534615](https://github.com/ArthurEstevanVargas/sidneibaron/actions/runs/35667534615) | Dependência Log4j Core vulnerável, incluindo CVE-2021-44228 |

O [README automático](../README.md) registra o resultado do build, a execução correspondente, o nome da imagem, suas tags e o digest quando a publicação conclui. O nome da imagem segue o repositório existente: `ghcr.io/arthurestevanvargas/sidneibaron`.

Os screenshots e logs de entrega são disponibilizados no pacote local de evidências. Não confundir as primeiras execuções bloqueadas pela conta com as falhas reais listadas acima.

Branch Protection e multi-stage são opcionais no enunciado. A infraestrutura de banco do endpoint demonstrativo `/conta` não integra este piloto de pipeline; ver os limites em Operacao.md.
