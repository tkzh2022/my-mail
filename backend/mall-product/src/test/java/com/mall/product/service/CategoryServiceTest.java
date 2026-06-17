package com.mall.product.service;

import com.mall.product.entity.Category;
import com.mall.product.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getCategoryTree_emptyList_returnsEmpty() {
        when(categoryMapper.selectList(any())).thenReturn(List.of());

        List<Category> result = categoryService.getCategoryTree();
        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryTree_buildsTreeCorrectly() {
        Category root = createCategory(1L, "Electronics", null);
        Category child1 = createCategory(2L, "Phones", 1L);
        Category child2 = createCategory(3L, "Laptops", 1L);
        Category grandchild = createCategory(4L, "iPhone", 2L);

        when(categoryMapper.selectList(any())).thenReturn(List.of(root, child1, child2, grandchild));

        List<Category> result = categoryService.getCategoryTree();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getChildren()).hasSize(2);
        assertThat(result.get(0).getChildren().get(0).getChildren()).hasSize(1);
    }

    @Test
    void getCategoryTree_multipleRoots() {
        Category root1 = createCategory(1L, "Electronics", null);
        Category root2 = createCategory(2L, "Clothing", 0L);

        when(categoryMapper.selectList(any())).thenReturn(List.of(root1, root2));

        List<Category> result = categoryService.getCategoryTree();
        assertThat(result).hasSize(2);
    }

    private Category createCategory(Long id, String name, Long parentId) {
        Category cat = new Category();
        cat.setId(id);
        cat.setName(name);
        cat.setParentId(parentId);
        cat.setStatus(1);
        cat.setSortOrder(0);
        return cat;
    }
}
