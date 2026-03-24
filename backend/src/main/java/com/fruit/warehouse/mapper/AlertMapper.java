package com.fruit.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.warehouse.entity.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    @Select("SELECT COUNT(*) FROM alert WHERE status = 0 AND deleted = 0")
    int countUnread();
}
