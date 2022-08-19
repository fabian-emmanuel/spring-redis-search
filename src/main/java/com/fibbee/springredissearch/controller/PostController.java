package com.fibbee.springredissearch.controller;

import com.fibbee.springredissearch.model.CategoryStats;
import com.fibbee.springredissearch.model.Page;
import com.fibbee.springredissearch.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PostController {
    private final PostService postService;

    @GetMapping("/search")
    public Page search(@RequestParam(name = "content", required = false) String content,
                       @RequestParam(name = "tags", required = false) Set<String> tags,
                       @RequestParam(name = "page", defaultValue = "1") Integer page) {
        return postService.search(content,tags,page);
    }

    @GetMapping("/categoryWisePost")
    public List<CategoryStats> getCategoryWiseTotalPost() {
        return postService.getCategoryWiseTotalPost();
    }
}
