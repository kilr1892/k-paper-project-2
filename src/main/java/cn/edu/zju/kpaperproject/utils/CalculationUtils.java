package cn.edu.zju.kpaperproject.utils;


import cn.edu.zju.kpaperproject.dto.EngineFactoryManufacturingTask;
import cn.edu.zju.kpaperproject.dto.SupplierTask;
import cn.edu.zju.kpaperproject.enums.CalculationEnum;
import cn.edu.zju.kpaperproject.enums.EngineFactoryEnum;
import cn.edu.zju.kpaperproject.enums.NumberEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 与计算相关.
 *
 * @author RichardLee
 * @version v1.0
 */
@Component
@Slf4j
public class CalculationUtils {

    /**
     * 需求预测(在每次循环结束后, 预测下一阶段)
     * 第1次循环, 用的是第0次的数据
     *
     * @param cycleTimes 循环次数
     * @param priceLow   价格下限
     * @param priceUpper 价格上限
     * @param quality    质量
     * @return 预测值
     */
    public static int demandForecast(int cycleTimes, int priceLow, int priceUpper, int quality) {
        int k1 = EngineFactoryEnum.engineFactoryDemandForecastInitK1;
        double k2 = EngineFactoryEnum.engineFactoryDemandForecastInitK2;
        int k1Step = EngineFactoryEnum.engineFactoryDemandForecastK1Step;
        double k2Step = EngineFactoryEnum.engineFactoryDemandForecastK2Step;

        k1 = k1 + cycleTimes * k1Step;
        k2 = k2 + cycleTimes * k2Step;

        int price;
        int demandForecast = 0;

        if (k1 - k2 * priceUpper / quality > 0) {
            while (demandForecast <= 0) {
                price = RandomUtils.nextInt(priceLow, priceUpper + 1);
                demandForecast = (int) Math.round(k1 - k2 * price / quality);
            }
        }
        return demandForecast;
    }

    /**
     * 判断价格是否有交集
     *
     * @param aEngineFactoryManufacturingTask 主机厂价格区间
     * @param aSupplierTask                   供应商价格区间
     * @return 是否有交集
     */
    public static boolean whetherPriceIntersection(EngineFactoryManufacturingTask aEngineFactoryManufacturingTask, SupplierTask aSupplierTask) {
        return whetherPriceIntersection(aEngineFactoryManufacturingTask.getEngineFactory2ServiceOfferPrice(), aSupplierTask.getSupplierPriceRange());
    }

    /**
     * 判断价格是否有交集
     *
     * @param engineFactoryPriceRange 主机厂价格区间
     * @param supplierPriceRange      供应商价格区间
     * @return 是否有交集
     */
    public static boolean whetherPriceIntersection(int[] engineFactoryPriceRange, int[] supplierPriceRange) {
        int enPriLow = engineFactoryPriceRange[0];
        int enPriUpper = engineFactoryPriceRange[1];
        int supPriLow = supplierPriceRange[0];
        int supPriUpper = supplierPriceRange[1];

        // 主机厂最小值>供应商最大值 或 供应商最小值>主机厂最大值
        return enPriLow <= supPriUpper && supPriLow <= enPriUpper;
    }

    /**
     * 计算精匹配中双方匹配度
     *
     * @param engineFactoryManufacturingTask 主机厂的任务
     * @param supplierTask                   供应商提供的服务
     * @param mapRelationshipMatrix          双方关系强度矩阵
     * @return 匹配度
     */
    public static double calMatchingDegree(EngineFactoryManufacturingTask engineFactoryManufacturingTask, SupplierTask supplierTask, Map<String, Double> mapRelationshipMatrix) {

        double a3slash = CalculationEnum.weightFactorA3slash;
        double b3slash = CalculationEnum.weightFactorB3slash;
        double x3slash = CalculationEnum.weightFactorX3slash;
        double y3slash = CalculationEnum.weightFactorY3slash;

        // 两地距离
        double distance = calDistance(engineFactoryManufacturingTask, supplierTask);
        double tmp1 = a3slash / (1 + distance);

        // 交集长度(分子)
        int tmp2Molecular = calTmp2Molecular(engineFactoryManufacturingTask, supplierTask);
        // 价格长度
        int absEngineFactoryPrice = calAbsEnginePrice(engineFactoryManufacturingTask);
        int absEnginePrice = calAbsEnginePrice(supplierTask);
        double tmp2 = b3slash * (tmp2Molecular) / (absEngineFactoryPrice + absEnginePrice);

        // 供应商质量 - 主机厂质量
        double tmp3 = x3slash * (supplierTask.getSupplierQuality() - engineFactoryManufacturingTask.getEngineFactoryExpectedQuality()) / 10;

        // 关系强度
        double tmp4 = y3slash * getRelationshipStrength(mapRelationshipMatrix, engineFactoryManufacturingTask, supplierTask);

        return tmp1 + tmp2 + tmp3 + tmp4;

    }

    /**
     * 获取双方关系强度
     *
     * @param mapRelationshipMatrix          关系强度Map
     * @param engineFactoryManufacturingTask 主机厂任务
     * @param supplierTask                   供应商任务
     * @return 关系强度值
     */
    private static double getRelationshipStrength(Map<String, Double> mapRelationshipMatrix, EngineFactoryManufacturingTask engineFactoryManufacturingTask, SupplierTask supplierTask) {
        String key = engineFactoryManufacturingTask.getEngineFactoryId() + supplierTask.getSupplierId();
//        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//        log.info(key +"::::"+mapRelationshipMatrix.get(key));
//        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        return mapRelationshipMatrix.get(key);
    }


