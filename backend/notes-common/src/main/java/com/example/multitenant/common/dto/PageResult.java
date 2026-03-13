package com.example.multitenant.common.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResult<T> {
  
  private final List<T> content;
  private final long totalCount;
  private final int page;
  private final int size;
  
  public PageResult(List<T> content, long totalCount, int page, int size) {
    this.content = content;
    this.totalCount = totalCount;
    this.page = page;
    this.size = size;
  }
  
  public static <T> PageResult<T> from(Page<T> page) {
    return new PageResult<>(
        page.getContent(),
        page.getTotalElements(),
        page.getNumber(),
        page.getSize()
    );
  }
}
