package com.vat.uploadservice.service;

import com.vat.uploadservice.dto.UploadResponseDto;
import com.vat.uploadservice.dto.VideoMessage;
import com.vat.uploadservice.exceptions.EmptyFileException;
import com.vat.uploadservice.exceptions.InvalidFileException;
import com.vat.uploadservice.exceptions.SizeExceedingException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class UploadService {

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    private final GridFsService gridFsService;
    private final RabbitTemplate rabbitTemplate;

    public UploadService(GridFsService gridFsService, RabbitTemplate rabbitTemplate) {
        this.gridFsService = gridFsService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public UploadResponseDto uploadFile(MultipartFile file, String userid) {
        try {
            validateVideoFile(file);
            String fileId = gridFsService.storeVideo(file, userid);

            sendVideoProcessingMessage(fileId, userid);

            return new UploadResponseDto(fileId, "file uploaded successfully");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InvalidFileException("Upload file could not be uploaded");
        }
    }

    private void sendVideoProcessingMessage(String fileId, String userId) {
        try {
            VideoMessage message = new VideoMessage(fileId, userId);
            System.out.println("Attempting to send message: " + message);

            rabbitTemplate.convertAndSend(exchange, routingKey, message);
            System.out.println("Message sent successfully to exchange: " + exchange + " with routing key: " + routingKey);

        } catch (Exception ex) {
            System.err.println("Failed to send processing message: " + ex.getMessage());
            System.err.println("Exchange: " + exchange + ", Routing Key: " + routingKey);
            ex.printStackTrace(); // This will show the full stack trace
        }
    }

    public void deleteVideo(String fileId) {
        try {
            gridFsService.deleteVideo(fileId);
            System.out.println("Video deleted successfully: " + fileId);
        } catch (Exception ex) {
            System.err.println("Failed to delete video: " + ex.getMessage());
            throw new InvalidFileException("Could not delete video file");
        }
    }

    private void validateVideoFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException("File cannot be empty");
        }

        if (file.getContentType() != null && !file.getContentType().startsWith("video/")) {
            throw new InvalidFileException("Only video files are allowed");
        }

        if (file.getSize() > 100_000_000) {
            throw new SizeExceedingException("File size cannot exceed 500MB");
        }
    }
}
