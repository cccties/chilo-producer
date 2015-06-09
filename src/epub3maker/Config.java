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
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * 外部 XML ファイルから設定を読み込むクラス
 *
 * @author tueda
 *
 */
public class Config {
    public static Properties prop = new Properties();

    public static final String CourseBaseDirKey = "CourseBaseDir";
    public static final String OutputBaseDirKey = "OutputBaseDir";
    public static final String LogLevelKey = "LogLevel";
    public static final String ProcessAllMetaKey = "ProcessAllMeta";
    public static final String ChapterFGColorKey = "ChapterFGColor";
    public static final String ChapterBGColorKey = "ChapterBGColor";
    public static final String ChapterWidthRatioKey = "ChapterWidthRatio";
    public static final String ChapterTextRatioKey = "ChapterTextRatio";
    public static final String ChapterFontFamilyKey = "CahpterFontFamily";
    public static final String ChapterFontStrokeKey = "ChapterFontStroke";
    public static final String ChapterFontStrokeWidthKey = "ChapterFontStrokeWidth";
    public static final String ChapterTextAlignKey = "ChapterTextAlign";
    public static final String PublishStyleKey = "PublishStyle";
    public static final String CourseVersionKey = "CourseVersion";
    public static final String InputPathKey = "InputPath";
    public static final String OutputPathKey = "OutputPath";
    public static final String OutputNameKey = "OutputName";
    
    public static final String SectionFGColorKey = "SectionFGColor";
    public static final String SectionBGColorKey = "SectionBGColor";
    public static final String SectionWidthRatioKey = "SectionWidthRatio";
    public static final String SectionTextRatioKey = "SectionTextRatio";
    public static final String SectionFontFamilyKey = "SectionFontFamily";
    public static final String SectionFontStrokeKey = "SectionFontStroke";
    public static final String SectionFontStrokeWidthKey = "SectionFontStrokeWidth";
    public static final String SectionTextAlignKey = "SectionTextAlign";

    public static final String SVGTextZenkakuRatioKey = "SVGTextZenkakuRatio";
    public static final String SVGTextHankakuRatioKey = "SVGTextHankakuRatio";
    
    private static final String defaultConfigFile = "chilo-epub3-maker.xml";
    
    public static ResourceBundle localStrings = ResourceBundle.getBundle("strings");
    
    private void read(String filename) throws InvalidPropertiesFormatException,
            IOException, Epub3MakerException {
        InputStream stream = new FileInputStream(filename);
        prop.loadFromXML(stream);
        stream.close();

        if (LogLevel.LOG_DEBUG.compareTo(LogLevel.valueOf(Config.getCurrentLogLevel())) <= 0) {
            prop.list(System.out);
        }

        if (prop.getProperty(CourseBaseDirKey) == null) {
            throw new Epub3MakerException("!!! CourseBaseDir is not set !!!");
        }
        if (prop.getProperty(OutputBaseDirKey) == null) {
            throw new Epub3MakerException("!!! OutputBaseDir is not set !!!");
        }
    }

    public Config(String filename) throws InvalidPropertiesFormatException,
            IOException, Epub3MakerException {
        if (filename == null) {
            filename = defaultConfigFile;
        }
        read(filename);
    }

    public Config() throws InvalidPropertiesFormatException, IOException,
            Epub3MakerException {
        read(defaultConfigFile);
    }

    public static void setProcessAllMeta(boolean is) {
        prop.setProperty(ProcessAllMetaKey, is ? "true" : "false");
    }
    
    public static void setPublishStyle(String format) {
    	prop.setProperty(PublishStyleKey, format);
    }
    
    public static void setCourseVersion(String version) {
    	prop.setProperty(CourseVersionKey, version);
    }

    public static boolean isProcessAllMeta() {
        return prop.getProperty(ProcessAllMetaKey).equalsIgnoreCase("true") ? true : false;
    }

    public static String getCourseBaseDir() {
        return prop.getProperty(CourseBaseDirKey);
    }
    
    public static String getOutputBaseDir() {
        return prop.getProperty(OutputBaseDirKey);
    }
    
    public static String getChapterFGColor() {
        return prop.getProperty(ChapterFGColorKey, "white");
    }

    public static String getChapterBGColor() {
        return prop.getProperty(ChapterBGColorKey, "orange");
    }
    
    public static String getChapterWidthRatio() {
        return prop.getProperty(ChapterWidthRatioKey, "100");
    }

    public static String getChapterTextRatio() {
        return prop.getProperty(ChapterTextRatioKey, "90");
    }

    public static String getChapterFontFamily() {
        return prop.getProperty(ChapterFontFamilyKey, "serif");
    }
    
    public static String getChapterFontStroke() {
        return prop.getProperty(ChapterFontStrokeKey, "none");
    }

    public static String getChapterFontStrokeWidth() {
        return prop.getProperty(ChapterFontStrokeWidthKey, "1");
    }

    public static String getChapterTextAlign() {
        return prop.getProperty(ChapterTextAlignKey, "center");
    }
    
    public static String getSectionFGColor() {
        return prop.getProperty(SectionFGColorKey, "blue");
    }
    
    public static String getSectionBGColor() {
        return prop.getProperty(SectionBGColorKey, "white");
    }
    
    public static String getSectionWidthRatio() {
        return prop.getProperty(SectionWidthRatioKey, "100");
    }

    public static String getSectionTextRatio() {
        return prop.getProperty(SectionTextRatioKey, "90");
    }

    public static String getSectionFontFamily() {
        return prop.getProperty(SectionFontFamilyKey, "serif");
    }
    
    public static String getSectionFontStroke() {
        return prop.getProperty(SectionFontStrokeKey, "none");
    }

    public static String getSectionFontStrokeWidth() {
        return prop.getProperty(SectionFontStrokeWidthKey, "1");
    }

    public static String getSectionTextAlign() {
        return prop.getProperty(SectionTextAlignKey, "left");
    }
    
    public static String getPublishStyle() {
    	return prop.getProperty(PublishStyleKey, "epub3");
    }
    
    public static int getCourseVersion() {
    	String s = prop.getProperty(CourseVersionKey, "2");
    	return Integer.parseInt(s); 
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
        return prop.getProperty(OutputNameKey, "default");
    }

    /**
     * 現在のLogLevelを返す．デフォルトは LOG_INFO．
     * @return
     */
    public static String getCurrentLogLevel() {
        return prop.getProperty(Config.LogLevelKey, "LOG_INFO");
    }
}