    /**
     * 计算关系强度第二个加法项的分子值
     *
     * @param aEngineFactoryManufacturingTask 主机厂任务分解
     * @param supplierTask                    供应商服务
     * @return 第二个加法项的分子值
     */
    private static int calTmp2Molecular(EngineFactoryManufacturingTask aEngineFactoryManufacturingTask, SupplierTask supplierTask) {
        int[] engineFactoryPriceRange = aEngineFactoryManufacturingTask.getEngineFactory2ServiceOfferPrice();
        int[] supplierPriceRange = supplierTask.getSupplierPriceRange();

        int enPriLow = engineFactoryPriceRange[0];
        int enPriUpper = engineFactoryPriceRange[1];
        int supPriLow = supplierPriceRange[0];
        int supPriUpper = supplierPriceRange[1];

        boolean hasPriceIntersection = whetherPriceIntersection(engineFactoryPriceRange, supplierPriceRange);
        if (hasPriceIntersection) {
            int intersectionLow = Math.max(enPriLow, supPriLow);
            int intersectionUpper = Math.min(enPriUpper, supPriUpper);
            return (intersectionUpper - intersectionLow) * 2;
        } else {
//            return enPriUpper - supPriLow;
            return supPriUpper - enPriLow;
        }
    }

    /**
     * 计算价格的长度
     *
     * @param object 主机厂任务或供应商服务
     * @return 价格长度
     */
    private static int calAbsEnginePrice(Object object) {
        int[] priceRange = new int[2];
        if (object instanceof EngineFactoryManufacturingTask) {
            EngineFactoryManufacturingTask o = (EngineFactoryManufacturingTask) object;
            priceRange = o.getEngineFactory2ServiceOfferPrice();
            return priceRange[1] - priceRange[0];
        } else if (object instanceof SupplierTask) {
            SupplierTask o = (SupplierTask) object;
            priceRange = o.getSupplierPriceRange();
        }
        return priceRange[1] - priceRange[0];
    }

    /**
     * 计算两地距离
     *
     * @param aEngineFactoryManufacturingTask 主机厂任务
     * @param supplierTask                    供应商服务
     * @return 两地间的距离
     */
    private static double calDistance(EngineFactoryManufacturingTask aEngineFactoryManufacturingTask, SupplierTask supplierTask) {

        double[] engineFactoryLocationXY = aEngineFactoryManufacturingTask.getEngineFactoryLocationXY();
        double[] supplierLocationXY = supplierTask.getSupplierLocationXY();
        int engineX = (int) (engineFactoryLocationXY[0]*10);
        int engineY = (int) (engineFactoryLocationXY[1]*10);
        int supplierX = (int) (supplierLocationXY[0]*10);
        int supplierY = (int) (supplierLocationXY[1]*10);
        double powX = Math.pow((engineX - supplierX), 2) / 100;
        double powY = Math.pow((engineY - supplierY), 2) / 100;
        return Math.sqrt(powX + powY);
    }

    /**
     * 计算两地距离
     * 直接使用两地坐标
     *
     * @param engineFactoryLocationXY 主机厂位置坐标
     * @param supplierLocationXY      供应商位置坐标
     * @return 两地间的距离
     */
    public static double calDistance(double[] engineFactoryLocationXY, double[] supplierLocationXY) {

        int engineX = (int) (engineFactoryLocationXY[0]*10);
        int engineY = (int) (engineFactoryLocationXY[1]*10);
        int supplierX = (int) (supplierLocationXY[0]*10);
        int supplierY = (int) (supplierLocationXY[1]*10);
        double powX = Math.pow((engineX - supplierX), 2) / 100;
        double powY = Math.pow((engineY - supplierY), 2) / 100;
        return Math.sqrt(powX + powY);
    }


    /**
     * 获取订单价格
     *
     * @param engineFactoryManufacturingTask 主机厂任务
     * @param supplierTask                   匹配上的唯一服务
     * @param flag                           是否有交集
     * @return 订单价格
     */
    public static int genTransactionContractOrderPrice(EngineFactoryManufacturingTask engineFactoryManufacturingTask, SupplierTask supplierTask, boolean flag) {
        int[] engineFactory2ServiceOfferPrice = engineFactoryManufacturingTask.getEngineFactory2ServiceOfferPrice();
        int engineFactory2ServiceOfferPriceLow = engineFactory2ServiceOfferPrice[NumberEnum.PRICE_LOW_ARRAY_INDEX];
        int engineFactory2ServiceOfferPriceUpper = engineFactory2ServiceOfferPrice[NumberEnum.PRICE_UPPER_ARRAY_INDEX];

        int[] supplierPriceRange = supplierTask.getSupplierPriceRange();
        int supplierPriceLow = supplierPriceRange[NumberEnum.PRICE_LOW_ARRAY_INDEX];
        int supplierPriceUpper = supplierPriceRange[NumberEnum.PRICE_UPPER_ARRAY_INDEX];

        int minPrice;
        int maxPrice;
        if (flag) {
            // 有交集
            // 双方low的高价
            minPrice = Math.max(engineFactory2ServiceOfferPriceLow, supplierPriceLow);
            // 双方upper的低价
            maxPrice = Math.min(engineFactory2ServiceOfferPriceUpper, supplierPriceUpper);
        } else {
            // 无交集
            // low的最低价
            minPrice = Math.min(engineFactory2ServiceOfferPriceLow, supplierPriceLow);
            // upper的最高价
            maxPrice = Math.max(engineFactory2ServiceOfferPriceUpper, supplierPriceUpper);
        }
        return RandomUtils.nextInt(minPrice, maxPrice + 1);
    }
}
