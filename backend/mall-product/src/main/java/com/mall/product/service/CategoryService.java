package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.product.entity.Category;
import com.mall.product.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    @Cacheable(value = "categories", key = "'tree'")
    public List<Category> getCategoryTree() {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSortOrder));

        Map<Long, List<Category>> grouped = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        for (Category root : roots) {
            root.setChildren(buildChildren(root.getId(), grouped));
        }
        return roots;
    }

    private List<Category> buildChildren(Long parentId, Map<Long, List<Category>> grouped) {
        List<Category> children = grouped.getOrDefault(parentId, new ArrayList<>());
        for (Category child : children) {
            child.setChildren(buildChildren(child.getId(), grouped));
        }
        return children;
    }
}
