package cn.edu.zju.kpaperproject.service;

import cn.edu.zju.kpaperproject.dto.*;
import cn.edu.zju.kpaperproject.pojo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * .
 *
 * @author RichardLee
 * @version v1.0
 */
public interface ProcessTaskService {
    /**
     * 获取交易契约
     *
     * @param listListEngineFactoryTasks 按主机厂分的任务集合, 每个元素是一个主机厂集合(集合内元素是该主机厂的任务集)
     * @param listListSupplierTask       按任务分开后的供应商服务集合
     * @param mapRelationshipMatrix      关系强度
     * @param listEngineFactory          活着的主机厂
     * @return 匹配上的主机厂与供应商的交易契约
     */
    ArrayList<TransactionContract> getTransactionContracts(
            ArrayList<ArrayList<EngineFactoryManufacturingTask>> listListEngineFactoryTasks
            , ArrayList<ArrayList<SupplierTask>> listListSupplierTask
            , Map<String, Double> mapRelationshipMatrix, List<TbEngineFactory> listEngineFactory);

    /**
     * 交易结算
     *
     * @param experimentsNumber                          实验次数
     * @param cycleTimes                                 循环次数
     * @param listTransactionContracts                   交易契约集合
     * @param mapRelationshipMatrix                      关系矩阵1
     * @param mapRelationshipMatrix2WithTbRelationMatrix 关系矩阵2
     * @param mapEngineFactoryIdVsRank
     * @param mapSupplierIdVsRank
     * @return 实际交易结果集合
     */
    List<OrderPlus> getTransactionSettlement(
            int experimentsNumber
            , int cycleTimes
            , ArrayList<TransactionContract> listTransactionContracts
            , Map<String, Double> mapRelationshipMatrix
            , Map<String, TbRelationMatrix> mapRelationshipMatrix2WithTbRelationMatrix, Map<String, EngineFactoryRank> mapEngineFactoryIdVsRank, Map<String, SupplierRank> mapSupplierIdVsRank);
}
