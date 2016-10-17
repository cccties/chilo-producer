/**
 * 
 * :-::-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-+:-+:-+:-+:-++:-:+:-:+:-:+:-:
 * 
 * This file is part of CHiLOⓇ  - http://www.cccties.org/en/activities/chilo/
 *   CHiLOⓇ is a next-generation learning system utilizing ebooks,  aiming 
 *   at dissemination of open education.
 *                          Copyright 2015 NPO CCC-TIES
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * :-::-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-:+:-+:-+:-+:-+:-++:-:+:-:+:-:+:-:
 * 
 */
package epub3maker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static epub3maker.Util.path2str;

public class WekoMaker {

	private static Log log = LogFactory.getLog(WekoMaker.class);

	private static final String METADATA_FILENAME = "Metadata.xlsx";
	private static final String SHEET_NAME = "Metadata";

	private static final String CC3 = "Creative Commons Version 3.0";

	enum ItemType {
		INFO("CHiLO Book info"),
		OUTPUT("CHiLO Book output"),
		TOPIC("CHiLO Book topic"),
		TEST_SECTION("CHiLO Book Test section");

		String itemTypeName;

		private ItemType(String itemTypeName){
			this.itemTypeName = itemTypeName;
		}
	}

	enum Metadata {
		WEKO_URL("WEKO_URL"),
		TYPE("アイテムタイプ"),
		POS_INDEX("POS_INDEX"),
		ITEM_KEY("ITEM_KEY"),
		TITLE("タイトル"),
		TITLE_EN("タイトル(英)"),
		LANGUAGE("言語"),
		PUBLISHED("公開日"),
		KEYWORD("キーワード"),
		KEYWORD_EN("キーワード(英)"),
		DESCRIPTION("内容記述"),
		GRANULARILY("粒度"),
		AUTHOR("著者(姓+名)"),
		PUBLISHER("発行者(姓+名)"),
		EDITOR("編集者(姓+名)"),
		FILE_LINK("ファイルリンク"),
		METADATA_LANGUAGE("メタデータの記述言語"),
		FILE_FORMAT("ファイルフォーマット"),
		EDUCATIONAL_CONTEXT("想定利用環境"),
		EDUCATIONAL_TYPICAL_LEARNING_TIME("推定学習時間"),
		EDUCATIONAL_DESCRIPTION("教育情報・備考"),
		RIGHTS_COST("使用料"),
		RIGHTS_COPYRIGHT_AND_OTHER_RESTRICTIONS("著作権・利用制限条件"),
		RIGHTS_DESCRIPTION("権利関係・備考"),
		CLASSIFICATION_TAXON_PATH_TAXON("分類体系における分類項目"),
		REVISED("コンテンツ更新日時"),
		RIGHTS("0 rights(姓+名)"),
		IDENTIFIER("0 identifier"),
		IS_PART_OF("部分である"),
		EPUB("EPUB(ファイル名)"),
		EPUB2("EPUB(表示名)"),
		WEB("WEB"),
		COVER("表紙(ファイル名)"),
		COVER2("表紙(表示名)"),
		INNER_COVER("内表紙(ファイル名)"),
		INNER_COVER2("内表紙(表示名)"),
		FILE_MAIN("コンテンツ本体(ファイル名)"),
		FILE_MAIN2("コンテンツ本体(表示名)"),
		FILE_TEXT("コンテンツ本体(ファイル名)"),
		FILE_TEXT2("コンテンツ本体(表示名)"),
		FILE_VIDEO_IMAGE("コンテンツ本体(ファイル名)"),
		FILE_VIDEO_IMAGE2("コンテンツ本体(表示名)"),
		FILE_JAVASCRIPT("コンテンツ本体(ファイル名)"),
		FILE_JAVASCRIPT2("コンテンツ本体(表示名)");

		static Metadata excludes[] = {EPUB2,COVER2,INNER_COVER2,FILE_MAIN2,FILE_TEXT2,FILE_VIDEO_IMAGE2,FILE_JAVASCRIPT2};

		String columnName;

		private Metadata(String s){
			this.columnName = s;
		}
	}

	Series series;
	String seriesName;

	Metadata list[];

	Path targetDirectory;
	Workbook wb;
	Sheet sh;
	int cur_row = 0;

	WekoMaker(){}

	private void init_list(){
		Metadata all[] = Metadata.values();
		List<Metadata> ex = Arrays.asList(Metadata.excludes);
		List<Metadata> l = new ArrayList<Metadata>();
		for(Metadata m: all){
			if(!ex.contains(m)){
				l.add(m);
			}
		}
		list = l.toArray(Metadata.excludes);
	}

