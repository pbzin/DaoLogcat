# DaoLogcat 🚀

<img src="./app/src/main/ic_launcher-playstore.png" width="96" height="96" alt="DaoLogcat icon" />

[![Repo Views](https://api.visitorbadge.io/api/visitors?path=https%3A%2F%2Fgithub.com%2Fpbzin%2FDaoLogcat&label=repo%20views&countColor=%230e75b6&style=flat)](https://visitorbadge.io/status?path=https%3A%2F%2Fgithub.com%2Fpbzin%2FDaoLogcat)
[![Downloads](https://img.shields.io/github/downloads/pbzin/DaoLogcat/total?style=flat&color=0e75b6&label=downloads)](https://github.com/pbzin/DaoLogcat/releases)

**DaoLogcat** é um leitor de logs (logcat) moderno e poderoso para Android, desenvolvido para oferecer alta performance e controle total sobre a depuração do sistema. Com uma interface baseada em **Material 3** e **Jetpack Compose**, ele combina um visual atualizado com funcionalidades essenciais para quem precisa de acesso root.

## 📸 Screenshots

<p>
  <img src="./docs/screenshots/daologcat-main.png" width="260" alt="DaoLogcat live log view" />
  <img src="./docs/screenshots/daologcat-settings.png" width="260" alt="DaoLogcat settings" />
  <img src="./docs/screenshots/daologcat-saved-logs.png" width="260" alt="DaoLogcat saved logs" />
</p>

## ✨ Funcionalidades

- **Captura em Tempo Real com Root**: Acesso completo a todos os buffers do sistema Android (Main, System, Crash, Events, etc.).
- **Interface Moderna (Material 3)**: Experiência fluida com suporte a temas dinâmicos (Escuro, Claro e Claro Suave).
- **Gerenciamento de Logs Salvos**: Acesse, visualize e compartilhe suas capturas anteriores em uma aba dedicada.
- **Limpeza de Buffer Definitiva**: Comando de limpeza que purga os buffers do sistema (`logcat -c`) para iniciar novas capturas sem resíduos.
- **Filtros Inteligentes**: Busque rapidamente por tags, PIDs, palavras-chave ou níveis de prioridade.
- **Limite de Display Customizável**: Você escolhe quantas linhas o app deve manter na tela (10k, 50k, 100k+), otimizando o uso de memória.
- **Privacidade de Dados**: Opção integrada para ocultar automaticamente dados sensíveis como números de telefone, e-mails e URLs.
- **Exportação Rápida**: Salve como `.txt` ou compartilhe logs instantaneamente com sua equipe.

## 🛠️ Requisitos

- **Android 12.0 (SDK 31)** ou superior.
- **Acesso Root**: Necessário para visualizar logs de todo o sistema (sem root, apenas os logs do próprio app estarão visíveis).

## 🚀 Como Iniciar

1. Inicie o app e autorize o acesso Root.
2. Acompanhe o fluxo de logs instantaneamente na tela principal.
3. Utilize os ícones na barra superior para alternar entre os **Logs Atuais** e os **Logs Salvos**.
4. Configure o limite de linhas no menu de **Configurações** para ajustar à sua necessidade de uso.

## 📄 Licença

Este projeto é um software livre distribuído sob a licença GNU GPL v3.

## ☕ Apoie o meu trabalho

<p align="center">
  <a href="https://buymeacoffee.com/pbzin">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" height="38" align="absmiddle">
  </a>
  <a href="https://github.com/sponsors/pbzin">
    <img src="https://img.shields.io/badge/Sponsor-%F0%9F%92%96-ea4aaa?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="GitHub Sponsors" height="38" align="absmiddle">
  </a>
  <br><br>
  <img src="https://img.shields.io/badge/Pix-%E2%9A%A1-32BCAD?style=for-the-badge&logo=pix&logoColor=white" alt="Pix" height="30" align="absmiddle">
  <br>
  <code>5198a8b3-6b89-4475-aec1-5adcfcfd12cf</code>
  <br><br>
  <img src="https://img.shields.io/badge/Bitcoin-F7931A?style=for-the-badge&logo=bitcoin&logoColor=white" alt="Bitcoin" height="30" align="absmiddle">
  <br>
  <code>1GkpDZDHYov7WZLs54Nv19f2KUoZPcACs2</code>
  <br>
  <img src="./app/src/main/res/drawable-nodpi/bitcoin_qr.png" width="150" alt="Bitcoin donation QR code">
  <br><br>
  <img src="https://img.shields.io/badge/Monero-FF6600?style=for-the-badge&logo=monero&logoColor=white" alt="Monero" height="30" align="absmiddle">
  <br>
  <code>45YtYmxUeXeFdokKPG1KWtMFLByS8nwmtiJjEiZ9LfbkNaSUCvyWWAx3VmtDKKkxPJFdQLSXxodRWMt7EBu5TmA3Qi9dgwT</code>
  <br>
  <img src="./app/src/main/res/drawable-nodpi/monero_qr.png" width="150" alt="Monero donation QR code">
</p>
