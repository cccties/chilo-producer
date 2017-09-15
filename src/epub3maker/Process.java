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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xml.sax.SAXException;

import static epub3maker.Util.*;

public class Process {

	private static Log log = LogFactory.getLog(Process.class);

    static final int VOLUME_COVER_PAGE = 1;
    static final int README_PAGE = 2;
    static final int SERIES_INTRODUCTION_PAGE = 3;
    static final int BOOK_SUMMARY_PAGE = 4;
    static final int DOCUMENT_START_PAGE = 10;

    enum Document {
		CARDVIEW("cardview.xhtml","cardview.xhtml"),
		NAV("nav.xhtml","nav.xhtml"),
		MEDIA_OVERLAY("chilo-video.smil","chilo-video.smil");

		String fileName;
		String templateFileName;

		private Document(String fileName, String templateFileName){
			this.fileName = fileName;
			this.templateFileName = templateFileName;
		}
	}

    Path outputTempPath;
    XmlStringReader sReader = null;
    String lang;
    Series series;
    Book book;
    Boolean chilo_video_created = false;

    public void process(Path seriesPath, boolean doWeko) throws Exception {
    	SettingReader reader = new SettingReader(seriesPath);
        series = reader.read();
        WekoMaker weko = null;
        if(doWeko){
        	weko = new WekoMaker();
        }

        series.showMeta();

        initializeOutputDirectory();
        if(doWeko){
        	weko.init(series);
        }

        for (Map.Entry<Integer, Book> e : series.getBooks().entrySet()) {
            book = e.getValue();

            log.info("### processing vol-" + book.getVolume());

            /*
             * Page 設定を表示する（debug）
             */
            reader.showPageSettings(book);

        	initializeBook();

        	createMimeTypeFile(outputTempPath.resolve(Paths.get("mimetype")));
        	createContainerFile(outputTempPath.resolve(Paths.get("container.xml")));
            
            PageSetting[] prefaceSettings = createPrefacePageSettings();
            PageSetting[] appendixSettings = createAppendixPageSettings();

            if(prefaceSettings != null){
            	createPages(prefaceSettings, null);
            }
            createXhtmlFiles();

            if(doWeko){
            	weko.createItem(book);
            }

            List<String> authorImages = new ArrayList<String>();
            createPages(appendixSettings, authorImages);

            List<PageSetting> sortedPageSettings = new ArrayList<>(book.getPageSettings());
            sortGeneratedPages(sortedPageSettings);

            createNavigationDocument(sortedPageSettings, Document.NAV);
            createNavigationDocument(sortedPageSettings, Document.CARDVIEW);

            try {
            	chilo_video_created = createMediaOverlay(sortedPageSettings, Document.MEDIA_OVERLAY);
            } catch(Exception ex){
            	log.info("createMediaOverlay throws exception");
            	// do nothing
            }

            // http://imagedrive.github.io/spec/epub301-publications.xhtml#sec-publication-resources
            createContentOpf(authorImages);

            List<Path> targetFilePaths = getArchivingTargets(authorImages);
            List<Path> extensionPaths = getExtensionFiles();

            Epub3Archiver archiver = new Epub3Archiver();
            archiver.archive(series, book, targetFilePaths, outputTempPath, extensionPaths);
        }

        if(doWeko){
        	weko.flush();
        }
    }

    private List<Path> getExtensionFiles() throws IOException {
    	return Util.getFilePaths(Config.getExtensionBaseDir().resolve("extension"),entry -> !(Files.isDirectory(entry)));
    }

    private List<Path> getArchivingTargets(List<String> authorImages) throws IOException {
        List<Path> targetFilePaths = new ArrayList<>();

        /*
         * 生成したファイル
         */
        for (PageSetting setting : book.getPageSettings()) {
        	Path textPath = setting.getTextPathForArchiveFile();
        	targetFilePaths.add(outputTempPath.resolve(textPath));
        }

        /*
         * テキストファイル以外
         */
        Path ignoreCommonPath = Paths.get(series.getInputPath(), "common", "text");
        Path ignoreCommonPath2 = Paths.get(series.getInputPath(), "common", "web-styles");
        Path ignoreCommonPath3 = Paths.get(series.getInputPath(), "common", "authorImages");

        List<Path> commonFilePaths;
        commonFilePaths = getFilePaths(Paths.get(series.getInputPath(), "common"), 
        		entry -> !(Files.isDirectory(entry)) && 
        		!entry.startsWith(ignoreCommonPath) && 
        		!entry.startsWith(ignoreCommonPath2) && 
        		!entry.startsWith(ignoreCommonPath3));
        targetFilePaths.addAll(commonFilePaths);

        final String bookName = book.getVolumeStr();
        Path ignoreBookPath = Paths.get(series.getInputPath(), bookName, "text");
        List<Path> bookFilePaths;

        bookFilePaths = getFilePaths(Paths.get(series.getInputPath(),bookName),
        		entry -> !(Files.isDirectory(entry)) && 
        		!entry.startsWith(ignoreBookPath));
        targetFilePaths.addAll(bookFilePaths);

        /*
         * authorImages
         */
        List<Path> authorImagePaths;
        authorImagePaths = getFilePaths(Paths.get(series.getInputPath(), "common", "authorImages"), 
        		entry -> !(Files.isDirectory(entry)) && 
        		authorImages.contains(entry.getFileName().toString()));
        targetFilePaths.addAll(authorImagePaths);

        /*
         * テンプレートファイル
         */
        List<Path> commonFilePaths2 = getFilePaths(Config.getTemplateDir("images"), entry -> !(Files.isDirectory(entry)));
        targetFilePaths.addAll(commonFilePaths2);

        List<Path> commonFilePaths3 = getFilePaths(Config.getTemplateDir("scripts"), entry -> !(Files.isDirectory(entry)));
        targetFilePaths.addAll(commonFilePaths3);

        List<Path> commonFilePaths4 = getFilePaths(Config.getTemplateDir("styles"), entry -> !(Files.isDirectory(entry)));
        targetFilePaths.addAll(commonFilePaths4);

        return targetFilePaths;
    }
    
