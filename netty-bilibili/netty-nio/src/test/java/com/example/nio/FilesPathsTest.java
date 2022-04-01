package com.example.nio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

@DisplayName("Files 和 Paths 常用方法")
public class FilesPathsTest {

    @Test
    void pathTest() throws IOException {
        Path path = Paths.get("from.txt");

        // 检测文件是否存在
        boolean exists = Files.exists(path);
        System.out.println(exists);

        // 创建多级目录
        if (!exists) {
            Files.createDirectory(path);
            Files.createDirectories(path);
        }

        // 复制文件
        Files.copy(path, Paths.get("dest.txt"), StandardCopyOption.REPLACE_EXISTING);
        // 移动文件
        Files.move(path, Paths.get("dest.txt"));
        // 删除文件，非空目录会抛异常
        Files.delete(path);

    }

    /**
     *  遍历目录
     */
    @Test
    void getFiles() throws IOException {
        AtomicInteger fileCount = new AtomicInteger(0);
        AtomicInteger dirCount = new AtomicInteger(0);
        Path path = Paths.get("/Users/qgs/IdeaProjects/netty-all");
        // 遍历文件夹 和 文件 (访问者)
        Files.walkFileTree(path, new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("-- " + dir);
                dirCount.incrementAndGet();
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                fileCount.incrementAndGet();
                return super.visitFile(file, attrs);
            }
        });
        System.out.println("fileCount.get() = " + fileCount.get());
        System.out.println("dirCount.get() = " + dirCount.get());
    }

    /**
     * 删除多级目录
     */
    @Test
    void deleteDir() throws IOException {
        Files.walkFileTree(Path.of(""), new SimpleFileVisitor<>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }

    /**
     * 复制目录
     */
    @Test
    void moveDir() throws IOException {
        String source = "/User/source";
        String dest = "/User/dest";

        Files.walk(Paths.get(source)).forEach(path -> {
            try {
                String destName = path.toString().replace(source, dest);
                // 目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(destName));
                } else if (Files.isRegularFile(path)) {
                    // 普通文件
                    Files.copy(path, Paths.get(destName));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

    }
}
