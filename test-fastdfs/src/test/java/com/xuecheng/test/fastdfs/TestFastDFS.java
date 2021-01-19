package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {
    //上传文件

    @Test
    public void testUpload() {

        try {
            //必须线加载fasrDFS-client.properties配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient,用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //向Stroage上传文件
                //先创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
                //正式上传文件
            //先确当个本地文件路径
            String filePath = "F:/xcEdu/xc_ui/xc-ui-pc-static-portal/img/asset-logo.png";
            String fileId = storageClient1.upload_file1(filePath, "png", null);
            System.out.println(fileId);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

    }

    //下载文件
    @Test
    public void testDownload() {

        try {
            //必须线加载fasrDFS-client.properties配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient,用于请求TrackerServer
            TrackerClient trackerClient = new TrackerClient();
            //连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取Stroage
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer);
            //向Stroage上传文件
            //先创建stroageClient
            StorageClient1 storageClient1 = new StorageClient1(trackerServer,storeStorage);
            //下载文件
                //文件id
            String fileId = "group1/M00/00/00/wKgqgl_0VkiABi9lAAARA0xqVgo618.png";
            byte[] bytes = storageClient1.download_file1(fileId);
            //使用输出流保存文件
            FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/xcEdu/xc_ui/logo2.png"));
            fileOutputStream.write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

}