	public void init(Series series){
		this.series = series;
		seriesName = series.getMeta(Series.KEY_SERIES_NAME);

		init_list();
		targetDirectory = Paths.get(series.getOutputPath());
		debug("init targetDirectory=" + targetDirectory);
		initWorkbook();
		createItem(ItemType.INFO);
	}

    public void createItem(Book book) throws IOException, Epub3MakerException {
		init(book);

        for (PageSetting setting : book.getPageSettings()) {
    		init(setting);
        	String type = setting.getPageType();

        	if (type.equals(PageSetting.VALUE_KEY_PAGE_TYPE_COVER)) {
        		createItem(ItemType.OUTPUT);
        	} else if (type.equals(PageSetting.VALUE_KEY_PAGE_TYPE_DOCUMENT)) {
            	createItem(ItemType.TOPIC);
            	topicNum++;
            } else if (type.equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
            	createItem(ItemType.TEST_SECTION);
            	topicNum++;
            }
        }
    }

	public void flush() throws IOException {
		debug("flush called");
		OutputStream os = new FileOutputStream(targetDirectory.resolve(METADATA_FILENAME).toFile());
		wb.write(os);
	}

    //

	Book book;
	String volStr;
	String volTitle;
	String volTop;

	PageSetting page;
	String position;
	String textPath;

	String lastSection;
	int sectionNum;
	int topicNum;

	private void init(Book book){
		this.book = book;
		volStr = book.getVolumeStr();
		volTitle = book.get(Series.KEY_BOOKLIST_BOOK_TITLE);
		volTop = path2str(Paths.get(series.getInputPath()).toAbsolutePath());

		lastSection = null;
		sectionNum = 1;
		topicNum = 1;
	}

	private void init(PageSetting page){
		this.page = page;
		String section = page.getSection();

		if(lastSection != null && section.compareTo(lastSection) != 0){
			sectionNum++;
			topicNum = 1;
		}
		lastSection = section;

		position = String.format("%s/%s/%02d_%s", seriesName, volTitle, sectionNum, section);
		textPath = path2str(page.getTextPathForArchiveFile());
	}

	private void initWorkbook(){
		wb = new XSSFWorkbook();
		sh = wb.createSheet(SHEET_NAME);
		Row row = sh.createRow(cur_row);
		Cell cell;
		for(int i = 0;i < list.length;i++){
			cell = row.createCell(i);
			cell.setCellValue(list[i].columnName);
		}
		cur_row++;
	}

	private void createItem(ItemType itemType){
		Row row = sh.createRow(cur_row);
		Cell cell;

		for(int i = 0;i < list.length;i++){
			String value = commonValue(itemType, list[i]);

			if(value == null){
				switch(itemType){
				case INFO:         value = infoValue(list[i]); break;
				case OUTPUT:       value = outputValue(list[i]); break;
				case TOPIC:        value = topicValue(list[i]); break;
				case TEST_SECTION: value = testValue(list[i]); break;
				}
			}

			if(value != null){
				cell = row.createCell(i);
				cell.setCellValue(value);
			}
		}
		cur_row++;
	}

	//
	// value
	//
	private String commonValue(ItemType itemType, Metadata m){
		String ret = null;
		switch(m){
		case TYPE:      ret = itemType.itemTypeName; break;
		case LANGUAGE:  ret = series.getMeta(Series.KEY_LANGUAGE); break;
		case PUBLISHED: ret = series.getMeta(Series.KEY_PUBLISHED); break;
		case AUTHOR:   ret = series.getMeta(Series.KEY_AUTHOR); break;
		case PUBLISHER: ret = series.getMeta(Series.KEY_PUBLISHER); break;
		case EDITOR:    ret = series.getMeta(Series.KEY_EDITOR); break;
		case REVISED:   ret = series.getMeta(Series.KEY_REVISED); break;
		case RIGHTS:    ret = series.getMeta(Series.KEY_RIGHTS); break;
		}
		return ret;
	}

	private String infoValue(Metadata m){
		String ret = null;
		switch(m){
		case POS_INDEX:   ret = seriesName; break;
		case TITLE:       ret = seriesName; break;
		case DESCRIPTION: ret = series.getMeta(Series.KEY_SERIES_INTRODUCTION); break;
		case FILE_LINK:   ret = null; break;
		}
		return ret;
	}

