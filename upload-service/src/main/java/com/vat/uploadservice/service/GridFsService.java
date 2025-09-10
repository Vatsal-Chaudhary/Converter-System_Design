package com.vat.uploadservice.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

@Service
public class GridFsService {

    private final GridFsTemplate gridFsTemplate;

    public GridFsService(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public String storeVideo(MultipartFile file, String userId) throws IOException {
        Document metadata = new Document();
        metadata.put("userId", userId);
        metadata.put("uploadDate", new Date());
        metadata.put("originalName", file.getOriginalFilename());

        ObjectId fileId = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                metadata
        );
        return fileId.toString();
    }

    public void deleteVideo(String fileId) throws IOException {
        if (!ObjectId.isValid(fileId)) {
            throw new IllegalArgumentException("Invalid ObjectId format: " + fileId);
        }

        ObjectId objectId = new ObjectId(fileId);
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));

        if (gridFSFile == null) {
            throw new IllegalArgumentException("File not found with ID: " + fileId);
        }

        // Delete the file and all its chunks from GridFS
        gridFsTemplate.delete(Query.query(Criteria.where("_id").is(objectId)));
        System.out.println("Deleted video file with ID: " + fileId);
    }

}
