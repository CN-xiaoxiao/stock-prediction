package com.xiaoxiao.stockbackend.litenter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xiaoxiao.stockbackend.entity.dto.Account;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 用于处理邮件发送的消息队列监听器
 */
@Component
@RabbitListener(queues = "direct.mail.queue1")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;

    @Value("${spring.mail.nickname}")
    String nickname;

    /**
     * 处理邮件发送
     * @param data 邮件信息
     */
    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = Objects.isNull(data.get("email")) ? null : data.get("email").toString();
        String code =  Objects.isNull(data.get("code")) ? null : data.get("code").toString();
        String jsonAccount = Objects.isNull(data.get("account")) ? null : data.get("account").toString();
        Account account = JSON.parseObject(jsonAccount, Account.class);
        String type = Objects.isNull(data.get("type")) ? null : data.get("type").toString();

        SimpleMailMessage message = switch (type) {
            case "register" ->
                    createMessage("欢迎注册我们的网站",
                            "您的邮件注册验证码为: "+code+"，有效时间3分钟，为了保障您的账户安全，请勿向他人泄露验证码信息。",
                            email);
            case "reset" ->
                    createMessage("您的密码重置邮件",
                            "你好，您正在执行重置密码操作，验证码为: "+code+"，有效时间3分钟，如非本人操作，请无视。",
                            email);
            case "resetSuccess" ->
                    createMessage("修改密码成功！",
                            "尊敬的用户: " + account.getUsername() + "，您好！您的密码已重置，请妥善保管您的密码。",
                            email);
            case "modify" ->
                    createMessage("您的邮箱修改邮件",
                            "你好，您正在执行邮箱修改操作，验证码为: " + code + "，有效时间3分钟，如非本人操作，请无视。",
                            email);
            default -> null;
        };
        if(message == null) return;
        sender.send(message);
    }

    /**
     * 快速封装简单邮件消息实体
     * @param title 标题
     * @param content 内容
     * @param email 收件人
     * @return 邮件实体
     */
    private SimpleMailMessage createMessage(String title, String content, String email){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(content);
        message.setTo(email);
        message.setFrom(nickname+'<'+username+'>');
        return message;
    }
}
