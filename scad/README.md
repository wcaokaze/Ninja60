
OpenSCADをインストールして `openscad` コマンドが使えるようにしておいてください

```sh
sudo snap install openscad
```
でインストールできる？

僕は
```sh
sudo apt install openscad
```
でしました

* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

`*.scad` ファイルの生成
```sh
./gradlew generateAllScads
```

`*.scad` ファイルを生成してopenscadで `*.stl` を作成
```sh
./gradlew generateAllStls
```

