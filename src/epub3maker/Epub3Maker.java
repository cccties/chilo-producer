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

import static epub3maker.Util.isPublishHtml;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Epub3Maker {

    public static void main(String[] args) {

        /*
         * 引数を処理
         */
        String configFile = null;
        String courseDir = null;
        String publishStyle= "epub3";
        String courseVersion = "2";
        for (int ai = 0; ai < args.length && args[ai].startsWith("-"); ai++) {
            if (args[ai].equals("-config")) {
                if (ai < args.length - 1) {
                    configFile = args[++ai];
                    continue;
                } else {
                    usage();
                }
            } else if (args[ai].equals("-course")) {
                if (ai < args.length - 1) {
                    courseDir = args[++ai];
                    continue;
                } else {
                    usage();
                }
            } else if (args[ai].equals("-publish")) {
            	if (ai < args.length - 1) {
            		publishStyle = args[++ai];
            		if (!publishStyle.equals("epub3") && !publishStyle.equals("html")) {
            			usage();
            		}
            	}
            } else if (args[ai].equals("-1")) {
            	courseVersion = "1";
            } else if (args[ai].equals("-2")) {
            	courseVersion = "2";
            }
        }

        try {
            /*
             * 設定値を読込
             */
            new Config(configFile);

            /*
             * meta ファイルを全て処理するかどうか？
             */
            Config.setProcessAllMeta(courseDir == null);
            Config.setPublishStyle(publishStyle);
            Config.setCourseVersion(courseVersion);

            ArrayList<Path> coursesList = new ArrayList<Path>();
            Path courseBasePath = Paths.get(Config.getCourseBaseDir());
            if (Config.isProcessAllMeta()) {
                /*
                 * course dir 一覧を取得
                 */
                DirectoryStream<Path> stream = Files.newDirectoryStream(
                        courseBasePath, new DirectoryStream.Filter<Path>() {
                            @Override
                            public boolean accept(Path entry)
                                    throws IOException {
                                return Files.isDirectory(entry);
                            }
                        });
                for (Path p : stream) {
                    coursesList.add(p);
                }

            } else {
                coursesList.add(courseBasePath.resolve(courseDir));
            }

            /*
             * TODO 本当は 10 thread ずつくらいを並列処理するように実装したい
             */
            if (Config.getCourseVersion() == 1) {
            	Epub3Maker proc = new Epub3Maker();
            	for (Path c : coursesList) {
            		proc.process(c);
            	}
            } else {
            	Epub3MakerV2 proc = new Epub3MakerV2();
            	for (Path c : coursesList) {
            		proc.process(c);
            	}
            }
        } catch (IOException e) {
            Util.infoPrintln(LogLevel.LOG_EMERG, e.getMessage());
            e.printStackTrace();
        } catch (Epub3MakerException e) {
            Util.infoPrintln(LogLevel.LOG_EMERG, e.getMessage());
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            Util.infoPrintln(LogLevel.LOG_EMERG, e.getMessage());
            e.printStackTrace();
        } catch (SAXException e) {
            Util.infoPrintln(LogLevel.LOG_EMERG, e.getMessage());
            e.printStackTrace();
        } catch (TransformerException e) {
            Util.infoPrintln(LogLevel.LOG_EMERG, e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Util.infoPrintln(LogLevel.LOG_EMERG, e.getMessage());
            e.printStackTrace();
        }
    }

    private static void usage() {
        /*
         * meta file (現状は xlsx）は，course dir 直下にある前提
         */
        System.err
                .println("Epub3Maker -course <course dir> -config <config file> -publish [epub3|html]");
        System.exit(1);
    }

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
        Config.localStrings = ResourceBundle.getBundle("strings",
                new Locale.Builder().setLanguage(course.getMeta(Course.KEY_LANGUAGE)).build());
        Util.infoPrintln(LogLevel.LOG_DEBUG, "Locale: " + Config.localStrings.getString("nav.title"));

        /*
         * Page 設定を表示する（debug）
         */
        showPageSettings(course);

        initilizeDirectories(course);

        createMimeTypeFile(outputTempDirectory.resolve(Paths
                .get("mimetype")));
        createContainerFile(outputTempDirectory.resolve(Paths
                .get("container.xml")));

        for (Map.Entry<Integer, Volume> e : course.getVolumes().entrySet()) {
            Volume volume = e.getValue();
            
            Util.infoPrintln(LogLevel.LOG_DEBUG, "Epub3Maker#process: ### processing vol-" + volume.getVolume());
            
            Epub3PackageDocument packageDocument = new Epub3PackageDocument(
                    outputTempDirectory.resolve(Paths.get("content.opf")));
            this.setPackageDocumentMetadatas(packageDocument, course, volume);

            createXhtmlFiles(course, volume);

            List<PageSetting> sortedPageSettings = new ArrayList<>(volume.getPageSettings());
            sortGeneratedPages(sortedPageSettings);

            writeDocument(
                    outputTempDirectory.resolve(NAVIGATION_DOCUMENT_FILE_NAME),
                    course,
                    makeNavigationDocument(course, volume, Config.localStrings.getString("nav.title"),
                            sortedPageSettings));

            // http://imagedrive.github.io/spec/epub301-publications.xhtml#sec-publication-resources
            packageDocument.setManifest(
                    makeItemsListForManifest(course, volume), Const.mediaTypes);

            packageDocument.setSpine(makeItemrefsListForSpine(course, volume));

            Files.createDirectories(packageDocument.getOPFPath().getParent());
            writeDocument(packageDocument.getOPFPath(), course,
                    packageDocument.getDocument());

            Set<Path> targetFilePaths = getArchivingTargets(course, volume);

            Epub3Archiver archiver = new Epub3Archiver();
            archiver.archive(course, volume, new ArrayList<Path>(
                    targetFilePaths));
        }
    }

    /**
     * @param course
     * @param volume
     * @return
     * @throws IOException
     */
    protected Set<Path> getArchivingTargets(Course course, Volume volume)
            throws IOException {
        Set<Path> targetFilePaths = new HashSet<>();

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
        List<Path> commonFilePaths = Util.getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common"), entry -> !(Files.isDirectory(entry)) && !entry.startsWith(ignoreCommonPath));
        targetFilePaths.addAll(commonFilePaths);

        final String volumeName = Volume.KEY_VOLUME_PREFIX + volume.getVolume();
        Path ignoreVolumePath = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), volumeName, "text");
        List<Path> volumeFilePaths = Util.getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), volumeName), entry -> !(Files.isDirectory(entry)) && !entry.startsWith(ignoreVolumePath));
        targetFilePaths.addAll(volumeFilePaths);