    private void initializeBook() throws Exception {
        validateSetting();

        /*
         * template から固定文字列を読み込む
         */
        lang = getSetting(Series.KEY_LANGUAGE);
        Path stringFilePath = Config.getTemplateBaseFile(lang, "strings.xml"); 
        if (!Files.exists(stringFilePath)) {
            throw new Epub3MakerException("テンプレートが見つかりません: " + stringFilePath.toString());
        }
        sReader = new XmlStringReader(stringFilePath.toString());
        sReader.read();

        /*
         * 出力ディレクトリを作成する
         */
    	outputTempPath = Paths.get(series.getOutputPath(), book.getVolumeStr() + "-temp");
        createDirectory(outputTempPath);
        cleanUpDirectory(outputTempPath);
        Files.createDirectories(outputTempPath.resolve(Paths.get("common", "text")));
    }

    private void createXhtmlFiles() throws IOException, Epub3MakerException {
        for (PageSetting setting : book.getPageSettings()) {            
            Path textPath = setting.getTextPathForArchiveFile();
            Path outputDirectory = outputTempPath.resolve(textPath.getParent());
            Files.createDirectories(outputDirectory);
            
            createPage(setting, outputTempPath.resolve(textPath)); 
        }
    }
    
    private void initializeOutputDirectory() throws IOException {
        Path targetDirectory = Paths.get(series.getOutputPath());
        Util.createDirectory(targetDirectory);
    }

    private void createNavigationDocument(List<PageSetting> sortedPages, Document doc) throws IOException {
    	String last = null;
    	List<Map<String, Object>> sections = new ArrayList<Map<String, Object>>();
    	List<Map<String, Object>> topics = null;
    	Map<String, Object> section = null;
    	
    	for (PageSetting page : sortedPages) {
    		String cur = page.getSection();
    		if (!isValueValid(cur)) {
    			continue;
    		}

    		Map<String, Object> map = new HashMap<String, Object>();
			map.put("link", path2str(page.getTextPathForArchiveFile()));
    		map.put("image-flag", "" + page.getVideoImage());
    		switch(doc){
    		case NAV:
    			map.put("id", convert2id(page.getTextPathForArchiveFile()));
    			break;
    		case CARDVIEW:
    			map.put("id", baseFilename(page.getTextPathForArchiveFile()));
    			break;
    		}

    		if (last == null || !last.equals(cur)) {
	    		section = map;
       			map.put("title", cur);
	    		map.put("video-image", "common/images/" + page.getVideoImage());
				sections.add(map);
				topics = null;
    			last = cur;
    		} else {
     			map.put("title", page.getTopic());
    			map.put("video-image", book.getVolumeStr() + "/images/" + page.getVideoImage());
    			if (topics == null) {
    				topics = new ArrayList<Map<String, Object>>();
    				section.put("topics", topics);
    			}
    			topics.add(map);
    		}
    	}

    	Content content = new Content();

//    	String fnamePrefix = String.format("vol-%03d-", book.getVolume());
//    	String coverImg = book.getCoverImage();
//    	if (coverImg != null && coverImg.length() > 0) {
//            content.put("prev-page", book.getVolumeStr() + "/text/" + fnamePrefix + "001.html");
//    	}
//        content.put("next-page", "common/text/" + fnamePrefix  + "002.html");
    	
    	content.put("sections", sections);
    	content.put("index", sReader.get("nav.title"));

        // book-list sheet
        appendBookList(content);
    	
    	Path outputFilePath;
//    	content.put("start-page", book.getVolumeStr() + "/text/" + pageFileName(book.getVolume(), DOCUMENT_START_PAGE + 1));
    	outputFilePath = outputTempPath.resolve(doc.fileName);

    	createTemplatePage(content, doc.templateFileName, outputFilePath);
    }

