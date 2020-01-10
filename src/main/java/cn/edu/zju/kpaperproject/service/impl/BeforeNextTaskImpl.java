package cn.edu.zju.kpaperproject.service.impl;


import cn.edu.zju.kpaperproject.dto.EngineFactoryManufacturingTask;
import cn.edu.zju.kpaperproject.dto.TransactionContract;
import cn.edu.zju.kpaperproject.enums.CalculationEnum;
import cn.edu.zju.kpaperproject.enums.EngineFactoryEnum;
import cn.edu.zju.kpaperproject.enums.NumberEnum;
import cn.edu.zju.kpaperproject.enums.SupplierEnum;
import cn.edu.zju.kpaperproject.mapper.*;
import cn.edu.zju.kpaperproject.pojo.*;
import cn.edu.zju.kpaperproject.service.BeforeNextTask;
import cn.edu.zju.kpaperproject.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * .
 *
 * @author RichardLee
 * @version v1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class BeforeNextTaskImpl implements BeforeNextTask {

    @Autowired
    private TbEngineFactoryMapper tbEngineFactoryMapper;
    @Autowired
    private TbEngineFactoryDynamicMapper tbEngineFactoryDynamicMapper;
    @Autowired
    private TbSupplierMapper tbSupplierMapper;
    @Autowired
    private TbSupplierDynamicMapper tbSupplierDynamicMapper;

    @Autowired
    private TbRelationMatrixMapper tbRelationMatrixMapper;

    @Autowired
    private EngineFactoryFinalProvisionMapper engineFactoryFinalProvisionMapper;

    @Autowired
    private TbSupplierTypeAvgMapper tbSupplierTypeAvgMapper;

    @Autowired
    private TbBalanceMapper tbBalanceMapper;

    @Autowired
    private TbTransactionContractMapper tbTransactionContractMapper;

    @Autowired
    private DemandForecastMapper demandForecastMapper;
    /**
     * 计算总资产
     * // 计算主机厂的产能利用率
     * // 主机厂修改服务质量-售价-产能
     * // 计算供应商的产能利用率
     * // 供应商修改服务质量-售价-产能
     * // 企业进入与退出
     * // 双方信誉度归一化
     * <p>
     * 对主机厂动态数据及服务商动态数据重算
     *
     * @param experimentsNumber                          实验次数
     * @param cycleTime                                  循环次数
     * @param listEngineFactoryFinalProvisions           最终交货结果集合
     * @param listOrderPlus                              实际交易结果集合
     * @param listTransactionContract                    交易契约集合
     * @param listEngineFactory                          主机厂集合
     * @param listEngineFactoryDynamic                   主机厂动态数据的集合
     * @param listSupplier                               供应商集合
     * @param listSupplierDynamics                       所有存活服务商动态数据集合
     * @param mapRelationshipMatrix2WithTbRelationMatrix 关系矩阵
     */
    @Override
    public void beforeNextTask(
            int experimentsNumber
            , int cycleTime
            , List<EngineFactoryFinalProvision> listEngineFactoryFinalProvisions
            , List<OrderPlus> listOrderPlus
            , ArrayList<TransactionContract> listTransactionContract
            , List<TbEngineFactory> listEngineFactory
            , List<TbEngineFactoryDynamic> listEngineFactoryDynamic
            , List<TbSupplier> listSupplier
            , List<TbSupplierDynamic> listSupplierDynamics
            , Map<String, TbRelationMatrix> mapRelationshipMatrix2WithTbRelationMatrix
            , ArrayList<ArrayList<EngineFactoryManufacturingTask>> listListEngineFactoryTaskDecomposition
    ) {

        // 填充map数据
        Map<String, TbEngineFactory> mapEngineFactory = getMapEngineFactoryIdVsEngineFactory(listEngineFactory);
        Map<String, TbSupplier> mapSupplier = getMapSupplierIdVsSupplier(listSupplier);

        Map<String, TbEngineFactoryDynamic> mapEngineFactoryDynamic = getMapEngineFactoryIdVsEngineFactoryDynamic(listEngineFactoryDynamic);
        Map<String, TbSupplierDynamic> mapSupplierDynamic = getMapSupplierIdVsSupplierDynamic(listSupplierDynamics);


        // 暂存主机厂利润和
        Map<String, Integer> mapEngineFactoryProfitSum = new HashMap<>(100);

        // 暂存供应方利润和
        Map<String, Integer> mapSupplierProfitSum = new HashMap<>(800);

        // 用来存id, 看是否有交易
        // 供应商对应所有的订单映射
        HashMap<String, List<TransactionContract>> mapSupplierIdVsTransactionContract = new HashMap<>(100);

        for (TransactionContract aTransactionContract : listTransactionContract) {
            String supplierId = aTransactionContract.getSupplierId();
            if (!mapSupplierIdVsTransactionContract.containsKey(supplierId)) {
                // 无
                List<TransactionContract> listTmp = new ArrayList<>();
                listTmp.add(aTransactionContract);
                mapSupplierIdVsTransactionContract.put(supplierId, listTmp);
            } else {
                // 有
                List<TransactionContract> listTmp = mapSupplierIdVsTransactionContract.get(supplierId);
                listTmp.add(aTransactionContract);
            }

        }


        // 算出各主机厂与供应商之间的交易利润和,并加入到各自对应的map集合中
        setMapEngineFactoryAndSupplierProfitSum(listOrderPlus, mapEngineFactoryProfitSum, mapSupplierProfitSum, mapEngineFactoryDynamic, mapSupplierDynamic);


        // 主机厂总资产计算
        // 主机厂与市场交易Map
        HashMap<String, EngineFactoryFinalProvision> mapEngineIdVsEngineFactoryFinalProvision = new HashMap<>(100);

        for (EngineFactoryFinalProvision aEngineFactoryFinalProvision : listEngineFactoryFinalProvisions) {
            mapEngineIdVsEngineFactoryFinalProvision.put(aEngineFactoryFinalProvision.getEngineFactoryId(), aEngineFactoryFinalProvision);

        }


        // 计算所有主机厂的总资产
        calAndSetEngineFactoryTotalAssets(listEngineFactoryDynamic, mapEngineFactoryProfitSum, mapEngineIdVsEngineFactoryFinalProvision);

        // 计算供应商总资产并更新
        calAndSetSupplierTotalAssets(listSupplierDynamics, mapSupplierProfitSum);


        // 主机厂的退出
        TbEngineFactory tbEngineFactory = null;

        // 主机厂的退出
        for (int i = 0; i < listEngineFactoryDynamic.size(); i++) {
            TbEngineFactoryDynamic aEngineFactoryDynamic = listEngineFactoryDynamic.get(i);
            int engineFactoryTotalAssets = aEngineFactoryDynamic.getEngineFactoryTotalAssetsP();
            if (engineFactoryTotalAssets < 0) {
                String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
                TbEngineFactory engineFactory = mapEngineFactory.get(engineFactoryId);
                engineFactory.setEngineFactoryAlive(false);
                listEngineFactoryDynamic.remove(i--);
            }
        }

        // 供应商的退出
        TbSupplier tbSupplier = null;
//        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {
        for (int i = 0; i < listSupplierDynamics.size(); i++) {
            TbSupplierDynamic aSupplierDynamic = listSupplierDynamics.get(i);
            int supplierTotalAssets = aSupplierDynamic.getSupplierTotalAssetsP();
            if (supplierTotalAssets < 0) {
                String supplierId = aSupplierDynamic.getSupplierId();
                tbSupplier = mapSupplier.get(supplierId);
                tbSupplier.setSupplierAlive(false);
                listSupplierDynamics.remove(i--);
            }
        }


        // 生成主机厂与主机厂之间能观测到的厂子
        Map<String, List<TbEngineFactoryDynamic>> mapEngineFactoryIdVsListEngineFactoryDynamic = getMapEngineFactoryIdVsListEngineFactoryDynamic(listEngineFactoryDynamic, mapEngineFactory);

        Map<String, EngineFactoryFinalProvision> mapEngineFactoryIdVsFinalProvision = new HashMap<>(listEngineFactoryFinalProvisions.size());
        for (EngineFactoryFinalProvision aEngineFactoryFinalProvision : listEngineFactoryFinalProvisions) {
            String engineFactoryId = aEngineFactoryFinalProvision.getEngineFactoryId();
            mapEngineFactoryIdVsFinalProvision.put(engineFactoryId, aEngineFactoryFinalProvision);
        }


        // 对主机厂计算并设置产能利用率, 调整产能/价格/质量
        calAndSetEngineFactoryCapacityUtilizationAndAdjustCapacityPriceQuality(listEngineFactoryDynamic, mapEngineIdVsEngineFactoryFinalProvision, mapEngineFactoryIdVsListEngineFactoryDynamic, mapEngineFactoryIdVsFinalProvision);


        // 生成供应商与主机厂之间能观测到的场子
        Map<String, List<TbEngineFactoryDynamic>> mapSupplierIdVsListEngineFactoryDynamic = getMapSupplierIdVsListEngineFactoryDynamic(listSupplierDynamics, mapSupplier, listEngineFactoryDynamic, mapEngineFactory);

        // 计算供应商的产能利用率
        calAndSetSupplierCapacityUtilizationAndAdjustCapacityPriceQuality(
                listSupplierDynamics, mapSupplierIdVsTransactionContract, listListEngineFactoryTaskDecomposition
                , mapSupplierIdVsListEngineFactoryDynamic, listTransactionContract, listOrderPlus);

        // # 企业进入与退出
        // 淘汰
        eliminationEngineFactoryAndSupplier(listEngineFactoryDynamic, mapEngineFactory, listSupplierDynamics, mapSupplier);


        // 主机厂创新
        for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamic) {
            String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
            if (mapEngineFactory.get(engineFactoryId).getEngineFactoryAlive()) {
                double engineFactoryInnovationProbability = aEngineFactoryDynamic.getEngineFactoryInnovationProbability();
                if (RandomUtils.nextDouble(0, 1) < engineFactoryInnovationProbability) {
                    // 有创新
                    aEngineFactoryDynamic.setEngineFactoryWhetherInnovation(true);
                    aEngineFactoryDynamic.setEngineFactoryInnovationTimes(aEngineFactoryDynamic.getEngineFactoryInnovationTimes() + 1);
                    // 质量改变
                    int newEngineFactoryQuality = aEngineFactoryDynamic.getEngineFactoryQualityQ() + RandomUtils.nextInt(0, 5) - 1;
                    // 质量限
                    newEngineFactoryQuality = newEngineFactoryQuality > 10 ? 10 : (newEngineFactoryQuality < 1 ? 1 : newEngineFactoryQuality);
                    aEngineFactoryDynamic.setEngineFactoryQualityQ(newEngineFactoryQuality);
                } else {
                    // 无创新
                    aEngineFactoryDynamic.setEngineFactoryWhetherInnovation(false);
                }
            }
        }
        // 供应商创新
        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {
            String supplierId = aSupplierDynamic.getSupplierId();
            if (mapSupplier.get(supplierId).getSupplierAlive()) {
                double supplierInnovationProbability = aSupplierDynamic.getSupplierInnovationProbability();
                if (RandomUtils.nextDouble(0, 1) < supplierInnovationProbability) {
                    // 有创新
                    aSupplierDynamic.setSupplierWhetherInnovation(true);
                    aSupplierDynamic.setSupplierInnovationTimes(aSupplierDynamic.getSupplierInnovationTimes() + 1);
                    // 质量改变
                    int newSupplierQuality = aSupplierDynamic.getSupplierQualityQs() + RandomUtils.nextInt(0, 5) - 1;
                    // 质量限
                    newSupplierQuality = newSupplierQuality > 10 ? 10 : (newSupplierQuality < 1 ? 1 : newSupplierQuality);
                    aSupplierDynamic.setSupplierQualityQs(newSupplierQuality);
                } else {
                    // 无创新
                    aSupplierDynamic.setSupplierWhetherInnovation(false);
                }
            }
        }

        // 信誉度前30的主机厂集合
        TbEngineFactoryDynamic[] arrEngineFactoryWith10HighestAsset = genEngineFactoryWith10HighestAsset(listEngineFactory, mapEngineFactoryDynamic);
        // 信誉度前30的供应商集合
        TbSupplierDynamic[] arrSupplierDynamicWith10HighestAsset = genSupplierWith10HighestAsset(listSupplier, mapSupplierDynamic);

        // 主机厂的进入
        // 所有还存活主机厂实际供给数
        int sumFinalProductNumberWithAlive = 0;
        // 算一下所有的主机厂实际供给数
        for (EngineFactoryFinalProvision aEngineFactoryFinalProvision : listEngineFactoryFinalProvisions) {
            String engineFactoryId = aEngineFactoryFinalProvision.getEngineFactoryId();
            TbEngineFactory tmp = mapEngineFactory.get(engineFactoryId);
            if (tmp.getEngineFactoryAlive()) {
                // 存活才加实际供给数
                sumFinalProductNumberWithAlive += aEngineFactoryFinalProvision.getFinalProductNumber();
            }
        }

        // 用来存储已有存活的主机厂位置
        Map<Double, Double> mapEngineFactoryPosition = new HashMap<>();
        // 用来存储已有存活的供应商位置
        Map<Double, Double> mapSupplierPosition = new HashMap<>();
        genMapEngineFactoryPositionAmdMapSupplierPosition(mapEngineFactoryPosition, listEngineFactory
                , mapSupplierPosition, listSupplier);


        // 需求预测的初始值
        int initMarketNeedNumber = demandForecastMapper.selectByPrimaryKey(cycleTime).getTrueDemandForecast();

        // 用来存新生成的主机厂id, 和信誉度最高的供应商id        supplierDynamicWithHighestCredit.getSupplierId();
        Map<String, String> mapNewEngineFactoryIdVsSupplierIdWithHighestCredit = new HashMap<>(3);
        EngineFactoryFinalProvision engineFactoryFinalProvision = listEngineFactoryFinalProvisions.get(0);
        int marketNeedNumber = engineFactoryFinalProvision.getMarketNeedNumber();
        TbEngineFactoryDynamic tbEngineFactoryDynamic;
        if (marketNeedNumber > sumFinalProductNumberWithAlive) {
            // 真实需求 > 所有主机厂的(阶段结束, 实际能提供的产品)之和
            // 随机生成1~3个主机厂
            int tmp = RandomUtils.nextInt(4, 7);
            for (int i = 0; i < tmp; i++) {
                tbEngineFactory = new TbEngineFactory();
                tbEngineFactoryDynamic = new TbEngineFactoryDynamic();
                // 第几轮实验
                tbEngineFactory.setExperimentsNumber(experimentsNumber);
                // 循环
                tbEngineFactory.setCycleTimes(cycleTime);

                // 工厂id
                String engineFactoryId = CommonUtils.genId();
//                log.info("new engine factory id  " + engineFactoryId);
//                log.info("  ");
//                log.info("  ");
//                log.info("  ");
                tbEngineFactory.setEngineFactoryId(engineFactoryId);
                // 随机获取总资产较大的那个供应商动态数据
                TbSupplierDynamic supplierDynamicWithHighestAsset = null;
                if (0 < arrSupplierDynamicWith10HighestAsset.length) {
                    supplierDynamicWithHighestAsset = arrSupplierDynamicWith10HighestAsset[RandomUtils.nextInt(0, arrSupplierDynamicWith10HighestAsset.length)];
                }
//                else {
//                    supplierDynamicWithHighestAsset = arrSupplierDynamicWith10HighestAsset[0];
//                }


                mapNewEngineFactoryIdVsSupplierIdWithHighestCredit.put(engineFactoryId, supplierDynamicWithHighestAsset.getSupplierId());

                // 地理位置
                tbSupplier = mapSupplier.get(supplierDynamicWithHighestAsset.getSupplierId());

                double[] position;
                if (i < tmp * 0.7) {
                    // 位置70%按照原来的
                    position = genNewPosition(new double[]{tbSupplier.getSupplierLocationGX(), tbSupplier.getSupplierLocationGY()}, mapEngineFactoryPosition);
                } else {
                    // 30%随机
                    position = InitEngineFactoryUtils.initPosition(mapEngineFactoryPosition);
                }

                tbEngineFactory.setEngineFactoryLocationGX(position[NumberEnum.POSITION_X_ARRAY_INDEX]);
                tbEngineFactory.setEngineFactoryLocationGY(position[NumberEnum.POSITION_Y_ARRAY_INDEX]);
                // 存活
                tbEngineFactory.setEngineFactoryAlive(true);
                // 存活次数初始化
                tbEngineFactory.setEngineFactoryAliveTimes(0);

                // 工厂动态数据------------------
                tbEngineFactoryDynamic.setCycleTimes(cycleTime);
                tbEngineFactoryDynamic.setEngineFactoryId(engineFactoryId);
                // 初始总资产
                tbEngineFactoryDynamic.setEngineFactoryTotalAssetsP(InitEngineFactoryUtils.initTotalAssets());
                // 最大产能
                tbEngineFactoryDynamic.setEngineFactoryCapacityM(InitEngineFactoryUtils.initCapacity());
                // 价格
                int[] price = InitEngineFactoryUtils.initPrice();
                tbEngineFactoryDynamic.setEngineFactoryPricePL(price[NumberEnum.PRICE_LOW_ARRAY_INDEX]);
                tbEngineFactoryDynamic.setEngineFactoryPricePU(price[NumberEnum.PRICE_UPPER_ARRAY_INDEX]);
                // 质量
                int quality = InitEngineFactoryUtils.initQuality();
                tbEngineFactoryDynamic.setEngineFactoryQualityQ(quality);
                // 需求预测
                // 需求预测(已改为读表)
                tbEngineFactoryDynamic.setEngineFactoryDemandForecastD(CalculationUtils.demandForecast(
                        price[NumberEnum.PRICE_LOW_ARRAY_INDEX], price[NumberEnum.PRICE_UPPER_ARRAY_INDEX], quality,initMarketNeedNumber));
                // 创新概率 0-0.5
                tbEngineFactoryDynamic.setEngineFactoryInnovationProbability(RandomUtils.nextDouble(0, 0.5));
                tbEngineFactoryDynamic.setEngineFactoryInnovationTimes(0);
                // 加入集合中
                listEngineFactory.add(tbEngineFactory);
                mapEngineFactory.put(engineFactoryId, tbEngineFactory);
                listEngineFactoryDynamic.add(tbEngineFactoryDynamic);
                mapEngineFactoryDynamic.put(engineFactoryId, tbEngineFactoryDynamic);
            }
        }


        // 一类服务, 供应商的实际需求量之和
        int[] sumArrEngineFactoryNeedServiceNumberWithAlive = new int[5];
        // 一类服务, 供应商信誉度之和
//        double[] sumArrSupplierCreditWithAlive = new double[5];
        // 一类服务, 供应商存活的数量
        int[] sumArrSupplierIsAlive = new int[5];


        for (TbEngineFactory aEngineFactory : listEngineFactory) {
            String engineFactoryId = aEngineFactory.getEngineFactoryId();
            TbEngineFactoryDynamic tbEngineFactoryDynamic1 = mapEngineFactoryDynamic.get(engineFactoryId);
            int engineFactoryDemandForecastD = Math.min(tbEngineFactoryDynamic1.getEngineFactoryDemandForecastD(),tbEngineFactoryDynamic1.getEngineFactoryCapacityM());
            sumArrEngineFactoryNeedServiceNumberWithAlive[0] += engineFactoryDemandForecastD;
            sumArrEngineFactoryNeedServiceNumberWithAlive[1] += engineFactoryDemandForecastD;
            sumArrEngineFactoryNeedServiceNumberWithAlive[2] += engineFactoryDemandForecastD;
            sumArrEngineFactoryNeedServiceNumberWithAlive[3] += engineFactoryDemandForecastD;
            sumArrEngineFactoryNeedServiceNumberWithAlive[4] += engineFactoryDemandForecastD;
        }

        // 存活的供应商中, 信誉度之和 存活个数计算
        for (TbSupplier tmpSupplier : listSupplier) {
            if (tmpSupplier.getSupplierAlive()) {
                String supplierId = tmpSupplier.getSupplierId();
                TbSupplierDynamic supplierDynamic = mapSupplierDynamic.get(supplierId);
//                double supplierCredit = supplierDynamic.getSupplierCreditA();
                switch (tmpSupplier.getSupplierType()) {
                    case 210:
//                        sumArrSupplierCreditWithAlive[0] += supplierCredit;
                        sumArrSupplierIsAlive[0]++;
                        break;
                    case 220:
//                        sumArrSupplierCreditWithAlive[1] += supplierCredit;
                        sumArrSupplierIsAlive[1]++;
                        break;
                    case 230:
//                        sumArrSupplierCreditWithAlive[2] += supplierCredit;
                        sumArrSupplierIsAlive[2]++;
                        break;
                    case 240:
//                        sumArrSupplierCreditWithAlive[3] += supplierCredit;
                        sumArrSupplierIsAlive[3]++;
                        break;
                    case 250:
//                        sumArrSupplierCreditWithAlive[4] += supplierCredit;
                        sumArrSupplierIsAlive[4]++;
                        break;
                    default:
                        throw new RuntimeException("no such type");
                }
            }

        }


        // 供应商的进入
        // 某类服务供应商产能之和
        int[] supplierTypeCode = {SupplierEnum.supplierType210, SupplierEnum.supplierType220, SupplierEnum.supplierType230
                , SupplierEnum.supplierType240, SupplierEnum.supplierType250};

        int[] sumArrSupplierCapacityWithAlive = new int[5];

        // 这个用来给某类供应商都死亡后的信誉度平均值
        double sumSupplierCapacityAllWithAlive = 0D;
        int sumSupplierNumberAllWithAlive = 0;
        for (TbSupplier tmpSupplier : listSupplier) {

            String supplierId = tmpSupplier.getSupplierId();
            TbSupplierDynamic aSupplierDynamic = mapSupplierDynamic.get(supplierId);

            if (tmpSupplier.getSupplierAlive()) {
                // 还活着的供应商
//                sumSupplierCapacityAllWithAlive += aSupplierDynamic.getSupplierCreditA();
                sumSupplierNumberAllWithAlive++;

                int type = tmpSupplier.getSupplierType();
                int supplierCapacity = aSupplierDynamic.getSupplierCapacityM();
                switch (type) {
                    case 210:
                        sumArrSupplierCapacityWithAlive[0] += supplierCapacity;
                        break;
                    case 220:
                        sumArrSupplierCapacityWithAlive[1] += supplierCapacity;
                        break;
                    case 230:
                        sumArrSupplierCapacityWithAlive[2] += supplierCapacity;
                        break;
                    case 240:
                        sumArrSupplierCapacityWithAlive[3] += supplierCapacity;
                        break;
                    case 250:
                        sumArrSupplierCapacityWithAlive[4] += supplierCapacity;
                        break;
                    default:
                        throw new RuntimeException("no such type");
                }
            }
        }
        // 用来存储新生成的供应商ID
        Map<String, String> mapNewSupplierIdVsEngineFactoryIdWithHighestCredit = new HashMap<>(50);
        // 随机生成供应商
        for (int i = 0; i < sumArrSupplierIsAlive.length; i++) {
//            double aveSupplierCreditTmp = 0;
//            if (sumArrSupplierIsAlive[i] != 0) {
//                // 某类供应商数量不为0
//                aveSupplierCreditTmp = sumArrSupplierCreditWithAlive[i] * 1D / sumArrSupplierIsAlive[i];
//            } else {
//                aveSupplierCreditTmp = 1;
//            }

            int engineFactoryNeedServiceNumberWithAlive = sumArrEngineFactoryNeedServiceNumberWithAlive[i];
            int supplierCapacity = sumArrSupplierCapacityWithAlive[i];
//            log.error("|||engineFactoryNeedServiceNumberWithAlive " + supplierTypeCode[i]+"  :  " + sumArrEngineFactoryNeedServiceNumberWithAlive[i]);
//            log.error("supplierCapacity " + supplierTypeCode[i]+"  :  " + sumArrSupplierCapacityWithAlive[i]);
//            log.error("|||supplierCapacity < engineFactoryNeedServiceNumberWithAlive : "+(supplierCapacity < engineFactoryNeedServiceNumberWithAlive));
            if (supplierCapacity < engineFactoryNeedServiceNumberWithAlive) {
                // 供应商产能 < 主机厂对该类服务的需求
                int tmp = RandomUtils.nextInt(4, 7);
//                log.info(supplierTypeCode[i]+" 生成供应商个数 "+tmp);
                for (int j = 0; j < tmp; j++) {
                    tbSupplier = new TbSupplier();
                    tbSupplier.setExperimentsNumber(experimentsNumber);
                    // 存活次数初始化
                    tbSupplier.setSupplierAliveTimes(0);
                    tbSupplier.setCycleTimes(cycleTime);
                    TbSupplierDynamic tbSupplierDynamic = new TbSupplierDynamic();
                    tbSupplierDynamic.setCycleTimes(cycleTime);

                    // 供应商id
                    String supplierId = CommonUtils.genId();
                    tbSupplier.setSupplierId(supplierId);

                    // 获取信誉度较大的一个主机厂动态数据
                    TbEngineFactoryDynamic engineFactoryDynamicWithHighestAsset = arrEngineFactoryWith10HighestAsset[RandomUtils.nextInt(0, arrEngineFactoryWith10HighestAsset.length)];

                    mapNewSupplierIdVsEngineFactoryIdWithHighestCredit.put(supplierId, engineFactoryDynamicWithHighestAsset.getEngineFactoryId());

                    // 地理位置
                    tbEngineFactory = mapEngineFactory.get(engineFactoryDynamicWithHighestAsset.getEngineFactoryId());
                    double[] position;
                    if (i < tmp * 0.7) {
                        position = genNewPosition(new double[]{tbEngineFactory.getEngineFactoryLocationGX(), tbEngineFactory.getEngineFactoryLocationGY()}, mapSupplierPosition);
                    } else {
                        position = InitSupplierUtils.initPosition(mapSupplierPosition);
                    }
                    tbSupplier.setSupplierLocationGX(position[NumberEnum.POSITION_X_ARRAY_INDEX]);
                    tbSupplier.setSupplierLocationGY(position[NumberEnum.POSITION_Y_ARRAY_INDEX]);
                    // 供应商代码
                    tbSupplier.setSupplierType(InitSupplierUtils.initType(supplierTypeCode[i]));
                    // 每阶段固定成本
                    tbSupplier.setSupplierFixedCostC(InitSupplierUtils.initFixedCost());
                    tbSupplier.setSupplierAlive(true);
                    // 动态数据----------------
                    tbSupplierDynamic.setCycleTimes(cycleTime);
                    // 供应商id
                    tbSupplierDynamic.setSupplierId(supplierId);
                    // 初始总资产
                    tbSupplierDynamic.setSupplierTotalAssetsP(InitSupplierUtils.initTotalAssets());
                    // 信誉度
//                    tbSupplierDynamic.setSupplierCreditA(aveSupplierCreditTmp);
                    // 最大产能
                    tbSupplierDynamic.setSupplierCapacityM(InitSupplierUtils.initCapacity());
                    // 价格
                    int[] price = InitSupplierUtils.initPrice();
                    tbSupplierDynamic.setSupplierPricePL(price[NumberEnum.PRICE_LOW_ARRAY_INDEX]);
                    tbSupplierDynamic.setSupplierPricePU(price[NumberEnum.PRICE_UPPER_ARRAY_INDEX]);
                    // 质量
                    tbSupplierDynamic.setSupplierQualityQs(InitSupplierUtils.initQuality());
                    // 创新概率0-0.5
                    tbSupplierDynamic.setSupplierInnovationProbability(RandomUtils.nextDouble(0, 0.5));
                    tbSupplierDynamic.setSupplierInnovationTimes(0);
                    // 动态数据里的type
                    tbSupplierDynamic.setSupplierType(InitSupplierUtils.initType(supplierTypeCode[i]));
                    // 加入集合中
                    listSupplier.add(tbSupplier);
                    mapSupplier.put(supplierId, tbSupplier);
                    listSupplierDynamics.add(tbSupplierDynamic);
                    mapSupplierDynamic.put(supplierId, tbSupplierDynamic);
                }
            }
        }

        // 算两者距离, 存入map里
        Map<String, Double> mapDistance = new HashMap<>(100);
        for (TbEngineFactory aTbEngineFactory : listEngineFactory) {
            String engineFactoryId = aTbEngineFactory.getEngineFactoryId();
            double[] engineFactoryLocation = {aTbEngineFactory.getEngineFactoryLocationGX(), aTbEngineFactory.getEngineFactoryLocationGY()};
            for (TbSupplier aSupplier : listSupplier) {
                String supplierId = aSupplier.getSupplierId();
                String key = engineFactoryId + supplierId;

                double[] supplierLocation = {aSupplier.getSupplierLocationGX(), aSupplier.getSupplierLocationGY()};
                double value = CalculationUtils.calDistance(engineFactoryLocation, supplierLocation);
                mapDistance.put(key, value);
            }
        }

        // 关系强度, 都是重新生成的, 每个阶段生成, 历史数据用map读出来,
        // 下一个阶段要用的关系矩阵
        List<TbRelationMatrix> listNewRelationMatrix = new ArrayList<>();
        // 两个循环生成关系矩阵
        for (TbEngineFactory aTbEngineFactory : listEngineFactory) {
            // 主机厂是活着的
            // 主机厂id
            String engineFactoryId = aTbEngineFactory.getEngineFactoryId();
            for (TbSupplier aTbSupplier : listSupplier) {
                // 供应商是活着的
                TbRelationMatrix tbRelationMatrix = new TbRelationMatrix();
                tbRelationMatrix.setExperimentsNumber(experimentsNumber);
                tbRelationMatrix.setCycleTimes(cycleTime);
                tbRelationMatrix.setEngineFactoryId(engineFactoryId);
                String supplierId = aTbSupplier.getSupplierId();
                tbRelationMatrix.setSupplierId(supplierId);
                // 获取之前的数据
                String mapKey = engineFactoryId + supplierId;
                tbRelationMatrix.setMapKey(mapKey);
                TbRelationMatrix oldRelationMatrix = mapRelationshipMatrix2WithTbRelationMatrix.get(mapKey);
                if (oldRelationMatrix != null) {
                    // 原来有的
                    double relationScore = oldRelationMatrix.getRelationScore();
                    // 这里要根据关系强度来修改
                    // 距离大小
                    double distanceValue = mapDistance.get(mapKey);
                    if (distanceValue > 13) {
                        relationScore = relationScore - 0.15;
                    } else if (distanceValue > 9) {
                        relationScore = relationScore - 0.1;
                    } else if (distanceValue > 5) {
                        relationScore = relationScore - 0.05;
                    }
                    tbRelationMatrix.setRelationScore(relationScore);


//                    tbRelationMatrix.setRelationScore(oldRelationMatrix.getRelationScore());
                    tbRelationMatrix.setInitialRelationalDegree(oldRelationMatrix.getInitialRelationalDegree());
                    tbRelationMatrix.setAccumulativeTotalScore(oldRelationMatrix.getAccumulativeTotalScore());
                    tbRelationMatrix.setTransactionNumber(oldRelationMatrix.getTransactionNumber());
                } else {
                    // 要么工厂新增, 要么供应商新增
                    if (mapNewEngineFactoryIdVsSupplierIdWithHighestCredit.containsKey(engineFactoryId)) {
                        // 工厂是新增的
                        if (supplierId.equals(mapNewEngineFactoryIdVsSupplierIdWithHighestCredit.get(engineFactoryId))) {
                            // 供应商id 与 信誉度最高的供应商id相同, 设定初始值为0.2  (1~3个工厂会有0.2)
                            tbRelationMatrix.setRelationScore(0.2);
                            tbRelationMatrix.setInitialRelationalDegree(0.2);
                        } else {
                            // 供应商id 就是普通的供应商
                            double initRelationshipStrengthScore = InitRelationMatrixUtils.initRelationshipStrengthScore();
                            tbRelationMatrix.setRelationScore(initRelationshipStrengthScore);
                            tbRelationMatrix.setInitialRelationalDegree(initRelationshipStrengthScore);
                        }
                    } else {
                        // 供应商是新增的
                        if (engineFactoryId.equals(mapNewSupplierIdVsEngineFactoryIdWithHighestCredit.get(supplierId))) {
                            // 主机厂id 与 信誉度最高的主机厂id相同   (1~3个供应商会有0.2)  (只有新生的工厂或新生的供应商有0.2)
                            tbRelationMatrix.setRelationScore(0.2);
                            tbRelationMatrix.setInitialRelationalDegree(0.2);
                        } else {
                            // 主机厂id 就是普通的主机厂
                            double initRelationshipStrengthScore = InitRelationMatrixUtils.initRelationshipStrengthScore();
                            tbRelationMatrix.setRelationScore(initRelationshipStrengthScore);
                            tbRelationMatrix.setInitialRelationalDegree(initRelationshipStrengthScore);
                        }
                    }
                    // 补全其他的值
                    tbRelationMatrix.setAccumulativeTotalScore(0d);
                    tbRelationMatrix.setTransactionNumber(0);
                }
                listNewRelationMatrix.add(tbRelationMatrix);
            }
        }


        // 计算并生成每类供应商的平均价格和平均质量
        // 生成并插入tb_supplier_type_avg表里
        calSupplierTypeAvgPriceAndQuality(experimentsNumber, cycleTime, listOrderPlus, mapSupplier);

        // 算出每个二级供应商的实际平均价格和实际平均质量
        setSupplierAvgPriceAndAvgQuality(listSupplierDynamics, listOrderPlus, mapSupplierDynamic);
        // 计算一级市场供需情况和二级市场供需情况
        calBalance(experimentsNumber, cycleTime, listEngineFactoryFinalProvisions, listEngineFactoryDynamic, mapEngineFactory, listOrderPlus, listSupplierDynamics, mapSupplier);

        for (TbEngineFactory aEngineFactory : listEngineFactory) {
            aEngineFactory.setCycleTimes(cycleTime);
            aEngineFactory.setEngineFactoryAliveTimes(aEngineFactory.getEngineFactoryAliveTimes() + 1);
            TbEngineFactoryDynamic tbEngineFactoryDynamic1 = mapEngineFactoryDynamic.get(aEngineFactory.getEngineFactoryId());
            tbEngineFactoryDynamic1.setCycleTimes(cycleTime);
            tbEngineFactoryDynamic1.setExperimentsNumber(experimentsNumber);
            // 需求预测
            tbEngineFactoryDynamic1.setEngineFactoryDemandForecastD(
                    CalculationUtils.demandForecast(
                            tbEngineFactoryDynamic1.getEngineFactoryPricePL(), tbEngineFactoryDynamic1.getEngineFactoryPricePU()
                            , tbEngineFactoryDynamic1.getEngineFactoryQualityQ(), initMarketNeedNumber));

        }
        for (TbSupplier aSupplier : listSupplier) {
            aSupplier.setCycleTimes(cycleTime);
            aSupplier.setSupplierAliveTimes(aSupplier.getSupplierAliveTimes() + 1);
            TbSupplierDynamic supplierDynamic = mapSupplierDynamic.get(aSupplier.getSupplierId());
            supplierDynamic.setCycleTimes(cycleTime);
            supplierDynamic.setExperimentsNumber(experimentsNumber);
        }

        storeTransactionContract(experimentsNumber, cycleTime, listTransactionContract);

        // 此时已经完成进入和退出
        storeIntoDatabase(listEngineFactory, listEngineFactoryDynamic, listSupplier, listSupplierDynamics, listNewRelationMatrix);

    }

    private void eliminationEngineFactoryAndSupplier(List<TbEngineFactoryDynamic> listEngineFactoryDynamic, Map<String, TbEngineFactory> mapEngineFactory, List<TbSupplierDynamic> listSupplierDynamics, Map<String, TbSupplier> mapSupplier) {
        // 要淘汰的主机厂
        Set<TbEngineFactory> setEngineFactory = new HashSet<>();
        // 要淘汰的供应商
//        Set<TbSupplier> setSupplier = new HashSet<>();

        // 主机厂(价格/质量)淘汰高的30%
        Queue<TbEngineFactoryDynamic> queueEngineFactoryDynamicTmp1 = new PriorityQueue<>(((o1, o2) ->
                (o2.getEngineFactoryPricePL()+o2.getEngineFactoryPricePU()) /2d / o2.getEngineFactoryQualityQ()
                        - (o1.getEngineFactoryPricePL()+o1.getEngineFactoryPricePU()) / 2d / o1.getEngineFactoryQualityQ() >= 0 ? 1 : -1));
        // 主机厂和供应商信誉度淘汰10%小的
//        Queue<TbEngineFactoryDynamic> queueEngineFactoryDynamicTmp2 = new PriorityQueue<>(((o1, o2) ->
//                o1.getEngineFactoryCreditH() - o2.getEngineFactoryCreditH() >= 0 ? 1 : -1));
//        Queue<TbSupplierDynamic> queueSupplierDynamic = new PriorityQueue<>(((o1, o2) ->
//                o1.getSupplierCreditA() - o2.getSupplierCreditA() > 0 ? 1 : -1));
        // 主机厂
        for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamic) {
            if (mapEngineFactory.get(aEngineFactoryDynamic.getEngineFactoryId()).getEngineFactoryAlive()) {
                queueEngineFactoryDynamicTmp1.add(aEngineFactoryDynamic);
//                queueEngineFactoryDynamicTmp2.add(aEngineFactoryDynamic);
            }
        }
        // 供应商
