package cn.edu.zju.kpaperproject.mapper;

import cn.edu.zju.kpaperproject.pojo.TbEngineFactoryDynamic;
import cn.edu.zju.kpaperproject.pojo.TbEngineFactoryDynamicExample;
import java.util.List;

import cn.edu.zju.kpaperproject.pojo.TbSupplierDynamic;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TbEngineFactoryDynamicMapper {
    int countByExample(TbEngineFactoryDynamicExample example);

    int deleteByExample(TbEngineFactoryDynamicExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TbEngineFactoryDynamic record);

    int insertSelective(TbEngineFactoryDynamic record);

    List<TbEngineFactoryDynamic> selectByExample(TbEngineFactoryDynamicExample example);

    TbEngineFactoryDynamic selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") TbEngineFactoryDynamic record, @Param("example") TbEngineFactoryDynamicExample example);

    int updateByExample(@Param("record") TbEngineFactoryDynamic record, @Param("example") TbEngineFactoryDynamicExample example);

    int updateByPrimaryKeySelective(TbEngineFactoryDynamic record);

    int updateByPrimaryKey(TbEngineFactoryDynamic record);

    // -------------------------------------
    void insertList(List<TbEngineFactoryDynamic> listEngineFactoryDynamic);
}