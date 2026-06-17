package com.mall.search.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

@Data
@Document(indexName = "mall_products")
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String subtitle;

    @Field(type = FieldType.Keyword)
    private Long categoryId;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private Integer salesCount;

    @Field(type = FieldType.Keyword)
    private String image;

    @Field(type = FieldType.Long)
    private Long merchantId;

    @Field(type = FieldType.Keyword)
    private String merchantName;

    @Field(type = FieldType.Keyword)
    private Integer status;
}
