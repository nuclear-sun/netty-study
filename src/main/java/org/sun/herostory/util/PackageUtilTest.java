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

    static public Set<Class<?>> listSubClazz(
            String packageName,
            boolean recursive,
            Class<?> superClazz) {

        Set<Class<?>> results = new HashSet<>();

        return null;

    }

    /**
     * 扫描一个包下所有的类，返回符合条件的类
     *
     * @param pack
     * @param recursive
     * @param filter
     * @return
     */
    public static Class<?>[] listClasses(String base, String pack, boolean recursive, Function<Class<?>, Boolean> filter) throws Exception {

        if(pack == null || pack.isEmpty()) {
            return null;
        }

        List<Class<?>> result = new ArrayList<>();

        String path = pack.replace(".", "/");

        File baseFile = new File(base);

        if(baseFile.isDirectory()) {

            URL[] urls = {new URL("file://" + baseFile.getAbsolutePath() + "/")};
            ClassLoader parent = Thread.currentThread().getContextClassLoader();
            URLClassLoader urlClassLoader = new URLClassLoader(urls, parent);

            Path basePath = baseFile.toPath();
            File realBase = new File(baseFile, path);

            Queue<File> q = new LinkedList<>();

            File[] files = realBase.listFiles();

            if(files != null) {

                for (File file : files) {
                    q.offer(file);
                }

                while (!q.isEmpty()) {

                    // 取出当前节点
                    File curr = q.poll();

                    // 访问当前节点
                    if (curr.isFile() || curr.getAbsoluteFile().toPath().endsWith(".class")) {
                        Path absolutePath = curr.getAbsoluteFile().toPath();
                        String relativize = basePath.relativize(absolutePath).toString();

                        String className = relativize.split("\\.")[0].replace("/", ".");

                        Class<?> clazz = Class.forName(className, false, urlClassLoader);

                        if(filter == null || filter.apply(clazz)) {
                            result.add(clazz);
                        }

                    } else if (curr.isDirectory()) {

                        if(recursive) {

                            File[] subFiles = curr.listFiles();
                            if(subFiles != null) {
                                for (File subFile : subFiles) {
                                    q.offer(subFile);
                                }
                            }
                        }

                    } else {
                        System.out.println("无法识别的文件类型：" + curr.getAbsolutePath());
                    }

                }
            }

        } else if(baseFile.isFile()) {

        }

        if(result == null) {
            return null;
        }
        Class<?>[] classes = new Class<?>[result.size()];
        return result.toArray(classes);
    }

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
