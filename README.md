English version is [HERE](#english).

# CHiLO Producer

### CHiLO Producerとは

CHiLO Producerは、あらかじめ作成された動画やテキストファイルからEPUB3形式のCHiLO Book を出力するJAVAベースのソフトウェアです。

このソフトウェアは、[_CHiLO_](http://www.cccties.org/activities/chilo/)Ⓡ の一部として提供されています。

### デモビデオ

https://youtu.be/aySpvTFteiQ

### 動作環境

* Java 8 が動作する環境  
* xlsx ファイルが編集できる環境（推奨：Microsoft Office 動作確認：LibreOffice)

### 利用方法

1. Java 8（ https://java.com/ja/download/ ）をインストールします。<br />※Macユーザーの方は、JDK8 （  http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html ） をインストールしてください。
1. GitHubからCHiLO Producer( https://github.com/cccties/CHiLO-Producer )をダウンロードし、展開します。<br />
-> 全体の構成はこちらを参照：[dir.md](dir.md)

CHiLO Bookを作成するときは、展開した先にある _chiloPro_ ディレクトリを開き、シリーズ毎に _chiloPro/templete-series_  をコピーしていきます。
```
(root)
　│  run.bat　＊CHiLOBook出力用のバッチファイル
　│  run.sh 　＊CHiLOBook出力用のシェルスクリプト
　│  （省略）
　│
　├─book_templates ＊CHiLO Bookのテンプレート集
　│   │
　│   ├─basic　＊Basicデザイン
　│   │
　│   ├─cardview　＊Cardviewデザイン
　│   │
　│   └─listview　＊Listviewデザイン
　│  
　├─chiloPro
　│   │
　│   └─template-series　＊シリーズディレクトリのテンプレート（このディレクトリをコピーして作成していく）
　│   　　│  structure-books.xlsx　＊CHiLO Bookの構造ファイル
　│   　　│  author.xlsx　＊著者情報ファイル
　│   　　│
　│   　　├─common　＊シリーズ内の素材を保存するディレクトリ
　│   　　│
　│   　　└─vol-n　＊各ブックの素材を保存するディレクトリ
　│ 　
　├─extension　＊CHiLO Bookをオンラインで読むためのhtmlファイル
　│   
　└─src　＊CHiLOBook出力用のjarファイルのソース
　（省略）
```

### CHiLO Book制作の流れ

1. CHiLO Bookにパッケージする "素材" を準備します。
1.  _chiloPro/templete-series_ を複製し、上記1.で準備した素材を保存します。
1. ExcelファイルにCHiLO Bookのメタデータを記述します。
1. CHiLO Bookを出力します。

詳細はこちらをご覧ください　-> [CHiLO Bookの制作](http://docs.cccties.org/creating-chilobook/production/)

### CHiLO Bookの構造

CHiLO Bookを作る前に、CHiLO Bookの構造を理解しましょう。
-> [CHiLO Bookの構造](http://docs.cccties.org/creating-chilobook/sturcture/)

### スタイルを変更する

CHiLO Bookのスタイルを変更したい場合は、 _book_templates/_ のファイルを変更してください。
-> [ブックテンプレートの変更](http://docs.cccties.org/creating-chilobook/changing-the-book-tamplate/)

### CHiLO Book 素材サンプル

CHiLO Bookの素材サンプルを公開しています。

https://github.com/cccties/chilo001

ダウンロードして、展開したファイルを、 _(root)/chiloPro/_ にコピーして、CHiLO Book制作の参考にしてください。

なお、この素材サンプルは、[CHiLO Book Library](http://chilos.jp)で公開している[「はじめての情報ネットワーク」](http://chilos.jp/s/?id=1)の素材となっております。

### CHiLO Producer ver.2への移行方法

http://docs.cccties.org/creating-chilobook/migrate-to-chilo-producer-ver2/

### 課題・質問

このソフトの課題、質問、及び要望はこちらにご記載ください。
-> [Issues](https://github.com/cccties/CHiLO-Producer/issues)

### ソース

EPUB出力用のjarファイルのソースは _src/_ に保存されています。
-> [src](/src)

### Licensing

ApacheLicense2.0 ライセンスの元、提供しております。 (see [LICENSE.txt](LICENSE.txt)) 

### Copyright

Copyright © 2015 NPO CCC-TIES

***

# <a name="english"> CHiLO Producer

### About CHiLO Producer

CHiLO Producer is a JAVA-based software which can output an EPUB3 format CHiLO Book using videos and text files created in advance. 

This software is provided as a part of [_CHiLO_](http://www.cccties.org/en/activities/chilo/)Ⓡ.

### Demo Video

https://youtu.be/ETXPHxJdXro

### Operating environment

* Environment for Java 8 to operate
* Environment to edit xlsx files（Recommended：Microsoft Office operation check：LibreOffice)

### How to Use CHiLO Producer

1. Install Java 8(https://java.com/ja/download/). <br />※Mac users: Install JDK8 ( http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html )
1. Download CHiLO Producer( https://github.com/cccties/CHiLO-Producer ) from GitHub and unzip.<br />
-> For overall structure, refer to：[dir_en.md](dir_en.md)

If the file has been successfully unziped, _chiloPro_ directory will be created. When creating CHiLO Books, copy _chiloPro/templete-series_english_ for each series.
```
(root)
　│  run.bat　＊Batch file for CHiLOBook output
　│  run.sh 　＊Shell script for CHiLOBook output
　│  (The rest is omitted.)
　│
　├─book_templates ＊Collections of templates for CHiLO Book
　│   │
　│   ├─basic　＊Basic design
　│   │
　│   ├─cardview　＊Cardview design
　│   │
　│   └─listview　＊Listview design
　│  
　├─chiloPro
　│   │
　│   └─template-series_english　*Template of the directory of the series(copy this directory for creating).
　│   　　│  structure-books.xlsx *Structure file of CHiLO Book
　│   　　│  author.xlsx *Author information file
　│   　　│
　│   　　├─common　*Directory for saving materials utilized in the series
　│   　　│
　│   　　└─vol-n　*Directory for saving materials of each Book
　│ 　
　├─extension　＊html file to read CHiLO Book online
　│   
　└─src　＊The source of jar files for CHiLO Book output 
(The rest is omitted.)
```

### Workflow of creating CHiLO Books

1. Prepare "resources" to embed in CHiLO Book
1. Duplicate _chiloPro/template-series_english_ and save the resources prepared in 1. above
1. Describe the metadata of the CHiLO Book to the Excel file
1. Output CHiLO Book

+More details can be found here ->[Production of CHiLO Book](http://docs.cccties.org/en/creating-chilobook/production/)

### Structure of CHiLO Book

Before the production of CHiLO Book, understand the structure of CHiLO Book.
-> [Structure of CHiLO Book](http://docs.cccties.org/en/creating-chilobook/sturcture/)

### Change the style

When you want to change the style of the CHiLO Book, change _book_templates/_ file
-> [Changing the book tamplate](http://docs.cccties.org/en/creating-chilobook/changing-the-book-tamplate/)

### Samples of CHiLO Book material

We are publicly disclosing materials for CHiLO Book.

https://github.com/cccties/chilo001

Download and copy the expanded file to _(root)/chiloPro/_ for reference when creating CHiLO Book.
These sample materials are of those of ["Introduction to Internet Network I"](http://chilos.jp/s/?id=1) available for free of charge at [CHiLO Book Library](http://chilos.jp).

### Migrating data to CHiLO Producer ver. 2

http://docs.cccties.org/en/creating-chilobook/migrate-to-chilo-producer-ver2/

### Problems and Inquiries

If there are any problems, questions, or requests related to this software, enter here -> [Issues](https://github.com/cccties/CHiLO-Producer/issues)

### Source

The source of jar file for EPUB output is saved in _src/_ 
-> [src](/src)

### Licensing

We offer CHiLO Producer under the license of ApacheLicense2.0. (See [LICENSE.txt](LICENSE.txt)) 

### Copyright

Copyright © 2015 NPO CCC-TIES
