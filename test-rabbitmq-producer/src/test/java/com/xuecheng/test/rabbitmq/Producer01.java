package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer01 {
    //队列
    private static final String QUEUE = "helloword";

    public static void main(String[] args) {
        //通过连接工厂，创建新的连接，和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.42.134");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");

        //设置虚拟机
        connectionFactory.setVirtualHost("/");

        Channel channel = null;

        //建立新的连接
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();

            //设置创建会话通道
            channel = connection.createChannel();
            //声明队列
            /**
             * 参数明细
             * 1、队列明细
             * 2、是否持久化，如果持久化，mq重启后队列还在
             * 3、exclusive，是否独占连接，队列只允许在该连接中访问，如果连接关闭，队列自动删除，
             *      如果将参数设置true，可用于临时队列的创建
             * 4、autoDelete 自动删除，队列不再使用是，是否自动删除此队列，如果将此参数和excluscanshu
             *      设置为true,就可以实现临时队列（队列不用了就自动删除
             *          ）
             * 5、arguments,参数，可以设置一个队列的扩展参数，比如，可设置存活时间
             */
            channel.queueDeclare(QUEUE, true, false, false, null);
            //发送消息
            /**
             * 1、exchange、交换机，人如果不指定，将使用mq的默认交换机
             * 2、routingKey，路由key，交换机根据路由key来将消息转发到指定的队列，如果使用默认的交换机，routingKey设置为队列的名称
             * 3、props，消息的属性
             * 4、body.消息内容
             */
            // 消息内容
            String message = "hello world 黑马程序员";

            channel.basicPublish("", QUEUE, null, message.getBytes());
            System.out.println("send to mq success"+message);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            //关闭连接

            //先关闭通道

            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }

            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
