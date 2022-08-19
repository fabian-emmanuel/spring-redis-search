package com.fibbee.springredissearch.repository;

import com.fibbee.springredissearch.model.CategoryStats;
import com.fibbee.springredissearch.model.Page;
import com.fibbee.springredissearch.model.Post;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.search.aggr.AggregationBuilder;
import redis.clients.jedis.search.aggr.AggregationResult;
import redis.clients.jedis.search.aggr.Reducers;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static redis.clients.jedis.search.aggr.Reducers.*;

@Repository
@RequiredArgsConstructor
public class PostRepository {
    private final UnifiedJedis jedis;
    private static final Integer PAGE_SIZE = 5;

    public Post save(Post post){
        if (post.getPostId() == null) {
            post.setPostId(UUID.randomUUID().toString());
        }
        Gson gson = new Gson();
        String key = "post:%s".formatted(post.getPostId());
        jedis.jsonSet(key, gson.toJson(post));
        jedis.sadd("posts", key);
        return post;
    }

    public void deleteAll() {
        Set<String> keys = jedis.smembers("post");
        if(!keys.isEmpty()) {
            keys.forEach(jedis::jsonDel);
        }
        jedis.del("post");
    }

    public Page search(String content, Set<String> tags, Integer page) {
        long totalResults;

        StringBuilder queryBuilder = new StringBuilder();

        if(content != null && !content.isEmpty()) {
            queryBuilder.append("@content:")
                    .append(content);
        }

        if(tags != null && !tags.isEmpty()) {
            queryBuilder.append(" @tags:{")
                    .append(String.join("|", tags))
                    .append("}");
        }

        String queryCriteria = queryBuilder.toString();

        Query query = queryCriteria.isEmpty()
                ? new Query()
                : new Query(queryCriteria);

        query.limit(PAGE_SIZE * (page-1), PAGE_SIZE);

        SearchResult searchResult
                = jedis.ftSearch("post-idx",query);

        totalResults = searchResult.getTotalResults();

        int numberOfPages =
                (int) Math.ceil((double)totalResults/PAGE_SIZE);

        List<Post> postList = searchResult.getDocuments()
                .stream()
                .map(this::convertDocumentToPost)
                .toList();

        return Page.builder()
                .posts(postList)
                .total(totalResults)
                .totalPage(numberOfPages)
                .currentPage(page)
                .build();
    }

    private Post convertDocumentToPost(Document document) {
        Gson gson = new Gson();

        String jsonDoc = document
                .getProperties()
                .iterator()
                .next()
                .getValue()
                .toString();

        return gson.fromJson(jsonDoc,Post.class);
    }

    public List<CategoryStats> getCategoryWiseTotalPost() {
        AggregationBuilder aggregationBuilder = new AggregationBuilder();

        aggregationBuilder.groupBy(
                "@tags",
                count().as("NO_OF_POST"),
                avg("@views").as("AVERAGE_VIEWS"));

        AggregationResult aggregationResult = jedis.ftAggregate("post-idx", aggregationBuilder);

        return LongStream.range(0, aggregationResult.totalResults)
                .mapToObj(idx -> aggregationResult.getRow((int) idx))
                .map(row -> CategoryStats
                        .builder()
                        .totalPosts(row.getLong("NO_OF_POST"))
                        .averageViews(new DecimalFormat("#.##")
                                .format(row.getDouble("AVERAGE_VIEWS")))
                        .tags(row.getString("tags"))
                        .build())
                .toList();
    }
}
