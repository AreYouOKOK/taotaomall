package com.lwhtarena.taotaomall.rest.service.impl;

import com.lwhtarena.taotaomall.rest.service.ContentService;

/**
 * @Author: 李文浩【lwhtarena@163.com】
 * @folder com.lwhtarena.taotaomall.rest.service.impl
 * @Date: 2016/12/12 2:31
 * @Description:<p>内容管理</p>
 * @版权 Copyright (c) 2016 lwhtarena.com. All Rights Reserved.
 */

@Service
public class ContentServiceImpl implements ContentService {
    @Autowired
    private TbContentMapper contentMapper;
    @Autowired
    private JedisClient jedisClient;
    @Value("${INDEX_CONTENT_REDIS_KEY}")
    private String INDEX_CONTENT_REDIS_KEY;


    @Override
    public List<TbContent> getContentList(long contentCid) {
        //从缓存中取内容
        try {
            String result = jedisClient.hget(INDEX_CONTENT_REDIS_KEY, contentCid + "");
            if (!StringUtils.isBlank(result)) {
                //把字符串转换成list
                List<TbContent> resultList = JsonUtils.jsonToList(result, TbContent.class);
                return resultList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //根据内容分类id查询内容列表
        TbContentExample example = new TbContentExample();
        Criteria criteria = example.createCriteria();
        criteria.andCategoryIdEqualTo(contentCid);
        //执行查询
        List<TbContent> list = contentMapper.selectByExample(example);

        //向缓存中添加内容
        try {
            //把list转换成字符串
            String cacheString = JsonUtils.objectToJson(list);
            jedisClient.hset(INDEX_CONTENT_REDIS_KEY, contentCid + "", cacheString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
