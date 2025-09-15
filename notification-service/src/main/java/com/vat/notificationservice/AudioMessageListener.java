package com.vat.notificationservice;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class AudioMessageListener {

    private final EmailService emailService;

    public AudioMessageListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void receiveAudioMessage(AudioMessage message) {
        System.out.println("Received audio message: " + message);
        String emailBody = String.format(
                "Hello,\n\nYour video has been converted to audio.\n\nDetails:\n- File ID: %s\n- Original Video ID: %s\n- Filename: %s\n\nThank you for using our service.",
                message.getFileId(),
                message.getOriginalVideoId(),
                message.getFilename()
        );
        emailService.sendEmail(
                message.getUserId(),
                "Audio Conversion Complete",
                emailBody
        );
    }
}
