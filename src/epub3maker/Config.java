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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 外部 XML ファイルから設定を読み込むクラス
 *
 * @author tueda
 *
 */
public class Config {

	private static Log log = LogFactory.getLog(Config.class);
	
    public static final String SeriesBaseDirKey = "SeriesBaseDir";
    public static final String OutputBaseDirKey = "OutputBaseDir";
    public static final String InputPathKey = "InputPath";
    public static final String OutputPathKey = "OutputPath";
    public static final String OutputNameKey = "OutputName";

    public static final String TemplateBaseDirKey = "TemplateBaseDir";
    public static final String TemplateKey = "Template";

    public static final String ExtensionBaseDirKey = "ExtensionBaseDir";

    public static final String Section = "Section";
    public static final String Topic = "Topic";

    public static final String FGColorKey = "FGColor";
    public static final String BGColorKey = "BGColor";
    public static final String WidthRatioKey = "WidthRatio";
    public static final String TextRatioKey = "TextRatio";
    public static final String FontFamilyKey = "FontFamily";
    public static final String FontStrokeKey = "FontStroke";
    public static final String FontStrokeWidthKey = "FontStrokeWidth";
    public static final String TextAlignKey = "TextAlign";

    public static final String SVGTextZenkakuRatioKey = "SVGTextZenkakuRatio";
    public static final String SVGTextHankakuRatioKey = "SVGTextHankakuRatio";
    
    private static String defaultFilename = "chilo-epub3-maker.xml";
	private static Properties prop = new Properties();
	private static Path homePath = null;

    private void read(Path path) throws InvalidPropertiesFormatException, IOException, Epub3MakerException {
        InputStream stream = new FileInputStream(path.toFile());
        prop.loadFromXML(stream);
        stream.close();

        if (log.isDebugEnabled()) {
        	log.debug("show settings in " + path);
            prop.list(System.out);
        }

        if (prop.getProperty(SeriesBaseDirKey) == null) {
            throw new Epub3MakerException("!!! SeriesBaseDir is not set !!!");
        }
        if (prop.getProperty(OutputBaseDirKey) == null) {
            throw new Epub3MakerException("!!! OutputBaseDir is not set !!!");
        }
        if (prop.getProperty(TemplateBaseDirKey) == null) {
            throw new Epub3MakerException("!!! TemplateBaseDir is not set !!!");
        }
    }

    public Config(String filename, String homeDir) throws InvalidPropertiesFormatException, IOException, Epub3MakerException {
    	if(homeDir != null){
    		homePath = Paths.get(homeDir);
    	}

    	Path path = null;
    	if(filename != null){
			path = Paths.get(filename);
    	} else if(homePath != null){
    		path = homePath.resolve(defaultFilename);
    	} else {
    		path = Paths.get(defaultFilename);
    	}

    	read(path);

    	if(homePath != null){
    		String str = prop.getProperty(TemplateBaseDirKey);
    		str = homePath.resolve(str).toString();
    		prop.setProperty(TemplateBaseDirKey, str);

    		prop.setProperty(ExtensionBaseDirKey, homePath.toString());
    	}
    }

    public static String getSeriesBaseDir() {
        return prop.getProperty(SeriesBaseDirKey);
    }
    
    public static String getOutputBaseDir() {
        return prop.getProperty(OutputBaseDirKey);
    }
    
    public static String getTemplateBaseDir() {
        return prop.getProperty(TemplateBaseDirKey);
    }
    
    public static String getSectionFGColor() {
        return prop.getProperty(Section + FGColorKey, "white");
    }

    public static String getSectionBGColor() {
        return prop.getProperty(Section + BGColorKey, "orange");
    }
    
    public static String getSectionWidthRatio() {
        return prop.getProperty(Section + WidthRatioKey, "100");
    }

    public static String getSectionTextRatio() {
        return prop.getProperty(Section + TextRatioKey, "90");
    }

    public static String getSectionFontFamily() {
        return prop.getProperty(Section + FontFamilyKey, "serif");
    }
    
    public static String getSectionFontStroke() {
        return prop.getProperty(Section + FontStrokeKey, "none");
    }

    public static String getSectionFontStrokeWidth() {
        return prop.getProperty(Section + FontStrokeWidthKey, "1");
    }

    public static String getSectionTextAlign() {
        return prop.getProperty(Section + TextAlignKey, "center");
    }
    
    public static String getTopicFGColor() {
        return prop.getProperty(Topic + FGColorKey, "blue");
    }
    
    public static String getTopicBGColor() {
        return prop.getProperty(Topic + BGColorKey, "white");
    }
    
    public static String getTopicWidthRatio() {
        return prop.getProperty(Topic + WidthRatioKey, "100");
    }

    public static String getTopicTextRatio() {
        return prop.getProperty(Topic + TextRatioKey, "90");
    }

    public static String getTopicFontFamily() {
        return prop.getProperty(Topic + FontFamilyKey, "serif");
    }
    
    public static String getTopicFontStroke() {
        return prop.getProperty(Topic + FontStrokeKey, "none");
    }

    public static String getTopicFontStrokeWidth() {
        return prop.getProperty(Topic + FontStrokeWidthKey, "1");
    }

    public static String getTopicTextAlign() {
        return prop.getProperty(Topic + TextAlignKey, "left");
    }
    
    public static double getSVGTextZenkakuRatio() {
        String s = prop.getProperty(SVGTextZenkakuRatioKey, "1");
        return Double.parseDouble(s);
    }
    
    public static double getSVGTextHankakuRatio() {
        String s = prop.getProperty(SVGTextHankakuRatioKey, "0.5");
        return Double.parseDouble(s);
    }
   
    public static void setInputPath(String path) {
        prop.setProperty(InputPathKey, path);
    }
    
    public static String getInputPath() {
        return prop.getProperty(InputPathKey, "./");
    }

    public static void setOutputPath(String path) {
        prop.setProperty(OutputPathKey, path);
    }
    
    public static String getOutputPath() {
        return prop.getProperty(OutputPathKey, "./");
    }

    public static void setOutputName(String name) {
        prop.setProperty(OutputNameKey, name);
    }
    
    public static String getOutputName() {
        return prop.getProperty(OutputNameKey, "");
    }

    public static void setTemplate(String name) {
    	prop.setProperty(TemplateKey, name);
    }

    public static String getTemplate() {
        return prop.getProperty(TemplateKey, "");
    }

    public static Path getTemplateDir(String name) {
    	String base = prop.getProperty(TemplateBaseDirKey, "");
    	String template = prop.getProperty(TemplateKey, ""); 
        return Paths.get(base, template, name);
    }

    public static Path getTemplateFile(String lang, String name) {
    	String template = prop.getProperty(TemplateKey, ""); 
        return Paths.get(template, "page_templates", lang, name);
    }

    public static Path getTemplateBaseFile(String lang, String name) {
    	String base = prop.getProperty(TemplateBaseDirKey, "");
    	String template = prop.getProperty(TemplateKey, ""); 
        return Paths.get(base, template, "page_templates", lang, name);
    }

    public static Path getExtensionBaseDir() {
    	String base = prop.getProperty(ExtensionBaseDirKey, "");
        return Paths.get(base);
    }
}
