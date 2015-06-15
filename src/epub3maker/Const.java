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

import java.util.HashMap;
import java.util.Map;

/**
 * 定数クラス
 * 
 * @author tueda
 *
 */
public class Const {
    public static Map<String, String> mediaTypes = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put("gif", "image/gif");
            put("png", "image/png");
            put("jpg", "image/jpeg");
            put("svg", "image/svg+xml");
            put("xhtml", "application/xhtml+xml");
            put("ncx", "application/x-dtbncx+xml");
            put("otf", "application/vnd.ms-opentype");
            put("woff", "application/font-woff");
            put("smil", "application/smil+xml");
            put("smi", "application/smil+xml");
            put("pls", "application/pls+xml");
            put("css", "text/css");
            put("js", "text/javascript");
            put("mp3", "audio/mpeg");
            put("mp4", "video/mp4");
        }
    };

    /*
     * Chapter Title 関連 
     */
    final public static String ChapterViewBoxWidth = "3000";
    final public static String ChapterViewBoxHeight = "360";
    final public static String ChapterFontBasePos = "30";
    final public static String ChapterTextAlignLeft = "left";
    final public static String ChapterTextAlignCenter = "center";
    final public static String ChapterTextAlignRight = "right";
    
    final public static String SectionViewBoxWidth = "3000";
    final public static String SectionViewBoxHeight = "360";
    final public static String SectionFontBasePos = "30";
    final public static String SectionTextAlignLeft = "left";
    final public static String SectionTextAlignCenter = "center";
    final public static String SectionTextAlignRight = "right";

    /*
     * pngファイル 　　　configできるようにしたい 
     */
    final public static String SKIP_IMG = "common/images/skip.png";
    final public static String COMMUNITY_IMG = "b_portal.png";
    final public static String TEST_PAGE_IMG = "b_ts.png";
}
