package com.fibbee.springredissearch.service;

import com.fibbee.springredissearch.model.CategoryStats;
import com.fibbee.springredissearch.model.Page;
import com.fibbee.springredissearch.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Page search(String content, Set<String> tags, Integer page) {
        return postRepository.search(content,tags,page);
    }

    public List<CategoryStats> getCategoryWiseTotalPost() {
        return postRepository.getCategoryWiseTotalPost();
    }
}
