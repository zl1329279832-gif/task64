package com.dao;

import com.entity.MingpianHistoryEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.MingpianHistoryView;

/**
 * 名片版本历史 Dao 接口
 *
 * @author
 */
public interface MingpianHistoryDao extends BaseMapper<MingpianHistoryEntity> {

   List<MingpianHistoryView> selectListView(Pagination page, @Param("params") Map<String, Object> params);

}
