package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.mapper.CategoryMapper;
import com.upgrad.FoodOrderingApp.api.model.CategoryDetailsResponse;
import com.upgrad.FoodOrderingApp.api.model.CategoryListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemList;
import com.upgrad.FoodOrderingApp.service.businness.CategoryService;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.entity.Category;
import com.upgrad.FoodOrderingApp.service.entity.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;
    private final ItemService itemService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryController(final CategoryService categoryService,
                              final ItemService itemService,
                              final CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.itemService = itemService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoryListResponse>> getCategories() {
        log.info("Get all categories");
        final List<CategoryListResponse> responseList = new ArrayList<>();
        final List<Category> categories = categoryService.getCategories();

        if (null != categories && !categories.isEmpty()) {
            categories.forEach(category -> {
                responseList.add(categoryMapper.mapCategoryToCategoryDetailsResponse(category));
            });
            log.info("Total number of categories {}", categories.size());
        }
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{category_id}")
    public ResponseEntity<CategoryDetailsResponse> getCategoryById(
            @PathVariable("category_id") final String categoryId) {
        log.info("Get category by UUID : {}", categoryId);
        final CategoryDetailsResponse response = new CategoryDetailsResponse();
        final Category category = categoryService.getCategoryById(categoryId);
        final List<Integer> itemIds = categoryService.getItemIdsFromCategoryItem(category.getId());
        if (null != itemIds && !itemIds.isEmpty()) {
            itemIds.forEach(id -> {
                final Item item = itemService.getItemById(id);
                if (null != item) {
                    final ItemList itemList = new ItemList();
                    itemList.setId(UUID.fromString(item.getUuid()));
                    itemList.setItemName(item.getItemName());
                    itemList.setPrice(item.getPrice());

                    if (null != item.getType()) {
                        if ("1".equals(item.getType())) {
                            itemList.setItemType(ItemList.ItemTypeEnum.NON_VEG);
                        } else {
                            itemList.setItemType(ItemList.ItemTypeEnum.VEG);
                        }
                    }
                    response.addItemListItem(itemList);
                }
            });
        }

        response.setId(UUID.fromString(category.getUuid()));
        response.setCategoryName(category.getCategoryName());
        return ResponseEntity.ok(response);
    }
}
