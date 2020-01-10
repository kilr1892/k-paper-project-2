package cn.edu.zju.kpaperproject.controller;

import cn.edu.zju.kpaperproject.dto.*;
import cn.edu.zju.kpaperproject.mapper.TbRelationMatrixMapper;
import cn.edu.zju.kpaperproject.pojo.*;
import cn.edu.zju.kpaperproject.service.BeforeNextTask;
import cn.edu.zju.kpaperproject.service.InitService;
import cn.edu.zju.kpaperproject.service.ProcessTaskService;
import cn.edu.zju.kpaperproject.service.StartTaskService;
import cn.edu.zju.kpaperproject.vo.GraphVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * RestfulApi
 *
 * @author RichardLee
 * @version v1.0
 */
@RestController
@Slf4j
public class TestController {
    @Autowired
    InitService initService;
    @Autowired
    StartTaskService startTaskService;
    @Autowired
    ProcessTaskService processTaskService;
    @Autowired
    BeforeNextTask beforeNextTask;

    @Autowired
    private TbRelationMatrixMapper tbRelationMatrixMapper;

    @GetMapping("/api/graph/{cycleTimes}")
    public Map<String, ArrayList> getMapper(@PathVariable("cycleTimes") int cycleTimes) {
        Map<String, ArrayList> mapResult = new HashMap<>(2);

        List<GraphVo> list = tbRelationMatrixMapper.selectPositionByCycleTime(cycleTimes);
        // key : node
        ArrayList<GraphNode> listGraphNodes = new ArrayList<>(list.size());
        // key : link
        ArrayList<GraphLink> listGraphLinks = new ArrayList<>(list.size());
        // # 存已经有的工厂id, 和大小
        Map<String, GraphNode> mapEngineFactory = new HashMap<>(100);
        // # 存已经有的供应商id
        Map<String, GraphNode> mapSupplier = new HashMap<>(100);

        String engineFactoryId;
        double relationScore;
        String supplierId;
        TbEngineFactory tbEngineFactory;
        TbSupplier tbSupplier;
        GraphNode graphNode;
        GraphLink graphLink;

        for (GraphVo aGraphVo : list) {
            tbEngineFactory = aGraphVo.getTbEngineFactory();
            engineFactoryId = aGraphVo.getEngineFactoryId();
            relationScore = aGraphVo.getRelationScore();

            tbSupplier = aGraphVo.getTbSupplier();
            supplierId = aGraphVo.getSupplierId();

            // #构造map
            // _主机厂存入map里
            graphNode = mapEngineFactory.get(engineFactoryId);
            if (graphNode == null) {
                graphNode = new GraphNode();
                graphNode.setId(engineFactoryId);
                graphNode.setName(engineFactoryId.substring(0, 3));
//                graphNode.setName(engineFactoryId.substring(0, 4) + "\n" + tbEngineFactory.getEngineFactoryLocationGX() + " , " + tbEngineFactory.getEngineFactoryLocationGY());
                graphNode.setX(tbEngineFactory.getEngineFactoryLocationGX());
                graphNode.setY(tbEngineFactory.getEngineFactoryLocationGY());
//                graphNode.setSymbolSize(relationScore);
                graphNode.setColor("#E54064");
                graphNode.setTotalAsset(aGraphVo.getTbEngineFactoryDynamic().getEngineFactoryTotalAssetsP());
                mapEngineFactory.put(engineFactoryId, graphNode);
            }
            // _供应商存入map里
            graphNode = mapSupplier.get(supplierId);
            if (graphNode == null) {
                graphNode = new GraphNode();
                graphNode.setId(supplierId);
                graphNode.setName(supplierId.substring(0, 3));
//                graphNode.setName(supplierId.substring(0, 4)+ "\n" + tbSupplier.getSupplierLocationGX() + " , " + tbSupplier.getSupplierLocationGY());
                graphNode.setX(tbSupplier.getSupplierLocationGX());
                graphNode.setY(tbSupplier.getSupplierLocationGY());
//                graphNode.setSymbolSize(8);
                graphNode.setColor("#3798FF");
                graphNode.setTotalAsset(aGraphVo.getTbSupplierDynamic().getSupplierTotalAssetsP());
                mapSupplier.put(supplierId, graphNode);
            }

            // # links
            // _关系强度大于某值的连线
            if (relationScore > 0.19) {
                // 连线了
                graphLink = new GraphLink();
                graphLink.setSourceId(engineFactoryId);
                graphLink.setTargetId(supplierId);
                // 线的粗细
                if (relationScore < 0.3) {
                    graphLink.setLineWidth(1.8);
                } else if (relationScore < 0.6) {
                    graphLink.setLineWidth(7.5);
                } else {
                    graphLink.setLineWidth(10.5);
                }
                listGraphLinks.add(graphLink);
            }
        }

        // #画4个坐标点
        int[] ints = {0, 10, 20};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                graphNode = new GraphNode();
                graphNode.setX(ints[i]);
                graphNode.setSymbolSize(20);
                graphNode.setColor("#F7C124");
                graphNode.setY(ints[j]);
                listGraphNodes.add(graphNode);
            }
        }



        // #主机厂与供应商node存入集合
        // _主机厂
        // __主机厂根据值从小到大
        Queue<GraphNode> queue = new PriorityQueue<>(((o1, o2) -> o1.getTotalAsset() - o2.getTotalAsset() >= 0 ? 1 : -1));
        for (Map.Entry<String, GraphNode> entry : mapEngineFactory.entrySet()) {
            queue.add(entry.getValue());
        }
        // __主机厂加入list中
        int i = 0;
        while (queue.peek() != null) {
            graphNode = queue.poll();
            graphNode.setSymbolSize(i * 2 + 14);
            listGraphNodes.add(graphNode);
            i++;
        }
        // _供应商
        for (Map.Entry<String, GraphNode> entry : mapSupplier.entrySet()) {
            queue.add(entry.getValue());
        }
        i = 0;
        int firstLevelLimit = (int) (queue.size() * 0.3);
        int secocdLevelLimit = (int) (queue.size() * 0.6);
        while (queue.peek() != null) {
            graphNode = queue.poll();
            if (i < firstLevelLimit) {
                graphNode.setSymbolSize(8);
            } else if (i < secocdLevelLimit) {
                graphNode.setSymbolSize(15);
            } else {
                graphNode.setSymbolSize(25);
            }
            listGraphNodes.add(graphNode);
            i++;
        }

        // #node和link加入map
        mapResult.put("nodes", listGraphNodes);
        mapResult.put("links", listGraphLinks);
        return mapResult;
    }

    @GetMapping("/haha/{experimentsNumber}/{cycleTime}")
    public String test(@PathVariable int experimentsNumber, @PathVariable int cycleTime) {

        log.info("#########################cycleTime  = " + cycleTime + " #########################");
        log.info("  ");
        log.info("  ");
        // 1 获取所有存活的主机厂
        List<TbEngineFactory> listEngineFactory = startTaskService.getListEngineFactoryWithAlive(experimentsNumber, cycleTime);

        List<TbEngineFactoryDynamic> listEngineFactoryDynamic = new ArrayList<>();

        // 2 生成所有活的供应商
        List<TbSupplier> listSuppliers = startTaskService.getListTbSuppliersWithAlive(experimentsNumber, cycleTime);
        List<TbSupplierDynamic> listSupplierDynamic = new ArrayList<>();

        // 3 主机厂任务分解
        ArrayList<ArrayList<EngineFactoryManufacturingTask>> listListEngineFactoryTaskDecomposition = startTaskService.genEngineFactoryTaskDecomposition(
                experimentsNumber, cycleTime, listEngineFactory, listEngineFactoryDynamic);

        // 4 供应商任务分解
        ArrayList<ArrayList<SupplierTask>> listListSupplierTask = startTaskService.genSupplierTask(
                experimentsNumber, cycleTime, listSuppliers, listSupplierDynamic);
        // 5 获取两者关系矩阵
        Map<String, Double> mapRelationshipMatrix = startTaskService.getMapRelationshipMatrix(experimentsNumber, cycleTime);
        Map<String, TbRelationMatrix> mapRelationshipMatrix2WithTbRelationMatrix = startTaskService.getMapRelationshipMatrix2WithTbRelationMatrix(experimentsNumber, cycleTime);
//            log.info("+++processTaskService.getTransactionContracts!!!!!");


//            Map<String, TbEngineFactory> mapEngineFactory = beforeNextTask.getMapEngineFactoryIdVsEngineFactory(listEngineFactory);
//            Map<String, TbSupplier> mapSupplier = beforeNextTask.getMapSupplierIdVsSupplier(listSuppliers);
//
//            Map<String, TbEngineFactoryDynamic> mapEngineFactoryDynamic = beforeNextTask.getMapEngineFactoryIdVsEngineFactoryDynamic(listEngineFactoryDynamic);
//            Map<String, TbSupplierDynamic> mapSupplierDynamic = beforeNextTask.getMapSupplierIdVsSupplierDynamic(listSupplierDynamic);

        // 6 获取任务契约
        ArrayList<TransactionContract> listTransactionContract = processTaskService.getTransactionContracts(listListEngineFactoryTaskDecomposition, listListSupplierTask, mapRelationshipMatrix, listEngineFactory);

        // 生成供应商和主机厂的排序
        Map<String, EngineFactoryRank> mapEngineFactoryIdVsRank = new LinkedHashMap<>(listEngineFactory.size());
        Map<String, SupplierRank> mapSupplierIdVsRank = new LinkedHashMap<>(listSuppliers.size());


        // listEngineFactoryDynamic排过序的
        startTaskService.genMapEngineFactoryIdVsRankAndmapSupplierIdVsRank(mapEngineFactoryIdVsRank, mapSupplierIdVsRank, listEngineFactory, listEngineFactoryDynamic, listSuppliers, listSupplierDynamic);




//            log.info("+++processTaskService.getTransactionSettlement!!!!!");
        // 7 最终生成数量
        List<OrderPlus> listOrderPlus = processTaskService.getTransactionSettlement(experimentsNumber, cycleTime, listTransactionContract, mapRelationshipMatrix, mapRelationshipMatrix2WithTbRelationMatrix, mapEngineFactoryIdVsRank, mapSupplierIdVsRank);
//            log.info("+++beforeNextTask.getListEngineFactoryFinalProvision!!!!!!");
        // 8 与市场交易结果
        List<EngineFactoryFinalProvision> listEngineFactoryFinalProvision = beforeNextTask.getListEngineFactoryFinalProvision(experimentsNumber, cycleTime, listOrderPlus);
//            log.info("+++beforeNextTask.beforeNextTask!!!!!");
        // 9 调整产能及进入退出
        beforeNextTask.beforeNextTask(
                experimentsNumber, cycleTime, listEngineFactoryFinalProvision, listOrderPlus, listTransactionContract
                , listEngineFactory, listEngineFactoryDynamic, listSuppliers, listSupplierDynamic
                , mapRelationshipMatrix2WithTbRelationMatrix, listListEngineFactoryTaskDecomposition);

        return "haha";
    }
}
