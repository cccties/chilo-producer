# CHiLO Producer

### CHiLO Producerとは

CHiLO Producerは、[CHiLO Book](https://github.com/cccties/CHiLO-Producer/wiki) に最適化された _"つまりCHiLO Bookしかつくれない"_ JAVAベースのソフトウェアです。なお、現在のバージョンは、Hybrid CHiLO Book（EPUB3形式）とWeb CHiLO Book（HTML5形式）を作成することができますが、_"Embedded CHiLO Bookは未実装"_ です。

このソフトウェアは、[_CHiLO_](http://www.cccties.org/activities/chilo/)Ⓡ の一部として提供されています。

### 動作環境

・Java 8 が動作する環境  
・xlsx ファイルが編集できる環境（動作確認：Microsoft Office, LibreOffice, OpenOffice)

### 利用方法

1. Java 8（ https://java.com/ja/download/ ）をインストールします。
1. GitHubからCHiLO Producer( https://github.com/cccties/CHiLO-Producer )をダウンロードし、展開します。<br>
-> 全体の構成はこちらを参照：[dir.md](dir.md)

CHiLO Bookを作成するときは、展開した先にある _chiloPro_ フォルダを開き、シリーズ毎に _chiloPro/templete-series_  をコピーしていきます。
```
(root)
　│  run.bat　＊CHiLOBook書き出し用のバッジファイル
　│  （省略）
　│
　└─chiloPro
  　　│
　    ├─common　＊全体の共通ファイル群
　    │
　    ├─sample-series　＊サンプル
　    │
　    └─template-series　＊シリーズフォルダのテンプレート（このフォルダをコピーして作成していく）
　    　　│  structure-books.xlsx　＊CHiLO Bookの構造ファイル
　    　　│
　    　　├─common　＊シリーズ内の素材を保存するフォルダ
　    　　│
　    　　└─vol-n　＊各ブックの素材を保存するフォルダ
　（省略）
```

### CHiLO Book制作の流れ

1. CHiLO Bookに埋め込む素材 _"必要な素材"_ を準備します。
1.  _chiloPro/templete-series_ をコピーし、必要な素材を保存します。
1. エクセルでCHiLO Bookの構造情報 _"構造ファイル"_ を作成します。
1. CHiLO Bookを書き出します。

詳細はこちらをご覧ください　-> [CHiLO Bookの制作](https://github.com/cccties/CHiLO-Producer/wiki/01.CHiLO-Book%E3%81%AE%E5%88%B6%E4%BD%9C)

### CHiLO Bookの構造

CHiLO Bookを作る前に、CHiLO Bookの構造を理解しましょう。
-> [CHiLO Bookの構造](https://github.com/cccties/CHiLO-Producer/wiki/00.CHiLO-Book%E3%81%AE%E6%A7%8B%E9%80%A0)

### 使い方のコツ

CHiLO ProducerはCHiLO Bookしか作れませんが、テンプレート、CSSファイル、設定ファイルを変更すると独自のCHiLO Bookを作成することができます。
-> [使い方のコツ](https://github.com/cccties/CHiLO-Producer/wiki/03.%E4%BD%BF%E3%81%84%E6%96%B9%E3%81%AE%E3%82%B3%E3%83%84)

### 課題・質問

このソフトの課題、質問、及び要望はこちらにご記載ください。
-> [Issues](https://github.com/cccties/CHiLO-Producer/issues)

### Licensing

ApacheLicense2.0. ライセンスの元、提供しております。 (see [LICENSE.txt](LICENSE.txt)) 

### Copyright

Copyright © 2015 NPO CCC-TIES All Right Reserved.