//        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamic) {
//            if (mapSupplier.get(aSupplierDynamic.getSupplierId()).getSupplierAlive()) {
//                queueSupplierDynamic.add(aSupplierDynamic);
//            }
//        }

        //---测试
//        log.info("Test queueEngineFactoryDynamicTmp2");
//        while (queueEngineFactoryDynamicTmp2.peek() != null) {
//            log.info(queueEngineFactoryDynamicTmp2.poll().getEngineFactoryCreditH().toString());
//        }
//        log.info("");
//        log.info("");
        //---
//
//
        // 主机厂与供应商应该淘汰的个数
        int engineFactoryDynamicShouldEliminationNumber = (int) (queueEngineFactoryDynamicTmp1.size() * 0.3);
//        int supplierShouldEliminationNumber = (int) (queueSupplierDynamic.size() * 0.1);
        for (int i = 0; i < engineFactoryDynamicShouldEliminationNumber; i++) {
            TbEngineFactoryDynamic engineFactoryDynamicTmp1 = queueEngineFactoryDynamicTmp1.poll();
//            TbEngineFactoryDynamic engineFactoryDynamicTmp2 = queueEngineFactoryDynamicTmp2.poll();
            setEngineFactory.add(mapEngineFactory.get(engineFactoryDynamicTmp1.getEngineFactoryId()));
//            setEngineFactory.add(mapEngineFactory.get(engineFactoryDynamicTmp2.getEngineFactoryId()));
        }
