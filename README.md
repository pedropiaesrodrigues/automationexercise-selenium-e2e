# Automation Exercise — testes end-to-end com Selenium

Atividade de Teste e Qualidade de Software: automação do **Test Case 3 — Login User with incorrect email and password** e do **Test Case 1 — Register User**, com **15 entradas** e análise de classes de equivalência e valores limite.

Referência: [casos oficiais do Automation Exercise](https://automationexercise.com/test_cases).

## Tecnologias e execução

- Java 17 ou superior, Maven 3.9+ e Google Chrome instalado.
- Selenium 4.26.0, JUnit Jupiter 5.11.3 e Maven Surefire 3.5.2.
- Selenium Manager obtém o ChromeDriver compatível automaticamente. A primeira execução precisa de acesso à internet.

```bash
git clone https://github.com/pedropiaesrodrigues/automationexercise-selenium-e2e.git
cd automationexercise-selenium-e2e
mvn test
```

Para executar sem janela gráfica:

```bash
mvn test -Dheadless=true
```

Para executar apenas um fluxo:

```bash
mvn test -Dtest=LoginTest
mvn test -Dtest=RegistroTest
```

Cada entrada usa uma nova sessão do Chrome. O projeto usa esperas explícitas, seletores `data-qa`/IDs e cliques reais. Não há `Thread.sleep`. Anúncios de terceiros são bloqueados por URLs via CDP para evitar sobreposição dos formulários; as páginas e respostas do Automation Exercise continuam reais. Não se utiliza o perfil pessoal do Chrome. Os avisos de versão do CDP podem aparecer com versões de Chrome posteriores ao Selenium; os comandos CDP utilizados são enviados diretamente ao driver.

## Particionamento de classes de equivalência

As classes consideradas são: credenciais com formato válido, mas inexistentes; e-mail vazio; e-mail malformado; senha vazia; dados de cadastro válidos; nome vazio. Cada representante exercita o formulário real.

E-mail válido na sintaxe **não significa** usuário cadastrado. No login, `selenium.<UUID>@example.com` representa um e-mail não cadastrado. No cadastro, esse mesmo formato representa um e-mail novo. Os dados de endereço, telefone e senha são fictícios.

## Análise de valor limite

Inspeção do formulário em **05/10/2026**: nome e senha têm `required`, sem `minlength` ou `maxlength`. Portanto, o limite mínimo observado para preenchimento é **1 caractere**. Testamos **0, 1 e 2 caracteres** para senha, e nome vazio/1 caractere/cadastro usual. Isso não afirma que exista uma política de senha forte: o teste verifica o comportamento disponibilizado pelo site.

As listas de nascimento oferecem dia **1–31**, mês **1–12** e ano **1900–2021**. Os cadastros usam o mínimo, o mínimo + 1, o máximo - 1 e o máximo de cada lista. Esses são limites de opções do formulário, não uma regra de idade. Não há limite superior de comprimento declarado para nome ou senha, por isso nenhum limite arbitrário foi inventado. As datas combinadas são calendariamente válidas.

## Matriz de entradas e resultados esperados

| ID | Entrada principal | Classe / limite | Resultado esperado |
| --- | --- | --- | --- |
| L01 | E-mail único não cadastrado + `SenhaIncorreta123!` | Credenciais inexistentes | Mensagem de credenciais incorretas |
| L02 | E-mail único não cadastrado + `x` | Senha com 1 caractere (mínimo) | Mensagem de credenciais incorretas |
| L03 | E-mail único não cadastrado + `xy` | Senha com 2 caracteres (mínimo + 1) | Mensagem de credenciais incorretas |
| L04 | E-mail vazio + `Senha123!` | E-mail obrigatório vazio | `validity.valueMissing`; envio bloqueado |
| L05 | `email-invalido` + `Senha123!` | E-mail sem arroba | `validity.typeMismatch`; envio bloqueado |
| L06 | `usuario@` + `Senha123!` | E-mail sem domínio | `validity.typeMismatch`; envio bloqueado |
| L07 | `teste@example.com` + senha vazia | Senha com 0 caracteres (abaixo do mínimo) | `validity.valueMissing`; envio bloqueado |
| R01 | Nome `A`, senha `x`, nascimento 01/01/1900 | Mínimos: nome, senha, dia, mês e ano | Criar, confirmar login e excluir conta |
| R02 | Nome `Ana`, senha `xy`, nascimento 02/02/1901 | Mínimo + 1: senha, dia, mês e ano | Criar, confirmar login e excluir conta |
| R03 | Nome `Teste Trinta`, senha `Senha123!`, 30/11/2020 | Máximo - 1: dia, mês e ano | Criar, confirmar login e excluir conta |
| R04 | Nome `Teste Selenium`, senha `Senha123!`, 31/12/2021 | Máximos: dia, mês e ano | Criar, confirmar login e excluir conta |
| R05 | Nome vazio + `teste@example.com` | Nome com 0 caracteres | `validity.valueMissing`; envio bloqueado |
| R06 | Nome `Teste` + e-mail vazio | E-mail obrigatório vazio | `validity.valueMissing`; envio bloqueado |
| R07 | Nome `Teste` + `sem-arroba` | E-mail malformado | `validity.typeMismatch`; envio bloqueado |
| R08 | Cadastro completo, exceto senha vazia | Senha com 0 caracteres | Impedir criação da conta |

Nos casos L01–L03, a asserção verifica exatamente `Your email or password is incorrect!` e ausência de logout. Nos casos inválidos no cliente, o Chrome bloqueia o envio antes do servidor: verificar essa validação é o resultado correto, em vez de esperar uma mensagem de autenticação. As mensagens nativas não são comparadas por texto, pois dependem do idioma do navegador.

## Cobertura dos passos oficiais

**Login:** abre o Chrome, acessa a home, verifica slider e título, clica em Signup / Login, verifica o título do formulário, preenche e-mail/senha incorretos, clica em Login e confere a mensagem.

**Registro:** verifica home e New User Signup, informa nome/e-mail único, verifica Enter Account Information, preenche título, nome, e-mail, senha e nascimento, marca newsletter e ofertas, preenche todos os dados de endereço (incluindo empresa e Address 2), cria a conta, verifica Account Created, continua, confere Logged in as com o nome esperado, exclui a conta, verifica Account Deleted e continua. O e-mail vem da primeira etapa e aparece desabilitado na segunda. A exclusão usa a UI e apenas a conta fictícia criada naquela sessão.

## Organização e evidências

- `BaseTest.java`: ciclo do Chrome, esperas, navegação inicial e capturas.
- `LoginTest.java`: 7 entradas de login.
- `RegistroTest.java`: 8 entradas de registro.
- `target/surefire-reports/`: relatórios XML/texto do Maven.
- `target/evidencias/`: screenshots ao final de cada entrada e nas confirmações de criação/exclusão.
- `evidencias/`: evidências da execução entregue, com resumo e relatórios.

Os testes independem de credenciais pessoais e produzem e-mails com UUID para evitar colisões. Após criação confirmada, a exclusão é tentada em `finally`, inclusive se a asserção do nome falhar. Uma interrupção de rede/browser ainda pode impedir a limpeza; nesse caso a execução falha e as evidências ajudam a diagnosticar.

O exemplo `GoogleTest.java` fornecido foi substituído pelos testes da atividade. O `pom.xml` foi adaptado com dependência JUnit completa (incluindo engine e testes parametrizados), Java 17 e plugins de compilação/execução. Não são necessários WebDriverManager nem testes de busca no Google.
