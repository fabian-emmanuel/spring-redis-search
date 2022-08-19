package com.fibbee.springredissearch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CategoryStats {
    private String tags;
    private Long totalPosts;
    private String averageViews;
}
