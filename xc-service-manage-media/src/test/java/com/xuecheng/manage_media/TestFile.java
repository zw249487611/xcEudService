package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//@SpringBootTest
//@RunWith(SpringRunner.class)
public class TestFile {
    //测试文件分块
    @Test
    public void testChunk() throws Exception {
        //源文件
        File sourcefile = new File("F:\\xcEdu\\video\\ffmpegTest\\lucene.avi");
        //块文件目录
        String chunkFileFolder = "F:\\xcEdu\\video\\ffmpegTest\\chunks\\";

        //先定义块文件大小
        long chunkFIleSize = 1 * 1024 * 1024;
        //块数
        long chunkFileSum = (long) Math.ceil(sourcefile.length() * 1.0 / chunkFIleSize);

        //创建读文件的对象
        //缓冲区
        byte[] b = new byte[1024];
        RandomAccessFile raf_read = new RandomAccessFile(sourcefile, "r");
        for (int i = 0; i < chunkFileSum; i++) {
            //块文件
            File chunkFile = new File(chunkFileFolder + i);

            RandomAccessFile raf_write = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                //创建一个向块文件写的对象

                raf_write.write(b, 0, len);
                //如果块文件的大小大于1M的时候，就开始写下一块
                if (chunkFile.length() >= chunkFIleSize) {
                    break;

                }
            }
            raf_write.close();
        }
        raf_read.close();
    }

    //测试文件合并
    @Test
    public void testMergeFile() throws IOException {
        //块文件目录
        String chunkFileFolderPath = "F:\\xcEdu\\video\\ffmpegTest\\chunks\\";
        //块文件目录对象
        File chunkFileFolder = new File(chunkFileFolderPath);
        //块文件列表
        File[] files = chunkFileFolder.listFiles();
        //将块文件排序
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                    return 1;
                }
                return -1;
            }
        });
        //文件合并
        File mergeFile = new File("F:\\xcEdu\\video\\ffmpegTest\\lucene2.avi");

        //创建新文件
        boolean newFile = mergeFile.createNewFile();
        //创建写对象
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        byte[] b = new byte[1024];//缓冲区
        for (File chunkFile : fileList) {
            //创建读的块文件对象
            RandomAccessFile raf_read = new RandomAccessFile(chunkFile, "r");
            int len = -1;
            while ((len = raf_read.read(b)) != -1) {
                raf_write.write(b, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();
    }
}
