package com.fibbee.springredissearch.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Post {
    private String postId;
    private String title;
    private String content;
    private Set<String> tags = new HashSet<>();
    private Integer views;
}