//        for (int i = 0; i < supplierShouldEliminationNumber; i++) {
//            setSupplier.add(mapSupplier.get(queueSupplierDynamic.poll().getSupplierId()));
//        }

        // 主机厂与供应商设置为false
        for (TbEngineFactory engineFactory : setEngineFactory) {
            engineFactory.setEngineFactoryAlive(false);
        }
//        for (TbSupplier aSupplier : setSupplier) {
//            aSupplier.setSupplierAlive(false);
//        }

    }

    /**
     * 供应商可见供应商范围
     *
     * @param listSupplierDynamics
     * @param mapSupplier
     * @param listEngineFactoryDynamic
     * @param mapEngineFactory
     * @return
     */
    private Map<String, List<TbEngineFactoryDynamic>> getMapSupplierIdVsListEngineFactoryDynamic(List<TbSupplierDynamic> listSupplierDynamics, Map<String, TbSupplier> mapSupplier, List<TbEngineFactoryDynamic> listEngineFactoryDynamic, Map<String, TbEngineFactory> mapEngineFactory) {
        listSupplierDynamics.sort(((o1, o2) -> o2.getSupplierTotalAssetsP() - o1.getSupplierTotalAssetsP()));
        Map<String, List<TbEngineFactoryDynamic>> mapRes = new HashMap<>(50);
        for (int i = 0; i < listSupplierDynamics.size(); i++) {
            TbSupplierDynamic aSupplierDynamic = listSupplierDynamics.get(i);
            String supplierId = aSupplierDynamic.getSupplierId();
            TbSupplier supplier = mapSupplier.get(supplierId);
            // 用来排序的
            Map<Double, TbEngineFactoryDynamic> mapDistanceVsTbEngineFactoryDynamic = new TreeMap<>(((o1, o2) -> o1 - o2 >= 0 ? 1 : -1));
            double[] supplierPosition = {supplier.getSupplierLocationGX(), supplier.getSupplierLocationGY()};
            for (TbEngineFactoryDynamic tmp : listEngineFactoryDynamic) {
                String tmpEngineFactoryId = tmp.getEngineFactoryId();
                TbEngineFactory tmpEngineFactory = mapEngineFactory.get(tmpEngineFactoryId);
                // 不是同一家, 还活着, 才计算距离
                double[] tmpEngineFactoryPosition = {tmpEngineFactory.getEngineFactoryLocationGX(), tmpEngineFactory.getEngineFactoryLocationGY()};
                double v = CalculationUtils.calDistance(supplierPosition, tmpEngineFactoryPosition);
                mapDistanceVsTbEngineFactoryDynamic.put(v, tmp);
            }

            // i=0,i=最后那个, i=中间的
            int indexLimit;
            if (i == 0) {
                indexLimit = (int) (mapDistanceVsTbEngineFactoryDynamic.size() * 0.8);
                indexLimit = indexLimit > 0 ? indexLimit : mapDistanceVsTbEngineFactoryDynamic.size();
            } else if (i == listEngineFactoryDynamic.size() - 1) {
                indexLimit = (int) (mapDistanceVsTbEngineFactoryDynamic.size() * 0.5);
                indexLimit = indexLimit > 0 ? indexLimit : 1;
            } else {
                indexLimit = (int) (mapDistanceVsTbEngineFactoryDynamic.size() * 0.2);
                indexLimit = indexLimit > 0 ? indexLimit : 1;
            }
            int index = 0;
            List<TbEngineFactoryDynamic> listTmp = new ArrayList<>();
            for (Map.Entry<Double, TbEngineFactoryDynamic> entry : mapDistanceVsTbEngineFactoryDynamic.entrySet()) {
                if (index <= indexLimit) {
                    listTmp.add(entry.getValue());
                }
                index++;
            }
            mapRes.put(supplierId, listTmp);
        }
        return mapRes;
    }

    /**
     * 主机厂可见主机厂范围
     *
     * @param listEngineFactoryDynamic listEngineFactoryDynamic会除去死的, 并排序
     * @param mapEngineFactory
     * @return
     */
    private Map<String, List<TbEngineFactoryDynamic>> getMapEngineFactoryIdVsListEngineFactoryDynamic(List<TbEngineFactoryDynamic> listEngineFactoryDynamic, Map<String, TbEngineFactory> mapEngineFactory) {
        // 都是活的了, 再排个序
        listEngineFactoryDynamic.sort(((o1, o2) -> o2.getEngineFactoryTotalAssetsP() - o1.getEngineFactoryTotalAssetsP()));

        // 主机厂可见的企业Map
        Map<String, List<TbEngineFactoryDynamic>> mapRes = new HashMap<>(30);
        for (int i = 0; i < listEngineFactoryDynamic.size(); i++) {
            TbEngineFactoryDynamic aEngineFactoryDynamic = listEngineFactoryDynamic.get(i);
            String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
            TbEngineFactory engineFactory = mapEngineFactory.get(engineFactoryId);
            // 做个一排序的
            Map<Double, TbEngineFactoryDynamic> mapDistanceVsTbEngineFactoryDynamic = new TreeMap<>(((o1, o2) -> o1 - o2 >= 0 ? 1 : -1));
            double[] engineFactoryPosition = {engineFactory.getEngineFactoryLocationGX(), engineFactory.getEngineFactoryLocationGY()};

            // 计算每一个其他厂与这个厂的距离
            for (TbEngineFactoryDynamic tmp : listEngineFactoryDynamic) {
                String tmpEngineFactoryId = tmp.getEngineFactoryId();
                TbEngineFactory tmpEngineFactory = mapEngineFactory.get(tmpEngineFactoryId);
                if (!engineFactoryId.equals(tmpEngineFactoryId)) {
                    // 不是同一家, 还活着, 才计算距离
                    double[] tmpEngineFactoryPosition = {tmpEngineFactory.getEngineFactoryLocationGX(), tmpEngineFactory.getEngineFactoryLocationGY()};
                    double v = CalculationUtils.calDistance(engineFactoryPosition, tmpEngineFactoryPosition);
                    mapDistanceVsTbEngineFactoryDynamic.put(v, tmp);
                }
            }

            // i=0,i=最后那个, i=中间的
            int indexLimit;
            if (i == 0) {
                indexLimit = (int) (mapDistanceVsTbEngineFactoryDynamic.size() * 0.8);
                indexLimit = indexLimit > 0 ? indexLimit : mapDistanceVsTbEngineFactoryDynamic.size();
            } else if (i == listEngineFactoryDynamic.size() - 1) {
                indexLimit = (int) (mapDistanceVsTbEngineFactoryDynamic.size() * 0.5);
                indexLimit = indexLimit > 0 ? indexLimit : 1;
            } else {
                indexLimit = (int) (mapDistanceVsTbEngineFactoryDynamic.size() * 0.2);
                indexLimit = indexLimit > 0 ? indexLimit : 1;
            }
            int index = 0;
            List<TbEngineFactoryDynamic> listTmp = new ArrayList<>();
            for (Map.Entry<Double, TbEngineFactoryDynamic> entry : mapDistanceVsTbEngineFactoryDynamic.entrySet()) {
                if (index <= indexLimit) {
                    listTmp.add(entry.getValue());
                }
                index++;
            }
            mapRes.put(engineFactoryId, listTmp);
        }
        return mapRes;
    }


    /**
     * 生成还存活的供应商及主机厂的位置
     *
     * @param mapEngineFactoryPosition 存入的主机厂位置
     * @param listEngineFactory        主机厂静态集合
     * @param mapSupplierPosition      存入的供应商位置
     * @param listSupplier             供应商静态数据集合
     */
    private void genMapEngineFactoryPositionAmdMapSupplierPosition(Map<Double, Double> mapEngineFactoryPosition, List<TbEngineFactory> listEngineFactory, Map<Double, Double> mapSupplierPosition, List<TbSupplier> listSupplier) {
        for (TbEngineFactory aEngineFactory : listEngineFactory) {
            if (aEngineFactory.getEngineFactoryAlive()) {
                mapEngineFactoryPosition.put(aEngineFactory.getEngineFactoryLocationGX(), aEngineFactory.getEngineFactoryLocationGY());
            }
        }
        for (TbSupplier aSupplier : listSupplier) {
            if (aSupplier.getSupplierAlive()) {
                mapSupplierPosition.put(aSupplier.getSupplierLocationGX(), aSupplier.getSupplierLocationGY());
            }
        }
    }

    /**
     * 生成前10%总资产最高的供应商
     *
     * @param listSupplier       供应商集合
     * @param mapSupplierDynamic 供应商动态数据集合映射
     * @return 生成前10%总资产最高的供应商集合
     */
    private TbSupplierDynamic[] genSupplierWith10HighestAsset(List<TbSupplier> listSupplier, Map<String, TbSupplierDynamic> mapSupplierDynamic) {
        Queue<TbSupplierDynamic> queueSupplierDynamic = new PriorityQueue<>((o1, o2) -> {
            double v = o2.getSupplierTotalAssetsP() - o1.getSupplierTotalAssetsP();
            return v >= 0 ? 1 : -1;
        });
        for (TbSupplier aSupplier : listSupplier) {
            if (aSupplier.getSupplierAlive()) {
                queueSupplierDynamic.add(mapSupplierDynamic.get(aSupplier.getSupplierId()));
            }
        }
        int tmp = (int) Math.round(queueSupplierDynamic.size() * 0.1);
        tmp = tmp > 0 ? tmp : 1;
        TbSupplierDynamic[] arrSupplierDynamic = new TbSupplierDynamic[tmp];
        for (int i = 0; i < tmp; i++) {
            arrSupplierDynamic[i] = queueSupplierDynamic.poll();
        }
        return arrSupplierDynamic;
    }

    /**
     * 生成前10%总资产最高的主机厂
     *
     * @param listEngineFactory       主机厂集合
     * @param mapEngineFactoryDynamic 主机厂动态数据集合映射
     * @return 生成前10%总资产最高的主机厂集合
     */
    private TbEngineFactoryDynamic[] genEngineFactoryWith10HighestAsset(List<TbEngineFactory> listEngineFactory, Map<String, TbEngineFactoryDynamic> mapEngineFactoryDynamic) {
        Queue<TbEngineFactoryDynamic> queueEngineFactoryDynamics = new PriorityQueue<>((o1, o2) -> {
            double v = o2.getEngineFactoryTotalAssetsP() - o1.getEngineFactoryTotalAssetsP();
            return v >= 0 ? 1 : -1;
        });
        for (TbEngineFactory aEngineFactory : listEngineFactory) {
            if (aEngineFactory.getEngineFactoryAlive()) {
                queueEngineFactoryDynamics.add(mapEngineFactoryDynamic.get(aEngineFactory.getEngineFactoryId()));
            }
        }
        int tmp = (int) Math.round(queueEngineFactoryDynamics.size() * 0.1);
        tmp = tmp <= 0 ? 1 : tmp;
        TbEngineFactoryDynamic[] arrEngineFactoryDynamic = new TbEngineFactoryDynamic[tmp];
        for (int i = 0; i < tmp; i++) {
            arrEngineFactoryDynamic[i] = queueEngineFactoryDynamics.poll();
        }
        return arrEngineFactoryDynamic;
    }

    /**
     * 存交易契约
     *
     * @param experimentsNumber       实验次数
     * @param cycleTime               循环次数
     * @param listTransactionContract 交易契约集合
     */
    private void storeTransactionContract(int experimentsNumber, int cycleTime, ArrayList<TransactionContract> listTransactionContract) {
        List<TbTransactionContract> listRes = new ArrayList<>();
        for (TransactionContract aTransactionContract : listTransactionContract) {
            TbTransactionContract tbTransactionContract = new TbTransactionContract();
            BeanUtils.copyProperties(aTransactionContract, tbTransactionContract);
            tbTransactionContract.setExperimentsNumber(experimentsNumber);
            tbTransactionContract.setCycleTimes(cycleTime);
            listRes.add(tbTransactionContract);
//        tbTransactionContractMapper.insertSelective(tbTransactionContract);
        }
        tbTransactionContractMapper.insertList(listRes);
    }

    /**
     * 计算一级市场供需情况和二级市场供需情况
     *
     * @param experimentsNumber                实验次数
     * @param cycleTime                        循环次数
     * @param listEngineFactoryFinalProvisions 市场最终成交订单
     * @param listEngineFactoryDynamic         主机厂动态数据集合
     * @param mapEngineFactory                 主机厂静态映射
     * @param listOrderPlus                    主机与供应商订单
     * @param listSupplierDynamics             供应商动态数据集合
     * @param mapSupplier                      供应商映射
     */
    private void calBalance(int experimentsNumber, int cycleTime, List<EngineFactoryFinalProvision> listEngineFactoryFinalProvisions, List<TbEngineFactoryDynamic> listEngineFactoryDynamic, Map<String, TbEngineFactory> mapEngineFactory, List<OrderPlus> listOrderPlus, List<TbSupplierDynamic> listSupplierDynamics, Map<String, TbSupplier> mapSupplier) {
        TbBalance tbBalance = new TbBalance();
        tbBalance.setExperimentsNumber(experimentsNumber);
        tbBalance.setCycleTimes(cycleTime);
        // 一级市场供需平衡
        int sumActualSaleNumber = 0;
        int sumEngineFactoryCapacity = 0;
        for (EngineFactoryFinalProvision aEngineFactoryFinalProvision : listEngineFactoryFinalProvisions) {
            String engineFactoryId = aEngineFactoryFinalProvision.getEngineFactoryId();
            TbEngineFactory engineFactory = mapEngineFactory.get(engineFactoryId);
            if (engineFactory.getEngineFactoryAlive()) {
                sumActualSaleNumber += aEngineFactoryFinalProvision.getActualSaleNumber();
            }
        }
        for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamic) {
            String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
            TbEngineFactory engineFactory = mapEngineFactory.get(engineFactoryId);
            if (engineFactory.getEngineFactoryAlive()) {
                sumEngineFactoryCapacity += aEngineFactoryDynamic.getEngineFactoryCapacityM();
            }
        }
        tbBalance.setEngineFactoryBalance(sumActualSaleNumber * 1.0 / sumEngineFactoryCapacity);
        // 二级市场供需平衡
        int sumSupplierActualNumber = 0;
        for (OrderPlus aOrderPlus : listOrderPlus) {
            String supplierId = aOrderPlus.getSupplierId();
            TbSupplier tbSupplier = mapSupplier.get(supplierId);
            if (tbSupplier.getSupplierAlive()) {
                sumSupplierActualNumber += aOrderPlus.getSupplierActualNumberM();
            }
        }
        int sumSupplierCapacity = 0;
        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {
            String supplierId = aSupplierDynamic.getSupplierId();
            TbSupplier tbSupplier = mapSupplier.get(supplierId);
            if (tbSupplier.getSupplierAlive()) {
                sumSupplierCapacity += aSupplierDynamic.getSupplierCapacityM();
            }
        }
        tbBalance.setSupplierBalance(sumSupplierActualNumber * 1.0 / sumSupplierCapacity);

        tbBalanceMapper.insertSelective(tbBalance);
    }

    /**
     * 计算并生成每类供应商的平均价格和平均质量
     * 生成并插入tb_supplier_type_avg表里
     *
     * @param experimentsNumber 实验次数
     * @param cycleTime         循环次数
     * @param listOrderPlus     主机厂与供应商交易结果
     * @param mapSupplier       供应商静态数据集合
     */
    private void calSupplierTypeAvgPriceAndQuality(int experimentsNumber, int cycleTime, List<OrderPlus> listOrderPlus, Map<String, TbSupplier> mapSupplier) {
        ArrayList<TbSupplierTypeAvg> listSupplierTypeAvgArray = new ArrayList<>();
        // 供应商类型 - 实际金额
        Map<Integer, Integer> mapTmpPriceSum = new HashMap<>(listOrderPlus.size());
        // 供应商类型 - 质量
        Map<Integer, Integer> mapTmpQualitySum = new HashMap<>(listOrderPlus.size());
        // 供应商类型 - 实际交易数量
        Map<Integer, Integer> mapTmpNumberSum = new HashMap<>(listOrderPlus.size());
        for (OrderPlus orderPlus : listOrderPlus) {
            String supplierId = orderPlus.getSupplierId();
            TbSupplier tbSupplier = mapSupplier.get(supplierId);
            int supplierType = tbSupplier.getSupplierType();

            mapTmpPriceSum.putIfAbsent(supplierType, 0);
            mapTmpQualitySum.putIfAbsent(supplierType, 0);
            mapTmpNumberSum.putIfAbsent(supplierType, 0);

            int priceSum = mapTmpPriceSum.get(supplierType);
            int qualitySum = mapTmpQualitySum.get(supplierType);
            int numberSum = mapTmpNumberSum.get(supplierType);

            priceSum += orderPlus.getSupplierActualPriceP();
            mapTmpPriceSum.put(supplierType, priceSum);

            qualitySum += orderPlus.getSupplierActualQualityQs();
            mapTmpQualitySum.put(supplierType, qualitySum);

            numberSum++;
            mapTmpNumberSum.put(supplierType, numberSum);

        }
        for (Map.Entry<Integer, Integer> entry : mapTmpNumberSum.entrySet()) {
            int supplierType = entry.getKey();
            int numberSum = entry.getValue();

            int priceSum = mapTmpPriceSum.get(supplierType);
            int qualitySum = mapTmpQualitySum.get(supplierType);

            TbSupplierTypeAvg tbSupplierTypeAvg = new TbSupplierTypeAvg();
            tbSupplierTypeAvg.setExperimentsNumber(experimentsNumber);
            tbSupplierTypeAvg.setCycleTimes(cycleTime);
            tbSupplierTypeAvg.setSupplierType(supplierType);
            tbSupplierTypeAvg.setAvgActurePrice((int) (priceSum * 1.0 / numberSum));
            tbSupplierTypeAvg.setAvgActureQuality((int) (qualitySum * 1.0 / numberSum));

            listSupplierTypeAvgArray.add(tbSupplierTypeAvg);
        }


        tbSupplierTypeAvgMapper.insertList(listSupplierTypeAvgArray);
    }

    /**
     * 算出并设置每个二级供应商的实际平均价格和实际平均质量
     *
     * @param listSupplierDynamics 供应商动态数据
     * @param listOrderPlus        主机厂与供应商交易结果
     * @param mapSupplierDynamic   供应商动态数据
     */
    private void setSupplierAvgPriceAndAvgQuality(List<TbSupplierDynamic> listSupplierDynamics, List<OrderPlus> listOrderPlus, Map<String, TbSupplierDynamic> mapSupplierDynamic) {
        // 供应商id - 实际金额
        Map<String, Integer> mapTmpPriceSum = new HashMap<>(listOrderPlus.size());
        // 供应商id - 质量
        Map<String, Integer> mapTmpQualitySum = new HashMap<>(listOrderPlus.size());
        // 供应商id - 实际交易单数
        Map<String, Integer> mapTmpNumberSum = new HashMap<>(listOrderPlus.size());
        for (OrderPlus orderPlus : listOrderPlus) {
            String supplierId = orderPlus.getSupplierId();

            mapTmpPriceSum.putIfAbsent(supplierId, 0);
            mapTmpQualitySum.putIfAbsent(supplierId, 0);
            mapTmpNumberSum.putIfAbsent(supplierId, 0);


            int priceSum = mapTmpPriceSum.get(supplierId);
            int qualitySum = mapTmpQualitySum.get(supplierId);
            int numberSum = mapTmpNumberSum.get(supplierId);

            priceSum += orderPlus.getSupplierActualPriceP();
            mapTmpPriceSum.put(supplierId, priceSum);

            qualitySum += orderPlus.getSupplierActualQualityQs();
            mapTmpQualitySum.put(supplierId, qualitySum);

            numberSum++;
            mapTmpNumberSum.put(supplierId, numberSum);
        }

        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {
            String supplierId = aSupplierDynamic.getSupplierId();
            TbSupplierDynamic supplierDynamic = mapSupplierDynamic.get(supplierId);
            if (mapTmpPriceSum.containsKey(supplierId)) {
                int priceSum = mapTmpPriceSum.get(supplierId);
                int qualitySum = mapTmpQualitySum.get(supplierId);
                int numberSum = mapTmpNumberSum.get(supplierId);
                supplierDynamic.setAvgPrice((int) (priceSum * 1.0 / numberSum));
                supplierDynamic.setAvgQuality((int) (qualitySum * 1.0 / numberSum));
            }
        }
    }

    /**
     * 将更新计算好后的数据存入数据库
     *
     * @param listEngineFactory        主机厂静态数据
     * @param listEngineFactoryDynamic 主机厂动态数据
     * @param listSupplier             供应商静态数据
     * @param listSupplierDynamics     供应商动态数据
     * @param listNewRelationMatrix    主机厂与供应商之间的关系矩阵
     */
    private void storeIntoDatabase(List<TbEngineFactory> listEngineFactory, List<TbEngineFactoryDynamic> listEngineFactoryDynamic, List<TbSupplier> listSupplier, List<TbSupplierDynamic> listSupplierDynamics, List<TbRelationMatrix> listNewRelationMatrix) {
        tbEngineFactoryMapper.insertList(listEngineFactory);
        tbEngineFactoryDynamicMapper.insertList(listEngineFactoryDynamic);
        tbSupplierMapper.insertList(listSupplier);
        tbSupplierDynamicMapper.insertList(listSupplierDynamics);
        tbRelationMatrixMapper.insertList(listNewRelationMatrix);
    }

    /**
     * 用于填充供应商动态数据map
     *
     * @param listSupplierDynamics 供应商动态数据集合
     * @return 供应商id - 供应商对应的动态数据
     */
    @Override
    public Map<String, TbSupplierDynamic> getMapSupplierIdVsSupplierDynamic(List<TbSupplierDynamic> listSupplierDynamics) {
        Map<String, TbSupplierDynamic> mapEngineFactory = new HashMap<>(100);
        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {
            String supplierId = aSupplierDynamic.getSupplierId();
            mapEngineFactory.put(supplierId, aSupplierDynamic);
        }
        return mapEngineFactory;
    }

    /**
     * 用于填充主机厂动态数据map
     *
     * @param listEngineFactoryDynamic 主机厂动态数据集合
     * @return 主机厂id - 主机厂对应的动态数据
     */
    @Override
    public Map<String, TbEngineFactoryDynamic> getMapEngineFactoryIdVsEngineFactoryDynamic(List<TbEngineFactoryDynamic> listEngineFactoryDynamic) {
        Map<String, TbEngineFactoryDynamic> mapEngineFactoryDynamic = new HashMap<>(100);
        for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamic) {
            String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
            mapEngineFactoryDynamic.put(engineFactoryId, aEngineFactoryDynamic);
        }
        return mapEngineFactoryDynamic;
    }

    /**
     * 把供应商id 和 对应的供应商静态数据 存入map中
     *
     * @param listSupplier 供应商静态数据集合
     * @return 供应商id - 供应商静态数据
     */
    @Override
    public Map<String, TbSupplier> getMapSupplierIdVsSupplier(List<TbSupplier> listSupplier) {
        Map<String, TbSupplier> mapSupplier = new HashMap<>(100);
        for (TbSupplier aSupplier : listSupplier) {
            String supplierId = aSupplier.getSupplierId();
            mapSupplier.put(supplierId, aSupplier);
        }
        return mapSupplier;
    }

    /**
     * 把主机厂id 与 主机厂静态数据 存入map中
     *
     * @param listEngineFactory 主机厂静态数据集合
     * @return 主机厂id - 主机厂静态数据
     */
    @Override
    public Map<String, TbEngineFactory> getMapEngineFactoryIdVsEngineFactory(List<TbEngineFactory> listEngineFactory) {
        Map<String, TbEngineFactory> mapEngineFactory = new HashMap<>(100);
        for (TbEngineFactory aEngineFactory : listEngineFactory) {
            String engineFactoryId = aEngineFactory.getEngineFactoryId();
            mapEngineFactory.put(engineFactoryId, aEngineFactory);
        }
        return mapEngineFactory;
    }

    /**
     * 对供应商计算并设置产能利用率,
     * 调整产能/价格/质量
     *
     * @param listSupplierDynamics                    供应商动态数据集合
     * @param mapSupplierIdVsTransactionContract
     * @param listListEngineFactoryTaskDecomposition
     * @param mapSupplierIdVsListEngineFactoryDynamic
     * @param listTransactionContract
     * @param listOrderPlus
     */
    private void calAndSetSupplierCapacityUtilizationAndAdjustCapacityPriceQuality(
            List<TbSupplierDynamic> listSupplierDynamics
            , HashMap<String, List<TransactionContract>> mapSupplierIdVsTransactionContract
            , ArrayList<ArrayList<EngineFactoryManufacturingTask>> listListEngineFactoryTaskDecomposition
            , Map<String, List<TbEngineFactoryDynamic>> mapSupplierIdVsListEngineFactoryDynamic
            , List<TransactionContract> listTransactionContract, List<OrderPlus> listOrderPlus) {


        //
//        // # 计算所有供应商的产能利用率
//        // 一类服务市场总需求数量
//        int[] sumArrSupplierOrderNumber = new int[5];
//        // 一类服务供应商的个数
//        int[] sumArrSupplierNumber = new int[5];
//        // 一类供应商需求数量平均值
//        double[] avgArrSupplierOrderNumber = new double[5];
//
//        // 产品质量
//        int[] sumArrSupplierQuality = new int[5];
//        // 产品成交的平均质量
//        int[] avgArrSupplierQuality = new int[5];
//
//        // 计算供应商一类服务的市场需求量/服务商个数/平均需求/质量/平均质量
//        calArrAvgNumberAndQuantity(listTransactionContract, sumArrSupplierOrderNumber, sumArrSupplierNumber, avgArrSupplierOrderNumber, sumArrSupplierQuality, avgArrSupplierQuality);


        // 主机厂和主机厂的任务
        Map<String, ArrayList<EngineFactoryManufacturingTask>> mapEngineFactoryIdVsListEngineFactoryManufacturingTask = new HashMap<>(50);
        for (ArrayList<EngineFactoryManufacturingTask> aListEngineFactoryManufacturingTask : listListEngineFactoryTaskDecomposition) {
            String engineFactoryId = aListEngineFactoryManufacturingTask.get(0).getEngineFactoryId();
            mapEngineFactoryIdVsListEngineFactoryManufacturingTask.put(engineFactoryId, aListEngineFactoryManufacturingTask);
        }

        // 主机厂和主机厂对应的交易契约
        // 要重新来, 先组装orderPlus和主机厂对应关系, 再把id和list对应起来
        Map<String, List<TransactionContract>> mapEngineFactoryIdVsListTransactionContract = new HashMap<>(50);
        for (TransactionContract aTransactionContract : listTransactionContract) {
            String engineFactoryId = aTransactionContract.getEngineFactoryId();
            List<TransactionContract> transactionContracts = mapEngineFactoryIdVsListTransactionContract.get(engineFactoryId);
            if (transactionContracts == null) {
                List<TransactionContract> listTmp = new ArrayList<>();
                listTmp.add(aTransactionContract);
                mapEngineFactoryIdVsListTransactionContract.put(engineFactoryId, listTmp);
            } else {
                transactionContracts.add(aTransactionContract);
            }
        }

        Map<String, List<OrderPlus>> mapEngineIdVsListOrderPlus = new HashMap<>(100);
        for (OrderPlus aOrderPlus : listOrderPlus) {

            String engineFactoryId = aOrderPlus.getEngineFactoryId();
            List<OrderPlus> listValue = mapEngineIdVsListOrderPlus.get(engineFactoryId);
            if (listValue == null) {
                List<OrderPlus> listTmp = new ArrayList<>();
                listTmp.add(aOrderPlus);
                mapEngineIdVsListOrderPlus.put(engineFactoryId, listTmp);
            } else {
                listValue.add(aOrderPlus);
            }
        }


        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {


            int minEngineFactoryNeedServiceNumber = Integer.MAX_VALUE;
            String supplierId = aSupplierDynamic.getSupplierId();

            // 计算利用率并更新
            // 某类供应总需求
            // 算出卖出所有的零件数  sumSupplierOrderNumber
            int sumSupplierOrderNumber = 0;
            if (mapSupplierIdVsTransactionContract.containsKey(supplierId)) {
                List<TransactionContract> listTransactionContractForSupplier = mapSupplierIdVsTransactionContract.get(supplierId);
                for (TransactionContract aTransactionContract : listTransactionContractForSupplier) {
                    int engineFactoryNeedServiceNumber = aTransactionContract.getEngineFactoryNeedServiceNumber();
                    sumSupplierOrderNumber += engineFactoryNeedServiceNumber;
                }
            }

            // 主机厂对该类服务的任务价总和
            int sumTypePrice = 0;
            // 有多少主机厂开价
            int sumNumber = 0;
            // 交易契约质量
            int sumEngineFactoryQuality = 0;

            // 供应商能观测到的厂
            List<TbEngineFactoryDynamic> listEngineFactoryDynamics = mapSupplierIdVsListEngineFactoryDynamic.get(supplierId);
            for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamics) {
                String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
                List<OrderPlus> listOrderPluses = mapEngineIdVsListOrderPlus.get(engineFactoryId);
                if (listOrderPluses != null) {


                    List<TransactionContract> listTransactionContracts = mapEngineFactoryIdVsListTransactionContract.get(engineFactoryId);
                    OrderPlus orderPlus = null;
                    TransactionContract transactionContract = null;
                    int supplierType = aSupplierDynamic.getSupplierType();
                    switch (supplierType) {
                        case 210:
                            orderPlus = listOrderPluses.get(0);
                            transactionContract = listTransactionContracts.get(0);
                            break;
                        case 220:
                            orderPlus = listOrderPluses.get(1);
                            transactionContract = listTransactionContracts.get(1);
                            break;
                        case 230:
                            orderPlus = listOrderPluses.get(2);
                            transactionContract = listTransactionContracts.get(2);
                            break;
                        case 240:
                            orderPlus = listOrderPluses.get(3);
                            transactionContract = listTransactionContracts.get(3);
                            break;
                        case 250:
                            orderPlus = listOrderPluses.get(4);
                            transactionContract = listTransactionContracts.get(4);
                            break;
                        default:
                            throw new RuntimeException("haha");
                    }

                    sumTypePrice += orderPlus.getSupplierActualPriceP();
                    sumNumber++;


                    // 能观测到的最小需求量
                    int engineFactoryNeedServiceNumber = mapEngineFactoryIdVsListEngineFactoryManufacturingTask.get(engineFactoryId).get(0).getEngineFactoryNeedServiceNumber();
                    minEngineFactoryNeedServiceNumber = Math.min(minEngineFactoryNeedServiceNumber, engineFactoryNeedServiceNumber);

                    // 交易契约质量
                    sumEngineFactoryQuality += transactionContract.getOrderQuality();

                }


            }
            //  所有成交价格的平均值
            double avgSupplierOrderNumber = sumTypePrice * 1.0 / sumNumber;

            double avgSupplierQuality = sumEngineFactoryQuality * 1.0 / sumNumber;
            // 主机厂任务量最小值(发布任务之前的)


            // 计算产能利用率
            double supplierCapacityUtilization = sumSupplierOrderNumber * 1D / aSupplierDynamic.getSupplierCapacityM();
            // 更新产能利用率
            aSupplierDynamic.setSupplierCapacityUtilization(supplierCapacityUtilization);

            // 供应商产能
            int supplierCapacity = aSupplierDynamic.getSupplierCapacityM();

            // 供应商原来价格下限
            int supplierPricePL = aSupplierDynamic.getSupplierPricePL();
            // 供应商原来价格上限
            Integer supplierPricePU = aSupplierDynamic.getSupplierPricePU();
            // 供应商原来平均价
            double initAvgSupplierPrice = (supplierPricePL + supplierPricePU) / 2.0;

            if (supplierCapacityUtilization >= 0.8) {
                // 产品成交的平均质量
                // 利用率为1(供不应求)
                if (initAvgSupplierPrice >= avgSupplierOrderNumber) {
                    // 初始价格的平均价 >= 所有成交价格的平均值
                    // 调整产能
                    supplierCapacity = (int) Math.round(supplierCapacity * 1.5);
                    // 更新下一阶段的产能
                    aSupplierDynamic.setSupplierCapacityM(supplierCapacity);
                } else {
                    // 初始价格的平均价 < 所有成交价格的平均值
                    // 调整价格区间并更新
                    if (supplierPricePL < initAvgSupplierPrice && initAvgSupplierPrice < supplierPricePU) {
                        aSupplierDynamic.setSupplierPricePL(RandomUtils.nextInt(supplierPricePL, (int) initAvgSupplierPrice + 1));
                        aSupplierDynamic.setSupplierPricePU(RandomUtils.nextInt((int) initAvgSupplierPrice, supplierPricePU + 1));
                    } else {
                        aSupplierDynamic.setSupplierPricePL((int) Math.round(initAvgSupplierPrice / 2.0));
                        aSupplierDynamic.setSupplierPricePU((int) Math.round(initAvgSupplierPrice * 1.5));
                    }
                }
            } else {
                // 利用率 < 1(供过于求)
                if (minEngineFactoryNeedServiceNumber <= supplierCapacity) {
                    // 主机厂最小的任务量 <= 供应商产能
                    if (initAvgSupplierPrice >= avgSupplierOrderNumber) {
                        // 初始价格的平均价 >= 所有成交价格的平均值
                        if (supplierPricePL < initAvgSupplierPrice) {
                            aSupplierDynamic.setSupplierPricePL(RandomUtils.nextInt(supplierPricePL, (int) initAvgSupplierPrice + 1));
                            aSupplierDynamic.setSupplierPricePU(RandomUtils.nextInt((int) initAvgSupplierPrice, supplierPricePU + 1));
                        } else {
                            aSupplierDynamic.setSupplierPricePL((int) Math.round(initAvgSupplierPrice / 2.0));
                            aSupplierDynamic.setSupplierPricePU((int) Math.round(initAvgSupplierPrice * 1.5));
                        }
                    } else {
                        // 初始价格的平均价 < 所有成交价格的平均值
                        int supplierQuality = aSupplierDynamic.getSupplierQualityQs();
                        if (supplierQuality >= avgSupplierQuality) {
                            // 质量 >= 平均质量
                            // 调整产能
                            supplierCapacity = (int) Math.round(supplierCapacity * 0.9);
                            // 更新下一阶段的产能
                            aSupplierDynamic.setSupplierCapacityM(supplierCapacity);
                        } else {
                            // 质量 < 平均质量
                            if (supplierQuality < 10) {
                                supplierQuality++;
                                aSupplierDynamic.setSupplierQualityQs(supplierQuality);
                                // 更新总资产
                                aSupplierDynamic.setSupplierTotalAssetsP((int) Math.round(aSupplierDynamic.getSupplierTotalAssetsP() * 0.9));
                            }
                        }
                    }
                } else {
                    // 供应商产能 * 1.1
                    supplierCapacity = (int) Math.round(supplierCapacity * 1.1);
                    // 更新下一阶段的产能
                    aSupplierDynamic.setSupplierCapacityM(supplierCapacity);
                }
            }
        }
    }

    /**
     * 计算供应商一类服务的市场需求量/服务商个数/平均需求/质量/平均质量
     *
     * @param listTransactionContract   交易契约集合
     * @param sumArrSupplierOrderNumber 该类服务主机厂需求量
     * @param sumArrSupplierNumber      该类供应商数量
     * @param avgArrSupplierOrderNumber 该类服务平均需求量
     * @param sumArrSupplierQuality     该类服务总质量
     * @param avgArrSupplierQuality     该类服务平均质量
     */
    private void calArrAvgNumberAndQuantity(ArrayList<TransactionContract> listTransactionContract, int[] sumArrSupplierOrderNumber, int[] sumArrSupplierNumber, double[] avgArrSupplierOrderNumber, int[] sumArrSupplierQuality, int[] avgArrSupplierQuality) {
        for (TransactionContract aTransactionContract : listTransactionContract) {
            int taskType = aTransactionContract.getTaskType();
            int supplierOrderNumber = aTransactionContract.getEngineFactoryNeedServiceNumber();
            int orderQuality = aTransactionContract.getOrderQuality();
            switch (taskType) {
                case 210:
                    sumArrSupplierOrderNumber[0] += supplierOrderNumber;
                    sumArrSupplierNumber[0]++;
                    sumArrSupplierQuality[0] += orderQuality;
                    break;
                case 220:
                    sumArrSupplierOrderNumber[1] += supplierOrderNumber;
                    sumArrSupplierNumber[1]++;
                    sumArrSupplierQuality[1] += orderQuality;
                    break;
                case 230:
                    sumArrSupplierOrderNumber[2] += supplierOrderNumber;
                    sumArrSupplierNumber[2]++;
                    sumArrSupplierQuality[2] += orderQuality;
                    break;
                case 240:
                    sumArrSupplierOrderNumber[3] += supplierOrderNumber;
                    sumArrSupplierNumber[3]++;
                    sumArrSupplierQuality[3] += orderQuality;
                    break;
                case 250:
                    sumArrSupplierOrderNumber[4] += supplierOrderNumber;
                    sumArrSupplierNumber[4]++;
                    sumArrSupplierQuality[4] += orderQuality;
                    break;
                default:
                    throw new RuntimeException("no such type");
            }
        }
        // 算下平均值
        for (int i = 0; i < avgArrSupplierOrderNumber.length; i++) {
            // 供应商一类服务数量平均值
            avgArrSupplierOrderNumber[i] = sumArrSupplierOrderNumber[i] * 1D / sumArrSupplierNumber[i];
            // 供应商一类服务质量平均值
            avgArrSupplierQuality[i] = (int) Math.round(sumArrSupplierQuality[i] * 1D / sumArrSupplierNumber[i]);
        }
    }

    /**
     * 对主机厂计算并设置产能利用率,
     * 调整产能/价格/质量
     *
     * @param listEngineFactoryDynamic                     主机厂动态数据集合
     * @param mapEngineIdVsEngineFactoryFinalProvision     主机厂与市场实际交易集合
     * @param mapEngineFactoryIdVsListEngineFactoryDynamic 可见
     * @param mapEngineFactoryIdVsFinalProvision           主机id对应的成交价格
     */
    private void calAndSetEngineFactoryCapacityUtilizationAndAdjustCapacityPriceQuality(
            List<TbEngineFactoryDynamic> listEngineFactoryDynamic
            , HashMap<String, EngineFactoryFinalProvision> mapEngineIdVsEngineFactoryFinalProvision,
            Map<String, List<TbEngineFactoryDynamic>> mapEngineFactoryIdVsListEngineFactoryDynamic, Map<String, EngineFactoryFinalProvision> mapEngineFactoryIdVsFinalProvision) {

        for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamic) {
            String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
            EngineFactoryFinalProvision engineFactoryFinalProvision = mapEngineIdVsEngineFactoryFinalProvision.get(engineFactoryId);
            // 实际销量
            int actualSaleNumber = 0;
            if (engineFactoryFinalProvision != null) {
                actualSaleNumber = engineFactoryFinalProvision.getActualSaleNumber();
            }
            // 主机产能
            int engineFactoryCapacity = aEngineFactoryDynamic.getEngineFactoryCapacityM();
            // 计算利用率并更新
            double engineFactoryCapacityUtilization = actualSaleNumber * 1D / engineFactoryCapacity;
            aEngineFactoryDynamic.setEngineFactoryCapacityUtilization(engineFactoryCapacityUtilization);

            // 主机厂原来价格下限
            int engineFactoryPricePL = aEngineFactoryDynamic.getEngineFactoryPricePL();
            // 主机厂原来价格上限
            Integer engineFactoryPricePU = aEngineFactoryDynamic.getEngineFactoryPricePU();
            // 主机厂原来平均价
            double initAvgEngineFactoryToMarketPrice = (engineFactoryPricePL + engineFactoryPricePU) / 2.0;


            // 改厂可见主机厂
            List<TbEngineFactoryDynamic> listVisibleEngineFactoryDynamics = mapEngineFactoryIdVsListEngineFactoryDynamic.get(engineFactoryId);
            // 用来存成交价格
            int sumFinalMarketPrice = 0;
            // 用来记录产品的成交质量
            int sumFinalMarketQuality = 0;
            // 产品成交价的平均值
            int visibleActualSaleNumber = 0;
            for (TbEngineFactoryDynamic aVisibleEngineFactoryDynamic : listVisibleEngineFactoryDynamics) {
                String tmpId = aVisibleEngineFactoryDynamic.getEngineFactoryId();
                EngineFactoryFinalProvision tmpFinalProvision = mapEngineFactoryIdVsFinalProvision.get(tmpId);
                if (tmpFinalProvision != null) {
                    // 最终成交价格
                    sumFinalMarketPrice += tmpFinalProvision.getFinalMarketPrice();
                    // 成交质量
                    sumFinalMarketQuality += tmpFinalProvision.getFinalMarketQuality();
                    visibleActualSaleNumber++;
                }

            }

            // 该厂可见平均价格
//            double avgFinalMarketPrice = sumFinalMarketPrice * 1D / listVisibleEngineFactoryDynamics.size();
            double avgFinalMarketPrice = sumFinalMarketPrice * 1D / visibleActualSaleNumber;
            // 该厂可见平均质量
//            double avgFinalMarketQuality = sumFinalMarketQuality * 1D / listVisibleEngineFactoryDynamics.size();
            double avgFinalMarketQuality = sumFinalMarketQuality * 1D / visibleActualSaleNumber;


            // 主机厂修改服务质量-售价-产能
            if (engineFactoryCapacityUtilization >= 0.8) {
                // 利用率为1(供不应求)
                if (initAvgEngineFactoryToMarketPrice >= avgFinalMarketPrice) {
                    // 初始价格的平均价 >= 所有成交价格的平均值
                    // 调整产能
                    engineFactoryCapacity = (int) Math.round(engineFactoryCapacity * 1.3);
                    // 更新下一阶段的产能
                    aEngineFactoryDynamic.setEngineFactoryCapacityM(engineFactoryCapacity);
                } else {
                    // 初始价格的平均价 < 所有成交价格的平均值
                    // 调整价格区间并更新
                    if (avgFinalMarketPrice != 0) {
                        if (engineFactoryPricePL < avgFinalMarketPrice && avgFinalMarketPrice < engineFactoryPricePU) {
                            aEngineFactoryDynamic.setEngineFactoryPricePL(RandomUtils.nextInt(engineFactoryPricePL, (int) avgFinalMarketPrice + 1));
                            aEngineFactoryDynamic.setEngineFactoryPricePU(RandomUtils.nextInt((int) avgFinalMarketPrice, engineFactoryPricePU + 1));
                        } else {
                            aEngineFactoryDynamic.setEngineFactoryPricePL((int) (avgFinalMarketPrice / 2));
                            aEngineFactoryDynamic.setEngineFactoryPricePU((int) (avgFinalMarketPrice * 1.5));
                        }
                    }
                }
            } else {
                // 利用率 < 1(供过于求)
                if (initAvgEngineFactoryToMarketPrice >= avgFinalMarketPrice) {
                    // 初始价格的平均价 >= 所有成交价格的平均值
                    if (avgFinalMarketPrice != 0) {
                        if (engineFactoryPricePL < avgFinalMarketPrice) {
                            aEngineFactoryDynamic.setEngineFactoryPricePL(RandomUtils.nextInt(engineFactoryPricePL, (int) avgFinalMarketPrice + 1));
                            aEngineFactoryDynamic.setEngineFactoryPricePU(RandomUtils.nextInt((int) avgFinalMarketPrice, engineFactoryPricePU + 1));
                        } else {
                            aEngineFactoryDynamic.setEngineFactoryPricePL((int) Math.round(avgFinalMarketPrice / 2.0));
                            aEngineFactoryDynamic.setEngineFactoryPricePU((int) Math.round(avgFinalMarketPrice * 1.5));
                        }
                    }
                } else {
                    // 初始价格的平均价 < 所有成交价格的平均值
                    int engineFactoryQualityQ = aEngineFactoryDynamic.getEngineFactoryQualityQ();
                    if (engineFactoryQualityQ >= avgFinalMarketQuality) {
                        // 质量 >= 平均质量
                        // 调整产能
                        engineFactoryCapacity = (int) Math.round(engineFactoryCapacity * 0.9);
                        // 更新下一阶段的产能
                        aEngineFactoryDynamic.setEngineFactoryCapacityM(engineFactoryCapacity);
                    } else {
                        // 质量 < 平均质量
                        if (engineFactoryQualityQ < 10) {
                            engineFactoryQualityQ++;
                            aEngineFactoryDynamic.setEngineFactoryQualityQ(engineFactoryQualityQ);
                            // 更新总资产
                            aEngineFactoryDynamic.setEngineFactoryTotalAssetsP((int) Math.round(aEngineFactoryDynamic.getEngineFactoryTotalAssetsP() * 0.9));
                        }
                    }
                }
            }
        }
    }

    /**
     * 计算供应商总资产并更新
     *
     * @param listSupplierDynamics 供应商的动态数据集合(实际被更新的)
     * @param mapSupplierProfitSum 供应商与主机厂交易后的利润
     */
    private void calAndSetSupplierTotalAssets(List<TbSupplierDynamic> listSupplierDynamics, Map<String, Integer> mapSupplierProfitSum) {
        for (TbSupplierDynamic aSupplierDynamic : listSupplierDynamics) {
            String supplierId = aSupplierDynamic.getSupplierId();
            // 开始总资产
            int initSupplierTotalAssets = aSupplierDynamic.getSupplierTotalAssetsP();
            // 利润和
            int supplierProfitSum = 0;
            if (mapSupplierProfitSum.containsKey(supplierId)) {
                supplierProfitSum = mapSupplierProfitSum.get(supplierId);
            }
            // 固定成本
            int supplierFixedCost = SupplierEnum.supplierFixedCost;
            int supplierTotalAsserts = initSupplierTotalAssets + supplierProfitSum - supplierFixedCost;
            aSupplierDynamic.setSupplierTotalAssetsP(supplierTotalAsserts);
        }
    }

    /**
     * 计算所有主机厂的总资产
     *
     * @param listEngineFactoryDynamic                 所有主机厂的动态数据集合(实际被更新的)
     * @param mapEngineFactoryProfitSum                主机厂与供应商交易后的利润集合
     * @param mapEngineIdVsEngineFactoryFinalProvision 主机厂为市场交易提供的产品实际价格和卖出结果集合
     */
    private void calAndSetEngineFactoryTotalAssets(
            List<TbEngineFactoryDynamic> listEngineFactoryDynamic
            , Map<String, Integer> mapEngineFactoryProfitSum
            , HashMap<String, EngineFactoryFinalProvision> mapEngineIdVsEngineFactoryFinalProvision) {

        for (TbEngineFactoryDynamic aEngineFactoryDynamic : listEngineFactoryDynamic) {
            String engineFactoryId = aEngineFactoryDynamic.getEngineFactoryId();
            // 开始资产
            int initEngineFactoryTotalAssets = aEngineFactoryDynamic.getEngineFactoryTotalAssetsP();
            // 销售收入 = 售价 * 实际数量
            int totalSalesMoney = 0;
            // 主机厂与供应商交易的利润和
            int engineFactoryProfit = 0;

//            OrderPlus orderPlus = mapEngineIdVsOrderPlus.get(engineFactoryId+"210");
            EngineFactoryFinalProvision engineFactoryFinalProvision = mapEngineIdVsEngineFactoryFinalProvision.get(engineFactoryId);
            if (engineFactoryFinalProvision != null) {
                // 有生产产品
                int finalMarketPrice = engineFactoryFinalProvision.getFinalMarketPrice();
                int actualSaleNumber = engineFactoryFinalProvision.getActualSaleNumber();
                totalSalesMoney = finalMarketPrice * actualSaleNumber;
                // 与供应商的交易利润
                engineFactoryProfit = mapEngineFactoryProfitSum.get(engineFactoryId);
            }
            // 固定成本
            int engineFactoryFixedCost = EngineFactoryEnum.engineFactoryFixedCost;
            int engineFactoryTotalAsserts = initEngineFactoryTotalAssets + totalSalesMoney + engineFactoryProfit - engineFactoryFixedCost;

            // 更新模型中的总资产
            aEngineFactoryDynamic.setEngineFactoryTotalAssetsP(engineFactoryTotalAsserts);
        }
    }

    /**
     * 算出各主机厂与供应商之间的交易利润和,
     * 并加入到各自对应的map集合中
     * 并把主机厂和供应商的信誉度补全
     *
     * @param listOrderPlus             交易结算集合
     * @param mapEngineFactoryProfitSum 主机厂利润集合(主机厂id, 和供应商交易好后的利润), 算好后put该处
     * @param mapSupplierProfitSum      供应商利润集合(供应商id, 和主机厂交易好后的利润), 算好后put该处
     * @param mapEngineFactoryDynamic   主机厂的动态数据map
     * @param mapSupplierDynamic        供应商的动态数据map
     */
    private void setMapEngineFactoryAndSupplierProfitSum(
            List<OrderPlus> listOrderPlus
            , Map<String, Integer> mapEngineFactoryProfitSum
            , Map<String, Integer> mapSupplierProfitSum
            , Map<String, TbEngineFactoryDynamic> mapEngineFactoryDynamic
            , Map<String, TbSupplierDynamic> mapSupplierDynamic) {

        // 暂存主机厂利润
        int tmpEngineFactoryProfit = 0;
        // 暂存供应商利润
        int tmpSupplierProfit = 0;
        for (OrderPlus orderPlus : listOrderPlus) {
            String engineFactoryId = orderPlus.getEngineFactoryId();
            TbEngineFactoryDynamic engineFactoryDynamic = mapEngineFactoryDynamic.get(engineFactoryId);
            String supplierId = orderPlus.getSupplierId();
            TbSupplierDynamic supplierDynamic = mapSupplierDynamic.get(supplierId);


//            mapEngineIdVsOrderPlus.put(engineFactoryId, orderPlus);
            // 主机厂与供应商交易后的利润
            if (mapEngineFactoryProfitSum.containsKey(engineFactoryId)) {
                tmpEngineFactoryProfit = mapEngineFactoryProfitSum.get(engineFactoryId);
            } else {
                tmpEngineFactoryProfit = 0;
            }
            tmpEngineFactoryProfit += orderPlus.getEngineFactoryProfit();
            mapEngineFactoryProfitSum.put(engineFactoryId, tmpEngineFactoryProfit);
            // 供应商与主机厂交易后的利润
            if (mapSupplierProfitSum.containsKey(supplierId)) {
                tmpSupplierProfit = mapSupplierProfitSum.get(supplierId);
            } else {
                tmpSupplierProfit = 0;
            }
            tmpSupplierProfit += orderPlus.getSupplierProfit();
            mapSupplierProfitSum.put(supplierId, tmpSupplierProfit);
        }
    }

    /**
     * 生成新的坐标
     *
     * @param arrPosition 信誉度最高的主机厂或供应商的位置坐标
     * @param mapPosition 主机厂或供应商的位置map
     * @return 生成的新坐标
     */
    private double[] genNewPosition(double[] arrPosition, Map<Double, Double> mapPosition) {
        double x = arrPosition[0];
        double y = arrPosition[1];
        double xLow = x - 2.5 > 0 ? x - 2.5 : 0;
        double xHigh = x + 2.5 < 20 ? x + 2.5 : 20;
        double yLow = y - 2.5 > 0 ? y - 2.5 : 0;
        double yHigh = y + 2.5 < 20 ? y + 2.5 : 20;
        while (true) {
            double newX = RandomUtils.nextDouble(xLow, xHigh);
            double newY = RandomUtils.nextDouble(yLow, yHigh);
            // 强行一位小数
            x = (int) (newX * 10) / 10d;
            y = (int) (newY * 10) / 10d;
            Double valueY = mapPosition.get(x);
            if (valueY == null) {
                // 没有x的key, 就是ok的
                mapPosition.put(x, y);
                return new double[]{x, y};
            } else {
                // 有相同的x, 看看y一不一样
                if (y != valueY) {
                    mapPosition.put(x, y);
                    return new double[]{x, y};
                }
            }

        }
    }

