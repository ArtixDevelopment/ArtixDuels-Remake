# ArtixDuels

Um plugin completo e avançado de sistema de duelos para servidores Minecraft Spigot/Paper. O ArtixDuels oferece um sistema robusto de duelos PvP com múltiplos modos de jogo, sistema de estatísticas, recompensas, apostas, espectadores e muito mais.

## 🎮 Características Principais

### Sistema de Duelos
- **Múltiplos Modos de Duelo**: BedFight, StickFight, Soup, SoupRecraft, Gladiator, FastOB, Boxing, FireballFight, Sumo, BattleRush, TNTSumo
- **Sistema de Convites**: Desafie jogadores diretamente ou use matchmaking
- **Arenas Configuráveis**: Múltiplas arenas com spawns personalizados para jogadores e espectadores
- **Kits Customizáveis**: Crie e gerencie kits personalizados para cada modo de duelo
- **Countdown e Estados**: Sistema completo de estados de duelo (WAITING, COUNTDOWN, FIGHTING, FINISHED)

### Estatísticas e Histórico
- **Sistema de Estatísticas Completo**: Vitórias, derrotas, empates, winrate e muito mais
- **Histórico de Duelos**: Registro completo de todos os duelos realizados
- **Estatísticas por Modo**: Estatísticas separadas para cada modo de duelo
- **Armazenamento em MongoDB**: Banco de dados robusto para persistência de dados

### Interface e Experiência
- **GUIs Interativas**: Menus gráficos para seleção de modos, configuração e scoreboard
- **Scoreboard Dinâmico**: Scoreboard personalizável com placeholders e múltiplos modos
- **Tablist Customizada**: Tablist configurável com informações de duelos
- **NPCs Interativos**: Integração com Citizens para NPCs de duelos (opcional)
- **Sistema de Mensagens**: Mensagens totalmente customizáveis

### Recursos Avançados
- **Sistema de Recompensas**: Recompensas configuráveis para vitórias e derrotas (dinheiro, XP, itens)
- **Sistema de Apostas**: Sistema de apostas opcional para duelos
- **Sistema de Espectadores**: Permita que jogadores assistam duelos em andamento
- **Cooldowns Configuráveis**: Sistema de cooldown para prevenir spam de convites
- **Placeholders**: Suporte a placeholders para integração com outros plugins

## 📋 Requisitos

- **Minecraft**: Versão 1.8.8 ou superior
- **Servidor**: Spigot ou Paper 1.8.8+
- **Java**: JDK 8 ou superior
- **MongoDB** (Opcional): Versão 3.12 ou superior (apenas se usar MongoDB como armazenamento)
- **Citizens** (Opcional): Para suporte a NPCs interativos

### Tipos de Armazenamento

O plugin suporta dois tipos de armazenamento:

1. **MongoDB** (Padrão): Requer instalação e configuração do MongoDB
2. **Flat-File** (YAML): Armazena dados em arquivos YAML, não requer banco de dados externo

## 🚀 Instalação

