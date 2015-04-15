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

import static epub3maker.Util.IMAGE_SIZE_16_9;
import static epub3maker.Util.commonImagePath;
import static epub3maker.Util.getFilePaths;
import static epub3maker.Util.getModifiedTime;
import static epub3maker.Util.getModifiedTime2;
import static epub3maker.Util.getRelativePath;
import static epub3maker.Util.imageFileSizeType;
import static epub3maker.Util.infoPrintln;
import static epub3maker.Util.initializeDirectory;
import static epub3maker.Util.isPublishHtml;
import static epub3maker.Util.isValueValid;
import static epub3maker.Util.pageFileName;
import static epub3maker.Util.volumeImagePath;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.xml.sax.SAXException;

public class Epub3MakerV2 extends Epub3Maker {
    static final int VOLUME_COVER_PAGE = 1;
    static final int README_PAGE = 2;
    static final int SERIES_INTRODUCTION_PAGE = 3;
    static final int BOOK_SUMMARY_PAGE = 4;
    static final int DOCUMENT_START_PAGE = 10;

    XmlStringReader sReader = null;
    final String defaultLang = "ja";
    String lang;

    public void process(Path courseDir) throws Exception {
        /*
         * courseDir の構成は以下の通り． courseBaseDir/courseDir/meta.xlsx courseBaseDir
         * の下に複数のcourseDirは許容する． ひとつのコースに対してはひとつの meta しか許容しない．
         * したがって，courseDirが指定されれば meta.xlsx ファイル名は指定される 必要は無い．
         *
         * 2014/09/14 by tueda
         */
    	CourseSettingReader reader;
    	if (Config.getCourseVersion() == 1) {
    		reader = new CourseSettingReaderFromXlsx(courseDir);
    	} else {
    		reader = new CourseV2SettingReaderFromXlsx(courseDir);
    	}
        Course course = reader.read();

        showMeta(course);

        /*
         * Locale にしたがった固定文字列を読み込む
         */
//        Config.localStrings = ResourceBundle.getBundle("strings",
//                new Locale.Builder().setLanguage(course.getMeta(Course.KEY_LANGUAGE)).build());
//        Util.infoPrintln(LogLevel.LOG_DEBUG, "Locale: " + Config.localStrings.getString("nav.title"));
//        
        validateSeriesInformation(course);
        
        lang = course.getMeta(Course.KEY_LANGUAGE);
        Path stringFilePath = Paths.get(Config.getCourseBaseDir(), "common", "templates", lang, "strings.xml"); 
        if (!Files.exists(stringFilePath)) {
            throw new Epub3MakerException("テンプレートが見つかりません: " + lang);
        }
        sReader = new XmlStringReader(stringFilePath.toString());
        sReader.read();

        /*
         * Page 設定を表示する（debug）
         */
        showPageSettings(course);

        initializeOutputDirectory(course);

        for (Map.Entry<Integer, Volume> e : course.getVolumes().entrySet()) {
            Volume volume = e.getValue();
            
        	initializeDirectories(course, e.getKey());

        	createMimeTypeFile(outputTempDirectory.resolve(Paths
        			.get("mimetype")));
        	createContainerFile(outputTempDirectory.resolve(Paths
        			.get("container.xml")));
            
            infoPrintln(LogLevel.LOG_DEBUG, "Epub3Maker#process: ### processing vol-" + volume.getVolume());
            
            Epub3PackageDocument packageDocument = new Epub3PackageDocumentV2(
                    outputTempDirectory.resolve(Paths.get("content.opf")));
            this.setPackageDocumentMetadatas(packageDocument, course, volume);

            PageSetting[] prefaceSettings = createPrefacePageSettings(course, volume);
            PageSetting[] appendixSettings = createAppendixPageSettings(course, volume);

            createPrefaces(course, volume, prefaceSettings); 
            createXhtmlFiles(course, volume);

            List<String> authorImages = new ArrayList<String>();
            createAppendixes(course, volume, appendixSettings, authorImages);

            List<PageSetting> sortedPageSettings = new ArrayList<>(volume.getPageSettings());
            sortGeneratedPages(sortedPageSettings);

            createNav(course, volume, sortedPageSettings);
            
            // http://imagedrive.github.io/spec/epub301-publications.xhtml#sec-publication-resources
            if (!isPublishHtml()) {
            	createContentOpf(course, volume, authorImages);
            }

            Set<Path> targetFilePaths = getArchivingTargetsV2(course, volume, authorImages);

            Epub3Archiver archiver = new Epub3Archiver();
            if (isPublishHtml()) {
            	archiver.gatherFiles(course, volume, new ArrayList<Path>(targetFilePaths), outputTempDirectory.toString());
            } else {
            	archiver.archive(course, volume, new ArrayList<Path>(targetFilePaths), outputTempDirectory.toString());
            }
        }
    }

