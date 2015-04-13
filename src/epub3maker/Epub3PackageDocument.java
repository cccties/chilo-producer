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

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.FileTypeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Epub3PackageDocument {

    public Epub3PackageDocument(Path opfPath) {
        this.opfPath = opfPath;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            initializeDocument(this.opfPath, builder);
        } catch (ParserConfigurationException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    public Document getDocument() {
        return this.document;
    }

    public Path getOPFPath() {
        return this.opfPath;
    }

    private void initializeDocument(Path opfPath, DocumentBuilder builder)
            throws SAXException, IOException {
        // if(Files.exists(opfPath))
        // {
        // this.document = builder.parse(opfPath.toString());
        // }
        // else
        // {
        this.document = builder.newDocument();
        // }
    }

    public Element setPackageElement(String version, String uniqueIdentifier) {
        return this.setPackageElement(version, uniqueIdentifier, null);
    }

    public Element setPackageElement(String version, String uniqueIdentifier,
            Map<String, String> otherAttributes) {
        NodeList list = this.document.getElementsByTagName(TAG_PACKAGE.TAG_NAME
                .getLabel());

        if (list.getLength() != 0) {
            this.document.removeChild(list.item(0));
        }

        Map<String, String> attributes = otherAttributes;
        if (attributes == null) {
            attributes = new HashMap<>();
        }

        attributes.put(Epub3PackageDocument.NAMESPACE_XML.PREFIX.getLabel(),
                Epub3PackageDocument.NAMESPACE_XML.URI.getLabel());
        attributes.put(TAG_PACKAGE.ATTR_VERSION.getLabel(), version);
        attributes.put(TAG_PACKAGE.ATTR_UID.getLabel(), uniqueIdentifier);
        Element packageElement = createElement(this.document, "package",
                attributes);

        this.document.appendChild(packageElement);
        return packageElement;
    }

    public Element setMetadata(String identifier, String identifierId,
            String title, String language, String dctermsModified) {
        Element packageElement = getElementsByTagName(
                TAG_PACKAGE.TAG_NAME.getLabel(), 0);
        if (packageElement == null) {
            return null;
        }

        Element metadataElement = createElement(document,
                TAG_METADATA.TAG_NAME.getLabel(),
                new HashMap<String, String>() {
                    {
                        put(NAMESPACE_XML.PREFIX.getLabel() + ":"
                                + NAMESPACE_DC.PREFIX.getLabel(),
                                NAMESPACE_DC.URI.getLabel());
                    }
                });
        packageElement.appendChild(metadataElement);

        metadataElement.appendChild(createElement(this.document,
                TAG_DCMES_IDENTIFIER.TAG_NAME.getLabel(),
                new HashMap<String, String>() {
                    {
                        put(TAG_DCMES_IDENTIFIER.ATTR_ID.getLabel(),
                                identifierId);
                    }
                }));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(identifier, "???")));

        metadataElement.appendChild(
                document.createElement(TAG_DCMES_TITLE.TAG_NAME.getLabel()));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(title, " ")));

        metadataElement.appendChild(document
                .createElement(TAG_DCMES_LANGUAGE.TAG_NAME.getLabel()));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(language, "ja")));

        metadataElement.appendChild(createElement(this.document,
                TAG_META.TAG_NAME.getLabel(), new HashMap<String, String>() {
                    {
                        put(TAG_META.ATTR_PROPERTY.getLabel(),
                                "dcterms:modified");
                    }
                }));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(dctermsModified, " ")));

        return metadataElement;
    }

    public Element setMetadataPublishedDate(String date) {
        return setMetadataDate("publication", date);
    }

    public Element setMetadataModifiedDate(Element metadataElement, String date) {
        return setMetadataDate("modification", date);
    }

    public Element setMetadataDate(String type, String date) {
        Element metadataElement = getElementsByTagName(
                TAG_METADATA.TAG_NAME.getLabel(), 0);
        if (metadataElement == null) {
            return null;
        }

        metadataElement.appendChild(createElement(this.document,
                TAG_DCMES_DATE.TAG_NAME.getLabel(),
                new HashMap<String, String>() {
                    {
                        // put(TAG_DCMES_DATE.ATTR_OPF_EVENT.getLabel(), type);
                    }
                }));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(date, "")));

        return metadataElement;
    }

    public Element setMetadataCreator(String creator) {
        Element metadataElement = getElementsByTagName(
                TAG_METADATA.TAG_NAME.getLabel(), 0);
        if (metadataElement == null) {
            return null;
        }

        metadataElement.appendChild(createElement(this.document,
                TAG_DCMES_CREATOR.TAG_NAME.getLabel(), null));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(creator, " ")));

        return metadataElement;
    }

    public Element setMetadataRights(String rights) {
        Element metadataElement = getElementsByTagName(
                TAG_METADATA.TAG_NAME.getLabel(), 0);
        if (metadataElement == null) {
            return null;
        }

        metadataElement.appendChild(createElement(this.document,
                TAG_DCMES_RIGHTS.TAG_NAME.getLabel(), null));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(rights, " ")));

        return metadataElement;
    }

    public Element setMetadataCover(String coverId) {
        Element metadataElement = getElementsByTagName(
                TAG_METADATA.TAG_NAME.getLabel(), 0);
        if (metadataElement == null) {
            return null;
        }

        metadataElement.appendChild(createElement(this.document,
                TAG_META.TAG_NAME.getLabel(), new HashMap<String, String>() {
                    {
                        put(TAG_META.ATTR_NAME.getLabel(), "cover");
                        put(TAG_META.ATTR_CONTENT.getLabel(), coverId);
                    }
                }));

        return metadataElement;
    }

    public Element setMetadataPublisher(String publisher) {
        Element metadataElement = getElementsByTagName(
                TAG_METADATA.TAG_NAME.getLabel(), 0);
        if (metadataElement == null) {
            return null;
        }
        
        metadataElement.appendChild(createElement(this.document,
                TAG_DCMES_PUBLISHER.TAG_NAME.getLabel(), new HashMap<String, String>() {
        	/**
        	 * 
        	 */
        	private static final long serialVersionUID = 1L;
			{
        		put(TAG_META.ATTR_ID.getLabel(), "publisher0");
        	}
        }));
        metadataElement.getLastChild().appendChild(
                document.createTextNode(Epub3Maker.textNodeValue(publisher, " ")));

        return metadataElement;
    }

    public Element setManifest(Map<Path, String> paths, Map<String, String> mediaTypes) throws DOMException, Epub3MakerException {
        Element packageElement = getElementsByTagName(
                TAG_PACKAGE.TAG_NAME.getLabel(), 0);
        if (packageElement == null
                || getElementsByTagName(TAG_METADATA.TAG_NAME.getLabel(), 0) == null) {
            return null;
        }

        Element manifestElement = this.document
                .createElement(TAG_MANIFEST.TAG_NAME.getLabel());

        packageElement.appendChild(manifestElement);

        for (Iterator<Entry<Path, String>> ite = paths.entrySet().iterator(); ite.hasNext();) {
            Entry<Path, String> e = ite.next();
            Path path = e.getKey();
            manifestElement.appendChild(createElement(this.document,
                    TAG_ITEM.TAG_NAME.getLabel(),
                    new HashMap<String, String>() {
                        {
//                            Path partPath = path.subpath(
//                                    path.getNameCount() - Epub3Maker.DIRECTORY_DEPTH,
//                                    path.getNameCount());
                        	Path partPath = Epub3Maker.subtractBasePath(path);
                            put(TAG_ITEM.ATTR_HREF.getLabel(), partPath
                                    .toString().replaceAll("\\\\", "/"));

                            String tempPathStr = partPath
                                    .toString().replaceAll("\\\\", "/");
                            put(TAG_ITEM.ATTR_ID.getLabel(), tempPathStr.replaceAll("/", "-"));

                            put(TAG_ITEM.ATTR_MEDIA_TYPE.getLabel(),
                                    getMediaType(path.getFileName().toString(),
                                            mediaTypes));

                            if(Util.isValueValid(e.getValue()))
                            {
                                put(TAG_ITEM.ATTR_PROPERTIES.getLabel(), e.getValue());
                            }
                        }
                    }));
        }

        manifestElement.appendChild(createElement(this.document,
                TAG_ITEM.TAG_NAME.getLabel(), new HashMap<String, String>() {
                    {
                        final String fileName = "nav.xhtml";
                        put(TAG_ITEM.ATTR_HREF.getLabel(), fileName);

                        put(TAG_ITEM.ATTR_ID.getLabel(), fileName);

                        put(TAG_ITEM.ATTR_MEDIA_TYPE.getLabel(),
                                getMediaType(fileName, mediaTypes));

                        put(TAG_ITEM.ATTR_PROPERTIES.getLabel(), "nav");
                    }
                }));

        return manifestElement;
    }

    public Element setManifestCoverImage(String path,
            Map<String, String> mediaTypes) {
        Element manifestElement = getElementsByTagName(
                TAG_MANIFEST.TAG_NAME.getLabel(), 0);
        if (getElementsByTagName(TAG_MANIFEST.TAG_NAME.getLabel(), 0) == null) {
            return null;
        }

        // manifestElement.getElementsByTagName(name)
        manifestElement.appendChild(createElement(this.document,
                TAG_ITEM.TAG_NAME.getLabel(), new HashMap<String, String>() {
                    {
                        put(TAG_ITEM.ATTR_PROPERTIES.getLabel(), "cover-image");
                    }
                }));

        return manifestElement;
    }

    String getMediaType(String fileName, Map<String, String> mediaTypes) {
        int extPos = fileName.lastIndexOf('.');
        if (extPos != -1) {
            String type;
            if ((type = mediaTypes.get(fileName.substring(extPos + 1))) == null) {
                FileTypeMap fileTypeMap = FileTypeMap.getDefaultFileTypeMap();
                return fileTypeMap.getContentType(fileName);
            } else {
                return type;
            }
        }

        return "application/octet-stream";
    }

    public Element setSpine(List<String> paths) {
        
        /*
         * debug
         */
        if (LogLevel.LOG_DEBUG.compareTo(LogLevel.valueOf(Config.getCurrentLogLevel())) <= 0) {
            Util.infoPrintln(LogLevel.LOG_DEBUG, "Epub3PackageDocument#setSpine: ");
            for (String p : paths) {
                Util.infoPrintln(LogLevel.LOG_DEBUG, "  " + p);
            }
        }
        
        Element packageElement = getElementsByTagName(
                TAG_PACKAGE.TAG_NAME.getLabel(), 0);
        if (packageElement == null
                || getElementsByTagName(TAG_METADATA.TAG_NAME.getLabel(), 0) == null
                || getElementsByTagName(TAG_MANIFEST.TAG_NAME.getLabel(), 0) == null) {
            return null;
        }

        Element spineElement = this.document.createElement(TAG_SPINE.TAG_NAME
                .getLabel());

        packageElement.appendChild(spineElement);

        for (String path : paths) {
            spineElement.appendChild(createElement(this.document,
                    TAG_ITEMREF.TAG_NAME.getLabel(),
                    new HashMap<String, String>() {
                        {
                            put(TAG_ITEMREF.ATTR_IDREF.getLabel(),
                                    path.replace("/", "-"));
                        }
                    }));
        }

        return spineElement;
    }

    private Element getElementsByTagName(String tagName, int i) {
        NodeList list = this.document.getElementsByTagName(tagName);
        if (list == null || list.getLength() <= i) {
            return null;
        }
        return (Element) list.item(i);
    }

    static private Element createElement(Document document, String tagName,
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

    private Document document;
    private Path opfPath;

    public static enum NAMESPACE_XML {
        PREFIX("xmlns"), URI("http://www.idpf.org/2007/opf");

        private String label;

        NAMESPACE_XML(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public static enum NAMESPACE_DC {
        PREFIX("dc"), URI("http://purl.org/dc/elements/1.1/");

        private String label;

        NAMESPACE_DC(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // http://imagedrive.github.io/spec/epub301-publications.xhtml#sec-package-documents
    // package 要素は Package Document のルート要素である。
    public static enum TAG_PACKAGE {
        TAG_NAME("package"), ATTR_VERSION("version"), ATTR_UID(
                "unique-identifier"), ATTR_PREFIX("prefix"), ATTR_NS_XML_LANG(
                "xml:lang"), ATTR_DIR("dir"), ATTR_ID("id");

        private String label;

        TAG_PACKAGE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // package の最初の子要素であること（required）。
    public static enum TAG_METADATA {
        TAG_NAME("metadata");

        private String label;

        TAG_METADATA(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の子要素であること（required）。繰り返し可能
    public static enum TAG_DCMES_IDENTIFIER {
        TAG_NAME("dc:identifier"), ATTR_ID("id");

        private String label;

        TAG_DCMES_IDENTIFIER(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の子要素であること（required）。繰り返し可
    public static enum TAG_DCMES_TITLE {
        TAG_NAME("dc:title"), ATTR_ID("id"), ATTR_NS_XML_LANG("xml:lang"), ATTR_NS_XML_DIR(
                "dir"), ;

        private String label;

        TAG_DCMES_TITLE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の子要素であること（required）
    public static enum TAG_DCMES_LANGUAGE {
        TAG_NAME("dc:language"), ATTR_ID("id"), ;

        private String label;

        TAG_DCMES_LANGUAGE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の省略可能な子要素。繰り返し可能
    public static enum TAG_DCMES_CONTRIBUTOR {
        TAG_NAME("dc:contributor"), ATTR_ID("id"), ATTR_NS_XML_LANG("xml:lang"), ATTR_DIR(
                "dir"), ;

        private String label;

        TAG_DCMES_CONTRIBUTOR(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の省略可能な子要素。繰り返し可能
    public static enum TAG_DCMES_CREATOR {
        TAG_NAME("dc:creator"), ATTR_ID("id"), ATTR_NS_XML_LANG("xml:lang"), ATTR_DIR(
                "dir"), ;

        private String label;

        TAG_DCMES_CREATOR(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の省略可能な子要素。繰り返し可能
    public static enum TAG_DCMES_RIGHTS {
        TAG_NAME("dc:rights"), ATTR_ID("id"), ATTR_NS_XML_LANG("xml:lang"), ATTR_DIR(
                "dir"), ;

        private String label;

        TAG_DCMES_RIGHTS(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の省略可能な子要素。繰り返し可能
    public static enum TAG_DCMES_DATE {
        TAG_NAME("dc:date"), ATTR_ID("id"), ;
        // ATTR_OPF_EVENT("event"),;

        private String label;

        TAG_DCMES_DATE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の省略可能な子要素。繰り返し可能
    public static enum TAG_DCMES_SOURCE {
        TAG_NAME("dc:source"), ATTR_ID("id"), ;

        private String label;

        TAG_DCMES_SOURCE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の省略可能な子要素。繰り返し可能
    public static enum TAG_DCMES_TYPE {
        TAG_NAME("dc:type"), ATTR_ID("id"), ;

        private String label;

        TAG_DCMES_TYPE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    public static enum TAG_DCMES_PUBLISHER{
        TAG_NAME("dc:publisher"), ATTR_ID("id"),;

        private String label;

        TAG_DCMES_PUBLISHER(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の子要素。繰り返し可能
    public static enum TAG_META {
        TAG_NAME("meta"), ATTR_PROPERTY("property"), // 必須
        ATTR_REFINES("refines"), // コンテキストに依存
        ATTR_ID("id"), ATTR_SCHEME("scheme"), ATTR_NS_XML_LANG("xml:lang"), ATTR_DIR(
                "dir"), ATTR_CONTENT("content"), ATTR_NAME("name"), ;

        private String label;

        TAG_META(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の子要素。繰り返し可能
    public static enum TAG_LINK {
        TAG_NAME("link"), ATTR_HREF("href"), // 必須
        ATTR_REL("rel"), // 必須
        ATTR_REFINES("refines"), ATTR_ID("id"), ATTR_MEDIA_TYPE("media-type"), ;

        private String label;

        TAG_LINK(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // metadata の次の package の二番目の子要素であること（required）。
    public static enum TAG_MANIFEST {
        TAG_NAME("manifest"), ATTR_ID("id"), ;

        private String label;

        TAG_MANIFEST(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // manifestの子要素。繰り返し可能
    public static enum TAG_ITEM {
        TAG_NAME("item"), ATTR_ID("id"), // 必須
        ATTR_HREF("href"), // 必須
        ATTR_MEDIA_TYPE("media-type"), // 必須
        ATTR_FALLBACK("fallback"), // 条件つきで必須
        ATTR_PROPERTIES("properties"), ATTR_MEDIA_OVERLAY("media-overlay"), ;

        private String label;

        TAG_ITEM(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // manifest の次に記述される package の三番目の必須の子要素
    public static enum TAG_SPINE {
        TAG_NAME("spine"), ATTR_ID("id"), ATTR_TOC("toc"), ATTR_PAGE_PROGRESSION_DIRECTION(
                "page-progression-direction"), ;

        private String label;

        TAG_SPINE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // spine の子要素。繰り返し可
    public static enum TAG_ITEMREF {
        TAG_NAME("itemref"), ATTR_IDREF("idref"), // 必須
        ATTR_LINEAR("linear"), ATTR_ID("id"), ATTR_PROPERTIES("properties"), ;

        private String label;

        TAG_ITEMREF(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // spine または guide の次に来る、package の4番目か5番目の省略可能な子要素。
    public static enum TAG_BINDINGS {
        TAG_NAME("bindings"), ;

        private String label;

        TAG_BINDINGS(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // bindings の子要素。繰り返し可能
    public static enum TAG_MEDIA_TYPE {
        TAG_NAME("mediaType"), ATTR_MEDIA_TYPE("media-type"), ATTR_HANDLER(
                "handler"), ;

        private String label;

        TAG_MEDIA_TYPE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    // package の六番目の省略可能な要素。繰り返し可。
    public static enum TAG_COLLECTION {
        TAG_NAME("collection"), ATTR_LANG("lang"), ATTR_DIR("dir"), ATTR_ID(
                "id"), ATTR_ROLE("role"), // 必須
        ;

        private String label;

        TAG_COLLECTION(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }
}
