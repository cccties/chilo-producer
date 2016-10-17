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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Epub3Archiver {

    @SuppressWarnings("unused")
	private static Log log = LogFactory.getLog(Epub3Archiver.class);

	public void archive(Series series, Book book, List<Path> inputFilePaths, Path tempPath, List<Path> extensionPaths) throws Exception {
		Path output = series.getEpubFilePath(book);

        // charset指定しないと日本語ばける？
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(output.toFile())));

        // mimetypeファイルが最初のエントリーである必要あり
        writeMimetypeFile(tempPath.resolve("mimetype"), zos);
        writeFile("OEBPS/", tempPath.resolve("content.opf"), zos);
        writeFile("META-INF/", tempPath.resolve("container.xml"), zos);
        writeFile("OEBPS/", tempPath.resolve(Process.NavigationDocument.CARDVIEW.fileName), zos);
        writeFile("OEBPS/", tempPath.resolve(Process.NavigationDocument.NAV.fileName), zos);

        zos.setMethod(ZipOutputStream.DEFLATED);
        // zos.setLevel(9);

        for (Path p: inputFilePaths) {
        	writeInputFile(p, zos);
        }

        for (Path p: extensionPaths) {
            writeFile("", p, zos);
        }

        zos.close();
    }

    private void writeMimetypeFile(Path path, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(path.getFileName().toString());
        long size = Files.size(path);
        entry.setSize(size);
        entry.setCompressedSize(size);
        entry.setCrc(getCRCValue(path));
        // entry.setCrc(0x2CAB616F);
        entry.setMethod(ZipEntry.STORED);

        zos.putNextEntry(entry);
        zos.write(Files.readAllBytes(path));
    }

    private void writeFile(String dir, Path path, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(dir + path.getFileName().toString());

        zos.putNextEntry(entry);
        zos.write(Files.readAllBytes(path));
    }

    private void writeInputFile(Path path, ZipOutputStream zos) throws Exception {
        ZipEntry entry = new ZipEntry("OEBPS/" + Util.path2str(Process.subtractBasePath(path)));

        zos.putNextEntry(entry);
        zos.write(Files.readAllBytes(path));
    }

    /**
     * ファイルのCRC-32チェックサムを取得する。
     *
     * @param file
     *            ファイル
     * @return CRC-32チェックサム
     * @throws IOException
     *             ファイル入出力エラー
     */
    private long getCRCValue(Path file) throws IOException {
        CRC32 crc = new CRC32();

        try (BufferedInputStream input = new BufferedInputStream(
                new FileInputStream(file.toFile()))) {
            int b;
            while ((b = input.read()) != -1) {
                crc.update(b);
            }
        }
        return crc.getValue();
    }
}
