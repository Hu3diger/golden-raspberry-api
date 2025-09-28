# Golden Raspberry Awards API

Uma API REST desenvolvida em Spring Boot para gerenciar dados dos Golden Raspberry Awards, que reconhece anualmente os piores filmes de Hollywood.

## 📋 Sobre o Projeto

Esta API permite:
- Carregar dados de filmes a partir de um arquivo CSV
- Consultar filmes por ano e status de vencedor
- Calcular intervalos entre vitórias consecutivas de produtores
- Identificar produtores com menor e maior intervalo entre vitórias

## 🛠️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **H2 Database** (banco em memória)
- **OpenCSV** (para leitura de arquivos CSV)
- **Lombok** (para redução de código boilerplate)
- **Maven** (gerenciamento de dependências)
- **JUnit 5** (testes)

## 📁 Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/outsera/goldenraspberry/
│   │   ├── GoldenraspberryApplication.java     # Classe principal
│   │   ├── controller/
│   │   │   ├── MovieController.java            # Endpoints de filmes
│   │   │   └── ProducerController.java         # Endpoints de produtores
│   │   ├── dto/
│   │   │   ├── IntervalResponseDto.java        # DTO de resposta de intervalos
│   │   │   └── ProducerIntervalDto.java        # DTO de intervalo de produtor
│   │   ├── entity/
│   │   │   └── MovieEntity.java                # Entidade de filme
│   │   ├── repository/
│   │   │   └── MovieRepository.java            # Repositório de filmes
│   │   └── service/
│   │       └── MovieService.java               # Lógica de negócio
│   └── resources/
│       ├── application.properties              # Configurações da aplicação
│       └── movielist.csv                       # Dados dos filmes
└── test/
    └── java/com/outsera/goldenraspberry/
        └── MovieControllerIntegrationTest.java # Testes de integração
```

## 🚀 Como Executar o Projeto

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6+ (ou use o Maven Wrapper incluído)

### Passo a Passo

1. **Clone o repositório** (se aplicável):
   ```bash
   git clone <url-do-repositorio>
   cd goldenraspberry
   ```

2. **Compile o projeto**:
   ```bash
   # No Windows
   .\mvnw clean compile
   
   # No Linux/Mac
   ./mvnw clean compile
   ```

3. **Execute os testes**:
   ```bash
   # No Windows
   .\mvnw test
   
   # No Linux/Mac
   ./mvnw test
   ```

4. **Execute a aplicação**:
   ```bash
   # No Windows
   .\mvnw spring-boot:run
   
   # No Linux/Mac
   ./mvnw spring-boot:run
   ```

5. **Acesse a aplicação**:
   - API: http://localhost:8080
   - Console H2: http://localhost:8080/h2-console
     - URL JDBC: `jdbc:h2:mem:testdb`
     - Usuário: `sa`
     - Senha: `password`

## 📚 Endpoints da API

### Produtores

#### `GET /api/producers/intervals`
Retorna os produtores com menor e maior intervalo entre vitórias consecutivas.

**Resposta:**
```json
{
  "min": [
    {
      "producer": "Joel Silver",
      "interval": 1,
      "previousWin": 1990,
      "followingWin": 1991
    }
  ],
  "max": [
    {
      "producer": "Matthew Vaughn",
      "interval": 13,
      "previousWin": 2002,
      "followingWin": 2015
    }
  ]
}
```

### Filmes

#### `GET /api/movies`
Lista todos os filmes.

**Parâmetros opcionais:**
- `year`: Filtrar por ano
- `winner`: Filtrar por vencedores (true/false)

**Exemplos:**
- `GET /api/movies?year=1980` - Filmes de 1980
- `GET /api/movies?winner=true` - Apenas vencedores
- `GET /api/movies?year=1980&winner=true` - Vencedores de 1980

#### `GET /api/movies/{id}`
Retorna um filme específico pelo ID.

#### `GET /api/movies/years`
Lista todos os anos disponíveis.

## 🗃️ Estrutura dos Dados

### Arquivo CSV (movielist.csv)
O arquivo CSV deve ter o seguinte formato:
```csv
year;title;studios;producers;winner
1980;Can't Stop the Music;Associated Film Distribution;Allan Carr;yes
1980;Cruising;Lorimar Productions, United Artists;Jerry Weintraub;
```

**Campos:**
- `year`: Ano de lançamento
- `title`: Título do filme
- `studios`: Estúdios produtores
- `producers`: Produtores (separados por vírgula ou "and")
- `winner`: "yes" para vencedores, vazio para não-vencedores

### Entidade Movie
```java
{
  "id": 1,
  "year": 1980,
  "title": "Can't Stop the Music",
  "studios": "Associated Film Distribution",
  "producers": "Allan Carr",
  "winner": "yes"
}
```

## 🔧 Configurações

### Banco de Dados H2
O projeto usa H2 como banco em memória. As configurações estão em `application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
```

### Carregamento de Dados
- Os dados são carregados automaticamente do arquivo `movielist.csv` na inicialização
- O método `@PostConstruct` no `MovieService` processa o CSV
- O separador usado é `;` (ponto e vírgula)

## 🧪 Testes

### Executar Testes
```bash
# Todos os testes
.\mvnw test

# Testes específicos
.\mvnw test -Dtest=MovieControllerIntegrationTest
```

### Cobertura de Testes
- Testes de integração para endpoints principais
- Validação da estrutura de resposta JSON
- Verificação da lógica de cálculo de intervalos