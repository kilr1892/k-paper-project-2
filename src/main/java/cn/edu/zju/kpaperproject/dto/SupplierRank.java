package cn.edu.zju.kpaperproject.dto;

import cn.edu.zju.kpaperproject.pojo.TbEngineFactory;
import cn.edu.zju.kpaperproject.pojo.TbEngineFactoryDynamic;
import cn.edu.zju.kpaperproject.pojo.TbSupplier;
import cn.edu.zju.kpaperproject.pojo.TbSupplierDynamic;
import lombok.Getter;
import lombok.Setter;

/**
 * .
 *
 * @author RichardLee
 * @version v1.0
 */
@Getter
@Setter
public class SupplierRank {
    private int listSize;
    /** 排名 */
    private int rankNumber;
//    /** 供应商静态数据 */
//    private TbSupplier tbSupplier;
    /** 供应商动态数据 */
    private TbSupplierDynamic tbSupplierDynamic;
}
