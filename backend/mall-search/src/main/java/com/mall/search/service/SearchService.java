package com.mall.search.service;

import com.mall.common.result.PageResult;
import com.mall.search.document.ProductDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchOperations esOperations;

    public PageResult<ProductDocument> searchProducts(String keyword, Long categoryId,
                                                      Double priceMin, Double priceMax,
                                                      String sort, int page, int size) {
        NativeQueryBuilder builder = new NativeQueryBuilder();

        builder.withQuery(q -> q.bool(bq -> {
            bq.must(m -> m.multiMatch(mm -> mm
                    .query(keyword)
                    .fields("name^3", "subtitle")
                    .fuzziness("AUTO")));

            bq.filter(f -> f.term(t -> t.field("status").value(1)));

            if (categoryId != null) {
                bq.filter(f -> f.term(t -> t.field("categoryId").value(categoryId)));
            }
            if (priceMin != null) {
                bq.filter(f -> f.range(r -> r.field("price")
                        .gte(co.elastic.clients.json.JsonData.of(priceMin))));
            }
            if (priceMax != null) {
                bq.filter(f -> f.range(r -> r.field("price")
                        .lte(co.elastic.clients.json.JsonData.of(priceMax))));
            }
            return bq;
        }));

        if ("price_asc".equals(sort)) {
            builder.withPageable(PageRequest.of(page - 1, size, Sort.by("price").ascending()));
        } else if ("price_desc".equals(sort)) {
            builder.withPageable(PageRequest.of(page - 1, size, Sort.by("price").descending()));
        } else if ("sales".equals(sort)) {
            builder.withPageable(PageRequest.of(page - 1, size, Sort.by("salesCount").descending()));
        } else {
            builder.withPageable(PageRequest.of(page - 1, size));
        }

        SearchHits<ProductDocument> hits = esOperations.search(builder.build(), ProductDocument.class);

        List<ProductDocument> items = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        PageResult<ProductDocument> result = new PageResult<>();
        result.setItems(items);
        result.setTotal(hits.getTotalHits());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    public List<String> suggest(String prefix) {
        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
        queryBuilder.withQuery(q -> q.prefix(p -> p.field("name").value(prefix)));
        queryBuilder.withPageable(PageRequest.of(0, 10));

        SearchHits<ProductDocument> hits = esOperations.search(queryBuilder.build(), ProductDocument.class);
        return hits.getSearchHits().stream()
                .map(h -> h.getContent().getName())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    public void indexProduct(ProductDocument doc) {
        esOperations.save(doc);
    }

    public void deleteProduct(Long productId) {
        esOperations.delete(String.valueOf(productId), ProductDocument.class);
    }
}
