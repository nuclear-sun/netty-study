package org.sun.herostory.util;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

public class PackageUtil {


    /**
     * 私有化构造器
     */
    private PackageUtil() {}

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
            URLClassLoader urlClassLoader = new URLClassLoader(urls);

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
                    if (curr.isFile()) {
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

    public static void main(String[] args) throws Exception {

        File file = new File(System.getProperty("user.dir"));
        File base = new File(file, "target/classes/");


        Class<?>[] classes = PackageUtil.listClasses(base.getAbsolutePath(), "org.sun.herostory", true, null);
        for (Class<?> clazz : classes) {

            String name = clazz.getName();
            System.out.println(name);

        }
    }
}