//        targetFilePaths.addAll(findCommonCssFiles(course));
//        targetFilePaths.addAll(findVolumeCssFiles(course,
//                Volume.KEY_VOLUME_PREFIX + volume.getVolume()));
//
//        /*
//         * 静的 xhtml
//         */
//        targetFilePaths.addAll(findCommonStaticFiles(course));
//        targetFilePaths.addAll(findVolumeStaticsFiles(course,
//                Volume.KEY_VOLUME_PREFIX + volume.getVolume()));
//
//        /*
//         * images は . で始まるファイル以外は全部入れる
//         */
//        targetFilePaths.addAll(findCommonImagesFiles(course));
//        targetFilePaths.addAll(findVolumeImagesFiles(course,
//                Volume.KEY_VOLUME_PREFIX + volume.getVolume()));
//
//        /*
//         * videos は .mp4 で終わるファイルは全部入れる
//         */
//        targetFilePaths.addAll(findCommonVideosFiles(course));
//        targetFilePaths.addAll(findVolumeVideosFiles(course,
//                Volume.KEY_VOLUME_PREFIX + volume.getVolume()));
//
//        /*
//         * scripts は .js で終わるファイルは全部入れる
//         */
//        targetFilePaths.addAll(findCommonScriptFiles(course));
//        targetFilePaths.addAll(findVolumeScriptFiles(course,
//                Volume.KEY_VOLUME_PREFIX + volume.getVolume()));
        return targetFilePaths;
    }

    /**
     * @param course
     * @throws IOException
     */
    protected void initilizeDirectories(Course course) throws IOException {
        initializeOutputDirectory(course);

        outputTempDirectory = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "temp");

        Util.initializeDirectory(outputTempDirectory);

        Files.createDirectories(outputTempDirectory.resolve(Paths.get("common", "text")));
    }

    /**
     * @param course
     */
    protected void showMeta(Course course) {
        Map<String, String> meta = course.getMeta();
        Util.infoPrintln(LogLevel.LOG_DEBUG,"Meta");
        for (Iterator<String> ite = meta.keySet().iterator(); ite.hasNext();) {
            String key = ite.next();
            Util.infoPrintln(LogLevel.LOG_DEBUG, key + ": " + meta.get(key));
        }
    }

    /**
     * @param course
     * @throws Epub3MakerException
     */
    protected void showPageSettings(Course course) throws Epub3MakerException {
        List<PageSetting> pageSettings = course.getVolumes().get(1)
                .getPageSettings();
        Util.infoPrintln(LogLevel.LOG_DEBUG, "PageSettings");
        List<String> keyNames = course.getKeyNames();
        List<String> attributeNames = course.getAttributeNames();
        for (Iterator<PageSetting> psIte = pageSettings.iterator(); psIte
                .hasNext();) {
            PageSetting setting = psIte.next();
            Util.infoPrintln(LogLevel.LOG_DEBUG, "vol-" + setting.getVolume() + "-"
                    + setting.getPage());

            for (Iterator<String> keyIte = keyNames.iterator(); keyIte
                    .hasNext();) {
                String keyName = keyIte.next();
                List<Map<String, String>> keyValues = setting.getSettings()
                        .get(keyName);
                String msg = "";
                if (keyValues == null) {
                    msg = "keyValueが null です。ページ設定に空行があります。";
                    throw new Epub3MakerException(msg);
                }
                for (int i = 0, size = keyValues.size(); i < size; ++i) {
                    msg += keyName + ": ";
                    for (Iterator<String> anIte = attributeNames.iterator(); anIte
                            .hasNext();) {
                        String attributeName = anIte.next();
                        msg += (keyValues.get(i).get(attributeName) + ", ");
                    }
                    Util.infoPrintln(LogLevel.LOG_DEBUG, msg);
                }
            }
        }
    }

    /**
     * @param course
     * @param volume
     * @throws Epub3MakerException
     * @throws IOException
     * @throws Exception
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    protected void createXhtmlFiles(Course course, Volume volume)
            throws Epub3MakerException, IOException, Exception,
            ParserConfigurationException, SAXException {
        for (PageSetting setting : volume.getPageSettings()) {
            if(setting.getTextPathsSize() != setting.getObjectPathsSize())
            {
                throw new Epub3MakerException(course.getMeta("Course ID:" + Course.KEY_COURSE_ID) + "vol: " + volume.getVolume() +
                        "page: " + setting.getPage() + System.lineSeparator() +
                        "objectとtextの行の数が一致しません。objectとtextはセットで追加していくようにしてください。");
            }

            Path textPath = setting.getTextPathForArchiveFile();
            Path outputDirectory = outputTempDirectory.resolve(textPath
                    .getParent());
            Files.createDirectories(outputDirectory);

            writeDocument(outputTempDirectory.resolve(textPath), course,
                    createPage(course, volume, setting));
        }
    }

    protected void setPackageDocumentMetadatas(Epub3PackageDocument packageDocument, Course course, Volume volume)
    {
        Element elementPackage = packageDocument.setPackageElement("3.0",
                "BookId");
        elementPackage.setAttribute("xml:lang",
                course.getMeta(Course.KEY_LANGUAGE));


        String volumeName = Volume.KEY_VOLUME_PREFIX + (volume.getVolume());

//        DateTimeFormatter dtf = DateTimeFormatter
//                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'" + ZonedDateTime.now().getOffset() + "'");//+09:00をいれちゃうとepub3checkerがエラーはくのでoffsetはいれない
        String modifiedDate = Util.getModifiedTime();

        packageDocument.setMetadata(volume.getPageSettings().get(0).getIdentifier(), "BookId",
                course.getMeta(volumeName),
                course.getMeta(Course.KEY_LANGUAGE), modifiedDate);


        PageSetting firstPage = volume.getPageSettings().get(0);
        List<Map<String, String>> row =  firstPage.getSettings().get(
                PageSetting.KEY_PUBLISHED);// ;
        String tmp = row.get(0)
                .get(PageSetting.KEY_ATTR_VALUE);
        if (Util.isValueValid(tmp)) {
        	String date = tmp.replaceAll("/", "-");
        	packageDocument.setMetadataPublishedDate(date);
        }

        packageDocument.setMetadataRights(course
                .getMeta(Course.KEY_RIGHTS));

        packageDocument.setMetadataCreator(course
                .getMeta(Course.KEY_CREATOR));

        packageDocument.setMetadataPublisher(course
                .getMeta(Course.KEY_PUBLISHER));

        if (firstPage.getCoverImagePath() != null) {
            String path = firstPage.getCoverImagePath().toString()
                    .replaceAll("\\\\", "-");

            packageDocument.setMetadataCover(path.toString());
        }

    }

    protected void initializeOutputDirectory(Course course) throws IOException {
        Path targetDirectory = Paths
                .get(course.getMeta(Course.KEY_OUTPUT_PATH));
        Util.initializeDirectory(targetDirectory);
    }

    protected Document makeNavigationDocument(Course course, Volume volume,
            String title, List<PageSetting> sortedPages)
            throws ParserConfigurationException, IOException, DOMException, Epub3MakerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element htmlElement = createElement(document, "html",
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("xmlns", "http://www.w3.org/1999/xhtml");
                        put("xmlns:epub", "http://www.idpf.org/2007/ops");
                        put("xml:lang", course.getMeta(Course.KEY_LANGUAGE));
                    }
                });

        document.appendChild(htmlElement);

        Element headElement = createElement(document, "head", null);
        htmlElement.appendChild(headElement);

        headElement.appendChild(createElement(document, "title", null));
        headElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(title, " ")));

        addStylesToPage(findIndexCssFiles(course), document, headElement,
                Paths.get(NAVIGATION_DOCUMENT_FILE_NAME));

        Element bodyElement = createElement(document, "body", null);

        htmlElement.appendChild(bodyElement);

        bodyElement.appendChild(createElement(document, "nav",
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("epub:type", "toc");
                        put("id", "toc");
                    }
                }));

        Element navElement = (Element) bodyElement.getLastChild();
        navElement.appendChild(createElement(document, "h1", null));
        navElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(title, " ")));

        setNavigationList(course, sortedPages, document, navElement);

        setLandmarks(document, bodyElement, volume);


        return document;
    }

    private void setLandmarks(Document document, Element bodyElement, Volume volume)
    {
    	Element navElement = document.createElement("nav");
    	navElement.setAttribute("epub:type", "landmarks");
    	navElement.setAttribute("hidden", "hidden");
    	bodyElement.appendChild(navElement);

    	Map<String, String[]> landmarks = new HashMap<>();

    	if(volume.getPageSettings().get(0).getShowToc())
    	{
    		landmarks.put("toc", new String[]{NAVIGATION_DOCUMENT_FILE_NAME, "navi"});
    	}
    	Path coverPagePath = volume.getPageSettings().get(0).getCoverPagePath();
    	if(coverPagePath != null && coverPagePath.getNameCount() != 0)
    	{
    		landmarks.put("cover", new String[]{coverPagePath.toString().replaceAll("\\\\", "/"), "cover"});
    	}

    	Element olElement = document.createElement("ol");
    	navElement.appendChild(olElement);

    	for(Iterator<Entry<String, String[]>> ite = landmarks.entrySet().iterator(); ite.hasNext();)
    	{
    		Entry<String, String[]> entry = ite.next();
        	Element liElement = document.createElement("li");
        	olElement.appendChild(liElement);

        	Element aElement = document.createElement("a");
        	aElement.setAttribute("epub:type", entry.getKey());
        	aElement.setAttribute("href", entry.getValue()[0]);
        	aElement.appendChild(document.createTextNode(entry.getValue()[1]));
        	liElement.appendChild(aElement);
    	}
    }


    void setNavigationList(Course course, List<PageSetting> sortedPages, Document document, Element navElement)
    {
    	String currentChapter = null;
        Element currentElement = null;
    	Element rootOlElement = (Element)navElement.appendChild(createElement(document, "ol", null));
        for (PageSetting page : sortedPages) {
        	final String chapter = page.getSettings().get(PageSetting.KEY_SUBJECT).get(0).get(PageSetting.KEY_ATTR_VALUE);
        	final boolean isChapterChanged = (currentChapter == null || !currentChapter.equals(chapter));
        	if(isChapterChanged)
        	{
                currentChapter = chapter;

            	rootOlElement.appendChild(createElement(document, "li", null));
                Element liElement = (Element) rootOlElement.getLastChild();

                Element chapterLink = document.createElement("a");
                chapterLink.setAttribute("href", page.getTextPathForArchiveFile().toString().replaceAll("\\\\", "/"));
                liElement.appendChild(chapterLink);

                String pageTitle = page.getSettings()
                        .get(PageSetting.KEY_SUBJECT).get(0)
                        .get(PageSetting.KEY_ATTR_VALUE);
                liElement.getLastChild().appendChild(
                        document.createTextNode(Epub3Maker.textNodeValue(pageTitle,
                                " ")));

                Element nextOlElement = document.createElement("ol");
                liElement.appendChild(nextOlElement);
                currentElement = nextOlElement;

                continue;
        	}

            Element liElement = document.createElement("li");
            currentElement.appendChild(liElement);

            liElement.appendChild(createElement(document, "a",
                    new HashMap<String, String>() {
                        private static final long serialVersionUID = 1L;
                        {
                            Path textPath = page.getTextPathForArchiveFile();
                            // if(textPath != null)
                            // {
                            put("href",
                                    textPath.toString().replaceAll("\\\\", "/"));
                            // }
                        }
                    }));

            String pageTitle = page.getSettings()
                    .get(PageSetting.KEY_SUBSUBJECT).get(0)
                    .get(PageSetting.KEY_ATTR_VALUE);
            liElement.getLastChild().appendChild(
                    document.createTextNode(Epub3Maker.textNodeValue(pageTitle,
                            " ")));
        }
    }

    protected Map<Path, String> makeItemsListForManifest(Course course, Volume volume)
            throws IOException, ParserConfigurationException, SAXException {
        Map<Path, String> items = new HashMap<>();

        Path inputPath = Paths.get(course.getMeta(Course.KEY_INPUT_PATH));

        items.putAll(toMap(this.getArchivingTargets(course, volume), course, volume));

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

    protected Map<Path, String> toMap(Set<Path> paths, Course course, Volume volume)
    {
    	Path coverImage = null;
    	String coverStr = volume.getCoverImage();
    	if (Util.isValueValid(coverStr)) {
    		coverImage = Paths.get(course.getMeta(Course.KEY_INPUT_PATH), coverStr);
    	}
        Map<Path, String> map = new HashMap<>();
        for(Path path : paths)
        {
        	if (coverImage != null && path.equals(coverImage)) {
        		map.put(path, PageSetting.VALUE_KEY_ITEM_PROPERTY_COVER_IMAGE);
        	} else {
        		map.put(path, null);
        	}
        }
        return map;
    }

    //手抜きチェック
    protected boolean hasSvg(Path path) throws ParserConfigurationException, SAXException, IOException
    {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder builder = factory.newDocumentBuilder();
    	Document document = builder.parse(path.toString());

    	if(document.getElementsByTagName("svg").getLength() == 0)
    	{
    		return false;
    	}
    	return true;
    }

    List<String> makeItemrefsListForSpine(Course course, Volume volume)
            throws IOException, Epub3MakerException {

        List<Path> targetFilePaths = new ArrayList<>();

//        targetFilePaths.addAll(Util.getFilePaths(outputTempDirectory.resolve(Paths
//                .get("common", "text"))));
        /*
         * common の下は全部入れてはいけない…vol-xxx- のものだけ入れる．
         */
        StringBuilder volPrefix = new StringBuilder();
        volPrefix.append(String.format("vol-%03d-", volume.getVolume()));
        targetFilePaths.addAll(Util.findFilesPrefix(outputTempDirectory.resolve(Paths
                .get("common", "text")), volPrefix.toString()));
        
        targetFilePaths.addAll(Util.getFilePaths(outputTempDirectory.resolve(Paths
                .get("vol-" + volume.getVolume(), "text"))));

        final String prefix = Volume.KEY_VOLUME_PREFIX + volume.getVolume();
        List<String> items = makeSpineItemsFromPathList(targetFilePaths, prefix);

        Collections.sort(items, (a, b) -> {
            int aFirst = a.lastIndexOf('-');
            int aSecond = a.lastIndexOf('-', aFirst - 1);
//            int aThird = a.lastIndexOf('-', aSecond - 1);
            String aPart = a.substring(aSecond + 1);

            int bFirst = b.lastIndexOf('-');
            int bSecond = b.lastIndexOf('-', bFirst - 1);
//            int bThird = b.lastIndexOf('-', bSecond - 1);
            String bPart = b.substring(bSecond + 1);

            return aPart.compareTo(bPart);
        });

        /*
         * 静的 xhtml
         */
        ArrayList<Path> staticFilePaths = new ArrayList<>();
        staticFilePaths.addAll(findCommonStaticFiles(course));
        staticFilePaths.addAll(findVolumeStaticsFiles(course, Volume.KEY_VOLUME_PREFIX
                + volume.getVolume()));

        items.addAll(makeSpineItemsFromPathList(staticFilePaths, prefix));

        if(volume.getPageSettings().get(0).getShowToc())
        {
        	items.add(0, NAVIGATION_DOCUMENT_FILE_NAME);
        }

        Path coverPath = volume.getPageSettings().get(0).getCoverPagePath();
        if(coverPath != null)
        {
        	items.add(0, coverPath.toString().replaceAll("\\\\", "/"));
        }

        return items;
    }

    /**
     * @param targetFilePaths
     * @param prefix
     * @return
     * @throws Epub3MakerException
     */
    private List<String> makeSpineItemsFromPathList(List<Path> targetFilePaths,
            final String prefix) throws Epub3MakerException {
        List<String> items = new ArrayList<>();
        for (Path file : targetFilePaths) {
            Path p = subtractBasePath(file);
//            Path p = file.subpath(file.getNameCount() - DIRECTORY_DEPTH,
//                    file.getNameCount());
            if (p.startsWith("common") || p.startsWith(prefix)) {
                items.add(p.toString().replaceAll("\\\\", "/"));
            }
        }
        return items;
    }

    protected void sortGeneratedPages(List<PageSetting> pages) {
        Collections
                .sort(pages, (a, b) -> {
                    Path aPath = a.getTextPathForArchiveFile();

                    Path bPath = b.getTextPathForArchiveFile();

                    final String title[] = {
                            aPath.getFileName().toString()
                                    .replaceAll("\\\\", "-"),
                            bPath.getFileName().toString()
                                    .replaceAll("\\\\", "-") };
                    int first[] = new int[2];
                    int second[] = new int[2];
                    String part[] = new String[2];

                    for (int i = 0; i < 2; ++i) {
                        first[i] = title[i].lastIndexOf('-');
                        second[i] = title[i].lastIndexOf('-', first[i] - 1);
                        part[i] = title[i].substring(second[i] + 1);
                    }

                    return part[0].compareTo(part[1]);
                });
    }

    protected void createContainerFile(Path path) throws IOException {
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

    protected void createMimeTypeFile(Path path) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            bw.write("application/epub+zip");
        }
    }

    // Documentオブジェクトをファイルに出力
    protected void writeDocument(Path outFilePath, Course setting,
            Document document) throws Exception {

        // Path f =Paths.get("C:\\test\\sample.xml");
        FileOutputStream fos = new FileOutputStream(outFilePath.toFile());
        StreamResult result = new StreamResult(fos);

        // Transformerファクトリを生成
        TransformerFactory transFactory = TransformerFactory.newInstance();
        // Transformerを取得
        Transformer transformer = transFactory.newTransformer();

        DOMImplementation domImpl = document.getImplementation();
        DocumentType doctype = domImpl.createDocumentType("html", null, null);
        document.appendChild(doctype);

        // HTML5ということを表す。
        // transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
        // "about:legacy-compat");

        // エンコード：UTF-8、インデントありを指定
        transformer.setOutputProperty("encoding", "UTF-8");
        transformer.setOutputProperty("indent", "yes");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xalan}indent-amount", "4");

        // transformerに渡すソースを生成
        DOMSource source = new DOMSource(document);

        // 出力実行
        transformer.transform(source, result);
        fos.close();
    }

    protected Document createPage(Course course, Volume volume,
            PageSetting pageSetting) throws ParserConfigurationException,
            IOException, SAXException, DOMException, Epub3MakerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setValidating(false);
        //factory.setExpandEntityReferences(false);