    private boolean createMediaOverlay(List<PageSetting> sortedPages, Document doc) throws IOException {
    	List<Map<String, Object>> pages = new ArrayList<Map<String, Object>>();

    	for (PageSetting page : sortedPages) {
    		String begin = page.getClipBegin();
    		if(Util.isValueValid(begin)){
    			Map<String, Object> map = new HashMap<String, Object>();
    			map.put("id", convert2id(page.getTextPathForArchiveFile()));
    			map.put("text", path2str(page.getTextPathForArchiveFile()));
    			map.put("video", page.getObject(0));
    			map.put("youtube-id", page.getYoutubeId());
    			map.put("clip-begin", begin);
    			map.put("clip-end", page.getClipEnd());
    			pages.add(map);
    		}
    	}

    	if(pages.isEmpty()){
    		return false;
    	}

    	Content content = new Content();
    	content.put("pages", pages);
    	
    	Path outputFilePath;
    	outputFilePath = outputTempPath.resolve(doc.fileName);

    	createTemplatePage(content, doc.templateFileName, outputFilePath);
    	return true;
    }

	private void createContentOpf(List<String> authorImages) throws Epub3MakerException, IOException, ParserConfigurationException, SAXException
    {
    	Content content = new Content();

    	setManifest(makeManifestItems(authorImages), content);
    	setSpine(makeSpineItems(), content);
    	
    	appendMeta(content);
    	appendBookList(content);
        appendAuthor(authorImages, content);

    	content.put("update", getModifiedTime());
    	
        if (book.getCoverImagePath() != null) {
            String path = convert2id(book.getCoverImagePath());
            content.put("cover-image", path.toString());
        }

        createTemplatePage(content, "contentopf.xhtml", outputTempPath.resolve(Paths.get("content.opf")));
    }

    private Map<Path, String> makeManifestItems(List<String> authorImages) throws IOException, ParserConfigurationException, SAXException {
        Map<Path, String> items = new HashMap<>();
        Path coverImage = book.getCoverImagePath();
        if(coverImage != null) {
        	coverImage = Paths.get(series.getInputPath()).resolve(book.getCoverImagePath());
        }

        for(Path path: getArchivingTargets(authorImages)) {
        	if (coverImage != null && path.equals(coverImage)) {
        		items.put(path, PageSetting.VALUE_KEY_ITEM_PROPERTY_COVER_IMAGE);
        	} else {
        		items.put(path, null);
        	}
        }

        for (PageSetting setting : book.getPageSettings()) {
            Path textPath = setting.getTextPathForArchiveFile();
            items.put(outputTempPath.resolve(textPath), setting.getItemProperty());
        }

        return items;
    }

    private void setManifest(Map<Path, String> paths, Content content) throws Epub3MakerException, IOException {
    	List<Map<String, String>> list = new ArrayList<Map<String, String>>();

    	for (Entry<Path, String> e: paths.entrySet()) {
            Path path = subtractBasePath(e.getKey());
            String href = path2str(path);
            String id = convert2id(href);
            String type = Util.getContentType(path.getFileName().toString());
            String properties = null;
            if (Util.isValueValid(e.getValue())) {
            	properties = e.getValue();
            }
            addItem(href, id, type, properties, list);
        }

        addItem(Document.NAV.fileName, "nav", list);
        addItem(Document.CARDVIEW.fileName, "svg", list);
        if(chilo_video_created){
        	addItem(Document.MEDIA_OVERLAY.fileName, null, list);
        }

        for(Path p: getExtensionFiles()){
        	String name = p.getFileName().toString();
            addItem("../" + name, name, Util.getContentType(name), null, list);
        }

        content.put("manifest-items", list);
    }

    private void addItem(String filename, String properties, List<Map<String, String>> list){
    	addItem(filename, filename, Util.getContentType(filename), properties, list);
    }

    private void addItem(String href, String id, String mediaType, String properties, List<Map<String, String>> list){
        Map<String, String> map = new HashMap<String, String>();
        map.put("href", href);
        map.put("id", id);
        map.put("media-type", mediaType);
        map.put("properties", properties);
        list.add(map);
    }

    private List<String> makeSpineItems() throws IOException, Epub3MakerException {
        List<Path> targetFilePaths = new ArrayList<>();

        /*
         * common から …vol-xxx- のファイルを集める
         */
        StringBuilder volPrefix = new StringBuilder();
        volPrefix.append(String.format("vol-%03d-", book.getVolume()));
        targetFilePaths.addAll(Util.findFilesPrefix(outputTempPath.resolve(Paths.get("common", "text")), volPrefix.toString()));
        
        final String prefix = book.getVolumeStr();
        targetFilePaths.addAll(Util.getFilePaths(outputTempPath.resolve(Paths.get(prefix, "text"))));

        Collections.sort(targetFilePaths, (a, b) -> {
        	String namea = a.getFileName().toString();
        	String nameb = b.getFileName().toString();
        	return namea.compareTo(nameb);
        });

        List<String> items = makeSpineItemsFromPathList(targetFilePaths, prefix);

        /*
         * 静的 xhtml
         */
        ArrayList<Path> staticFilePaths = new ArrayList<>();
        Path staticDir = Paths.get(series.getInputPath(), "common", "statics");
        if (Files.exists(staticDir)) {
            staticFilePaths.addAll(Util.findStaticsFilesIn(staticDir));
        }
        staticDir = Paths.get(series.getInputPath(), prefix, "statics");
        if (Files.exists(staticDir)) {
            staticFilePaths.addAll(Util.findStaticsFilesIn(staticDir));
        }
        items.addAll(makeSpineItemsFromPathList(staticFilePaths, prefix));

        /*
         * 目次
         */
        int where;
        if(book.getPageSetting(0).getPageType().equals(PageSetting.VALUE_KEY_PAGE_TYPE_COVER)){
        	where = 1;
        } else {
        	where = 0;
        }
        items.add(where, Document.CARDVIEW.fileName);

        return items;
    }
	
