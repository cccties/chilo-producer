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

import java.io.UnsupportedEncodingException;

public class StringValidator {
    private static boolean checkCharacterCode(String str, String encoding) {
        if (str == null) {
            return false;
        }

        try {
            byte[] bytes = str.getBytes(encoding);
            return str.equals(new String(bytes, encoding));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("エンコード名称が正しくありません。", ex);
        }
    }

    public static boolean isWindows31j(String str) {
        return checkCharacterCode(str, "Windows-31j");
    }

    public static boolean isSJIS(String str) {
        return checkCharacterCode(str, "SJIS");
    }

    public static boolean isEUC(String str) {
        return checkCharacterCode(str, "euc-jp");
    }

    public static boolean isUTF8(String str) {
        return checkCharacterCode(str, "UTF-8");
    }

    private static final String[] NAMES = { "UTF-8", "SJIS", "euc-jp",
            "Windows-31j" };

    public static String getCharsetName(String str) {
        for (int i = 0; i < NAMES.length; ++i) {
            if (checkCharacterCode(str, NAMES[i])) {
                return NAMES[i];
            }
        }
        return "unknown charset";
    }
}
