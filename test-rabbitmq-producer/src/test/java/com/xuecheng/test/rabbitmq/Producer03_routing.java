package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer03_routing {
    //队列
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_ROUTING_INFORM = "exchange_routing_inform";
    private static final String ROUTINGKEY_EMAIL = "routing_email";
    private static final String ROUTINGKEY_SMS = "routing_sms";

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
            //声明两个消息队列
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);
            channel.queueDeclare(QUEUE_INFORM_SMS, true, false, false, null);
            //声明交换机
            /**
             * 参数明细：
             * 1、交换机的名称
             * 2、交换机的类型
             *        fanout:对应的rabbitmq的工作模式时：publish/subscribe
             *        direct：对应的routing工作模式
             *        topic:对应的Topic工作模式
             *        headers:对应的headers工作模式
             *
             */
            channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.DIRECT);
            //进行交换机和队列绑定
            /**
             * 参数明细：
             *  1、queue队列名称
             *  2、exchange,交换机名称
             *  3、routingKey,路由KEy，作用是交换机根据路由Key的值将消息转发到指定的队列中，在发布订阅模式中协调为空字符串
             */
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_ROUTING_INFORM,ROUTINGKEY_EMAIL);
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_ROUTING_INFORM,"inform");
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_ROUTING_INFORM,ROUTINGKEY_SMS);
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_ROUTING_INFORM,"inform");
            //发送消息
            /**
             * 1、exchange、交换机，人如果不指定，将使用mq的默认交换机
             * 2、routingKey，路由key，交换机根据路由key来将消息转发到指定的队列，如果使用默认的交换机，routingKey设置为队列的名称
             * 3、props，消息的属性
             * 4、body.消息内容
             */
            /*for (int i = 0; i < 5; i++) {
                // 消息内容
                String message = "send email inform message to User";

                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTINGKEY_EMAIL, null, message.getBytes());
                System.out.println("send to mq success"+message);
            }
            for (int i = 0; i < 5; i++) {
                // 消息内容
                String message = "send sms inform message to User";

                channel.basicPublish(EXCHANGE_ROUTING_INFORM, ROUTINGKEY_SMS, null, message.getBytes());
                System.out.println("send to mq success"+message);
            }*/

            for (int i = 0; i < 5; i++) {
                // 消息内容
                String message = "send infrom inform message to User";

                channel.basicPublish(EXCHANGE_ROUTING_INFORM, "inform", null, message.getBytes());
                System.out.println("send to mq success"+message);
            }

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
