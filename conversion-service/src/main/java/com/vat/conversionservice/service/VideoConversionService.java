package com.vat.conversionservice.service;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class VideoConversionService {

    public ObjectId convertToMp3AndStore(InputStream videoInputStream, String filename, GridFsTemplate audioGridFsTemplate, String userId)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", "pipe:0",
                "-vn",
                "-ar", "44100",
                "-ac", "2",
                "-b:a", "192k",
                "-f", "mp3",
                "pipe:1"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        StringBuilder ffmpegLog = new StringBuilder();

        try {
            executor.submit(() -> {
                try (OutputStream ffmpegStdin = process.getOutputStream()) {
                    IOUtils.copy(videoInputStream, ffmpegStdin);
                    System.out.println("Video data fed to FFmpeg successfully");
                } catch (IOException e) {
                    System.err.println("Error feeding video to FFmpeg: " + e.getMessage());
                }
            });

            ObjectId mp3FileId;
            try (InputStream ffmpegStdout = process.getInputStream()) {
                String mp3Filename = filename.replaceAll("\\.(mp4|avi|mov|mkv)$", ".mp3");
                Document metadata = new  Document();
                metadata.put("userId", userId);
                mp3FileId = audioGridFsTemplate.store(ffmpegStdout, mp3Filename, "audio/mpeg", metadata);
                System.out.println("MP3 stored directly to GridFS with ID: " + mp3FileId);
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg conversion failed with exit code: " + exitCode);
            }

            System.out.println("Conversion successful! Audio file ID: " + mp3FileId);
            return mp3FileId;

        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
