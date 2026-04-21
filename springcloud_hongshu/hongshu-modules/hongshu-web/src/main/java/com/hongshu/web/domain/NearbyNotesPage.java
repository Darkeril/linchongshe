package com.hongshu.web.domain;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class NearbyNotesPage<T> extends Page<T> {

    private String cityName;


    public NearbyNotesPage(long current, long size) {
        super(current, size);
    }
}
