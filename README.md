# pitiupi — plugin 21 (blackjack)

Trabalho de Redes de Computadores (UFSJ): um plugin de **21 (blackjack)** para o [pitiupi](https://github.com/GabrielFSSantos/pitiupi), uma ferramenta P2P feita por um aluno.

Este repositório tem a versão **pronta para rodar no Windows**. O Pitiupi e os plugins já estão compilados.

```
teste-21/
├── rodar.cmd            # abre o Pitiupi já apontando para dados/
├── pitiupi.jar          # núcleo do Pitiupi
└── dados/plugins/
    ├── blackjack.jar    # plugin 21
    ├── chat.jar
    ├── perguntas.jar
    └── quiz.jar
```

## Como rodar

1. **Java 17 ou mais novo.** No terminal (`Win+R` → `cmd`), rode `java -version`. Se não tiver Java, instale:
   ```
   winget install Microsoft.OpenJDK.17
   ```
   Depois feche e abra o terminal de novo.
2. **Baixe o repositório:** em *Code → Download ZIP*, ou com `git clone`.
3. Dentro de `teste-21`, dê dois cliques em **`rodar.cmd`**.
4. Na primeira vez, o Windows pergunta sobre o firewall. Permita o Java em **Redes privadas**.
5. Os dois computadores precisam estar na mesma rede. Pegue o IPv4 de cada um com `ipconfig`, adicione o outro como peer e abram o plugin **21**.
