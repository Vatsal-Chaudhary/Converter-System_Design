package com.vat.conversionservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoConfig {

    @Value("${mongodb.video.uri}")
    private String videoMongoUri;

    @Value("${mongodb.audio.uri}")
    private String audioMongoUri;

    @Bean(name = "videoMongoClient")
    @Primary
    public MongoClient videoMongoClient() {
        return MongoClients.create(videoMongoUri);
    }

    @Bean(name = "audioMongoClient")
    public MongoClient audioMongoClient() {
        return MongoClients.create(audioMongoUri);
    }

    @Bean(name = "videoDatabaseFactory")
    @Primary
    public MongoDatabaseFactory videoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(videoMongoClient(), "videodb");
    }

    @Bean(name = "audioDatabaseFactory")
    public MongoDatabaseFactory audioDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(audioMongoClient(), "audiodb");
    }

    @Bean(name = "videoMongoTemplate")
    @Primary
    public MongoTemplate videoMongoTemplate() {
        return new MongoTemplate(videoDatabaseFactory());
    }

    @Bean(name = "audioMongoTemplate")
    public MongoTemplate audioMongoTemplate() {
        return new MongoTemplate(audioDatabaseFactory());
    }

    @Bean(name = "videoGridFsTemplate")
    @Primary
    public GridFsTemplate videoGridFsTemplate() {
        return new GridFsTemplate(videoDatabaseFactory(), videoMongoTemplate().getConverter());
    }

    @Bean(name = "audioGridFsTemplate")
    public GridFsTemplate audioGridFsTemplate() {
        return new GridFsTemplate(audioDatabaseFactory(), audioMongoTemplate().getConverter());
    }
}
