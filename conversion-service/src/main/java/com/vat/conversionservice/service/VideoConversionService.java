package com.vat.conversionservice.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
public class VideoConversionService {

    public File convertToMp3(InputStream inputStream) throws IOException, InterruptedException {
        File tempVideo = null;
        File mp3File = null;

        try {
            tempVideo = File.createTempFile("video", ".mp4");
            FileUtils.copyInputStreamToFile(inputStream, tempVideo);

            mp3File = File.createTempFile("output", ".mp3");

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", tempVideo.getAbsolutePath(),
                    "-vn",
                    "-ar", "44100",
                    "-ac", "2",
                    "-b:a", "192k",
                    "-f", "mp3",
                    mp3File.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            System.out.println("FFmpeg output:");
            System.out.println(output);
            System.out.println("FFmpeg exit code: " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg conversion failed with exit code " + exitCode +
                                                   ". Output: " + output.toString());
            }

            if (!mp3File.exists() || mp3File.length() == 0) {
                throw new RuntimeException("MP3 file was not created or is empty");
            }

            System.out.println("Conversion successful. MP3 file size: " + mp3File.length() + " bytes");
            return mp3File;

        } finally {
            if (tempVideo != null && tempVideo.exists()) {
                tempVideo.delete();
            }
        }
    }
}
