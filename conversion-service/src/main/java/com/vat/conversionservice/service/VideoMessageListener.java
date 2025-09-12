package com.vat.conversionservice.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.vat.conversionservice.dto.AudioMessage;
import com.vat.conversionservice.dto.VideoMessage;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;

@Service
public class VideoMessageListener {

    private final VideoConversionService conversionService;
    private final GridFsTemplate videoGridFsTemplate;
    private final GridFsTemplate audioGridFsTemplate;
    private final RabbitTemplate rabbitTemplate;

    public VideoMessageListener(VideoConversionService conversionService,
                                @Qualifier("videoGridFsTemplate") GridFsTemplate videoGridFsTemplate,
                                @Qualifier("audioGridFsTemplate") GridFsTemplate audioGridFsTemplate, RabbitTemplate rabbitTemplate) {
        this.conversionService = conversionService;
        this.videoGridFsTemplate = videoGridFsTemplate;
        this.audioGridFsTemplate = audioGridFsTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.audio.key}")
    private String audioRoutingKey;

    @RabbitListener(queues = "${rabbitmq.queue.video.name}")
    public void processingVideo(VideoMessage message) {
        File mp3File = null;
        try {
            System.out.println("Processing Video Message: " + message.getFileId());

            ObjectId objectId = new ObjectId(message.getFileId());
            GridFSFile gridFSFile = videoGridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(objectId))
            );

            if (gridFSFile == null) {
                throw new RuntimeException("File not found: " + message.getFileId());
            }

            Resource resource = videoGridFsTemplate.getResource(gridFSFile);

            mp3File = conversionService.convertToMp3(resource.getInputStream());

            ObjectId mp3FileId = audioGridFsTemplate.store(
                    new FileInputStream(mp3File),
                    gridFSFile.getFilename().replace(".mp4", ".mp3"),
                    "audio/mpeg"
            );

            System.out.println("Audio file stored with ID: " + mp3FileId);

            AudioMessage audioMessage = new AudioMessage(
                    mp3FileId.toString(),
                    message.getFileId(),
                    message.getUserId(),
                    gridFSFile.getFilename().replace(".mp4", ".mp3")
            );

            System.out.println("Audio file stored with ID: " + mp3FileId);

            rabbitTemplate.convertAndSend(exchange, audioRoutingKey, audioMessage);

        } catch (Exception e) {
            System.err.println("Error processing video: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (mp3File != null && mp3File.exists()) {
                mp3File.delete();
            }
        }
    }
}

