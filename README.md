# Chrome Dinosaur Game
![in-game-screenshot-1](screenshots/screenshot-1.png)

![in-game-screenshot-2](screenshots/screenshot-2.png)

![in-game-screenshot-3](screenshots/screenshot-3.png)

![in-game-screenshot-3](screenshots/screenshot-4.jpeg)

![in-game-screenshot-3](screenshots/screenshot-5.jpeg)





## How to Run

### Option 1: Download JAR (Easiest)
1. Go to [Releases](https://github.com/Arpith-binsa/chrome-dino-java/releases)
2. Download `chrome-dino-enhanced.jar`
3. Run: `java -jar chrome-dino-enhanced.jar`

### Option 2: From Source
```bash
cd chrome-dino-java
javac -d bin src/**/*.java
java -cp resources:bin user_interface.GameWindow
```



## Description
A Java version of Chrome Offline T-Rex Game. Some features not affecting gameplay is missing _(will be added soon!)_. Game is running on smooth **100 fps**.

## Features
  - Dynamic Background
  - Infinite Gameplay
  - Progressive Difficulty
  - Obstacles: Cactuses, Birds(Pterodactyl)
  - Jump / Duck
  - Sounds
  - Score & High Score _( File R/W )_
  - Dynamic jump height based on key holding time
  - Quick fall, pressed duck key in midair will drop Dino faster
  - Pause
  - Intro
  - **MORE SOON**

## Keybinds
**Jump: `ARROW UP`, `SPACE`, `W`** <br/>
**Duck: `ARROW DOWN`, `S`** <br/>
**Debug: <code>\`(backtick)</code>** <br/>
**Pause: `ESC`, `P`**

## Additional Information
Possible creation of ".jar" file. Highest Scores will be stored in ".txt" file right next to ".jar" file

### Debug
  - Disables collisions
  - Shows hitboxes of Dino, Cactuses, Birds
  - Shows speed ( difficulty increase )
