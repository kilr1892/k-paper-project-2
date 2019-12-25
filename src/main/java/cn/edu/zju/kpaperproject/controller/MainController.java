package cn.edu.zju.kpaperproject.controller;

import cn.edu.zju.kpaperproject.dto.*;
import cn.edu.zju.kpaperproject.pojo.*;
import cn.edu.zju.kpaperproject.service.BeforeNextTask;
import cn.edu.zju.kpaperproject.service.InitService;
import cn.edu.zju.kpaperproject.service.ProcessTaskService;
import cn.edu.zju.kpaperproject.service.StartTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * .
 *
 * @author RichardLee
 * @version v1.0
 */
@RestController
@Slf4j
public class MainController {

    @Autowired
    InitService initService;
    @Autowired
    StartTaskService startTaskService;
    @Autowired
    ProcessTaskService processTaskService;
    @Autowired
    BeforeNextTask beforeNextTask;


    @RequestMapping("/start/{experimentsNumber}/{cycleTimesMax}")
    public String startService(@PathVariable("experimentsNumber") int experimentsNumber, @PathVariable("cycleTimesMax") int cycleTimesMax) {
        initService.init(experimentsNumber);
        int cycleTime = 1;
//        int cycleTimesMax = 20;
        log.info("!!!!START++++++++++++++++++++++++++");
        while (cycleTime < cycleTimesMax) {
            log.info("  ");
            log.info("  ");
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

            // 6 获取任务契约
            ArrayList<TransactionContract> listTransactionContract = processTaskService.getTransactionContracts(listListEngineFactoryTaskDecomposition, listListSupplierTask, mapRelationshipMatrix, listEngineFactory);

            // 生成供应商和主机厂的排序
            Map<String, EngineFactoryRank> mapEngineFactoryIdVsRank = new LinkedHashMap<>(listEngineFactory.size());
            Map<String, SupplierRank> mapSupplierIdVsRank = new LinkedHashMap<>(listSuppliers.size());
            startTaskService.genMapEngineFactoryIdVsRankAndmapSupplierIdVsRank(mapEngineFactoryIdVsRank, mapSupplierIdVsRank, listEngineFactory, listEngineFactoryDynamic, listSuppliers, listSupplierDynamic);


//            log.info("+++processTaskService.getTransactionSettlement!!!!!");
            List<OrderPlus> listOrderPlus = processTaskService.getTransactionSettlement(experimentsNumber, cycleTime, listTransactionContract, mapRelationshipMatrix, mapRelationshipMatrix2WithTbRelationMatrix, mapEngineFactoryIdVsRank, mapSupplierIdVsRank);
//            log.info("+++beforeNextTask.getListEngineFactoryFinalProvision!!!!!!");
            List<EngineFactoryFinalProvision> listEngineFactoryFinalProvision = beforeNextTask.getListEngineFactoryFinalProvision(experimentsNumber, cycleTime, listOrderPlus);
//            log.info("+++beforeNextTask.beforeNextTask!!!!!");
            beforeNextTask.beforeNextTask(experimentsNumber, cycleTime, listEngineFactoryFinalProvision, listOrderPlus, listTransactionContract, listEngineFactory, listEngineFactoryDynamic, listSuppliers, listSupplierDynamic, mapRelationshipMatrix2WithTbRelationMatrix,listListEngineFactoryTaskDecomposition);

            cycleTime++;
        }
        log.info("!!!!END++++++++++++++++++++++++++");


        return "success";
    }
}
