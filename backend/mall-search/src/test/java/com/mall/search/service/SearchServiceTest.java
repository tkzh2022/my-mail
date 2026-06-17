package com.mall.search.service;

import com.mall.common.result.PageResult;
import com.mall.search.document.ProductDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ElasticsearchOperations esOperations;
    @Mock
    private SearchHits<ProductDocument> searchHits;
    @Mock
    private SearchHit<ProductDocument> searchHit;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchProducts_returnsPageResult() {
        ProductDocument doc = new ProductDocument();
        doc.setId(1L);
        doc.setName("iPhone 15");

        when(esOperations.search(any(Query.class), eq(ProductDocument.class))).thenReturn(searchHits);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHits.getTotalHits()).thenReturn(1L);
        when(searchHit.getContent()).thenReturn(doc);

        PageResult<ProductDocument> result = searchService.searchProducts("iPhone", null, null, null, null, 1, 10);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("iPhone 15");
        assertThat(result.getTotal()).isEqualTo(1L);
    }

    @Test
    void searchProducts_withAllFilters() {
        when(esOperations.search(any(Query.class), eq(ProductDocument.class))).thenReturn(searchHits);
        when(searchHits.getSearchHits()).thenReturn(List.of());
        when(searchHits.getTotalHits()).thenReturn(0L);

        PageResult<ProductDocument> result = searchService.searchProducts(
                "phone", 5L, 100.0, 5000.0, "price_asc", 1, 10);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0L);
    }

    @Test
    void suggest_returnsNames() {
        ProductDocument doc = new ProductDocument();
        doc.setName("iPhone 15 Pro");

        when(esOperations.search(any(Query.class), eq(ProductDocument.class))).thenReturn(searchHits);
        when(searchHits.getSearchHits()).thenReturn(List.of(searchHit));
        when(searchHit.getContent()).thenReturn(doc);

        List<String> result = searchService.suggest("iPh");

        assertThat(result).contains("iPhone 15 Pro");
    }

    @Test
    void suggest_emptyResults() {
        when(esOperations.search(any(Query.class), eq(ProductDocument.class))).thenReturn(searchHits);
        when(searchHits.getSearchHits()).thenReturn(List.of());

        List<String> result = searchService.suggest("zzz");
        assertThat(result).isEmpty();
    }

    @Test
    void indexProduct_callsSave() {
        ProductDocument doc = new ProductDocument();
        doc.setId(1L);
        when(esOperations.save(any(ProductDocument.class))).thenReturn(doc);

        searchService.indexProduct(doc);
        verify(esOperations).save(doc);
    }

    @Test
    void deleteProduct_callsDelete() {
        searchService.deleteProduct(1L);
        verify(esOperations).delete("1", ProductDocument.class);
    }
}
