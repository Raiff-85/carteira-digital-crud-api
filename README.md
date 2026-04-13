# Carteira Digital API

API REST para gerenciamento de transações financeiras e usuários.

## Tecnologias

* **Java 21**
* **Spring Boot v3.4.3**
* **PostgreSQL** (via Docker na porta 5432)
* **Hibernate** (Validação e mapeamento)
* **Flyway** (Versionamento de banco de dados)
* **Lombok** v1.18.30 & **Bean Validation**

## Pré-requisitos

* **JDK 21** instalado.
* **Docker Desktop** rodando com o container PostgreSQL ativo na porta 5432.
* **DBeaver** ou a aba Database do IntelliJ para gerenciar o banco.

## Configuração do Ambiente

### 1. Criar o Banco de Dados
A aplicação espera um banco chamado **carteira_digital_db**. É possível criá-lo via IntelliJ ou DBeaver conectando ao seu servidor Docker local:
- **User:** postgres
- **Password:** senha123
- **Database:** carteira_digital_db

### 2. Setup Inicial das Tabelas (Primeira Execução)
Para evitar a escrita manual do SQL inicial, utilize o Hibernate para gerar a estrutura e o Flyway para assumir o controle posterior:

1. No arquivo src/main/resources/application.yaml, altere temporariamente ddl-auto: validate para **create**.
2. Execute o projeto: mvn spring-boot:run. O Hibernate criará as tabelas no Docker.
3. Pare a aplicação.
4. No seu gerenciador de banco, extraia o SQL das tabelas criadas e salve na pasta src/main/resources/db/migration com o nome **V1__create_nome_da_tabela.sql**.
5. **IMPORTANTE:** Retorne o ddl-auto no application.yaml para **validate**.

### 3. Versionamento com Flyway
Qualquer alteração posterior na estrutura deve ser feita via scripts SQL na pasta de migration (V2, V3, etc). O Flyway executará esses scripts automaticamente ao iniciar a aplicação.

## Como Executar

No terminal, na pasta raiz do projeto:

mvn spring-boot:run

A API estará disponível em http://localhost:8080.

## Plano de Testes e Validação (QA)
Como este projeto foca na qualidade de software, as seguintes camadas de testes foram implementadas:

1. Testes Automatizados (Back-end)
   Para rodar os testes unitários e de integração integrados ao Maven:

mvn test

2. Validação via Postman e Cypress

3. Dicas de Observabilidade
   Logs: Verifique o console do IntelliJ para acompanhar as queries SQL geradas pelo Hibernate e as migrações do Flyway.
   DBeaver: Utilize o Refresh (F5) para validar se os dados inseridos via API foram persistidos corretamente nas tabelas.