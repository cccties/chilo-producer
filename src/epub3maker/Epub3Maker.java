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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.logging.*;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.Log4JLogChute;

public class Epub3Maker {

	private static Log log = LogFactory.getLog(Epub3Maker.class);

    public static void main(String[] args) {

    	/*
         * 引数を処理
         */
    	String homeDir = null;
        String configFile = null;
        String seriesDir = null;
        String template = "basic";
        String inputPath = "./";
        String outputPath = "./";
        String outputName = "";
        boolean doWeko = false;

        for (int ai = 0; ai < args.length && args[ai].startsWith("-"); ai++) {
            if (args[ai].equals("-home")) {
                if (ai < args.length - 1) {
                    homeDir = args[++ai];
                    continue;
                } else {
                    usage();
                }
            } else if (args[ai].equals("-config")) {
            	if (ai < args.length - 1) {
            		configFile = args[++ai];
            		continue;
            	} else {
            		usage();
            	}
            } else if (args[ai].equals("-series")) {
                if (ai < args.length - 1) {
                    seriesDir = args[++ai];
                    continue;
                } else {
                    usage();
                }
            } else if (args[ai].equals("-template")) {
                if (ai < args.length - 1) {
                    template = args[++ai];
                    continue;
                } else {
                    usage();
                }
            } else if (args[ai].equals("-input-path")) {
                if (ai < args.length - 1) {
                    inputPath = args[++ai];
                } else {
                    usage();
                }
            } else if (args[ai].equals("-output-path")) {
                if (ai < args.length - 1) {
                    outputPath = args[++ai];
                } else {
                    usage();
                }
            } else if (args[ai].equals("-output-name")) {
                if (ai < args.length - 1) {
                    outputName = args[++ai];
                } else {
                    usage();
                }
            } else if (args[ai].equals("-weko")) {
            	doWeko = true;
            }
        }

        Velocity.addProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, "velocity");

        try {
            /*
             * 設定値を読込
             */
            new Config(configFile, homeDir);

            Config.setTemplate(template);
            Config.setInputPath(inputPath);
            Config.setOutputPath(outputPath);
            Config.setOutputName(outputName);

            ArrayList<Path> seriesList = new ArrayList<Path>();
            Path seriesBasePath = Paths.get(Config.getSeriesBaseDir());
            if (seriesDir == null) {
                /*
                 * series dir 一覧を取得
                 */
                DirectoryStream<Path> stream = Files.newDirectoryStream(
                        seriesBasePath, new DirectoryStream.Filter<Path>() {
                            @Override
                            public boolean accept(Path entry)
                                    throws IOException {
                                return Files.isDirectory(entry);
                            }
                        });
                for (Path p : stream) {
                    seriesList.add(p);
                }

            } else if(seriesDir.endsWith(".xlsx")){
                seriesList.add(Paths.get(seriesDir));
            } else {
                seriesList.add(seriesBasePath.resolve(seriesDir));
            }

            Process proc = new Process();
            for (Path c : seriesList) {
            	proc.process(c, doWeko);
            }
        } catch (Exception e) {
            log.fatal(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void usage() {
        /*
         * meta file (現状は xlsx）は，series dir 直下にある前提
         */
        System.err.println("Epub3Maker -series <series dir> -config <config file> -input-path <path> -output-path <path> -output-name <string>");
        System.exit(1);
    }
}