//        factory.setNamespaceAware(false);
//        factory.setXIncludeAware(false);
        //factory.setFeature("http://xml.org/sax/features/validation", true);
        //factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", true);
        //factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
        //factory.setFeature("http://xml.org/sax/features/external-general-entities", true);
        //factory.setFeature("http://xml.org/sax/features/external-parameter-entities", true);
       // factory.setFeature("http://www.w3.org/TR/MathML2/dtd/xhtml-math11-f.dtd", false);
        // factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

//        EntityResolver er = new EntityResolver() {
//            public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId)
//                    throws SAXException, java.io.IOException
//             {
//               if (publicId.equals("--myDTDpublicID--"))
//                 // this deactivates the open office DTD
//                 return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
//               else return null;
//             }};
//        builder.setEntityResolver(er);


        Document document = builder.newDocument();

        // if(pageSetting.getSettings().get(PageSetting.KEY_PAGE_TYPE).get(0).get(PageSetting.KEY_ATTR_VALUE).equals(PageSetting.VALUE_KEY_PAGE_TYPE_BASIC)
        // &&
        // pageSetting.getSettings().get(PageSetting.KEY_TEXT).get(0).get(PageSetting.KEY_ATTR_ATTRIBUTE).equals(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_PREFORMAT))
        // {
        // Path path =
        // Paths.get(course.getMeta("input-path")).resolve(pageSetting.getTextPath(0));
        // // factory.setNamespaceAware(true);
        // // DocumentBuilder buildera = factory.newDocumentBuilder();
        // return builder.parse(path.toString());
        // }

        createPageHeader(course, volume, pageSetting, document);
        createPageBody(course, pageSetting, document, builder);

        return document;
    }

    private Document createPageHeader(Course course, Volume volume,
            PageSetting pageSetting, Document document)
            throws ParserConfigurationException, IOException, DOMException, Epub3MakerException {
        Element rootElement = createElement(document, "html",
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("xmlns", "http://www.w3.org/1999/xhtml");
                        put("xmlns:epub", "http://www.idpf.org/2007/ops");
                        put("xml:lang", course.getMeta(Course.KEY_LANGUAGE));
                    }
                });
        document.appendChild(rootElement);

        Element headElement = createElement(document, "head", null);
        rootElement.appendChild(headElement);

        headElement.appendChild(createElement(document, "meta",
                new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("charset", "UTF-8");
                    }
                }));

        headElement.appendChild(createElement(document, "title", null));
        headElement
                .getElementsByTagName("title")
                .item(0)
                .appendChild(
                        document.createTextNode(Epub3Maker.textNodeValue(
                                pageSetting.getSettings()
                                        .get(PageSetting.KEY_SUBJECT).get(0)
                                        .get(PageSetting.KEY_ATTR_VALUE), " ")));

        Path textPath = pageSetting.getTextPathForArchiveFile();
        // if(textPath != null)
        // {
        addAllStylesToPage(course, volume, document, headElement, textPath, pageSetting.getPageType());
        final String itemProperties = pageSetting.getItemProperty();
        if(Util.isValueValid(itemProperties) && itemProperties.contains("mathml"))
        {
        	appendMathjaxConfig(pageSetting, document, headElement);
        }

        addJavascriptsToPage(pageSetting, document, headElement);
        // }

        return document;
    }

    private void appendMathjaxConfig(PageSetting setting, Document document, Element headElement)
    {
    	Element element = document.createElement("script");
        element.setAttribute("type", "text/x-mathjax-config");

        element.appendChild(document.createTextNode(MATHJAX_CONFIG_SCRIPT));

        headElement.appendChild(element);
    }

    private void addJavascriptsToPage(PageSetting setting, Document document, Element headElement)
    {
        for(int i = 0, size = setting.getJavascriptFilePathsSize(); i < size; ++i)
        {
            final Path scriptFilePath = setting.getJavascriptFilePath(i);
            if(scriptFilePath == null)
            {
                continue;
            }
            Element element = document.createElement("script");
            element.setAttribute("type", "text/javascript");
            final Path textPath = setting.getTextPathForArchiveFile().getParent();
            element.setAttribute("src", textPath.relativize(scriptFilePath).toString().replaceAll("\\\\", "/"));
            headElement.appendChild(element);
        }
    }

    void addAllStylesToPage(Course course, Volume volume,
            Document document, Element headElement, Path documentPath, String type)
            throws IOException, DOMException, Epub3MakerException {
    	List<Path> commonCssFilePaths = Util.getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common", "styles"),
        		entry -> !(Files.isDirectory(entry)) && entry.getFileName().toString().endsWith(".css"));
        addStylesToPage(commonCssFilePaths, document, headElement, documentPath);

        List<Path> volumeCssFilePaths = Util.getFilePaths(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), Volume.KEY_VOLUME_PREFIX + volume.getVolume(), "styles"),
        		entry -> !(Files.isDirectory(entry)) && entry.getFileName().toString().endsWith(".css"));
        addStylesToPage(volumeCssFilePaths, document, headElement, documentPath);
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

    public static Path getBasePath(Path target)
    {
    	int i;
    	int count = target.getNameCount();
    	for(i = 0; i < count; ++i)
    	{
    		if(target.getName(i).toString().equals("common"))
    		{
    			break;
    		}
    		else if(target.getName(i).toString().matches("^" + Volume.KEY_VOLUME_PREFIX + "\\d+$"))
    		{
    			break;
    		}
    	}
    	return target.subpath(0, i);
    }

    void addStylesToPage(List<Path> cssFiles, Document document,
            Element headElement, Path documentPath) throws DOMException, Epub3MakerException {
        for (Path path : cssFiles) {
            headElement.appendChild(createElement(document, "link",
                    new HashMap<String, String>() {
                        private static final long serialVersionUID = 1L;
                        {
                            Path documentDirectory = documentPath.getParent();
                            Path p = subtractBasePath(path);

                            if (documentDirectory == null) {
                                put("href", p.toString().replaceAll("\\\\", "/"));
//                                        path.subpath(
//                                                path.getNameCount()
//                                                        - DIRECTORY_DEPTH,
//                                                path.getNameCount()).toString()
//                                                .replaceAll("\\\\", "/"));
                            } else {
                                put("href",
                                        documentDirectory
                                                .relativize(p)
//                                                        path.subpath(
//                                                                path.getNameCount()
//                                                                        - DIRECTORY_DEPTH,
//                                                                path.getNameCount()))
                                                .toString()
                                                .replaceAll("\\\\", "/"));
                            }
                            put("rel", "stylesheet");
                            put("type", "text/css");
                        }
                    }));
        }
    }



    private List<Path> findCommonScriptFiles(Course course) throws IOException {
        return Util.findScriptFilesIn(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), "common", "scripts"));
    }

    private List<Path> findVolumeScriptFiles(Course course, String volumeName) throws IOException {
        return Util.findScriptFilesIn(Paths.get(course.getMeta(Course.KEY_INPUT_PATH), volumeName, "scripts"));
    }

    private List<Path> findCommonCssFiles(Course course) throws IOException {
        Path commonCssDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "common", "styles");

        return Util.findCssFilesIn(commonCssDirectoryPath);
    }
    
    List<Path> findIndexCssFiles(Course course) throws IOException {
    	return findCommonCssFiles(course);
    }

    private List<Path> findVolumeCssFiles(Course course, String volumeName)
            throws IOException {
        Path volumeCssDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), volumeName, "styles");

        return Util.findCssFilesIn(volumeCssDirectoryPath);
    }

    private List<Path> findCommonStaticFiles(Course course) throws IOException {
        Path commonStaticsDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "common", "statics");

        return Util.findStaticsFilesIn(commonStaticsDirectoryPath);
    }

    private List<Path> findVolumeStaticsFiles(Course course, String volumeName)
            throws IOException {
        Path volumeCssDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), volumeName, "statics");

        return Util.findStaticsFilesIn(volumeCssDirectoryPath);
    }

    private List<Path> findCommonImagesFiles(Course course) throws IOException {
        Path commonStaticsDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "common", "images");

        return Util.findImagesFilesIn(commonStaticsDirectoryPath);
    }

    private List<Path> findVolumeImagesFiles(Course course, String volumeName)
            throws IOException {
        Path volumeCssDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), volumeName, "images");

        return Util.findImagesFilesIn(volumeCssDirectoryPath);
    }

    private List<Path> findCommonVideosFiles(Course course) throws IOException {
        Path commonStaticsDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "common", "videos");

        return Util.findVideosFilesIn(commonStaticsDirectoryPath);
    }

    private List<Path> findVolumeVideosFiles(Course course, String volumeName)
            throws IOException {
        Path volumeCssDirectoryPath = Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), volumeName, "videos");

        return Util.findVideosFilesIn(volumeCssDirectoryPath);
    }

    Document createPageBody(Course course, PageSetting pageSetting,
            Document document, DocumentBuilder builder)
            throws ParserConfigurationException, IOException, DOMException,
            SAXException, Epub3MakerException {
        Element bodyElement = createElement(document, "body", null);
        document.getElementsByTagName("html").item(0).appendChild(bodyElement);

        bodyElement.appendChild(document.createComment("Header"));

        appendPageChapter(pageSetting, document, bodyElement);

        appendCommunityButton(pageSetting, document, bodyElement);

        appendPageSection(pageSetting, document, bodyElement);

        // "object"と"text"のsizeの値は一致しているはず
        for (int i = 0, size = pageSetting.getSettings()
                .get(PageSetting.KEY_OBJECT).size(); i < size; ++i) {

            appendObject(course, pageSetting, document, bodyElement, i);

            appendText(course, pageSetting, document, builder, bodyElement, i);
        }

        return document;
    }

    /**
     * @param course
     * @param pageSetting
     * @param document
     * @param builder
     * @param bodyElement
     * @param i
     * @throws IOException
     * @throws SAXException
     */
    protected void appendText(Course course, PageSetting pageSetting, Document document,
            DocumentBuilder builder, Element bodyElement, int i)
            throws IOException, SAXException {
        Path textRelativePath = pageSetting.getTextPath(i);
        if (textRelativePath == null) {
            return;
        }

        Path textPath = Paths.get(course.getMeta("input-path")).resolve(
                textRelativePath);

        Map<String, String> targetText = pageSetting.getSettings()
                .get(PageSetting.KEY_TEXT).get(i);
        String attribute = targetText.get(PageSetting.KEY_ATTR_ATTRIBUTE);

        if (attribute.equals(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_FILE)) {
            if (textPath != null && !textPath.equals("")) {
                try (BufferedReader br = Files.newBufferedReader(textPath)) {
                    String str;
                    while ((str = br.readLine()) != null) {
                        // 改行だけの行は飛ばす。
                        // if(str.equals(System.getProperty("line.separator")))
                        if (str.isEmpty()) {
                            continue;
                        }
                        bodyElement.appendChild(createElement(document,
                                "p", new HashMap<String, String>() {
                                    private static final long serialVersionUID = 1L;
                                    {
                                        String className = targetText.get(PageSetting.KEY_ATTR_CLASS);
                                        if(Util.isValueValid(className))
                                        {
                                        	put("class", className);
                                        }
                                    }}));
                        setOptions(targetText, (Element)bodyElement.getLastChild());

                        bodyElement.getLastChild().appendChild(
                                document.createTextNode(Epub3Maker
                                        .textNodeValue(str, " ")));
                    }
                }
            }
        } else if (pageSetting.getTextData(i).get(PageSetting.KEY_ATTR_ATTRIBUTE)
                .equals(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_PREFORMAT)) {
            //System.out.println(builder.isValidating());

//                List<String> s = Files.readAllLines(textPath);
//                s.add(1, "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1 plus MathML 2.0//EN\" \"http://www.w3.org/TR/MathML2/dtd/xhtml-math11-f.dtd\">");
//                StringBuilder sb = new StringBuilder();
//                for(String line : s)
//                {
//                	sb.append(line).append(System.lineSeparator());
//                }
//                InputStream is = new ByteArrayInputStream(sb.toString().getBytes());
//
//                Document partDocument = builder.parse(is);
            Document partDocument = builder.parse(textPath.toString());
            NodeList list = partDocument.getChildNodes();
            for (int nodeNumber = 0, nodeListSize = list.getLength(); nodeNumber < nodeListSize; ++nodeNumber) {
            	Node node = list.item(nodeNumber);
            	String name = node.getNodeName();
            	if(node != null && !name.equals("html"))
            	{
            		bodyElement.appendChild(document.importNode(node, true));
            	}
            }
        } else {// if (attribute
           //     .equals(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_STRING)) {
            bodyElement.appendChild(createElement(document, "p", null));
            setOptions(targetText, (Element)bodyElement.getLastChild());
            bodyElement.getLastChild().appendChild(
                    document.createTextNode(Epub3Maker.textNodeValue(
                            targetText.get("value"), " ")));
        }
    }

    /**
     * @param pageSetting
     * @param document
     * @param bodyElement
     * @param objectNumber
     */
    protected void appendObject(Course couser, PageSetting pageSetting, Document document,
            Element bodyElement, int objectNumber) {
        Map<String, String> targetObject = pageSetting.getSettings()
                .get(PageSetting.KEY_OBJECT).get(objectNumber);
        Path objectValue = pageSetting.getObjectPath(objectNumber);
        if (objectValue != null && !objectValue.equals("")) {
            Path archiveTextPath = pageSetting.getTextPathForArchiveFile();
            String objectPath = archiveTextPath.getParent()
                    .relativize(objectValue).toString()
                    .replaceAll("\\\\", "/");
            final String attribute = targetObject.get(PageSetting.KEY_ATTR_ATTRIBUTE);
            if (attribute.equals(PageSetting.VALUE_ATTR_ATTRIBUTE_OBJECT_IMAGE)) {
//                bodyElement
//                        .appendChild(createElement(document, "div", null));
                String className = targetObject.get(PageSetting.KEY_ATTR_CLASS);
                //if(Util.isValueValid(className))
                {
                    appendNewDivElement(document,bodyElement, className);
                }

                Element imageElement = createElement(document, "img",
                        new HashMap<String, String>() {
                    private static final long serialVersionUID = 1L;
                    {
                        put("alt", "");
                        put("src", objectPath);

                    }
                });

                setOptions(targetObject, imageElement);
                bodyElement.getLastChild().appendChild(imageElement);
            } else if(attribute.equals(PageSetting.VALUE_ATTR_ATTRIBUTE_OBJECT_VIDEO)){
                String className = targetObject.get(PageSetting.KEY_ATTR_CLASS);
                //if(Util.isValueValid(className))
                {
                    appendNewDivElement(document,bodyElement, className);
                }
                Element video = createElement(document, "video",
                        new HashMap<String, String>() {
                            private static final long serialVersionUID = 1L;
                            {
                                put("controls", "controls");
                                put("src", objectPath);
                            }
                        });
                video.appendChild(document
                        .createTextNode("video tag not supported"));

                setOptions(targetObject, video);
                bodyElement.getLastChild().appendChild(video);
            }
        }
    }

    private static final String OPTION_DELIMITER = " ";
	void setOptions(Map<String, String> targetObject, Element targetElement) {
		String option = targetObject.get(PageSetting.KEY_ATTR_OPTION);
		if(Util.isValueValid(option))
		{
			StringBuilder sb = new StringBuilder(option);
			String[] options = escapeDelimiterInValue(sb).toString().split(OPTION_DELIMITER);
			for(String opt : options)
			{
				int equalSignPos = opt.indexOf('=');
				String attributeName = opt.substring(0, equalSignPos);
				String attributeValue = unescapeDelimiterInValue(sb.replace(0, sb.length(), opt.substring(equalSignPos + 1))).toString();
				if(attributeValue.charAt(0) == '"')
				{
					attributeValue = attributeValue.substring(1, attributeValue.length() - 1);
				}
				targetElement.setAttribute(attributeName, attributeValue);
			}
		}
	}

	private static final String ESCAPED_OPTION_DELIMITER = "\n";
	private StringBuilder escapeDelimiterInValue(StringBuilder option)
	{
		boolean inValue = false;
		for(int i = 0, length = option.length(); i < length; ++i)
		{
			char c = option.charAt(i);
			if(c == '\"')
			{
				inValue = !inValue;
				continue;
			}
			if(inValue && c == OPTION_DELIMITER.charAt(0))
			{
				option.setCharAt(i, ESCAPED_OPTION_DELIMITER.charAt(0));
			}
		}
		return option;
	}

	private StringBuilder unescapeDelimiterInValue(StringBuilder option)
	{
		for(int i = 0, length = option.length(); i < length; ++i)
		{
			char c = option.charAt(i);
			if(c == ESCAPED_OPTION_DELIMITER.charAt(0))
			{
				option.setCharAt(i, OPTION_DELIMITER.charAt(0));
			}
		}
		return option;
	}

    /**
     * @param pageSetting
     * @param document
     * @param bodyElement
     */
    void appendPageSection(PageSetting pageSetting, Document document,
            Element bodyElement) {
        String subsubject = pageSetting.getSection();
        if (Util.isValueValid(subsubject)) {
            Element sectionElement = document.createElement("h2");
            bodyElement.appendChild(sectionElement);

            setElementClass(sectionElement,pageSetting.getSectionClass());
            setOptions(pageSetting.getSettings().get(PageSetting.KEY_SUBSUBJECT).get(0), sectionElement);

            bodyElement.getLastChild().appendChild(
                    document.createTextNode(Epub3Maker.textNodeValue(
                            subsubject, " ")));
        }
    }

    /**
     * @param pageSetting
     * @param document
     * @param bodyElement
     */
    protected void appendCommunityButton(PageSetting pageSetting,
            Document document, Element bodyElement) {
        String communityButtonUrl = pageSetting.getCommunityButton();
        if(Util.isValueValid(communityButtonUrl))
        {
            Element divElement = appendNewDivElement(document,bodyElement, pageSetting.getCommunityButtonImageClass());
            setOptions(pageSetting.getSettings().get(PageSetting.KEY_COMMUNITY_BUTTON_IMAGE).get(0), divElement);

        	Element elementCommunityButtonLink = document.createElement("a");
        	elementCommunityButtonLink.setAttribute("href", communityButtonUrl);
        	divElement.appendChild(elementCommunityButtonLink);

        	Element elementCommunityButtonImage = document.createElement("img");
            Path communityButtonImagePath = pageSetting.getCommunityButtonImagePath();
        	if(communityButtonImagePath == null)
        	{
        		elementCommunityButtonImage.setAttribute("src", COMMUNITY_BUTTON_PATH.toString().replaceAll("\\\\", "/"));
        	}
        	else
        	{
        		elementCommunityButtonImage.setAttribute("src", pageSetting.getTextPathForArchiveFile().getParent().relativize(communityButtonImagePath).toString().replaceAll("\\\\", "/"));
        	}
        	setOptions(pageSetting.getSettings().get(PageSetting.KEY_COMMUNITY_BUTTON).get(0), elementCommunityButtonLink);
        	elementCommunityButtonLink.appendChild(elementCommunityButtonImage);

        }
    }

    protected Element appendNewDivElement(Document document, Element targetElement, String className)
    {
        return (Element)targetElement.appendChild(createNewDivElement(document, className));
    }

    private Element createNewDivElement(Document document, String className)
    {
        Element divElement = document.createElement("div");
        setElementClass(divElement, className);
        return divElement;
    }

    /**
     * @param pageSetting
     * @param document
     * @param bodyElement
     * @throws Epub3MakerException 
     */
    protected void appendPageChapter(PageSetting pageSetting, Document document,
            Element bodyElement) throws Epub3MakerException {
        String attr = pageSetting.getSettings().get(PageSetting.KEY_SUBJECT)
                .get(0).get(PageSetting.KEY_ATTR_ATTRIBUTE);
        
        if (attr == null) {
            throw new Epub3MakerException("Chapter: attribute が未設定です");
        } else if (PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_STRING.equalsIgnoreCase(attr)) {
            appendPageChapterString(pageSetting, document, bodyElement);
        } else if (attr.startsWith(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_SVG)) {
            String magni = attr.substring(PageSetting.VALUE_ATTR_ATTRIBUTE_TEXT_SVG.length());
            appendPageChapterSVG(pageSetting, document, bodyElement, Integer.parseInt(magni));
        } else {
            throw new Epub3MakerException("Chapter: 不正な attribute です: " + attr);
        }
    }

    /**
     * @param pageSetting
     * @param document
     * @param bodyElement
     */
    private void appendPageChapterString(PageSetting pageSetting, Document document,
            Element bodyElement) {
        String subject = pageSetting.getChapter();
        if (Util.isValueValid(subject)) {
        	Element subjectElement = document.createElement("h1");
            bodyElement.appendChild(subjectElement);

            setElementClass(subjectElement, pageSetting.getChapterClass());
            setOptions(pageSetting.getSettings().get(PageSetting.KEY_SUBJECT).get(0), subjectElement);

            bodyElement.getLastChild().appendChild(
                    document.createTextNode(Epub3Maker.textNodeValue(subject,
                            " ")));
        }
    }

    /**
     * SVGを使う場合は item-property に svg の指定が必要
     * 
     * @param pageSetting
     * @param document
     * @param bodyElement
     * @param magni
     */
    void createSVGnode(String subject, Element subjectElement, 
    		Document document, Element bodyElement,
    		String boxWidth, String boxHeight, String bgColor, 
    		boolean withRect, String fontBasePos,
    		String textRatio, String align, String fontFamily, String fontStroke, 
    		String fgColor, int magni) {
        /*
         * avg constant attributes
         * xmlns="http://www.w3.org/2000/svg" 
         * xmlns:xlink="http://www.w3.org/1999/xlink" 
         * viewbox="0 0 6000 1000" preserveAspectRatio="xMidYMin meet"
         */
        subjectElement.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        subjectElement.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");
        subjectElement.setAttribute("viewBox", "0 0 " + boxWidth + " " + boxHeight);
        subjectElement.setAttribute("preserveAspectRatio", "none");
        subjectElement.setAttribute("width", Config.getChapterWidthRatio() + "%");
        
        /*
         * rect element
         */
        if (withRect) {
        	Element rectElement = document.createElement("rect");
        	subjectElement.appendChild(rectElement);
        	rectElement.setAttribute("width", boxWidth);
        	rectElement.setAttribute("height", boxHeight);
        	rectElement.setAttribute("fill", bgColor);
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
        
        int fontsize = 
                Math.min(subject.length() != 0 
                        ? vwidth * (10 + magni) / (subject.length() * 10) 
                        : vheight, vheight)
                    * textRation / 100;
        int ty = vheight - (vheight - fontsize) / 2 - basePos;

        Element textElement = document.createElement("text");
        subjectElement.appendChild(textElement);
        textElement.setAttribute("x", "" + tx);
        textElement.setAttribute("y", "" + ty);
        textElement.setAttribute("font-size", "" + fontsize);
        textElement.setAttribute("font-family", fontFamily);
        textElement.setAttribute("stroke", fontStroke);
        textElement.setAttribute("fill", fgColor);
        textElement.setAttribute("text-anchor", textAnchor);

        textElement.appendChild(
        		document.createTextNode(Epub3Maker.textNodeValue(subject, " ")));
    }
    
    private void appendPageChapterSVG(PageSetting pageSetting, Document document,
            Element bodyElement, int magni) {
        String subject = pageSetting.getChapter();
    	if (!Util.isValueValid(subject))
    		return;
        /*
         * style で位置指定などがしやすいので div でくくる
         */
        Element divElement = document.createElement("div");
        bodyElement.appendChild(divElement);
        Element subjectElement = document.createElement("svg");
        divElement.appendChild(subjectElement);

        createSVGnode(subject, subjectElement, document, bodyElement, 
        		Const.ChapterViewBoxWidth, Const.ChapterViewBoxHeight, Config.getChapterBGColor(),
        		true, Const.ChapterFontBasePos, Config.getChapterTextRatio(), Config.getChapterTextAlign(),
        		Config.getChapterFontFamily(), Config.getChapterFontStroke(), Config.getChapterFGColor(), magni);

        setElementClass(divElement, pageSetting.getChapterClass());
        setOptions(pageSetting.getSettings().get(PageSetting.KEY_SUBJECT).get(0), subjectElement);
    }

    boolean setElementClass(Element element, String className)
    {
    	if(Util.isValueValid(className))
    	{
    		element.setAttribute("class", className);
    		return true;
    	}
    	return false;
    }

    Element createElement(Document document, String tagName,
            Map<String, String> attributes) {
        Element element = document.createElement(tagName);

        if (attributes == null) {
            return element;
        }

        Iterator<Entry<String, String>> ite = attributes.entrySet().iterator();
        while (ite.hasNext()) {
            final Entry<String, String> attribute = ite.next();
            element.setAttribute(attribute.getKey(), attribute.getValue());
        }

        return element;
    }

    static public String textNodeValue(String value, String alternativeValue) {
        // textNodeに空文字をいれちゃうと、パース時に例外発生するっぽいので、
        if (value == null || value.isEmpty()) {
            return alternativeValue;
        }
        return value;
    }

    protected Path outputTempDirectory;
    public static final int DIRECTORY_DEPTH = 3;
    private static final Path COMMUNITY_BUTTON_PATH = Paths.get("..", "..", "common", "images", "community-button.png");
    public static final String NAVIGATION_DOCUMENT_FILE_NAME = "nav.xhtml";
    public static final String MATHJAX_CONFIG_SCRIPT =
    		"\n            MathJax.Hub.Config({\n" + //System.lineSeparator() +
    		"                SVG: { linebreaks: { width: \"100%\" } },\n" + //System.lineSeparator() +
    		"                jax: [\"input/TeX\",\"input/MathML\",\"output/SVG\"],\n" + //System.lineSeparator() +
    		"                extensions: [\"tex2jax.js\",\"mml2jax.js\",\"MathEvents.js\"],\n" + //System.lineSeparator() +
    		"                tex2jax: {\n" + //System.lineSeparator() +
    		"                    inlineMath: [ ['$','$'], [\"\\\\(\",\"\\\\)\"] ],\n" + //System.lineSeparator() +
    		"                    displayMath: [ ['$$','$$'], [\"\\\\[\",\"\\\\]\"] ]\n" + //System.lineSeparator() +
			"                },\n" + //System.lineSeparator() +
			"                TeX: {\n" + //System.lineSeparator() +
			"                    extensions: [\"noErrors.js\",\"noUndefined.js\",\"autoload-all.js\"]\n" + //System.lineSeparator() +
			"                },\n" + //System.lineSeparator() +
			"                MathMenu: {\n" + //System.lineSeparator() +
			"                    showRenderer: false\n" + //System.lineSeparator() +
			"                },\n" + //System.lineSeparator() +
			"                menuSettings: {\n" + //System.lineSeparator() +
			"                    zoom: \"none\"\n" + //System.lineSeparator() +
			"                },\n" +// System.lineSeparator() +
			"                messageStyle: \"none\"\n" +// System.lineSeparator() +
			"           });\n";
}