    private void setSpine(List<String> paths, Content content) {
        List<String> list = new ArrayList<String>();
        for (String path : paths) {
        	list.add(convert2id(path));
        }

        content.put("spine-list", list);
    }

    /**
     * @param targetFilePaths
     * @param prefix
     * @return
     * @throws Epub3MakerException
     */
    private List<String> makeSpineItemsFromPathList(List<Path> targetFilePaths, final String prefix) throws Epub3MakerException {
        List<String> items = new ArrayList<>();
        for (Path file : targetFilePaths) {
            Path p = subtractBasePath(file);
            if (p.startsWith("common") || p.startsWith(prefix)) {
                items.add(path2str(p));
            }
        }
        return items;
    }

    private static Path getBasePath(Path target) {
    	int i;
    	int count = target.getNameCount();
    	String template = Config.getTemplate();
    	for(i = 0; i < count; ++i)
    	{
    		if(target.getName(i).toString().equals("common"))
    		{
    			break;
    		}
    		else if(target.getName(i).toString().matches("^" + Book.VOLUME_PREFIX + "\\d+$"))
    		{
    			break;
    		}
    		else if(target.getName(i).toString().equals(template)){
    			break;
    		}
    	}
    	return target.subpath(0, i);
    }

    public static Path subtractBasePath(Path target) throws Epub3MakerException
    {
    	Path base = getBasePath(target);

    	if(base.getNameCount() < target.getNameCount())
    	{
    		return target.subpath(base.getNameCount(), target.getNameCount());
    	}
    	else
    	{
        	throw new Epub3MakerException("ディレクトリ構造が壊れています。" + System.lineSeparator() + target);
    	}
    }

    private void sortGeneratedPages(List<PageSetting> pages) {
        Collections.sort(pages, (a, b) -> {
        	String namea = a.getTextPathForArchiveFile().getFileName().toString();
        	String nameb = b.getTextPathForArchiveFile().getFileName().toString();
        	return namea.compareTo(nameb);
        });
    }

    private void createContainerFile(Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            bw.newLine();
            bw.write("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">");
            bw.newLine();
            bw.write("<rootfiles>");
            bw.newLine();
            bw.write("<rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>");
            bw.newLine();
            bw.write("</rootfiles>");
            bw.newLine();
            bw.write("</container>");
            bw.newLine();
        }
    }

