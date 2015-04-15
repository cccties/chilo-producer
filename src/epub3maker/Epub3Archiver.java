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

public class Epub3Archiver {
    public void archive(Course course, Volume volume, List<Path> inputFilePaths)
            throws Exception {

        Path output = Paths.get(
                course.getMeta().get(Course.KEY_OUTPUT_PATH),
                course.getMeta().get(Course.KEY_COURSE_NAME) + "-vol-"
                        + volume.getVolume() + ".epub");
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(output.toFile())));// charset指定しないと日本語ばける？

        writeMimetypeFile(
                Paths.get(course.getMeta().get(Course.KEY_INPUT_PATH), "temp",
                        "mimetype"), zos); // mimetypeファイルが最初のエントリーである必要あり
        writePackageDocumentFile(Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "temp", "content.opf"),
                zos);
        writeContainerFile(Paths.get(course.getMeta(Course.KEY_INPUT_PATH),
                "temp", "container.xml"), zos);
        writeNavigationDocumentFile(Paths.get(
                course.getMeta(Course.KEY_INPUT_PATH), "temp", Epub3Maker.NAVIGATION_DOCUMENT_FILE_NAME),
                zos);

        zos.setMethod(ZipOutputStream.DEFLATED);
        // zos.setLevel(9);
        for (Path inputPath : inputFilePaths) {
            String p = makePathForArchiveFile(inputPath);
            ZipEntry entry = new ZipEntry(p);

            zos.putNextEntry(entry);
            zos.write(Files.readAllBytes(inputPath));
        }
        zos.close();
    }
    
    public void archive(Course course, Volume volume, List<Path> inputFilePaths, String OutputTempDir)
            throws Exception {

        String outputName = course.getMeta(Course.KEY_OUTPUT_NAME);
        Path output = Paths.get(
                course.getMeta().get(Course.KEY_OUTPUT_PATH),
                (outputName != null ? outputName : "") + 
                course.getMeta().get(Course.KEY_COURSE_NAME) + "-vol-"
                        + volume.getVolume() + ".epub");
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
                new FileOutputStream(output.toFile())));// charset指定しないと日本語ばける？

        writeMimetypeFile(Paths.get(OutputTempDir, "mimetype"), zos); // mimetypeファイルが最初のエントリーである必要あり
        writePackageDocumentFile(Paths.get(OutputTempDir, "content.opf"),
                zos);
        writeContainerFile(Paths.get(OutputTempDir, "container.xml"), zos);
        writeNavigationDocumentFile(Paths.get(OutputTempDir, Epub3Maker.NAVIGATION_DOCUMENT_FILE_NAME), zos);

        zos.setMethod(ZipOutputStream.DEFLATED);
        // zos.setLevel(9);
        for (Path inputPath : inputFilePaths) {
            String p = makePathForArchiveFile(inputPath);
            ZipEntry entry = new ZipEntry(p);

            zos.putNextEntry(entry);
            zos.write(Files.readAllBytes(inputPath));
        }
        zos.close();
    }

    private void writeMimetypeFile(Path path, ZipOutputStream zos)
            throws IOException {
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

    private void writePackageDocumentFile(Path path, ZipOutputStream zos)
            throws IOException {
        ZipEntry entry = new ZipEntry("OEBPS/" + path.getFileName().toString());

        zos.putNextEntry(entry);
        zos.write(Files.readAllBytes(path));
    }

    private void writeNavigationDocumentFile(Path path, ZipOutputStream zos)
            throws IOException {
        ZipEntry entry = new ZipEntry("OEBPS/" + path.getFileName().toString());

        zos.putNextEntry(entry);
        zos.write(Files.readAllBytes(path));
    }

    private void writeContainerFile(Path path, ZipOutputStream zos)
            throws IOException {
        ZipEntry entry = new ZipEntry("META-INF/"
                + path.getFileName().toString());

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

    public String makePathForArchiveFile(Path inputFilePath) throws Epub3MakerException {
        return Paths
                .get("OEBPS")
                .resolve(
                		Epub3Maker.subtractBasePath(inputFilePath)).toString()
//                        inputFilePath.subpath(inputFilePath.getNameCount() - Epub3Maker.DIRECTORY_DEPTH,
//                                inputFilePath.getNameCount())).toString()
                .replaceAll("\\\\", "/");
    }

    public void  gatherFiles(Course course, Volume volume, List<Path> inputFilePaths, String OutputTempDir) throws Epub3MakerException, IOException
    {
        for (Path inputPath : inputFilePaths) {
        	Path outFile = Paths.get(OutputTempDir, Epub3Maker.subtractBasePath(inputPath).toString().replace("web-styles", "styles"));
        	Path outDir = outFile.getParent();
        	Files.createDirectories(outDir);
        	Files.copy(inputPath, outFile);
        }
    }
}