    protected Set<Path> getArchivingTargetsV2(Course course, Volume volume, List<String> authorImages) throws IOException {
        Set<Path> targetFilePaths;
        targetFilePaths = new HashSet<>();
        Path inputPath = Paths.get(course.getMeta(Course.KEY_INPUT_PATH));
        for (PageSetting setting : volume.getPageSettings()) {
        	Path textPath = setting.getTextPathForArchiveFile();
        	targetFilePaths.add(outputTempDirectory.resolve(textPath));
        }

        {
        	Path coverPagePath = volume.getPageSettings().get(0)
        			.getCoverPagePath();
        	if (coverPagePath != null && !coverPagePath.equals(Paths.get(""))) {
        		targetFilePaths.add(inputPath.resolve(coverPagePath));
        	}
        }

        Path ignoreCommonPath = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common", "text");
        Path ignoreCommonPath2, ignoreCommonPath3;
        List<Path> commonFilePaths;
        if (isPublishHtml()) {
        	ignoreCommonPath2 = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common", "styles");
            ignoreCommonPath3 = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common", "videos");
            commonFilePaths = getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common"), 
                    entry -> !(Files.isDirectory(entry)) && 
                    !entry.startsWith(ignoreCommonPath) && 
                    !entry.startsWith(ignoreCommonPath2) &&
                    !entry.startsWith(ignoreCommonPath3));
        } else {
        	ignoreCommonPath2 = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common", "web-styles");
            commonFilePaths = getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common"), 
                    entry -> !(Files.isDirectory(entry)) && 
                    !entry.startsWith(ignoreCommonPath) && 
                    !entry.startsWith(ignoreCommonPath2));
        }
        targetFilePaths.addAll(commonFilePaths);

        final String volumeName = Volume.KEY_VOLUME_PREFIX + volume.getVolume();
        Path ignoreVolumePath = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), volumeName, "text");
        Path ignoreVolumePath2;
        List<Path> volumeFilePaths;
        if (isPublishHtml()) {
            ignoreVolumePath2 = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), volumeName, "videos");
            volumeFilePaths = getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), volumeName), 
                    entry -> !(Files.isDirectory(entry)) && 
                    !entry.startsWith(ignoreVolumePath) &&
                    !entry.startsWith(ignoreVolumePath2));
        } else {
            volumeFilePaths = getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), 
                    volumeName), entry -> !(Files.isDirectory(entry)) && 
                    !entry.startsWith(ignoreVolumePath));
        }
        targetFilePaths.addAll(volumeFilePaths);

        List<Path> commonFilePaths2 = getFilePaths(Paths.get(Config.getCourseBaseDir(), "common", "images"), entry -> !(Files.isDirectory(entry)));
        targetFilePaths.addAll(commonFilePaths2);

        /*
         * 著者画像は使用したものだけコピー
         */
        List<Path> authorFilePaths = getFilePaths(Paths.get(Config.getCourseBaseDir(), "common", "authorImages"), entry -> !(Files.isDirectory(entry)));
        List<Path> validAuthorFilePaths = new ArrayList<Path>();
        for (Path path : authorFilePaths) {
//            Util.infoPrintln(LogLevel.LOG_DEBUG, "hoge: " + path.getFileName());
            for (String authorImage : authorImages) {
                if (authorImage.equals(path.getFileName().toString())) {
                    validAuthorFilePaths.add(path);
                }
            }
        }
        targetFilePaths.addAll(validAuthorFilePaths);

        if (isPublishHtml()) {
            List<Path> commonFilePaths3 = getFilePaths(Paths.get(Config.getCourseBaseDir(), "common", "scripts"), entry -> !(Files.isDirectory(entry)));
            targetFilePaths.addAll(commonFilePaths3);
        }

        return targetFilePaths;
    }
    
    protected Map<Path, String> makeItemsListForManifestV2(Course course, Volume volume, List<String> authorImages)
            throws IOException, ParserConfigurationException, SAXException {
        Map<Path, String> items = new HashMap<>();

        Path inputPath = Paths.get(course.getMeta(Course.KEY_INPUT_PATH));

        items.putAll(toMap(this.getArchivingTargetsV2(course, volume, authorImages), course, volume));

        for (PageSetting setting : volume.getPageSettings()) {
            Path textPath = setting.getTextPathForArchiveFile();
            items.put(outputTempDirectory.resolve(textPath), setting.getItemProperty());
        }

        Path coverPagePath = volume.getPageSettings().get(0)
                .getCoverPagePath();
        if (coverPagePath != null && !coverPagePath.equals(Paths.get(""))) {
            Path p = inputPath.resolve(coverPagePath);
            String property = null;
            if(hasSvg(p))
            {
                property = "svg";
            }
            items.put(p, property);

        }

        return items;
    }

    void initializeDirectories(Course course, int vol) throws IOException {
    	if (isPublishHtml()) {
            outputTempDirectory = Paths.get(
                    course.getMeta(Course.KEY_OUTPUT_PATH), Volume.KEY_VOLUME_PREFIX + vol + "-html");
    	} else {
    		outputTempDirectory = Paths.get(
    				course.getMeta(Course.KEY_OUTPUT_PATH), Volume.KEY_VOLUME_PREFIX + vol + "-temp");
    	}

        initializeDirectory(outputTempDirectory);

        Files.createDirectories(outputTempDirectory.resolve(Paths.get("common", "text")));
    }

    protected void createXhtmlFiles(Course course, Volume volume) throws IOException, Epub3MakerException {
        for (PageSetting setting : volume.getPageSettings()) {
//            if(setting.getTextPathsSize() != setting.getObjectPathsSize())
//            {
//                throw new Epub3MakerException(course.getMeta("Course ID:" + Course.KEY_COURSE_ID) + "vol: " + volume.getVolume() +
//                        "page: " + setting.getPage() + System.lineSeparator() +
//                        "objectとtextの行の数が一致しません。objectとtextはセットで追加していくようにしてください。");
//            }
            
            Path textPath = setting.getTextPathForArchiveFile();
            Path outputDirectory = outputTempDirectory.resolve(textPath
                    .getParent());
            Files.createDirectories(outputDirectory);
            
            createPage(course, volume, setting, outputTempDirectory.resolve(textPath)); 
        }
    }
    
    void createNav(Course course, Volume volume, List<PageSetting> sortedPages) throws IOException {
    	String curChapter = null;
    	List<Map<String, Object>> chapters = new ArrayList<Map<String, Object>>();
    	List<Map<String, Object>> sections = null;
    	Map<String, Object> chapterMap = null;
    	
    	for (PageSetting page : sortedPages) {
    		String chapter = page.getChapter();
    		if (!isValueValid(chapter)) {
    			continue;
    		}
    		if (curChapter == null || !curChapter.equals(chapter)) {
    			if (chapterMap != null) {
    				chapterMap.put("sections", sections);
    				sections = null;
    				chapters.add(chapterMap);
    				chapterMap = null;
    			}
    			chapterMap = new HashMap<String, Object>();
    			chapterMap.put("link", page.getTextPathForArchiveFile().toString().replaceAll("\\\\", "/"));
    			chapterMap.put("title", chapter);
    			curChapter = chapter;
    			continue;
    		}
    		Map<String, Object> sectionMap = new HashMap<String, Object>();
    		sectionMap.put("link", page.getTextPathForArchiveFile().toString().replaceAll("\\\\", "/"));
    		sectionMap.put("title", page.getSection());
    		if (sections == null) {
    			sections = new ArrayList<Map<String, Object>>();
    		}
    		sections.add(sectionMap);
    	}
		if (chapterMap != null) {
			chapterMap.put("sections", sections);
			chapters.add(chapterMap);
			chapterMap = null;
		}

    	Content content = new Content();

    	String fnamePrefix = String.format("vol-%03d-", volume.getVolume());
    	content.put("prev-page", volume.getVolumeStr() + "/text/" + fnamePrefix + "001.html");
        content.put("next-page", "common/text/" + fnamePrefix  + "002.html");
    	
    	content.put("chapters", chapters);
    	content.put("index", sReader.get("nav.title"));
    	content.put(Course.KEY_GOOGLE_ANALYTICS_ID, course.getMeta(Course.KEY_GOOGLE_ANALYTICS_ID));

    	if (isPublishHtml()) {
    	    // series-information sheet
    	    appendMeta(course, content);
            content.put("file-name", "nav.html");
    	}
    	
        // book-list sheet
        appendBookList(course, volume, content);
    	
    	Path outputFileName;
    	content.put("start-page", Volume.KEY_VOLUME_PREFIX + "1/text/" + pageFileName(1, DOCUMENT_START_PAGE + 1));
    	if (isPublishHtml()) {
    		outputFileName = outputTempDirectory.resolve(NAVIGATION_DOCUMENT_FILE_NAME.replace("xhtml", "html"));
    	} else {
    		outputFileName = outputTempDirectory.resolve(NAVIGATION_DOCUMENT_FILE_NAME);
    	}
    	createTemplatePage(content, "nav.xhtml", outputFileName);
    }
    
    void createContentOpf(Course course, Volume volume, List<String> authorImages) throws Epub3MakerException, IOException, ParserConfigurationException, SAXException
    {
        Epub3PackageDocumentV2 packageDocument = new Epub3PackageDocumentV2(
                outputTempDirectory.resolve(Paths.get("content.opf")));
        setPackageDocumentMetadatas(packageDocument, course, volume);
	
    	Content content = new Content();
    	packageDocument.setManifest(
            makeItemsListForManifestV2(course, volume, authorImages), Const.mediaTypes, content);
    	
    	packageDocument.setSpine(makeItemrefsListForSpine(course, volume), content);
    	
    	appendMeta(course, content);
    	appendBookList(course, volume, content);
        // Author
        AppendAuthor(course, content, authorImages);

    	content.put("update", getModifiedTime());
    	
        PageSetting firstPage = volume.getPageSettings().get(0);
        if (firstPage.getCoverImagePath() != null) {
            String path = firstPage.getCoverImagePath().toString()
                    .replaceAll("\\\\", "-");
            content.put("cover-image", path.toString());
        }
    	createTemplatePage(content, "contentopf.xhtml", packageDocument.getOPFPath());
    }

	List<String> makeItemrefsListForSpine(Course course, Volume volume)
            throws IOException, Epub3MakerException {
		
		List<String> items = super.makeItemrefsListForSpine(course, volume);

        String cover = items.get(1); // 1ページ目はVolume Cover
        items.remove(1);
        
        items.add(0, cover); // もくじの前に移動

        return items;
    }
	
    protected void createPage(Course course, Volume volume,
            PageSetting pageSetting, Path outFilePath) throws IOException, Epub3MakerException {
    	String pageType = pageSetting.getPageType();
    	Content content;
        if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_DOCUMENT)) {
        	content = createContent(course, volume, pageSetting, outFilePath, null);
        	createTemplatePage(content, "document.xhtml", outFilePath);	
        } else if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_COVER)) {
        	content = createContent(course, volume, pageSetting, outFilePath, null);
        	createTemplatePage(content, "cover.xhtml", outFilePath);
        } else if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
        	content = createContent(course, volume, pageSetting, outFilePath, null);
        	createTemplatePage(content, "volume-test.xhtml", outFilePath);
        } else if (pageType.equals(PageSetting.VALUE_KEY_PAGE_TYPE_SECTION_COVER)) {
        	content = createContent(course, volume, pageSetting, outFilePath, null);
        	createTemplatePage(content, "section-cover.xhtml", outFilePath);
        }
    	
    }

    PageSetting[] createPrefacePageSettings(Course course, Volume volume) throws IOException {
    	int vol = volume.getVolume();
    	String[] templateFiles = {"readme.xhtml", "series_introduction.xhtml", "book_summary.xhtml"};
    	String subject = sReader.get("readme/chapter");
    	PageSetting settings[] = new PageSetting[templateFiles.length];
    	settings[0] = new PageSetting(vol, README_PAGE, "common", subject, "", templateFiles[0]);
        settings[1] = new PageSetting(vol, SERIES_INTRODUCTION_PAGE, "common", subject, sReader.get("readme/section1"), templateFiles[1]);
        String volStr = volume.getVolumeStr();
        String bookSummary = course.bookBookSummary(volStr);
        settings[2] = null;
        if (isValueValid(bookSummary)) {
        	settings[2] =new PageSetting(vol, BOOK_SUMMARY_PAGE, "common", subject, sReader.get("readme/section2"), templateFiles[2]);
        }
    	for (int i = 0; i < settings.length; i++) {
    		if (settings[i] != null) {
    			volume.getPageSettings().add(settings[i]);
    		}
    	}
    	return settings;
    }
    	
    void createPrefaces(Course course, Volume volume, PageSetting[] settings) throws IOException, Epub3MakerException {
    	for (int i = 0; i < settings.length; i++) {
    		if (settings[i] != null) {
    			Path outFileName = outputTempDirectory.resolve(settings[i].getTextPathForArchiveFile());
    			Content content = createContent(course, volume, settings[i], outFileName, null);
    			createTemplatePage(content, settings[i].getText(0), outFileName);
    		}
    	}
    }

    PageSetting[] createAppendixPageSettings(Course course, Volume volume) throws IOException {
    	int vol = volume.getVolume();
    	String[] templateFiles = {"section_end_cover.xhtml", "precaution.xhtml", "author.xhtml", "copyright.xhtml"};
    	String subject = sReader.get("appendix/chapter");
    	PageSetting settings[] = new PageSetting[templateFiles.length];
    	int maxPage = volume.getMaxPage();
    	settings[0] = new PageSetting(vol, ++maxPage, "common", subject, "", templateFiles[0]);
    	settings[1] = new PageSetting(vol, ++maxPage, "common", subject, sReader.get("appendix/section1"), templateFiles[1]);
    	settings[2] = new PageSetting(vol, ++maxPage, "common", subject, sReader.get("appendix/section2"), templateFiles[2]);
    	settings[3] = new PageSetting(vol, ++maxPage, "common", sReader.get("copyright"), "", templateFiles[3]);
    	for (int i = 0; i < settings.length; i++) {
    		volume.getPageSettings().add(settings[i]);
    	}
    	return settings;
    }

    void createAppendixes(Course course, Volume volume, PageSetting[] settings, List<String> authorImages) throws IOException, Epub3MakerException {
    	for (int i = 0; i < settings.length; i++) {
    		Path outFileName = outputTempDirectory.resolve(settings[i].getTextPathForArchiveFile());
    		Content content = createContent(course, volume, settings[i], outFileName, authorImages);
        	createTemplatePage(content, settings[i].getText(0), outFileName);
    	}
    	
    }
    
    void contentSetSVGnode(Content content, String prefix, String subject,
    		String boxWidth, String boxHeight, String bgColor, 
    		boolean withRect, String fontBasePos,
    		String widthRatio, String textRatio, String align, String fontFamily, 
    		String fontStroke, String fgColor, int magni) {
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
        if (Const.ChapterTextAlignLeft.equalsIgnoreCase(align)) {
            tx = 0;
            textAnchor = "start";
        } else if (Const.ChapterTextAlignRight.equalsIgnoreCase(align)) {
            tx = vwidth;
            textAnchor = "end";
        }
        
        int relativeLength = Util.stringLengthRelative(subject);
        Util.infoPrintln(LogLevel.LOG_DEBUG, "relativeLength: " + relativeLength);
        
        int fontsize = 
                Math.min(relativeLength != 0 
                        ? vwidth * (10 + magni) / (relativeLength * 10) 
                        : vheight, vheight)
                    * textRation / 100;
        Util.infoPrintln(LogLevel.LOG_DEBUG, "fontsize: " + fontsize);
        
        int ty = vheight - (vheight - fontsize) / 2 - basePos;

        content.put(prefix + "x", "" + tx);
        content.put(prefix + "y", "" + ty);
        content.put(prefix + "font-size", "" + fontsize);
        content.put(prefix + "font-family", fontFamily);
        content.put(prefix + "stroke", fontStroke);
        content.put(prefix + "fill", fgColor);
        content.put(prefix + "text-anchor", textAnchor);
    }
    
    void contentSetSVGnode(Content content, PageSetting pageSetting, String type) {
    	String attr;
    	int magni;
    
    	if (type.startsWith("chapter")) {
    		attr = pageSetting.getAttribute(PageSetting.KEY_SUBJECT);
    		magni = Integer.parseInt(attr.substring(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_SVG.length()));
    		contentSetSVGnode(content, type, pageSetting.getChapter(),
    				Const.ChapterViewBoxWidth, Const.ChapterViewBoxHeight, Config.getChapterBGColor(),
    				true, Const.ChapterFontBasePos, Config.getChapterWidthRatio(), Config.getChapterTextRatio(),
    				Config.getChapterTextAlign(), Config.getChapterFontFamily(), Config.getChapterFontStroke(), Config.getChapterFGColor(), magni);
    	} else if (type.startsWith("section")) {
    		attr = pageSetting.getAttribute(PageSetting.KEY_SUBSUBJECT);
    		magni = Integer.parseInt(attr.substring(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_SVG.length()));
    		contentSetSVGnode(content, type, pageSetting.getSection(), 
    				Const.SectionViewBoxWidth, Const.SectionViewBoxHeight, Config.getSectionBGColor(),
    				false, Const.SectionFontBasePos, Config.getSectionWidthRatio(), Config.getSectionTextRatio(),
    				Config.getSectionTextAlign(), Config.getSectionFontFamily(), Config.getSectionFontStroke(), Config.getSectionFGColor(), magni);
    	}
    }
    
    /**
     * 
     * @param course
     * @param volume
     * @param pageSetting
     * @param outFilePath
     * @param authorImages null の場合は appendAuthor を呼ばない
     * @return
     * @throws IOException
     * @throws Epub3MakerException
     */
    Content createContent(Course course, Volume volume, PageSetting pageSetting, Path outFilePath, List<String> authorImages) throws IOException, Epub3MakerException 
    {
    	Content content = new Content();
    	
    	// vol-{n} sheet
    	content.put(PageSetting.KEY_SUBJECT, pageSetting.getChapter());
    	content.put(PageSetting.KEY_SUBSUBJECT, pageSetting.getSection());

    	appendMain(pageSetting, content);
    	
    	appendVideoImage(pageSetting, course, content);

    	appendTextPath(pageSetting, course, content);
    	
    	content.put(PageSetting.KEY_JAVASCRIPT_FILE, pageSetting.getJavaScriptFilePath());
    	
    	content.put(PageSetting.KEY_YOUTUBE_ID, pageSetting.getYoutubeId());
    	
    	appendCC(pageSetting, content);
    	
    	// series-information sheet
    	appendMeta(course, content);
    	
    	// book-list sheet
    	appendBookList(course, volume, pageSetting, content);

    	// Author
    	AppendAuthor(course, content, authorImages);

    	// others
    	if (isValueValid(pageSetting.getChapter())) {
    		contentSetSVGnode(content, pageSetting, "chapter-");
    	}
    	if (isValueValid(pageSetting.getSection())) {
    		contentSetSVGnode(content, pageSetting, "section-");
    	}
    	Path readme = volume.getPage(README_PAGE).getTextPathForArchiveFile();
    	content.put("back-to-readme", getRelativePath(pageSetting, volume.getPage(README_PAGE)));
    	content.put("document-start-page", getRelativePath(pageSetting, volume.getPage(DOCUMENT_START_PAGE + 1)));
    	AppendTestList(pageSetting, volume, content);
    	content.put("test-image", commonImagePath(Const.TEST_PAGE_IMG));
    	content.put("update", getModifiedTime2());

    	// book summary があるかどうかチェックする
        String volStr = volume.getVolumeStr();
        String bookSummary = course.bookBookSummary(volStr);
        boolean isBookSummary = false;
        if (Util.isValueValid(bookSummary)) {
            isBookSummary = true;
        }
    	
    	int page = pageSetting.getPage();
    	int prev = page - 1;
    	int next = page + 1;

        if (isPublishHtml()) {
//            Util.infoPrintln(LogLevel.LOG_DEBUG, "Link: " + pageSetting.getChapter() + " page : " + page);
            if (page == README_PAGE) {
//                Util.infoPrintln(LogLevel.LOG_DEBUG, "hoge!");
                content.put("prev-page", "../../nav.html");
            } else if (page != VOLUME_COVER_PAGE) {
                if (page == DOCUMENT_START_PAGE) {
                    prev = SERIES_INTRODUCTION_PAGE;
                    if (isBookSummary) {
                        prev = BOOK_SUMMARY_PAGE;
                    }
                }
                content.put("prev-page", getRelativePath(pageSetting, volume.getPage(prev)));
//                Util.infoPrintln(LogLevel.LOG_DEBUG, "prev: " + getRelativePath(pageSetting, volume.getPage(prev)));
            }
            if (page != volume.getMaxPage()) {
                if ((page == SERIES_INTRODUCTION_PAGE && !isBookSummary)
                        || page == BOOK_SUMMARY_PAGE) {
                    next = DOCUMENT_START_PAGE;
                }
                if (page != VOLUME_COVER_PAGE) {
                    content.put("next-page", getRelativePath(pageSetting, volume.getPage(next)));
                } else {
                    content.put("next-page", "../../nav.html");
                }
            }
        } else {
            if (page != 1) {
                if (page == DOCUMENT_START_PAGE) {
                    prev = BOOK_SUMMARY_PAGE;
                }
                content.put("prev-page", getRelativePath(pageSetting, volume.getPage(prev)));
            }
            if (page != volume.getMaxPage()) {
                content.put("next-page", getRelativePath(pageSetting, volume.getPage(next)));
            }
        }
    	content.put("file-name", outFilePath.getFileName().toString());
    	content.put("volumes", course.getVolumes().size());
    	
        return content;
    }
    
    void appendMeta(Course course, Content content) {
    	String[] keys = {
    			Course.KEY_OUTPUT_NAME, 
    			Course.KEY_COURSE_ID, 
    			Course.KEY_LANGUAGE, 
    			Course.KEY_CREATOR, 
                Course.KEY_EDITOR, 
    			Course.KEY_PUBLISHER, 
    			Course.KEY_PUBLISHED, 
    	    	Course.KEY_RIGHTS, 
//    	    	Course.KEY_SERIES_INTRODUCTION, 
    	    	Course.KEY_SERIES_URL, 
    	    	Course.KEY_DEPLOY_URL, 
    	    	Course.KEY_OG_SITE_NAME, 
    	    	Course.KEY_GOOGLE_ANALYTICS_ID, 
    	    	Course.KEY_FB_APP_ID};
    	for ( String key : keys) {
    		content.put(key.replace(':', '-'), course.getMeta(key));
    	}
    	/*
    	 * Series Introductionは改行に対応する
    	 */
    	String intro = course.getMeta(Course.KEY_SERIES_INTRODUCTION);
    	if (Util.isValueValid(intro)) {
    	    String [] intros = intro.split("\n");
    	    StringBuffer sb = new StringBuffer();
    	    for (int i = 0; i < intros.length - 1; i++) {
    	        sb.append(intros[i]);
    	        sb.append("<br />");
    	    }
    	    sb.append(intros[intros.length - 1]);
    	    content.put(Course.KEY_SERIES_INTRODUCTION, sb.toString());
    	}
    	
    	content.put(Course.KEY_COURSE_NAME2, course.getMeta(Course.KEY_COURSE_NAME));
    	content.put(Course.KEY_COVER,  commonImagePath(course.getMeta(Course.KEY_COVER)));
    	String tmp = course.getMeta(Course.KEY_FB_ADMINS);
    	if (isValueValid(tmp)) {
    		String[] fbAdmins = tmp.split(",");
    		List<String> list = new ArrayList<String>();
    		for (String s : fbAdmins) {
    			list.add(s.trim());
    		}
    		content.put(Course.KEY_FB_ADMINS.replace(':', '-'), list);
    	}
        content.put(Course.KEY_OG_SITE_NAME.replace(':', '-'), course.getMeta(Course.KEY_OG_SITE_NAME));
    }

    void appendBookList(Course course, Volume volume, Content content) {
        appendBookList(course, volume, null, content);
    }
    
    void appendBookList(Course course, Volume volume, PageSetting pageSetting, Content content) {
    	String volStr = volume.getVolumeStr();
    	content.put(Course.KEY_BOOKLIST_VOL, Integer.toString(volume.getVolume()));
    	content.put(Course.KEY_BOOKLIST_SERIES_TITLE, course.bookSeriesTitle(volStr));
    	String summary = course.bookBookSummary(volStr);
    	if (Util.isValueValid(summary)) {
    	    String [] summaries = summary.split("\n");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < summaries.length - 1; i++) {
                sb.append(summaries[i]);
                sb.append("<br />");
            }
            sb.append(summaries[summaries.length - 1]);
            content.put(Course.KEY_BOOKLIST_BOOK_SUMMARY, sb.toString());
    	}
        content.put(Course.KEY_BOOKLIST_EPUB_DOWNLOAD_URL, course.bookEpubDownloadUrl(volStr));
