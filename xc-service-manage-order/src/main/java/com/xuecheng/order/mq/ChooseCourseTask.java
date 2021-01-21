package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * 定时向mq发送消息
 */
@Component
public class ChooseCourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        if(xcTask!=null && StringUtils.isNotEmpty(xcTask.getId())){
            taskService.finishTask(xcTask.getId());
        }
    }

    //定时发送添加选课任务
    @Scheduled(cron = "0 0/1 * * * *") //每隔一分钟执行
    public void sendChooseCourseTask() {
        //得到一分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE, -1);
        Date time = calendar.getTime();//一分钟前的时间得到了

        List<XcTask> xcTaskList = taskService.findXcTaskList(time, 100);
        System.out.println(xcTaskList);

        //调用service发布消息，将添加选课的任务发给mq
        for (XcTask xcTask : xcTaskList) {
            //取任务
            if (taskService.getTask(xcTask.getId(), xcTask.getVersion()) > 0) {
                String ex = xcTask.getMqExchange();//要发送的交换机
                String routingkey = xcTask.getMqRoutingkey();//发送的routingkey
                taskService.publish(xcTask, ex, routingkey);
            }
        }
    }




    /**
     * 下面两个为测试任务，，需要测试的时候，把注解【之一】打开就好，，记住，只打开一个注解，这只是不同类别的功能而已，但其实是一个注解
     */
    //定义任务调度的策略
//    @Scheduled(cron = "0/3 * * * * *") //每隔3秒执行
//    @Scheduled(fixedRate = 3000) //在任务开始后3秒执行下一次调度
//    @Scheduled(fixedDelay = 3000) //在任务结束后3秒才开始执行
    public void task1() {
        LOGGER.info("==============测试定时任务1开始=================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("==============测试定时任务1结束=================");

    }

//    @Scheduled(fixedRate = 3000) //在任务开始后3秒执行下一次调度
//    @Scheduled(fixedDelay = 3000) //在任务结束后3秒才开始执行
    public void task2() {
        LOGGER.info("==============测试定时任务1开始=================");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        LOGGER.info("==============测试定时任务1结束=================");

    }
}