//--------------------------生成最终的交货结果-----------------------------

    /**
     * 生成最终的交货结果
     *
     * @param cycleTimes    实验次数
     * @param listOrderPlus 产品订单
     * @return 最终交货结果集合
     */
    @Override
    public List<EngineFactoryFinalProvision> getListEngineFactoryFinalProvision(int experimentsNumber, int cycleTimes, List<OrderPlus> listOrderPlus) {

        List<EngineFactoryFinalProvision> listEngineFactoryFinalProvision = new ArrayList<>();

        EngineFactoryFinalProvision engineFactoryFinalProvision = null;

        // tmp实际成交数量
        int actualProductNumber = 0;
        // tmp实际价格
        int actualPrice = 0;
        int tmpPrice = 0;
        // tmp实际质量
        int actualQuality = 0;

        // 市场初始需求
//        int initMarketNeedNumber = calInitMarketNeedNumber(cycleTimes);
        int initMarketNeedNumber = demandForecastMapper.selectByPrimaryKey(cycleTimes).getTrueDemandForecast();
        int restMarketNeedNumber = initMarketNeedNumber;
        for (int i = 0; i < listOrderPlus.size(); i++) {
            OrderPlus orderPlus = listOrderPlus.get(i);
            int tmp = i % 5;
            if (tmp == 0) {
                // 实例化
                engineFactoryFinalProvision = new EngineFactoryFinalProvision();
                // 实验次数
                engineFactoryFinalProvision.setExperimentsNumber(experimentsNumber);
                // 循环次数
                engineFactoryFinalProvision.setCycleTimes(cycleTimes);
                // 主机厂Id
                engineFactoryFinalProvision.setEngineFactoryId(orderPlus.getEngineFactoryId());
                // 实际数量
                actualProductNumber = orderPlus.getSupplierActualNumberM();
                // 最终质量
                actualQuality = 0;
                // 最终面向市场价格
                actualPrice = RandomUtils.nextInt(orderPlus.getEngineFactoryToServiceOfferPriceLow(), orderPlus.getEngineFactoryToServiceOfferPriceUpper() + 1);
//                int[] engineFactory2ServiceOfferPrice = orderPlus.getEngineFactoryToServiceOfferPrice();
//                actualPrice = RandomUtils.nextInt(engineFactory2ServiceOfferPrice[0], engineFactory2ServiceOfferPrice[1] + 1);
                tmpPrice = orderPlus.getSupplierActualPriceP();

            }

            // 最终产品数量
            actualProductNumber = Math.min(orderPlus.getSupplierActualNumberM(), actualProductNumber);
            // 最终面向市场价格
            tmpPrice += orderPlus.getSupplierActualPriceP();
            // 最终质量
            actualQuality += orderPlus.getSupplierActualQualityQs();

            if (tmp == 4) {
                // 最终产品数量
                engineFactoryFinalProvision.setFinalProductNumber(actualProductNumber);
                // 最终面向市场价格
                tmpPrice = (int) (tmpPrice * CalculationEnum.saleProductsCp);
                actualPrice = Math.max(actualPrice, tmpPrice);
                engineFactoryFinalProvision.setFinalMarketPrice(actualPrice);

                // 最终质量
                actualQuality = actualQuality / 5;
                engineFactoryFinalProvision.setFinalMarketQuality(actualQuality);
                // 市场需求量
                engineFactoryFinalProvision.setMarketNeedNumber(initMarketNeedNumber);

                // 最终卖出量(实际销售额)
                int actualSaleNumber;
                if (restMarketNeedNumber > actualProductNumber) {
                    actualSaleNumber = Math.min(actualProductNumber, restMarketNeedNumber);
                    restMarketNeedNumber -= actualSaleNumber;
                } else {
                    actualSaleNumber = 0;
                }

                engineFactoryFinalProvision.setActualSaleNumber(actualSaleNumber);

                // 加入返回值数组中
                listEngineFactoryFinalProvision.add(engineFactoryFinalProvision);
            }
        }

        if (listEngineFactoryFinalProvision.size()== 0) {
            throw new RuntimeException("listEngineFactoryFinalProvision is null ");
        }
        engineFactoryFinalProvisionMapper.insertList(listEngineFactoryFinalProvision);
        return listEngineFactoryFinalProvision;
    }

    /**
     * 计算市场需求
     *
     * @param cycleTimes 循环次数
     * @return 市场需求量
     */
    public int calInitMarketNeedNumber(int cycleTimes) {
        int k1 = EngineFactoryEnum.engineFactoryDemandForecastInitK1;
        double k2 = EngineFactoryEnum.engineFactoryDemandForecastInitK2;
        int k1Step = EngineFactoryEnum.engineFactoryDemandForecastK1Step;
        double k2Step = EngineFactoryEnum.engineFactoryDemandForecastK2Step;
        double cm = CalculationEnum.saleProductsInitCm;
        k1 = k1 + (cycleTimes - 1) * k1Step;
        k2 = k2 + (cycleTimes - 1) * k2Step;
        cm = cm + (cycleTimes - 1) * 0.2;

        int pa = RandomUtils.nextInt(EngineFactoryEnum.engineFactoryInitPriceLow, EngineFactoryEnum.engineFactoryInitPriceUpper + 1);
        int qa = RandomUtils.nextInt(EngineFactoryEnum.engineFactoryInitQualityLow, EngineFactoryEnum.engineFactoryInitQualityUpper + 1);
        int res = (int) Math.round((k1 - k2 * pa / qa) * cm);
        return res > 0 ? res : 0;
    }
}