	private String outputValue(Metadata m){
		String ret = null;
		switch(m){
		case POS_INDEX:   ret = seriesName + "/" + volTitle; break;
		case ITEM_KEY:    ret = volTop; break;
		case TITLE:       ret = volTitle; break;
		case DESCRIPTION: ret = book.get(Series.KEY_BOOKLIST_BOOK_SUMMARY); break;
		case IDENTIFIER:  ret = book.get(Series.KEY_BOOKLIST_ID); break;
		case IS_PART_OF:  ret = seriesName; break;
		case EPUB:        ret = path2str(series.getEpubFilePath(book).toAbsolutePath()); break;
		case EPUB2:       ret = path2str(series.getEpubFilePath(book).getFileName()); break;
		case WEB:         ret = web(textPath); break;
		case COVER:       ret = path2str(book.getCoverImagePath()); break;
		case COVER2:      ret = path2str(getFileName(book.getCoverImagePath())); break;
		case INNER_COVER: ret = commonImageFile(series.getMeta(Series.KEY_V2_COVER)); break;
		case INNER_COVER2:ret = series.getMeta(Series.KEY_V2_COVER); break;
		}
		return ret;
	}

	private String topicValue(Metadata m){
		String ret = null;
		switch(m){
		case POS_INDEX:        ret = position; break;
		case ITEM_KEY:         ret = volTop; break;
		case TITLE:            ret = topicTitle(page.getTopic()); break;
		case FILE_LINK:        ret = page.getYoutubeId(); break;
		case RIGHTS_COPYRIGHT_AND_OTHER_RESTRICTIONS:
                                       ret = cc2type(page.getCC()); break;
		case RIGHTS_DESCRIPTION:
                                       ret = cc2str(page.getCC()); break;
		case IS_PART_OF:       ret = position; break;
		case WEB:              ret = web(textPath); break;
		case FILE_MAIN:        ret = path2str(page.getObjectPath(0)); break;
		case FILE_MAIN2:       ret = page.getObject(0); break;
		case FILE_TEXT:        ret = path2str(page.getTextPath(0)); break;
		case FILE_TEXT2:       ret = path2str(getFileName(page.getTextPathForArchiveFile())); break;
		case FILE_VIDEO_IMAGE: ret = volumeImageFile(page.getVideoImage()); break;
		case FILE_VIDEO_IMAGE2:ret = page.getVideoImage(); break;
		case FILE_JAVASCRIPT:  ret = path2str(page.getJavascriptFilePath(0)); break;
		case FILE_JAVASCRIPT2: ret = path2str(getFileName(page.getJavascriptFilePath(0))); break; 
		}
		return ret;
	}

	private String testValue(Metadata m){
		String ret = null;
		switch(m){
		case POS_INDEX:        ret = position; break;
		case TITLE:            ret = topicTitle(lastSection); break;
		case FILE_LINK:        ret = page.getObject(0); break;
		case RIGHTS_COPYRIGHT_AND_OTHER_RESTRICTIONS:
                                       ret = cc2type(page.getCC()); break;
		case RIGHTS_DESCRIPTION:
                                       ret = cc2str(page.getCC()); break;
		case IS_PART_OF:       ret = position; break;
		case WEB:              ret = web(textPath); break;
		}
		return ret;
	}

	private Path getFileName(Path path){
		if(path != null){
			return path.getFileName();
		}
		return null;
	}

	private String commonImageFile(String filename){
		if(filename != null){
			filename = "common/images/" + filename;
		}
		return filename;
	}

	private String volumeImageFile(String filename){
		if(filename != null){
			filename = volStr + "/images/" + filename;
		}
		return filename;
	}

	private String topicTitle(String name){
		return String.format("%02d%02d_%s", sectionNum, topicNum, name);
	}

	private String web(String url){
		if(url != null){
			url = url.replace('/', '-');
		}
		return url;
	}

	private String cc2type(String cc){
		if(cc != null){
			cc = CC3;
		}
		return cc;
	}

	Map<String,String> ccmap = new HashMap<String,String>(){{
		put("zero",    "(なし)");
		put("by",      "Attribution");
		put("by-nc",   "Attribution-NonCommercial");
		put("by-nd",   "Attribution-NoDerivs");
		put("by-sa",   "Attribution-ShareAlike");
		put("by-nc-nd","Attribution-NonCommercial-NoDerivs");
		put("by-nc-sa","Attribution-NonCommercial-ShareAlike");
	}};

	private String cc2str(String cc){
		if(cc != null){
			cc = ccmap.get(cc);
		}
		return cc;
	}

	private void debug(String str){
		log.debug("WekoMaker: " + str);
	}
}
