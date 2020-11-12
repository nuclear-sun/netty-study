package org.sun.herostory.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sun.herostory.handler.ICmdHandler;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PackageUtilTest {

    private static final Logger logger = LoggerFactory.getLogger(PackageUtilTest.class);

    public interface IClassFilter {
        boolean accept(Class<?> clazz);
    }


    /**
     * 私有化构造器
     */
    private PackageUtilTest() {}



    public static Collection<Class<?>> loadClassesInDir(File baseDir, String packageName, boolean recursive, IClassFilter filter)
            throws Exception {

        if(baseDir == null || !baseDir.isDirectory() || packageName == null) {
            return null;
        }


        Path basePath = baseDir.toPath();
        String replace = packageName.replace(".", "/");
        File scanBaseDir = new File(baseDir, replace);

        Set<Class<?>> results = new HashSet<>();

        URL[] urls = new URL[]{baseDir.toURI().toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

        File[] subFiles = scanBaseDir.listFiles();

        if(subFiles == null || subFiles.length == 0) {
            return results;
        }

        Queue<File> queue = new LinkedList<>(Arrays.asList(subFiles));

        while (!queue.isEmpty()) {

            File curr = queue.poll();

            if(curr.isFile() && curr.getAbsolutePath().endsWith(".class")) {

                Path relativize = basePath.relativize(curr.toPath());

                String tmp = relativize.toString().replace("\\", ".").replace("/", ".");

                int lastIndex = tmp.lastIndexOf(".");

                String className = tmp.substring(0, lastIndex);

                Class<?> aClass = Class.forName(className, true, urlClassLoader);

                if(filter == null || filter.accept(aClass)) {
                    results.add(aClass);
                }

            } else if(curr.isDirectory() && recursive) {
                File[] children = curr.listFiles();
                if(children != null) {
                    for (File child : children) {
                        queue.offer(child);
                    }
                }
            } else {
                // unknown condition
                continue;
            }
        }

        return results;
    }

    public static Collection<Class<?>> loadClassesInJar(File jarFile, String packageName, boolean recursive, IClassFilter filter) throws Exception {

        if(jarFile == null || !jarFile.isFile()) {
            return null;
        }

        // 使用 URLClassLoader 添加新的路径
        URL[] urls = {jarFile.toURI().toURL()};
        URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

        JarInputStream jarInputStream = null;

        Set<Class<?>> results = new HashSet<>();

        try {
            jarInputStream = new JarInputStream(new FileInputStream(jarFile));
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {

                if (entry.isDirectory()) {
                    continue;
                } else {
                    String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }

                    // 验证是否在目标包下
                    String tmp = name.replace("/", ".");
                    int lastIndexOf = tmp.lastIndexOf(".");
                    String className = tmp.substring(0, lastIndexOf);

                    if (!className.startsWith(packageName)) {
                        continue;
                    }

                    int lastIndexOfDot = className.lastIndexOf(".");
                    String realPackageName = className.substring(0, lastIndexOfDot);
                    if(!recursive && !realPackageName.equals(packageName)) {
                        continue;
                    }

                    Class<?> aClass = Class.forName(className, false, urlClassLoader);
                    if (filter == null || filter.accept(aClass)) {
                        results.add(aClass);
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if(jarInputStream != null) {
                jarInputStream.close();
            }
        }

        return results;

    }

    private static void testInjar() throws Exception {

        File file = new File("target/herostory-1.0-SNAPSHOT.jar");
        String packageName = "org.sun.herostory";

        IClassFilter filter = new IClassFilter() {
            @Override
            public boolean accept(Class<?> clazz) {
                int modifiers = clazz.getModifiers();
                if((modifiers & Modifier.ABSTRACT) != 0) {
                    return false;
                }

                if(ICmdHandler.class.isAssignableFrom(clazz)) {
                    return true;
                }
                return false;
            }
        };

        IClassFilter filter1 = new IClassFilter() {
            @Override
            public boolean accept(Class<?> clazz) {

                if("ICmdHandler".equals(clazz.getSimpleName())) {
                    return true;
                }
                return false;
            }
        };

        Collection<Class<?>> classes = loadClassesInJar(file, packageName, true, filter);

        for (Class<?> aClass : classes) {
            System.out.println(aClass.getName());
        }
    }

    private static void testInDir() throws Exception {

        File file = new File(System.getProperty("user.dir"));
        File base = new File(file, "target/classes/");

        String packageName = "org.sun.herostory";

        IClassFilter filter = new IClassFilter() {
            @Override
            public boolean accept(Class<?> clazz) {
                int modifiers = clazz.getModifiers();
                if((modifiers & Modifier.ABSTRACT) != 0) {
                    return false;
                }

                if(ICmdHandler.class.isAssignableFrom(clazz)) {
                    return true;
                }
                return false;
            }
        };

        Collection<Class<?>> classes1 = PackageUtilTest.loadClassesInDir(base, packageName, true, filter);

        for (Class<?> clazz : classes1) {

            String name = clazz.getName();
            System.out.println(name);
        }
    }

    public static void main(String[] args) throws Exception {

        //testInDir();
        testInjar();

    }
}