    private void createMimeTypeFile(Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write("application/epub+zip");
        }
    }

    private void createPage(PageSetting pageSetting, Path outFilePath) throws IOException, Epub3MakerException {
    	String pageType = pageSetting.getPageType();
    	Content content;
        if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_DOCUMENT)) {
        	content = createContent(pageSetting, outFilePath, null);
        	createTemplatePage(content, "document.xhtml", outFilePath);	
        } else if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_COVER)) {
        	content = createContent(pageSetting, outFilePath, null);
        	createTemplatePage(content, "cover.xhtml", outFilePath);
        } else if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
        	content = createContent(pageSetting, outFilePath, null);
        	createTemplatePage(content, "book-test.xhtml", outFilePath);
        } else if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_INSIDE_COVER)) {
        	content = createContent(pageSetting, outFilePath, null);
        	createTemplatePage(content, "inside-cover.xhtml", outFilePath);
        }
    	
    }

    private PageSetting[] createPrefacePageSettings() throws IOException {
    	/*
    	 * check condition to create readme page
    	 */
    	String intro = getSetting(Series.KEY_SERIES_INTRODUCTION);
       	String summary = book.get(Series.KEY_BOOKLIST_BOOK_SUMMARY);
       	if(!Util.isValueValid(intro) && !Util.isValueValid(summary)) {
    		return null;
    	}

       	int vol = book.getVolume();
    	String[] templateFiles = {"readme.xhtml"};
    	String section = sReader.get("readme/section");
    	PageSetting settings[] = new PageSetting[templateFiles.length];
    	settings[0] = new PageSetting(vol, README_PAGE, "common", section, "", templateFiles[0]);

    	for (int i = 0; i < settings.length; i++) {
    		if (settings[i] != null) {
    			book.addPageSetting(settings[i]);
    		}
    	}
    	return settings;
    }

    private PageSetting[] createAppendixPageSettings() throws IOException {
    	int vol = book.getVolume();
    	//String[] templateFiles = {"section_end_cover.xhtml", "precaution.xhtml", "author.xhtml", "copyright.xhtml"};//章末情報を出す場合はこちらを使う
    	String[] templateFiles = {"author.xhtml", "copyright.xhtml"}; 
    	
    	String subject = sReader.get("appendix/section");
    	PageSetting settings[] = new PageSetting[templateFiles.length];
    	int maxPage = book.getMaxPage();
		/*//章末情報を出す場合はこちらを使う
    	settings[0] = new PageSetting(vol, ++maxPage, "common", subject, "", templateFiles[0]);
    	settings[1] = new PageSetting(vol, ++maxPage, "common", subject, sReader.get("appendix/section1"), templateFiles[1]);
    	settings[2] = new PageSetting(vol, ++maxPage, "common", subject, sReader.get("appendix/section2"), templateFiles[2]);
    	settings[3] = new PageSetting(vol, ++maxPage, "common", sReader.get("copyright"), "", templateFiles[3]);
		*/
    	settings[0] = new PageSetting(vol, ++maxPage, "common", subject, "", templateFiles[0]);
    	settings[1] = new PageSetting(vol, ++maxPage, "common", sReader.get("copyright"), "", templateFiles[1]);

    	for (int i = 0; i < settings.length; i++) {
    		book.addPageSetting(settings[i]);
    	}
    	return settings;
    }

    private void createPages(PageSetting[] settings, List<String> authorImages) throws IOException, Epub3MakerException {
    	for (int i = 0; i < settings.length; i++) {
    		Path outFileName = outputTempPath.resolve(settings[i].getTextPathForArchiveFile());
    		Content content = createContent(settings[i], outFileName, authorImages);
        	createTemplatePage(content, settings[i].getText(0), outFileName);
    	}
    	
    }
    
    private void contentSetSVGnode(Content content, String prefix, String subject,
    		String boxWidth, String boxHeight, String bgColor, 
    		boolean withRect, String fontBasePos,
    		String widthRatio, String textRatio, String align, String fontFamily, 
    		String fontStroke, String fontStrokeWidth, String fgColor, int magni) {
    	content.put(prefix + "view-box", "0 0 " + boxWidth + " " + boxHeight);
    	content.put(prefix + "width", widthRatio + "%");
        
        /*
         * rect element
         */
        if (withRect) {
        	content.put(prefix + "rect", "width=\"" + boxWidth + "\" height=\"" + boxHeight + "\" fill=\"" + bgColor + "\"");
        }
    	
        /*
         * text element
         * fontsize = min(6000 / 文字数 * 1.5, 1000) * 0.9
         * y = 1000 - (1000 - fontsize) / 2 - 50
         */
        int vwidth = Integer.parseInt(boxWidth);
        int vheight = Integer.parseInt(boxHeight);
        int basePos = Integer.parseInt(fontBasePos);
        int textRation = Integer.parseInt(textRatio);

        /*
         * デフォルトは centering
         */
        int tx = vwidth / 2;
        String textAnchor = "middle";
        if (Const.SectionTextAlignLeft.equalsIgnoreCase(align)) {
            tx = 0;
            textAnchor = "start";
        } else if (Const.SectionTextAlignRight.equalsIgnoreCase(align)) {
            tx = vwidth;
            textAnchor = "end";
        }
        
        int relativeLength = Util.stringLengthRelative(subject);
//        log.debug("relativeLength: " + relativeLength);
        
        int fontsize = 
                Math.min(relativeLength != 0 
                        ? vwidth * (10 + magni) / (relativeLength * 10) 
                        : vheight, vheight)
                    * textRation / 100;
//        log.debug("fontsize: " + fontsize);
        
        int ty = vheight - (vheight - fontsize) / 2 - basePos;

        content.put(prefix + "x", "" + tx);
        content.put(prefix + "y", "" + ty);
        content.put(prefix + "font-size", "" + fontsize);
        content.put(prefix + "font-family", fontFamily);
        content.put(prefix + "stroke", fontStroke);
        content.put(prefix + "stroke-width", fontStrokeWidth);
        content.put(prefix + "fill", fgColor);
        content.put(prefix + "text-anchor", textAnchor);
    }
    
    private void contentSetSVGnode(Content content, PageSetting pageSetting, String type) {
    	String attr;
    	int magni;
    
    	if (type.startsWith("section")) {
    		attr = pageSetting.getAttribute(PageSetting.KEY_SECTION);
    		magni = Integer.parseInt(attr.substring(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_SVG.length()));
    		contentSetSVGnode(content, type, pageSetting.getSection(),
    				Const.SectionViewBoxWidth, Const.SectionViewBoxHeight, Config.getSectionBGColor(),
    				true, Const.SectionFontBasePos, Config.getSectionWidthRatio(), Config.getSectionTextRatio(),
    				Config.getSectionTextAlign(), Config.getSectionFontFamily(), Config.getSectionFontStroke(), Config.getSectionFontStrokeWidth(), Config.getSectionFGColor(), magni);
    	} else if (type.startsWith("topic")) {
    		attr = pageSetting.getAttribute(PageSetting.KEY_TOPIC);
    		magni = Integer.parseInt(attr.substring(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_SVG.length()));
    		contentSetSVGnode(content, type, pageSetting.getTopic(), 
    				Const.TopicViewBoxWidth, Const.TopicViewBoxHeight, Config.getTopicBGColor(),
    				false, Const.TopicFontBasePos, Config.getTopicWidthRatio(), Config.getTopicTextRatio(),
    				Config.getTopicTextAlign(), Config.getTopicFontFamily(), Config.getTopicFontStroke(), Config.getTopicFontStrokeWidth(), Config.getTopicFGColor(), magni);
    	}
    }
    
    /**
     * 
     * @param series
     * @param book
     * @param pageSetting
     * @param outFilePath
     * @param authorImages null の場合は appendAuthor を呼ばない
     * @return
     * @throws IOException
     * @throws Epub3MakerException
     */
    private Content createContent(PageSetting pageSetting, Path outFilePath, List<String> authorImages) throws IOException, Epub3MakerException {
    	Content content = new Content();

    	// vol-{n} sheet
    	content.put(PageSetting.KEY_SECTION, pageSetting.getSection());
    	content.put(PageSetting.KEY_TOPIC, pageSetting.getTopic());

    	appendMain(pageSetting, content);
    	appendVideoImage(pageSetting, content);
    	appendTextPath(pageSetting, content);
    	
    	content.put(PageSetting.KEY_JAVASCRIPT_FILE, pageSetting.getJavaScriptFilePath());
    	content.put(PageSetting.KEY_YOUTUBE_ID, pageSetting.getYoutubeId());
    	
    	appendCC(pageSetting, content);
    	
    	// series-information sheet
    	appendMeta(content);
    	
    	// book-list sheet
    	appendBookList(pageSetting, content);

    	// Author
    	appendAuthor(authorImages, content);

    	// others
    	if (isValueValid(pageSetting.getSection())) {
    		contentSetSVGnode(content, pageSetting, "section-");
    	}
    	if (isValueValid(pageSetting.getTopic())) {
    		contentSetSVGnode(content, pageSetting, "topic-");
    	}

    	content.put("back-to-readme", getRelativePath(pageSetting, book.getPage(README_PAGE)));
    	content.put("document-start-page", getRelativePath(pageSetting, book.getPage(DOCUMENT_START_PAGE + 1)));
    	appendTestList(pageSetting, content);
//    	content.put("test-image", commonImagePath(Const.TEST_PAGE_IMG));
    	content.put("update", getModifiedTime2());
    	
//    	int page = pageSetting.getPage();
//    	int prev = page - 1;
//    	int next = page + 1;
//
//    	if (page != 1) {
//    		if (page == DOCUMENT_START_PAGE) {
//    			prev = BOOK_SUMMARY_PAGE;
//    		}
//    		content.put("prev-page", getRelativePath(pageSetting, book.getPage(prev)));
//    	}
//    	if (page != book.getMaxPage()) {
//    		content.put("next-page", getRelativePath(pageSetting, book.getPage(next)));
//    	}

    	content.put("file-name", outFilePath.getFileName().toString());
    	content.put("file-basename", baseFilename(outFilePath));
    	content.put("volumes", series.getBooks().size());

    	content.put("clip-begin", pageSetting.getClipBegin());
    	content.put("clip-end", pageSetting.getClipEnd());

    	return content;
    }

    private String getSetting(String key) {
    	String ret = book.get(key);
    	if(ret == null){
    		ret = series.getMeta(key);
    	}
    	return ret;
    }

    private String getInsideCover() {
    	String ret = book.get(Series.KEY_BOOKLIST_INSIDE_COVER);
    	if(ret == null){
    		ret = series.getMeta(Series.KEY_V2_COVER);
	    	if(ret == null){
	    		ret = series.getMeta(Series.KEY_V2_COVER2);
	    	}
			ret = commonImagePath(ret);
		} else {
			ret = volumeImagePath(book, ret);
		}
    	return ret;
    }

    private void appendMeta(Content content) {
    	String[] keys = {
    			Series.KEY_OUTPUT_NAME, 
    			Series.KEY_VERSION,
    			Series.KEY_LANGUAGE, 
    			Series.KEY_AUTHOR, 
    			Series.KEY_PUBLISHER, 
                Series.KEY_EDITOR, 
    			Series.KEY_PUBLISHED, 
                Series.KEY_REVISED,
    	    	Series.KEY_RIGHTS, 
    	    	Series.KEY_SERIES_NAME, 
    	    	};
    	for ( String key : keys) {
    		content.put(key.replace(':', '-'), getSetting(key));
    	}
    	/*
    	 * Series Introductionは改行に対応する
    	 */
    	String intro = getSetting(Series.KEY_SERIES_INTRODUCTION);
    	if (Util.isValueValid(intro)) {
    	    String [] intros = intro.split("\n");
    	    StringBuffer sb = new StringBuffer();
    	    for (int i = 0; i < intros.length - 1; i++) {
    	        sb.append(intros[i]);
    	        sb.append("<br />");
    	    }
    	    sb.append(intros[intros.length - 1]);
    	    content.put(Series.KEY_SERIES_INTRODUCTION, sb.toString());
    	}

    	// SettingReader#createInsideCover で使われる
    	content.put("cover",  getInsideCover());
    	content.put("inside-cover",  getInsideCover());
    }

    private void appendBookList(Content content) {
        appendBookList(null, content);
    }
    
    private void appendBookList(PageSetting pageSetting, Content content) {
    	content.put(Series.KEY_BOOKLIST_VOL, Integer.toString(book.getVolume()));
    	content.put(Series.KEY_BOOKLIST_BOOK_TITLE, book.get(Series.KEY_BOOKLIST_BOOK_TITLE));
        content.put(Series.KEY_BOOKLIST_ID, book.get(Series.KEY_BOOKLIST_ID));

        String summary = book.get(Series.KEY_BOOKLIST_BOOK_SUMMARY);
    	if (Util.isValueValid(summary)) {
    	    String [] summaries = summary.split("\n");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < summaries.length - 1; i++) {
                sb.append(summaries[i]);
                sb.append("<br />");
            }
            sb.append(summaries[summaries.length - 1]);
            content.put(Series.KEY_BOOKLIST_BOOK_SUMMARY, sb.toString());
    	}

    	if (pageSetting != null && (pageSetting.isCommunity() || pageSetting.getPage() == README_PAGE)) {
            content.put(Series.KEY_BOOKLIST_COMMUNITY_URL, book.get(Series.KEY_BOOKLIST_COMMUNITY_URL));
        }
    }
    
	private void appendMain(PageSetting pageSetting, Content content) throws Epub3MakerException {
		Path archiveTextPath = pageSetting.getTextPathForArchiveFile();
		if (pageSetting.getPageType().equals("test")) {
		    if (Util.isValueValid(pageSetting.getObject(0))) {
		        content.put("main", pageSetting.getObject(0));
		    } else {
		        throw new Epub3MakerException("page-type test で main が未設定です");
		    }
			content.put("main-type", pageSetting.getAttribute(PageSetting.KEY_OBJECT));
		} else {
			Path objectValue = pageSetting.getObjectPath(0);
			if (objectValue != null && isValueValid(objectValue.toString())) {
				String objectPath = path2str(archiveTextPath.getParent().relativize(objectValue));
				content.put("main", objectPath);
				content.put("main-type", pageSetting.getAttribute(PageSetting.KEY_OBJECT));
			}
		}
	}
	
	private void appendVideoImage(PageSetting pageSetting, Content content) {
		String videoFileName = pageSetting.getVideoImage(); 
		if (isValueValid(videoFileName)) {
			String imageFileName = volumeImagePath(book, videoFileName);
			if (isValueValid(imageFileName)) {
				content.put(PageSetting.KEY_VIDEO_IMAGE, imageFileName);
				Path absFilePath= Paths.get(series.getInputPath(), book.getVolumeStr(), "images", imageFileName);
				int imageFileSizeType = imageFileSizeType(absFilePath);
				content.put("video-width", "320");
				if (imageFileSizeType == IMAGE_SIZE_16_9) {
					content.put("video-height", "180");
				} else {
					content.put("video-height", "240");
				}
			}
		}
	}

	private void appendTextPath(PageSetting pageSetting, Content content) throws IOException {
		Path textRelativePath = pageSetting.getTextPath(0);
		if (textRelativePath != null) {
			Path textPath = Paths.get(series.getInputPath()).resolve(textRelativePath);
			if (textPath.toFile().isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textPath.toFile()), "UTF-8"));
				StringWriter sr = new StringWriter();
