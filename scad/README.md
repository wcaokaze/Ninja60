
OpenSCADをインストールして `openscad` コマンドが使えるようにしておいてください

##### snap
```sh
sudo snap install openscad
```

##### Debian, Ubuntu
```sh
sudo apt install openscad
```

##### macOS
```sh
brew install openscad
```

##### Arch
```sh
pacman -S openscad
```

##### Windows
略

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

`*.scad` ファイルの生成
```sh
./gradlew generateAllScads
```

`*.scad` ファイルを生成してopenscadで `*.stl` を作成
```sh
./gradlew generateAllStls
```

