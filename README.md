# DaoLogcat 🚀

<img src="./app/src/main/ic_launcher-playstore.png" width="96" height="96" alt="DaoLogcat icon" />

<p align="left">
  <a href="https://visitorbadge.io/status?path=https%3A%2F%2Fgithub.com%2Fpbzin%2FDaoLogcat">
    <img src="https://api.visitorbadge.io/api/visitors?path=https%3A%2F%2Fgithub.com%2Fpbzin%2FDaoLogcat&label=repo%20views&countColor=%230e75b6&style=flat" alt="Repo Views" />
  </a>
  &nbsp;
  <a href="https://github.com/pbzin/DaoLogcat/releases">
    <img src="https://img.shields.io/github/downloads/pbzin/DaoLogcat/total?style=flat&color=0e75b6&label=downloads" alt="Downloads" />
  </a>
</p>

**DaoLogcat** is a modern and powerful logcat reader for Android, designed to provide high performance and full control over system debugging. With an interface based on **Material 3** and **Jetpack Compose**, it combines an updated look with essential features for those who need root access.

## 📸 Screenshots

<p>
  <img src="./docs/screenshots/daologcat-main.png" width="260" alt="DaoLogcat live log view" />
  <img src="./docs/screenshots/daologcat-settings.png" width="260" alt="DaoLogcat settings" />
  <img src="./docs/screenshots/daologcat-saved-logs.png" width="260" alt="DaoLogcat saved logs" />
</p>

## ✨ Features

- **Real-Time Capture with Root**: Full access to all Android system buffers (Main, System, Crash, Events, etc.).
- **Modern Interface (Material 3)**: Fluid experience with support for dynamic themes (Dark, Light, and Soft Light).
- **Saved Logs Management**: Access, view, and share your previous captures in a dedicated tab.
- **Definitive Buffer Clearing**: Clear command that purges system buffers (`logcat -c`) to start new captures without residue.
- **Smart Filters**: Quickly search by tags, PIDs, keywords, or priority levels.
- **Customizable Display Limit**: Choose how many lines the app should keep on screen (10k, 50k, 100k+), optimizing memory usage.
- **Data Privacy**: Built-in option to automatically hide sensitive data like phone numbers, emails, and URLs.
- **Fast Export**: Save as `.txt` or share logs instantly with your team.

## 🛠️ Requirements

- **Android 12.0 (SDK 31)** or higher.
- **Root Access**: Required to view all system logs (without root, only the app's own logs will be visible).

## 🚀 Getting Started

1. Start the app and authorize Root access.
2. Monitor the log stream instantly on the main screen.
3. Use the icons in the top bar to switch between **Live Logs** and **Saved Logs**.
4. Configure the line limit in the **Settings** menu to suit your needs.

## 📄 License

This project is free software distributed under the GNU GPL v3 license.

## ☕ Support my work

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
