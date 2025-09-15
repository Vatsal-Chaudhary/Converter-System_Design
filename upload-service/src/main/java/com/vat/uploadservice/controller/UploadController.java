package com.vat.uploadservice.controller;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.vat.uploadservice.dto.UploadResponseDto;
import com.vat.uploadservice.service.UploadService;
import org.bson.types.ObjectId;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
public class UploadController {

    private final UploadService uploadService;
    private final GridFsTemplate gridFsTemplate;

    public UploadController(UploadService uploadService, GridFsTemplate gridFsTemplate) {
        this.uploadService = uploadService;
        this.gridFsTemplate = gridFsTemplate;
    }

    @PostMapping("/video")
    public ResponseEntity<UploadResponseDto> upload(@RequestParam("file")MultipartFile file
                                                  , @RequestHeader("User-Id") String userId) {
        if (!userId.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body(null);
        }
        return new ResponseEntity<>(uploadService.uploadFile(file, userId), HttpStatus.OK);
    }

    @GetMapping("/video/{fileId}")
    public ResponseEntity<Resource> downloadVideo(@PathVariable("fileId") String fileId) {
        System.out.println("=== DOWNLOAD REQUEST START ===");
        System.out.println("Requested fileId: " + fileId);
        try {
            if (!ObjectId.isValid(fileId)) {
                System.out.println("Invalid ObjectId format: " + fileId);
                return ResponseEntity.badRequest().build();
            }

            ObjectId objectId = new ObjectId(fileId);
            System.out.println("Converted to ObjectId: " + objectId);

            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));

            if (gridFSFile == null) {
                System.out.println("GridFS query result: NOT FOUND");
                return ResponseEntity.notFound().build();
            }

            System.out.println("GridFS query result: " + "FOUND");

            Resource resource = gridFsTemplate.getResource(gridFSFile);
            assert gridFSFile.getMetadata() != null;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(gridFSFile.getMetadata().getString("_contentType")))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + gridFSFile.getFilename() + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("Error downloading video: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/admin/video/{fileId}")
    public ResponseEntity<String> deleteVideo(@PathVariable("fileId") String fileId) {
        System.out.println("=== DELETE REQUEST START ===");
        System.out.println("Requested fileId: " + fileId);

        try {
            uploadService.deleteVideo(fileId);
            System.out.println("Video deleted successfully: " + fileId);
            return ResponseEntity.ok("Video deleted successfully");

        } catch (Exception e) {
            System.err.println("Error deleting video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete video");
        } finally {
            System.out.println("=== DELETE REQUEST END ===");
        }
    }

    @GetMapping("/admin/debug/files")
    public ResponseEntity<String> listAllFiles() {
        try {
            StringBuilder sb = new StringBuilder("Files in GridFS:\n");
            gridFsTemplate.find(new Query()).forEach(file -> {
                sb.append("ID: ").append(file.getObjectId())
                        .append(", Filename: ").append(file.getFilename())
                        .append(", Size: ").append(file.getLength())
                        .append("\n");
            });
            return ResponseEntity.ok(sb.toString());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
