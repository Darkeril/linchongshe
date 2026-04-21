package com.hongshu.web.service.web.impl;

import cn.hutool.core.util.RandomUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.hongshu.common.core.constant.NoteConstant;
import com.hongshu.web.domain.dto.EsRecordDTO;
import com.hongshu.web.domain.vo.RecordSearchVo;
import com.hongshu.web.service.web.IWebEsRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ES
 *
 * @author: hongshu
 */
@Service
@Slf4j
public class WebEsRecordServiceImpl implements IWebEsRecordService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;


    /**
     * 获取搜索记录
     */
    @Override
    public List<RecordSearchVo> getRecordByKeyWord(EsRecordDTO esRecordDTO) {
        String keyword = esRecordDTO.getKeyword();
        String uid = esRecordDTO.getUid();

        List<RecordSearchVo> records = new ArrayList<>();
        try {
            // 构建搜索请求
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.RECORD_INDEX);

            // 添加查询条件，根据uid过滤
            if (StringUtils.isNotBlank(uid)) {
                builder.query(q -> q.bool(b -> {
                    b.must(m -> m.term(t -> t.field("uid").value(uid)));
                    if (StringUtils.isNotBlank(keyword)) {
                        b.must(m -> m.match(f -> f.field("content").query(keyword)));
                    }
                    return b;
                }));
            }

            // 设置排序规则和高亮显示
            builder.sort(o -> o.field(f -> f.field("time").order(SortOrder.Desc)));
            builder.highlight(h -> h.fields("content", m -> m).preTags("<font color='black'>").postTags("</font>"));
            builder.size(10);

            // 执行搜索请求
            SearchRequest searchRequest = builder.build();
            SearchResponse<RecordSearchVo> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVo.class);

            // 获取搜索结果
            List<Hit<RecordSearchVo>> hits = searchResponse.hits().hits();

            // 处理搜索结果
            for (Hit<RecordSearchVo> hit : hits) {
                RecordSearchVo recordSearchVo = hit.source();
                records.add(recordSearchVo);
            }

            return records;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 热门搜索
     */
    @Override
    public List<RecordSearchVo> getHotRecord() {
        List<RecordSearchVo> records = new ArrayList<>();
        try {
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                return records;
            }
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.RECORD_INDEX);
            builder.sort(o -> o.field(f -> f.field("searchCount").order(SortOrder.Desc)));
            builder.size(10);
            SearchRequest searchRequest = builder.build();
            SearchResponse<RecordSearchVo> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVo.class);
            //得到所有的数据
            List<Hit<RecordSearchVo>> hits = searchResponse.hits().hits();
            for (Hit<RecordSearchVo> hit : hits) {
                RecordSearchVo recordSearchVo = hit.source();
                records.add(recordSearchVo);
            }
            return records;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 增加搜索记录
     */
    @Override
    public void addRecord(EsRecordDTO esRecordDTO) {
        String keyword = esRecordDTO.getKeyword();
        String uid = esRecordDTO.getUid();
        try {
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                elasticsearchClient.indices().create(c -> c.index(NoteConstant.RECORD_INDEX));
            }

            // 构建搜索请求
            SearchRequest.Builder builder = new SearchRequest.Builder().index(NoteConstant.RECORD_INDEX);
            if (StringUtils.isNotBlank(keyword) && StringUtils.isNotBlank(uid)) {
                builder.query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("content.keyword").value(keyword.trim())))
                        .must(m -> m.term(t -> t.field("uid").value(uid)))
                ));
            }
            builder.size(10);
            SearchRequest searchRequest = builder.build();
            SearchResponse<RecordSearchVo> searchResponse = elasticsearchClient.search(searchRequest, RecordSearchVo.class);
            List<Hit<RecordSearchVo>> hits = searchResponse.hits().hits();

            boolean isDuplicate = false;
            List<String> contents = new ArrayList<>();

            for (Hit<RecordSearchVo> hit : hits) {
                RecordSearchVo recordSearchVo = hit.source();
                if (recordSearchVo.getContent().equals(keyword.trim()) && recordSearchVo.getUid().equals(uid)) {
                    isDuplicate = true;
                }
                recordSearchVo.setSearchCount(recordSearchVo.getSearchCount() + 1);
                UpdateResponse<RecordSearchVo> response = elasticsearchClient.update(u -> u.index(NoteConstant.RECORD_INDEX).id(hit.id()).doc(recordSearchVo), RecordSearchVo.class);
                log.info("response", response.toString());
                contents.add(recordSearchVo.getContent());
            }

            // 如果未找到相同内容，则插入新记录
            if (!isDuplicate && StringUtils.isNotBlank(keyword)) {
                RecordSearchVo recordSearchVo = new RecordSearchVo();
                recordSearchVo.setContent(keyword);
                recordSearchVo.setUid(uid);
                recordSearchVo.setTime(System.currentTimeMillis());
                recordSearchVo.setSearchCount(1L);
                String id = RandomUtil.randomString(12);
                elasticsearchClient.create(c -> c.index(NoteConstant.RECORD_INDEX).id(id).document(recordSearchVo));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除搜索记录
     */
    @Override
    public void clearRecordByUser(EsRecordDTO esRecordDTO) {
        String keyword = esRecordDTO.getKeyword();
        String uid = esRecordDTO.getUid();
        try {
            // 检查索引是否存在
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (!exists.value()) {
                log.warn("Index does not exist. No records to clear.");
                return;
            }
            // 构建删除请求
            DeleteByQueryRequest.Builder deleteRequestBuilder = new DeleteByQueryRequest.Builder()
                    .index(NoteConstant.RECORD_INDEX)
                    .query(q -> q.bool(b -> {
                        b.must(m -> m.term(t -> t.field("uid").value(uid)));
                        if (StringUtils.isNotBlank(keyword)) {
                            b.must(m -> m.term(t -> t.field("content").value(keyword.trim())));
                        }
                        return b;
                    }))
                    .timeout(t -> t.time("60s"))  // 设置超时时间
                    .refresh(true)                // 等待刷新，使用 Boolean
                    .waitForCompletion(true)      // 等待完成
                    .scrollSize(1000L)            // 设置批次大小
                    .conflicts(Conflicts.Proceed); // 处理冲突

            // 执行删除操作
            DeleteByQueryResponse deleteResponse = elasticsearchClient.deleteByQuery(deleteRequestBuilder.build());
            log.info("Deleted {} records for uid: {}, keyword: {}", deleteResponse.deleted(), uid, keyword);
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch error while clearing records for uid: {}, keyword: {}", uid, keyword, e);
            throw new RuntimeException("Failed to clear search records", e);
        } catch (Exception e) {
            log.error("Unexpected error while clearing records for uid: {}, keyword: {}", uid, keyword, e);
            throw new RuntimeException("Failed to clear search records", e);
        }
    }


    /**
     * 清空搜索记录
     */
    @Override
    public void clearAllRecord() {
        try {
            // 检查索引是否存在
            BooleanResponse exists = elasticsearchClient.indices().exists(e -> e
                    .index(NoteConstant.RECORD_INDEX));
            if (exists.value()) {
                // 删除整个索引
                elasticsearchClient.indices().delete(d -> d.index(NoteConstant.RECORD_INDEX));
                // 重新创建索引
                elasticsearchClient.indices().create(c -> c.index(NoteConstant.RECORD_INDEX));
                log.info("All search records have been cleared.");
            } else {
                log.warn("Index does not exist. No records to clear.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
