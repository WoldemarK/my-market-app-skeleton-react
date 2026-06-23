package ru.yandex.mymarketappskeleton.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {
   private int pageNumber;
    private  int pageSize;
    private  boolean hasNext;
    private  boolean hasPrevious;
}