//                StringWriter plane = new StringWriter();
//                String regex = "<.+?>";
				String str;
				while ((str = br.readLine()) != null) {
					sr.write(str + "\n");
//					plane.write(str.replaceAll(regex, "").trim() + "\n");
				}
				content.put(PageSetting.KEY_TEXT, sr.toString());
				sr.close();
				br.close();
			}
		}
    }
    
    private void appendCC(PageSetting pageSetting, Content content) throws IOException {
    	
    	String cc = pageSetting.getCC(); 
    	if (cc == null) {
    		content.put("cc-link", null);
    	} else {
    		content.put("cc-link", sReader.get("cclink-" + cc));
    		content.put("cc-alt", "cc-" + cc);
    	}
    }

	private void appendAuthor(List<String> authorImages, Content content) throws IOException, Epub3MakerException {
		AuthorReader ar = new AuthorReader(Paths.get(series.getInputPath(), "authors.xlsx"));
		ar.read();
		String author = getSetting(Series.KEY_AUTHOR);
		if (!isValueValid(author)) {
			return;
		}
		
		if (!ar.isAuthorExist(author)) {
		    throw new Epub3MakerException("著者が存在しません: " + author);
		}
		
		String picture = ar.getPicture(author);
		content.put("picture", Util.authorImagePath(picture)); 
		/*
		 * 覚えて置く
		 */
		if(authorImages != null && Util.isValueValid(picture)) {
			if(!authorImages.contains(picture)){
				authorImages.add(picture);
			}
		}
		content.put("organization", ar.getOrganization(author));
		content.put("author-name", ar.getName(author));
		content.put("author-name2", ar.getName2(author));

		List<Map<String, String>> list = new ArrayList<>();
		List<String> addTitles = ar.getAdditionalTitles(author);
		List<String> addValues = ar.getAdditionalValues(author);
		for (int i = 0; i < addTitles.size(); i++) {
			String title = addTitles.get(i);
			String value = addValues.get(i);

			if (title == null && value == null)
				continue;

			Map<String, String> map = new HashMap<>();

			if (title == null) {
				map.put("title", "");
			} else {
				map.put("title", title);
			}
			if (value == null) {
				map.put("content", "");
			} else {
				value = value.replaceAll("\r", "");
				value = value.replaceAll("\n", "<br/>\n");
				map.put("content", value);
			}
			list.add(map);
		}
		content.put("introductionList", list);
	}

	private void appendTestList(PageSetting pageSetting, Content content) {
		List<Map<String, String>> list = new ArrayList<>();
		for (PageSetting setting : book.getPageSettings()) {
			if (setting.getPageType().equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
				Map<String, String> map = new HashMap<>();
				map.put("title", setting.getSettings().get(PageSetting.KEY_SECTION).get(0).get(PageSetting.KEY_ATTR_VALUE));
				map.put("link", getRelativePath(pageSetting, setting));
				list.add(map);
			}
		}
		content.put("testList", list);
	}

    private void createTemplatePage(Content content, String templateFileName, Path outFilePath) throws IOException {
    	// template name
    	content.put("template", Config.getTemplate());

    	Properties p = new Properties();
    	p.setProperty("input.encoding", "UTF-8");
    	p.setProperty("output.encoding", "UTF-8");
    	p.setProperty("file.resource.loader.path", Config.getTemplateBaseDir());
    	Velocity.init(p);
    	
    	VelocityContext context = content.getVelocityContext();
    	
    	org.apache.velocity.Template template = Velocity.getTemplate(Config.getTemplateFile(lang, templateFileName).toString(), "UTF-8");
    	
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilePath.toFile()), "UTF-8"));
    	template.merge(context, bw);
    	bw.close();
    }
    
    /**
     * 必須項目
     * identifier
     * language(指定した言語フォルダがなくてもエラー)
     * author(指定した著者がなくてもエラー)
     * published
     * series-name
     * @return
     * @throws Epub3MakerException 
     */
    private void validateSetting() throws Epub3MakerException {
        String meta;
        boolean isLanguage = true,
                isAuthor = true,
                isPublished = true,
                isSeriesName = true,
                isAllValid = true;
        
        meta = getSetting(Series.KEY_LANGUAGE);
        if (!Util.isValueValid(meta)) {
            isLanguage = false;
            isAllValid = false;
        }
        
        meta = getSetting(Series.KEY_AUTHOR);
        if (!Util.isValueValid(meta)) {
            isAuthor = false;
            isAllValid = false;
        }
        
        meta = getSetting(Series.KEY_PUBLISHED);
        if (!Util.isValueValid(meta)) {
            isPublished = false;
            isAllValid = false;
        }

        meta = getSetting(Series.KEY_SERIES_NAME);
        if (!Util.isValueValid(meta)) {
            isSeriesName = false;
            isAllValid = false;
        }

        if (!isAllValid) {
            StringBuilder msg = new StringBuilder();
            msg.append(book.getVolumeStr() + ": ");
            msg.append("必須項目が未設定です\n");
            if (!isLanguage) {
                msg.append("\t" + Series.KEY_LANGUAGE + "\n");
            }
            if (!isAuthor) {
                msg.append("\t" + Series.KEY_AUTHOR + "\n");
            }
            if (!isPublished) {
                msg.append("\t" + Series.KEY_PUBLISHED + "\n");
            }
            if (!isSeriesName) {
                msg.append("\t" + Series.KEY_SERIES_NAME + "\n");
            }
            throw new Epub3MakerException(msg.toString());
        }
    }

    private String convert2id(String str){
    	if(str != null){
    		str = str.replaceAll("/", "-");
    	}
    	return str;
    }

    private String convert2id(Path path){
    	String str = path2str(path);
    	return convert2id(str);
    }

    private String baseFilename(Path path){
    	return path.getFileName().toString().replace(".xhtml", "");
    }
}
