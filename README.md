# DaoLogcat 🚀

![Icon](./app/src/main/ic_launcher-playstore.png)

**DaoLogcat** é um leitor de logs (logcat) moderno para Android, focado em performance, design Material 3 e funcionalidades avançadas para desenvolvedores e entusiastas que possuem acesso **Root**.

Baseado no legado do MatLog e CatLog, o DaoLogcat foi reconstruído com **Jetpack Compose** para oferecer uma experiência fluida e nativa.

## ✨ Funcionalidades

- **Captura Completa com Root**: Acesso total aos buffers do sistema (Main, System, Crash, Events, etc.).
- **Interface Material 3**: Design moderno com suporte a temas Claro, Escuro e "Claro Suave".
- **Filtros Avançados**: Filtre logs por tag, palavra-chave, PID ou nível de prioridade (Verbose, Debug, Info, etc.).
- **Gerenciamento de Logs Salvos**: Organize e visualize capturas antigas diretamente no app.
- **Limpeza Profunda**: Botão de limpeza que purga os buffers do sistema (`logcat -c`) para uma depuração limpa.
- **Limite de Exibição Personalizável**: Configure exatamente quantas linhas deseja manter na memória (ex: 10.000, 50.000 ou mais).
- **Exportação Fácil**: Salve logs em arquivos `.txt` ou compartilhe diretamente com outros apps.
- **Privacidade**: Opção para omitir informações sensíveis (e-mails, números de telefone, URLs) dos logs.

## 🛠️ Requisitos

- Android 8.0+
- **Acesso Root** (Recomendado para captura total do sistema).
  - *Nota: Em dispositivos sem root, o app mostrará apenas seus próprios logs ou exigirá permissões via ADB.*

## 🚀 Como usar

1. Abra o app e conceda permissão de Superusuário.
2. Os logs começarão a fluir em tempo real.
3. Use o ícone de **Filtro** para encontrar o que procura.
4. Clique no ícone de **Pasta** na barra superior para ver seus logs salvos.
5. Ajuste o **Limite de Linhas** nas configurações para otimizar a performance no seu dispositivo.

## 📄 Licença

```
Copyright (C) 2024 DaoLogcat Devs

Este programa é um software livre: você pode redistribuí-lo e/ou modificá-lo
sob os termos da Licença Pública Geral GNU conforme publicada pela
Free Software Foundation, versão 3 da Licença ou qualquer versão posterior.
```

---
*Baseado originalmente no MatLog de Daniel Ciao e CatLog de Nolan Lawson.*