//        Util.infoPrintln(LogLevel.LOG_DEBUG, "CommunityURL : " + course.bookCommunityUrl(volStr));
        if (pageSetting != null && pageSetting.isCommunity()) {
            content.put(Course.KEY_BOOKLIST_COMMUNITY_URL, course.bookCommunityUrl(volStr));
        }
    }
    
	void appendMain(PageSetting pageSetting, Content content) throws Epub3MakerException {
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
				String objectPath = archiveTextPath.getParent().relativize(objectValue).toString().replaceAll("\\\\", "/");
				content.put("main", objectPath);
				content.put("main-type", pageSetting.getAttribute(PageSetting.KEY_OBJECT));
			}
		}
	}
	
	void appendVideoImage(PageSetting pageSetting, Course course, Content content) {
		String videoFileName = pageSetting.getVideoImage(); 
		if (isValueValid(videoFileName)) {
			String imageFileName = volumeImagePath(pageSetting.getVolume(), videoFileName);
			if (isValueValid(imageFileName)) {
				content.put(PageSetting.KEY_VIDEO_IMAGE, imageFileName);
				Path absFilePath= Paths.get(course.getMeta(Course.KEY_INPUT_PATH), Volume.KEY_VOLUME_PREFIX + pageSetting.getVolume(), "images", imageFileName);
				int imageFileSizeType = imageFileSizeType(absFilePath);
				if (isPublishHtml()) {
                    if (imageFileSizeType == IMAGE_SIZE_16_9) {
                        content.put("movie-width", "movie_wide");
                    } else {
                        content.put("movie-width", "movie");
                    }
				} else {
				    content.put("video-width", "320");
				    if (imageFileSizeType == IMAGE_SIZE_16_9) {
				        content.put("video-height", "180");
				    } else {
				        content.put("video-height", "240");
				    }
				}
			}
		}
	}

	void appendTextPath(PageSetting pageSetting, Course course, Content content) throws IOException {
		Path textRelativePath = pageSetting.getTextPath(0);
		if (textRelativePath != null) {
			Path textPath = Paths.get(course.getMeta("input-path")).resolve(textRelativePath);
			if (textPath.toFile().isFile()) {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textPath.toFile()), "UTF-8"));
				StringWriter sr = new StringWriter();
				String str;
				while ((str = br.readLine()) != null) {
					sr.write(str + "\n");
				}
				content.put(PageSetting.KEY_TEXT, sr.toString());
				sr.close();
				br.close();
			}
		}
    }
    
    void appendCC(PageSetting pageSetting, Content content) throws IOException {
    	
    	Path ccPath = pageSetting.getCCPath();
    	if (ccPath == null) {
    		content.put("cc-link", null);
    	} else {
    		content.put("cc-link", sReader.get("cclink-" + pageSetting.getCC()));
    		content.put("cc-alt", ccPath.getFileName().toString().replace(".png", ""));
    		content.put("cc-img", getRelativePath(pageSetting.getTextPathForArchiveFile().getParent(), ccPath));
    	}
    }

	void AppendAuthor(Course course, Content content, List<String> authorImages) throws IOException, Epub3MakerException {
		AuthorReader ar = new AuthorReader(Paths.get(Config.getCourseBaseDir(), "common", "authors.xlsx"));
		ar.read();
		String author = course.getMeta(Course.KEY_CREATOR);
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
		    authorImages.add(picture);
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
				value = value.replaceAll("\r\n", "<br/>\n");
				value = value.replaceAll("\n", "<br/>\n");
				map.put("content", value);
			}
			list.add(map);
		}
		content.put("introductionList", list);
	}

	void AppendTestList(PageSetting pageSetting, Volume volume, Content content) {
		List<Map<String, String>> list = new ArrayList<>();
		for (PageSetting s : volume.getPageSettings()) {
			if (s.getPageType().equals(PageSetting.VALUE_KEY_PAGE_TYPE_TEST)) {
				Map<String, String> map = new HashMap<>();
				map.put("title", s.getSettings().get(PageSetting.KEY_SUBJECT).get(0).get(PageSetting.KEY_ATTR_VALUE));
				map.put("link", getRelativePath(pageSetting, s));
				list.add(map);
			}
		}
		content.put("testList", list);
	}

    void createTemplatePage(Content content, String templateFileName, Path outFilePath) throws IOException {
    	Properties p = new Properties();
    	p.setProperty("input.encoding", "UTF-8");
    	p.setProperty("output.encoding", "UTF-8");
    	Velocity.init(p);
    	
    	VelocityContext context = content.getVelocityContext();
    	
    	String templateDir;
    	if (isPublishHtml()) {
    		templateDir = "web-templates";
    	} else {
    		templateDir = "templates";
    	}
    	org.apache.velocity.Template template = Velocity.getTemplate(Paths.get(Config.getCourseBaseDir(), "common", templateDir, lang, templateFileName).toString(), "UTF-8");
    	
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilePath.toFile()), "UTF-8"));
    	template.merge(context, bw);
    	bw.close();
    }
    
    /**
     * 必須項目
     * identifier
     * language(指定した言語フォルダがなくてもエラー)
     * creator(指定した著者がなくてもエラー)
     * published
     * series-name
     * @return
     * @throws Epub3MakerException 
     */
    private void validateSeriesInformation(Course course) throws Epub3MakerException {
        String meta;
        boolean isIdentifier = true,
                isLanguage = true,
                isCreator = true,
                isPublished = true,
                isSeriesName = true,
                isAllValid = true;
        
        
        meta = course.getMeta(Course.KEY_COURSE_ID);
        if (!Util.isValueValid(meta)) {
            isIdentifier = false;
            isAllValid = false;
        }
        
        meta = course.getMeta(Course.KEY_LANGUAGE);
        if (!Util.isValueValid(meta)) {
            isLanguage = false;
            isAllValid = false;
        }
        
        meta = course.getMeta(Course.KEY_CREATOR);
        if (!Util.isValueValid(meta)) {
            isCreator = false;
            isAllValid = false;
        }
        
        meta = course.getMeta(Course.KEY_PUBLISHED);
        if (!Util.isValueValid(meta)) {
            isPublished = false;
            isAllValid = false;
        }

        meta = course.getMeta(Course.KEY_COURSE_NAME);
        if (!Util.isValueValid(meta)) {
            isSeriesName = false;
            isAllValid = false;
        }

        if (!isAllValid) {

            StringBuilder msg = new StringBuilder();
            msg.append("必須項目が未設定です\n");
            if (!isIdentifier) {
                msg.append("\t" + Course.KEY_COURSE_ID2 + "\n");
            }
            if (!isLanguage) {
                msg.append("\t" + Course.KEY_LANGUAGE + "\n");
            }
            if (!isCreator) {
                msg.append("\t" + Course.KEY_CREATOR + "\n");
            }
            if (!isPublished) {
                msg.append("\t" + Course.KEY_PUBLISHED + "\n");
            }
            if (!isSeriesName) {
                msg.append("\t" + Course.KEY_COURSE_NAME2 + "\n");
            }
            throw new Epub3MakerException(msg.toString());
        }
    }
}
