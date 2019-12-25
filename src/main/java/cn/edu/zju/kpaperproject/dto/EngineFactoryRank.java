package cn.edu.zju.kpaperproject.dto;

import cn.edu.zju.kpaperproject.pojo.TbEngineFactory;
import cn.edu.zju.kpaperproject.pojo.TbEngineFactoryDynamic;
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
public class EngineFactoryRank {
    private int listSize;
    /** 排名 */
    private int rankNumber;
//    /** 主机厂静态数据 */
//    private TbEngineFactory tbEngineFactory;
    /** 主机厂动态数据 */
    private TbEngineFactoryDynamic tbEngineFactoryDynamic;
}