1. Baixe a versão mais recente do plugin do [releases](https://github.com/PotDevxs/ArtixDuels/releases)
2. Coloque o arquivo `ArtixDuels.jar` na pasta `plugins` do seu servidor
3. Inicie o servidor para gerar os arquivos de configuração
4. Configure o tipo de armazenamento no arquivo `config.yml`:
   - Para usar **Flat-File** (sem MongoDB): `database.type: "flatfile"`
   - Para usar **MongoDB**: Configure `database.type: "mongodb"` e as credenciais
5. Reinicie o servidor

## ⚙️ Configuração

### Configuração do Banco de Dados (config.yml)

O plugin suporta dois tipos de armazenamento. Configure o tipo desejado no `config.yml`:

#### Usando MongoDB (Padrão)

```yaml
database:
  type: "mongodb"
  connection-string: "mongodb://localhost:27017"
  database-name: "artixduels"
```

#### Usando Flat-File (YAML) - Sem necessidade de MongoDB

```yaml
database:
  type: "flatfile"
```

Quando usar `flatfile`, os dados serão armazenados em:
- **Estatísticas**: `plugins/ArtixDuels/stats/<UUID>.yml` (um arquivo por jogador)
- **Histórico**: `plugins/ArtixDuels/duel_history.yml` (um arquivo único)

**Nota**: O tipo pode ser `flatfile`, `flat-file` ou `file` - todos funcionam da mesma forma.

### Configuração de Duelos

```yaml
duels:
  request-timeout: 30  # Tempo em segundos para expirar convites
  countdown-time: 5    # Tempo de countdown antes do duelo começar
  default-kit: "default"
```

### Configuração de Cooldowns

```yaml
cooldowns:
  duel: 60    # Cooldown entre duelos (segundos)
  request: 10 # Cooldown entre convites (segundos)
```

### Configuração de Recompensas

```yaml
rewards:
  enabled: true
  money: 100.0  # Dinheiro para o vencedor
  exp: 50       # XP para o vencedor
  win:
    diamond:
      type: "ITEM"
      material: "DIAMOND"
      chance: 50.0
      min-amount: 1
      max-amount: 3
```

### Configuração de Apostas

```yaml
betting:
  enabled: false
  min-bet: 0.0
  max-bet: 10000.0
```

### Configuração de Arenas

```yaml
arenas:
  arena1:
    player1-spawn: "world,0,100,0,0,0"
    player2-spawn: "world,10,100,10,180,0"
    spectator-spawn: "world,5,105,5,0,0"
```

### Configuração de Kits

Os kits são configurados no arquivo `kits.yml`:

```yaml
kits:
  default:
    display-name: "§aKit Padrão"
    contents: []
    armor: []
```

## 📝 Comandos

### Comandos de Jogador

| Comando | Aliases | Descrição |
|---------|---------|-----------|
| `/duelo <jogador> [kit] [arena]` | `/duel`, `/duelos` | Desafiar um jogador para um duelo |
| `/accept` | `/aceitar` | Aceitar um convite de duelo pendente |
| `/deny` | `/recusar` | Recusar um convite de duelo pendente |
| `/stats [jogador]` | `/estatisticas`, `/estatisticas` | Ver estatísticas de duelos |
| `/spectate <jogador>` | `/espectar`, `/spec` | Espectar um duelo em andamento |
| `/history` | `/historico` | Ver histórico de duelos |
| `/scoreboard` | `/sb`, `/score` | Configurar modo do scoreboard |

### Comandos Administrativos

| Comando | Aliases | Permissão | Descrição |
|---------|---------|-----------|-----------|
| `/dueladmin` | `/dueladm`, `/dadm` | `artixduels.admin` | Comandos administrativos do plugin |

#### Subcomandos do `/dueladmin`:
- `reload` - Recarregar configurações do plugin
- `arena create <nome>` - Criar uma nova arena
- `arena delete <nome>` - Deletar uma arena
- `arena setspawn <nome> <tipo>` - Definir spawn de uma arena (1, 2 ou spectator)
- `kit create <nome>` - Criar um novo kit
- `kit delete <nome>` - Deletar um kit
- `stats reset <jogador>` - Resetar estatísticas de um jogador

## 🔐 Permissões

| Permissão | Descrição | Padrão |
|-----------|-----------|--------|
| `artixduels.admin` | Acesso a comandos administrativos | `op` |
| `artixduels.duel` | Usar comandos de duelo | `true` |
| `artixduels.spectate` | Espectar duelos | `true` |
| `artixduels.stats` | Ver estatísticas | `true` |
| `artixduels.history` | Ver histórico | `true` |

## 🎯 Modos de Duelo

O plugin suporta os seguintes modos de duelo:

1. **BedFight** - Duelo com camas
2. **StickFight** - Duelo com gravetos
3. **Soup** - Duelo com sopa
4. **SoupRecraft** - Duelo com sopa e recraft
5. **Gladiator** - Duelo gladiador
6. **FastOB** - Duelo rápido
7. **Boxing** - Boxe
8. **FireballFight** - Duelo com fireballs
9. **Sumo** - Sumo
10. **BattleRush** - Batalha rápida
11. **TNTSumo** - Sumo com TNT

## 📁 Estrutura do Projeto

```
ArtixDuels/
├── src/main/java/dev/artix/artixduels/
│   ├── commands/          # Comandos do plugin
│   ├── database/          # Gerenciamento de banco de dados
│   ├── gui/               # Interfaces gráficas
│   ├── listeners/          # Event listeners
│   ├── managers/          # Gerenciadores de funcionalidades
│   ├── models/            # Modelos de dados
│   └── npcs/              # Sistema de NPCs
└── src/main/resources/
    ├── config.yml         # Configuração principal
    ├── kits.yml           # Configuração de kits
    ├── messages.yml       # Mensagens do plugin
    ├── npcs.yml           # Configuração de NPCs
    ├── scoreboard.yml     # Configuração do scoreboard
    ├── tablist.yml        # Configuração da tablist
    └── plugin.yml         # Informações do plugin
```

## 🔧 Compilação

### Pré-requisitos
- Maven 3.6+
- JDK 8+

### Passos para Compilar

1. Clone o repositório:
```bash
git clone https://github.com/PotDevxs/ArtixDuels.git
cd ArtixDuels
```

2. Compile o projeto:
```bash
mvn clean package
```

3. O arquivo JAR será gerado em `target/ArtixDuels.jar`

## 📦 Dependências

### Dependências Principais
- **Spigot API 1.8.8-R0.1-SNAPSHOT** - API do Spigot
- **MongoDB Java Driver 3.12.14** - Driver para MongoDB (necessário apenas se usar MongoDB)
- **Citizens 2.0.32-SNAPSHOT** (Opcional) - Para suporte a NPCs

**Nota**: O MongoDB é opcional. Você pode usar o sistema Flat-File (YAML) que não requer nenhum banco de dados externo.

### Repositórios Maven
- SpigotMC Repository
- Sonatype Repository
- Citizens Repository

## 🎨 Funcionalidades Avançadas

### Sistema de Scoreboard
- Scoreboard dinâmico com placeholders
- Múltiplos modos de exibição
- Preferências por jogador
- Atualização em tempo real

### Sistema de Tablist
- Tablist customizável
- Informações de duelos em andamento
- Atualização assíncrona

### Sistema de NPCs
- NPCs interativos com Citizens
- Hologramas informativos
- Integração com sistema de duelos

### Sistema de Placeholders
- Placeholders para estatísticas
- Placeholders para duelos ativos
- Integração com PlaceholderAPI (se disponível)

## 🐛 Troubleshooting

### Problemas Comuns

**O plugin não conecta ao MongoDB:**
- Verifique se está usando MongoDB (tipo `mongodb` no config.yml)
- Se não quiser usar MongoDB, altere `database.type` para `flatfile`
- Se usar MongoDB, verifique se o MongoDB está rodando
- Confirme a string de conexão no `config.yml`
- Verifique as permissões de acesso ao banco de dados

**Problemas com Flat-File:**
- Verifique as permissões de escrita na pasta do plugin
- Certifique-se de que o servidor tem permissão para criar arquivos
- Os arquivos são criados automaticamente na primeira execução

**NPCs não aparecem:**
- Certifique-se de que o Citizens está instalado
- Verifique a configuração no arquivo `npcs.yml`
- Confirme que os NPCs foram criados corretamente

**Arenas não funcionam:**
- Verifique se os spawns estão configurados corretamente
- Confirme que as coordenadas estão no formato correto: `world,x,y,z,yaw,pitch`
- Certifique-se de que a arena existe no `config.yml`

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fazer um Fork do projeto
2. Criar uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abrir um Pull Request

## 📄 Licença

Este projeto está sob licença proprietária. Todos os direitos reservados.

## 👨‍💻 Desenvolvedor

Desenvolvido por **Faastyzin**

## 📞 Suporte

Para suporte, reportar bugs ou sugerir features:
- Abra uma [Issue](https://github.com/PotDevxs/ArtixDuels/issues) no GitHub
- Entre em contato através do servidor de suporte

## 🔄 Changelog

### Versão 1.0
- Lançamento inicial
- Sistema completo de duelos
- Múltiplos modos de jogo
- Sistema de estatísticas
- Integração com MongoDB
- Sistema de recompensas
- Sistema de apostas
- Sistema de espectadores
- GUIs interativas
- Scoreboard e Tablist customizáveis
- Suporte a NPCs (Citizens)

---

**Nota**: Este plugin requer um servidor Spigot/Paper para funcionar. O MongoDB é opcional - você pode usar o sistema Flat-File (YAML) que não requer banco de dados externo. Se optar por usar MongoDB, certifique-se de que está instalado e configurado corretamente.

