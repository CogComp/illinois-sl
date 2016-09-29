package edu.illinois.cs.cogcomp.sl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class WeightVectorUtils {

    private final static Logger log = LoggerFactory.getLogger(WeightVectorUtils.class);

    public static void save(String fileName, WeightVector wv) throws IOException {
        BufferedOutputStream stream =
                new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileName)));

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));

        float[] w = wv.getWeightArray();

        writer.write("WeightVector");
        writer.newLine();

        writer.write(w.length + "");
        writer.newLine();

        int numNonZero = 0;
        for (int index = 0; index < w.length; index++) {
            if (w[index] != 0) {
                writer.write(index + ":" + w[index]);
                writer.newLine();
                numNonZero++;
            }
        }

        writer.close();

        log.info("Number of non zero weights: " + numNonZero);
    }

    public static WeightVector load(String fileName) {
        try {
            GZIPInputStream zipin = new GZIPInputStream(new FileInputStream(fileName));

            BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

            String line;

            line = reader.readLine().trim();
            if (!line.equals("WeightVector")) {
                reader.close();
                throw new IOException("Invalid model file.");
            }

            line = reader.readLine().trim();
            int size = Integer.parseInt(line);

            WeightVector w = new WeightVector(size);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(":");
                int index = Integer.parseInt(parts[0]);
                float value = Float.parseFloat(parts[1]);
                w.setElement(index, value);
            }

            zipin.close();

            return w;
        } catch (Exception e) {
            log.error("Error loading model file {}", fileName);
            System.exit(-1);
        }
        return null;
    }

    public static WeightVector loadWeightVectorFromClassPath(String fileName) {
        try {
            Class<WeightVectorUtils> clazz = WeightVectorUtils.class;
            List<URL> list = lsResources(clazz, fileName);

            if (list.size() == 0) {
                log.error("File {} not found on the classpath", fileName);
                throw new Exception("File not found on classpath");
            }
            InputStream stream = list.get(0).openStream();

            GZIPInputStream zipin = new GZIPInputStream(stream);

            BufferedReader reader = new BufferedReader(new InputStreamReader(zipin));

            String line;

            line = reader.readLine().trim();
            if (!line.equals("WeightVector")) {
                reader.close();
                throw new IOException("Invalid model file.");
            }

            line = reader.readLine().trim();
            int size = Integer.parseInt(line);

            WeightVector w = new WeightVector(size);

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split(":");
                int index = Integer.parseInt(parts[0]);
                float value = Float.parseFloat(parts[1]);
                w.setElement(index, value);
            }

            zipin.close();
            return w;
        } catch (Exception e) {
            log.error("Error loading model file {}", fileName);
            System.exit(-1);
        }
        return null;
    }


    /**
     * Lists resources that are contained within a path. This works for any resource on the
     * classpath, either in the file system or in a jar file. The function returns a list of URLs,
     * connections to which can be opened for reading.
     * <p>
     * <b>NB</b>: This method works only for full file names. If you need to list the files of a
     * directory contained in the classpath use lsResourcesDir(Class, String) in illinois-core-utilities
     *
     * @param clazz The class whose path is scanned
     * @param path The name of the resource(s) to be returned
     * @return A list of URLs
     */
    public static List<URL> lsResources(Class clazz, String path) throws URISyntaxException,
            IOException {
        URL dirURL = clazz.getResource(path);

        if (dirURL == null) {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            dirURL = loader.getResource(path);
        }

        if (dirURL == null) {
            return new ArrayList<>();
        }

        String dirPath = dirURL.getPath();
        if (dirURL.getProtocol().equals("file")) {
            String[] list = new File(dirURL.toURI()).list();
            List<URL> urls = new ArrayList<>();

            if (list == null) {
                // if the list is null, but the dirURL is not, then dirURL is
                // actually a file!
                urls.add(dirURL);
            } else {
                for (String l : list) {
                    URL url = (new File(dirPath + File.separator + l)).toURI().toURL();
                    urls.add(url);
                }
            }
            return urls;
        }

        if (dirURL.getProtocol().equals("jar")) {
            int exclamation = dirPath.indexOf("!");
            String jarPath = dirPath.substring(5, exclamation);
            String jarRoot = dirPath.substring(0, exclamation + 1);

            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries();

            List<URL> urls = new ArrayList<>();
            while (entries.hasMoreElements()) {
                JarEntry element = entries.nextElement();

                String name = element.getName();

                // Because the path string comes from JarEntry, We SHOULD use
                // '/'' instead of File.SEPERATOR.
                // And it seems that the only way to figure out if a JarEntry
                //  path is a folder or file is to check the last character.
                if (name.startsWith(path) && !name.equals(path + "/")) {
                    URL url = new URL("jar:" + jarRoot + "/" + name);
                    urls.add(url);
                }
            }
            return urls;
        }
        throw new UnsupportedOperationException("Cannot list files for URL " + dirURL);
    }
}
