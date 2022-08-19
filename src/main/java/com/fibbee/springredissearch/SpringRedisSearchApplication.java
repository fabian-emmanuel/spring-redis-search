package com.fibbee.springredissearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fibbee.springredissearch.model.Post;
import com.fibbee.springredissearch.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.Arrays;

import static redis.clients.jedis.search.Schema.*;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringRedisSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRedisSearchApplication.class, args);
    }

    private final PostRepository postRepository;
    private final UnifiedJedis jedis;

    @Value("classpath:data.json")
    Resource resourceFile;

    @Bean
    CommandLineRunner init() {
        return args -> {

            postRepository.deleteAll();

            try {
                jedis.ftDropIndex("post-idx");
            } catch (Exception e) {
                System.out.println("Index is not available");
            }

            String data = new String(resourceFile.getInputStream().readAllBytes());

            ObjectMapper objectMapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Post[] posts = objectMapper.readValue(data, Post[].class);
            Arrays.stream(posts).forEach(postRepository::save);

            Schema schema = new Schema()
                    .addField(new Field(FieldName.of("$.content").as("content"), FieldType.TEXT,true,false))
                    .addField(new TextField(FieldName.of("$.title").as("title")))
                    .addField(new Field(FieldName.of("$.tags[*]").as("tags"), FieldType.TAG))
                    .addField(new Field(FieldName.of("$.views").as("views"), FieldType.NUMERIC,false,true));

            IndexDefinition indexDefinition
                    = new IndexDefinition(IndexDefinition.Type.JSON)
                    .setPrefixes("post:");

            jedis.ftCreate("post-idx",
                    IndexOptions.defaultOptions().setDefinition(indexDefinition),
                    schema);

        };
    }

}
