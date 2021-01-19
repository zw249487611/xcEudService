package com.xuecheng.manage_cms;

import com.rabbitmq.client.*;

import java.io.IOException;

public class Consumer03_routing_email {
    //队列
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
//    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_ROUTING_INFORM = "exchange_routing_inform";
    private static final String ROUTINGKEY_EMAIL = "routing_email";

    public static void main(String[] args) throws Exception {
        //通过连接工厂，创建新的连接，和mq建立连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.42.134");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("admin");

        //设置虚拟机
        connectionFactory.setVirtualHost("/");

        Connection connection = connectionFactory.newConnection();

        Channel channel = connection.createChannel();

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

        channel.queueDeclare(QUEUE_INFORM_EMAIL,true,false,false,null);
        //声明一个交换机
        //参数：String exchange, String type
        /**
         * 参数明细：
         * 1、交换机的名称
         * 2、交换机的类型
         * fanout：对应的rabbitmq的工作模式是 publish/subscribe
         * direct：对应的Routing	工作模式
         * topic：对应的Topics工作模式
         * headers： 对应的headers工作模式
         */
        channel.exchangeDeclare(EXCHANGE_ROUTING_INFORM, BuiltinExchangeType.DIRECT);
        //进行交换机和队列绑定
        //参数：String queue, String exchange, String routingKey
        /**
         * 参数明细：
         * 1、queue 队列名称
         * 2、exchange 交换机名称
         * 3、routingKey 路由key，作用是交换机根据路由key的值将消息转发到指定的队列中，在发布订阅模式中调协为空字符串
         */
        channel.queueBind(QUEUE_INFORM_EMAIL, EXCHANGE_ROUTING_INFORM,ROUTINGKEY_EMAIL);

        //实现消费方法
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
            /**
             * 当消费者接受到消息后，此方法将被调用
             * @param consumerTag   消费者标签，用来白哦是消费者的，在监听队列时设置channerl.basicConsume
             * @param envelope  信封，通过envelope
             * @param properties
             * @param body
             * @throws IOException
             */
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                //交换机
                String exchange = envelope.getExchange();
                //消息id,mq在channel中用来表示消息的id，可用于确认消息已接受
                long deliveryTag = envelope.getDeliveryTag();
                //消息内容
                String message = new String(body, "utf-8");
                System.out.println("recesive message" +message+"接受");
            }
        };
        //监听队列
        /**
         * 1、queue。队列名称
         * 2、autoACK，自动回复，当消费者接收到消息后，要告诉mq消息已接受，如果此参数设置为true表示会自动回复mq.如果设置为false,要通过编程实现回复
         * 3、callbakc,消费方法，当消费者接收到消息要执行的方法。
         */
        channel.basicConsume(QUEUE_INFORM_EMAIL,true,defaultConsumer);
    }
}
