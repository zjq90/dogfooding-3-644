package com.library.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.BookCategory;
import com.library.mapper.BookCategoryMapper;
import com.library.service.BookCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY_ALL = "categories:all";

    @Override
    public List<BookCategory> getAllCategories() {
        List<BookCategory> categories = (List<BookCategory>) redisTemplate.opsForValue().get(CACHE_KEY_ALL);
        if (categories == null) {
            log.debug("从数据库加载分类列表");
            categories = baseMapper.selectAllCategories();
            redisTemplate.opsForValue().set(CACHE_KEY_ALL, categories, 10, TimeUnit.MINUTES);
        }
        return categories;
    }

    @Override
    public List<BookCategory> getCategoryTree() {
        List<BookCategory> allCategories = getAllCategories();
        
        List<BookCategory> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());
        
        for (BookCategory root : rootCategories) {
            buildCategoryTree(root, allCategories);
        }
        
        log.debug("构建分类树完成，根节点数量: {}", rootCategories.size());
        return rootCategories;
    }
    
    private void buildCategoryTree(BookCategory parent, List<BookCategory> allCategories) {
        List<BookCategory> children = allCategories.stream()
                .filter(c -> parent.getId() != null && parent.getId().equals(c.getParentId()))
                .collect(Collectors.toList());
        
        parent.setChildren(children);
        
        for (BookCategory child : children) {
            buildCategoryTree(child, allCategories);
        }
    }

    @Override
    public String getCategoryName(Long categoryId) {
        if (categoryId == null) {
            return "";
        }
        String cacheKey = "category:name:" + categoryId;
        String name = (String) redisTemplate.opsForValue().get(cacheKey);
        if (name == null) {
            name = baseMapper.selectNameById(categoryId);
            if (name != null) {
                redisTemplate.opsForValue().set(cacheKey, name, 30, TimeUnit.MINUTES);
            }
        }
        return name;
    }
    
    @Override
    public boolean save(BookCategory entity) {
        boolean result = super.save(entity);
        if (result) {
            clearCategoryCache();
            log.info("分类添加成功，已清除缓存: {}", entity.getName());
        }
        return result;
    }
    
    @Override
    public boolean updateById(BookCategory entity) {
        boolean result = super.updateById(entity);
        if (result) {
            clearCategoryCache();
            redisTemplate.delete("category:name:" + entity.getId());
            log.info("分类更新成功，已清除缓存: {}", entity.getName());
        }
        return result;
    }
    
    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            clearCategoryCache();
            redisTemplate.delete("category:name:" + id);
            log.info("分类删除成功，已清除缓存: {}", id);
        }
        return result;
    }
    
    private void clearCategoryCache() {
        redisTemplate.delete(CACHE_KEY_ALL);
        log.debug("已清除分类列表缓存");
    }
}
