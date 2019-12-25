package cn.edu.zju.kpaperproject.pojo;

import java.util.ArrayList;
import java.util.List;

public class TbSupplierDynamicExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TbSupplierDynamicExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberIsNull() {
            addCriterion("experiments_number is null");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberIsNotNull() {
            addCriterion("experiments_number is not null");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberEqualTo(Integer value) {
            addCriterion("experiments_number =", value, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberNotEqualTo(Integer value) {
            addCriterion("experiments_number <>", value, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberGreaterThan(Integer value) {
            addCriterion("experiments_number >", value, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("experiments_number >=", value, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberLessThan(Integer value) {
            addCriterion("experiments_number <", value, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberLessThanOrEqualTo(Integer value) {
            addCriterion("experiments_number <=", value, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberIn(List<Integer> values) {
            addCriterion("experiments_number in", values, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberNotIn(List<Integer> values) {
            addCriterion("experiments_number not in", values, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberBetween(Integer value1, Integer value2) {
            addCriterion("experiments_number between", value1, value2, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andExperimentsNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("experiments_number not between", value1, value2, "experimentsNumber");
            return (Criteria) this;
        }

        public Criteria andCycleTimesIsNull() {
            addCriterion("cycle_times is null");
            return (Criteria) this;
        }

        public Criteria andCycleTimesIsNotNull() {
            addCriterion("cycle_times is not null");
            return (Criteria) this;
        }

        public Criteria andCycleTimesEqualTo(Integer value) {
            addCriterion("cycle_times =", value, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesNotEqualTo(Integer value) {
            addCriterion("cycle_times <>", value, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesGreaterThan(Integer value) {
            addCriterion("cycle_times >", value, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("cycle_times >=", value, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesLessThan(Integer value) {
            addCriterion("cycle_times <", value, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesLessThanOrEqualTo(Integer value) {
            addCriterion("cycle_times <=", value, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesIn(List<Integer> values) {
            addCriterion("cycle_times in", values, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesNotIn(List<Integer> values) {
            addCriterion("cycle_times not in", values, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesBetween(Integer value1, Integer value2) {
            addCriterion("cycle_times between", value1, value2, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andCycleTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("cycle_times not between", value1, value2, "cycleTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierIdIsNull() {
            addCriterion("supplier_id is null");
            return (Criteria) this;
        }

        public Criteria andSupplierIdIsNotNull() {
            addCriterion("supplier_id is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierIdEqualTo(String value) {
            addCriterion("supplier_id =", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdNotEqualTo(String value) {
            addCriterion("supplier_id <>", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdGreaterThan(String value) {
            addCriterion("supplier_id >", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdGreaterThanOrEqualTo(String value) {
            addCriterion("supplier_id >=", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdLessThan(String value) {
            addCriterion("supplier_id <", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdLessThanOrEqualTo(String value) {
            addCriterion("supplier_id <=", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdLike(String value) {
            addCriterion("supplier_id like", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdNotLike(String value) {
            addCriterion("supplier_id not like", value, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdIn(List<String> values) {
            addCriterion("supplier_id in", values, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdNotIn(List<String> values) {
            addCriterion("supplier_id not in", values, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdBetween(String value1, String value2) {
            addCriterion("supplier_id between", value1, value2, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierIdNotBetween(String value1, String value2) {
            addCriterion("supplier_id not between", value1, value2, "supplierId");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPIsNull() {
            addCriterion("supplier_total_assets_p is null");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPIsNotNull() {
            addCriterion("supplier_total_assets_p is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPEqualTo(Integer value) {
            addCriterion("supplier_total_assets_p =", value, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPNotEqualTo(Integer value) {
            addCriterion("supplier_total_assets_p <>", value, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPGreaterThan(Integer value) {
            addCriterion("supplier_total_assets_p >", value, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_total_assets_p >=", value, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPLessThan(Integer value) {
            addCriterion("supplier_total_assets_p <", value, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPLessThanOrEqualTo(Integer value) {
            addCriterion("supplier_total_assets_p <=", value, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPIn(List<Integer> values) {
            addCriterion("supplier_total_assets_p in", values, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPNotIn(List<Integer> values) {
            addCriterion("supplier_total_assets_p not in", values, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPBetween(Integer value1, Integer value2) {
            addCriterion("supplier_total_assets_p between", value1, value2, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierTotalAssetsPNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_total_assets_p not between", value1, value2, "supplierTotalAssetsP");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMIsNull() {
            addCriterion("supplier_capacity_m is null");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMIsNotNull() {
            addCriterion("supplier_capacity_m is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMEqualTo(Integer value) {
            addCriterion("supplier_capacity_m =", value, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMNotEqualTo(Integer value) {
            addCriterion("supplier_capacity_m <>", value, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMGreaterThan(Integer value) {
            addCriterion("supplier_capacity_m >", value, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_capacity_m >=", value, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMLessThan(Integer value) {
            addCriterion("supplier_capacity_m <", value, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMLessThanOrEqualTo(Integer value) {
            addCriterion("supplier_capacity_m <=", value, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMIn(List<Integer> values) {
            addCriterion("supplier_capacity_m in", values, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMNotIn(List<Integer> values) {
            addCriterion("supplier_capacity_m not in", values, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMBetween(Integer value1, Integer value2) {
            addCriterion("supplier_capacity_m between", value1, value2, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityMNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_capacity_m not between", value1, value2, "supplierCapacityM");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLIsNull() {
            addCriterion("supplier_price_p_l is null");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLIsNotNull() {
            addCriterion("supplier_price_p_l is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLEqualTo(Integer value) {
            addCriterion("supplier_price_p_l =", value, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLNotEqualTo(Integer value) {
            addCriterion("supplier_price_p_l <>", value, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLGreaterThan(Integer value) {
            addCriterion("supplier_price_p_l >", value, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_price_p_l >=", value, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLLessThan(Integer value) {
            addCriterion("supplier_price_p_l <", value, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLLessThanOrEqualTo(Integer value) {
            addCriterion("supplier_price_p_l <=", value, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLIn(List<Integer> values) {
            addCriterion("supplier_price_p_l in", values, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLNotIn(List<Integer> values) {
            addCriterion("supplier_price_p_l not in", values, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLBetween(Integer value1, Integer value2) {
            addCriterion("supplier_price_p_l between", value1, value2, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePLNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_price_p_l not between", value1, value2, "supplierPricePL");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUIsNull() {
            addCriterion("supplier_price_p_u is null");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUIsNotNull() {
            addCriterion("supplier_price_p_u is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUEqualTo(Integer value) {
            addCriterion("supplier_price_p_u =", value, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUNotEqualTo(Integer value) {
            addCriterion("supplier_price_p_u <>", value, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUGreaterThan(Integer value) {
            addCriterion("supplier_price_p_u >", value, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_price_p_u >=", value, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePULessThan(Integer value) {
            addCriterion("supplier_price_p_u <", value, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePULessThanOrEqualTo(Integer value) {
            addCriterion("supplier_price_p_u <=", value, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUIn(List<Integer> values) {
            addCriterion("supplier_price_p_u in", values, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUNotIn(List<Integer> values) {
            addCriterion("supplier_price_p_u not in", values, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUBetween(Integer value1, Integer value2) {
            addCriterion("supplier_price_p_u between", value1, value2, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierPricePUNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_price_p_u not between", value1, value2, "supplierPricePU");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsIsNull() {
            addCriterion("supplier_quality_qs is null");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsIsNotNull() {
            addCriterion("supplier_quality_qs is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsEqualTo(Integer value) {
            addCriterion("supplier_quality_qs =", value, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsNotEqualTo(Integer value) {
            addCriterion("supplier_quality_qs <>", value, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsGreaterThan(Integer value) {
            addCriterion("supplier_quality_qs >", value, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_quality_qs >=", value, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsLessThan(Integer value) {
            addCriterion("supplier_quality_qs <", value, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsLessThanOrEqualTo(Integer value) {
            addCriterion("supplier_quality_qs <=", value, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsIn(List<Integer> values) {
            addCriterion("supplier_quality_qs in", values, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsNotIn(List<Integer> values) {
            addCriterion("supplier_quality_qs not in", values, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsBetween(Integer value1, Integer value2) {
            addCriterion("supplier_quality_qs between", value1, value2, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierQualityQsNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_quality_qs not between", value1, value2, "supplierQualityQs");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationIsNull() {
            addCriterion("supplier_capacity_utilization is null");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationIsNotNull() {
            addCriterion("supplier_capacity_utilization is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationEqualTo(Double value) {
            addCriterion("supplier_capacity_utilization =", value, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationNotEqualTo(Double value) {
            addCriterion("supplier_capacity_utilization <>", value, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationGreaterThan(Double value) {
            addCriterion("supplier_capacity_utilization >", value, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationGreaterThanOrEqualTo(Double value) {
            addCriterion("supplier_capacity_utilization >=", value, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationLessThan(Double value) {
            addCriterion("supplier_capacity_utilization <", value, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationLessThanOrEqualTo(Double value) {
            addCriterion("supplier_capacity_utilization <=", value, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationIn(List<Double> values) {
            addCriterion("supplier_capacity_utilization in", values, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationNotIn(List<Double> values) {
            addCriterion("supplier_capacity_utilization not in", values, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationBetween(Double value1, Double value2) {
            addCriterion("supplier_capacity_utilization between", value1, value2, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierCapacityUtilizationNotBetween(Double value1, Double value2) {
            addCriterion("supplier_capacity_utilization not between", value1, value2, "supplierCapacityUtilization");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeIsNull() {
            addCriterion("supplier_type is null");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeIsNotNull() {
            addCriterion("supplier_type is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeEqualTo(Integer value) {
            addCriterion("supplier_type =", value, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeNotEqualTo(Integer value) {
            addCriterion("supplier_type <>", value, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeGreaterThan(Integer value) {
            addCriterion("supplier_type >", value, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_type >=", value, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeLessThan(Integer value) {
            addCriterion("supplier_type <", value, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeLessThanOrEqualTo(Integer value) {
            addCriterion("supplier_type <=", value, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeIn(List<Integer> values) {
            addCriterion("supplier_type in", values, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeNotIn(List<Integer> values) {
            addCriterion("supplier_type not in", values, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeBetween(Integer value1, Integer value2) {
            addCriterion("supplier_type between", value1, value2, "supplierType");
            return (Criteria) this;
        }

        public Criteria andSupplierTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_type not between", value1, value2, "supplierType");
            return (Criteria) this;
        }

        public Criteria andAvgPriceIsNull() {
            addCriterion("avg_price is null");
            return (Criteria) this;
        }

        public Criteria andAvgPriceIsNotNull() {
            addCriterion("avg_price is not null");
            return (Criteria) this;
        }

        public Criteria andAvgPriceEqualTo(Integer value) {
            addCriterion("avg_price =", value, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceNotEqualTo(Integer value) {
            addCriterion("avg_price <>", value, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceGreaterThan(Integer value) {
            addCriterion("avg_price >", value, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceGreaterThanOrEqualTo(Integer value) {
            addCriterion("avg_price >=", value, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceLessThan(Integer value) {
            addCriterion("avg_price <", value, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceLessThanOrEqualTo(Integer value) {
            addCriterion("avg_price <=", value, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceIn(List<Integer> values) {
            addCriterion("avg_price in", values, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceNotIn(List<Integer> values) {
            addCriterion("avg_price not in", values, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceBetween(Integer value1, Integer value2) {
            addCriterion("avg_price between", value1, value2, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgPriceNotBetween(Integer value1, Integer value2) {
            addCriterion("avg_price not between", value1, value2, "avgPrice");
            return (Criteria) this;
        }

        public Criteria andAvgQualityIsNull() {
            addCriterion("avg_quality is null");
            return (Criteria) this;
        }

        public Criteria andAvgQualityIsNotNull() {
            addCriterion("avg_quality is not null");
            return (Criteria) this;
        }

        public Criteria andAvgQualityEqualTo(Integer value) {
            addCriterion("avg_quality =", value, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityNotEqualTo(Integer value) {
            addCriterion("avg_quality <>", value, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityGreaterThan(Integer value) {
            addCriterion("avg_quality >", value, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityGreaterThanOrEqualTo(Integer value) {
            addCriterion("avg_quality >=", value, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityLessThan(Integer value) {
            addCriterion("avg_quality <", value, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityLessThanOrEqualTo(Integer value) {
            addCriterion("avg_quality <=", value, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityIn(List<Integer> values) {
            addCriterion("avg_quality in", values, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityNotIn(List<Integer> values) {
            addCriterion("avg_quality not in", values, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityBetween(Integer value1, Integer value2) {
            addCriterion("avg_quality between", value1, value2, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andAvgQualityNotBetween(Integer value1, Integer value2) {
            addCriterion("avg_quality not between", value1, value2, "avgQuality");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityIsNull() {
            addCriterion("supplier_innovation_probability is null");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityIsNotNull() {
            addCriterion("supplier_innovation_probability is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityEqualTo(Double value) {
            addCriterion("supplier_innovation_probability =", value, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityNotEqualTo(Double value) {
            addCriterion("supplier_innovation_probability <>", value, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityGreaterThan(Double value) {
            addCriterion("supplier_innovation_probability >", value, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityGreaterThanOrEqualTo(Double value) {
            addCriterion("supplier_innovation_probability >=", value, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityLessThan(Double value) {
            addCriterion("supplier_innovation_probability <", value, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityLessThanOrEqualTo(Double value) {
            addCriterion("supplier_innovation_probability <=", value, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityIn(List<Double> values) {
            addCriterion("supplier_innovation_probability in", values, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityNotIn(List<Double> values) {
            addCriterion("supplier_innovation_probability not in", values, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityBetween(Double value1, Double value2) {
            addCriterion("supplier_innovation_probability between", value1, value2, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationProbabilityNotBetween(Double value1, Double value2) {
            addCriterion("supplier_innovation_probability not between", value1, value2, "supplierInnovationProbability");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationIsNull() {
            addCriterion("supplier_whether_innovation is null");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationIsNotNull() {
            addCriterion("supplier_whether_innovation is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationEqualTo(Boolean value) {
            addCriterion("supplier_whether_innovation =", value, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationNotEqualTo(Boolean value) {
            addCriterion("supplier_whether_innovation <>", value, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationGreaterThan(Boolean value) {
            addCriterion("supplier_whether_innovation >", value, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationGreaterThanOrEqualTo(Boolean value) {
            addCriterion("supplier_whether_innovation >=", value, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationLessThan(Boolean value) {
            addCriterion("supplier_whether_innovation <", value, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationLessThanOrEqualTo(Boolean value) {
            addCriterion("supplier_whether_innovation <=", value, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationIn(List<Boolean> values) {
            addCriterion("supplier_whether_innovation in", values, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationNotIn(List<Boolean> values) {
            addCriterion("supplier_whether_innovation not in", values, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationBetween(Boolean value1, Boolean value2) {
            addCriterion("supplier_whether_innovation between", value1, value2, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierWhetherInnovationNotBetween(Boolean value1, Boolean value2) {
            addCriterion("supplier_whether_innovation not between", value1, value2, "supplierWhetherInnovation");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesIsNull() {
            addCriterion("supplier_innovation_times is null");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesIsNotNull() {
            addCriterion("supplier_innovation_times is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesEqualTo(Integer value) {
            addCriterion("supplier_innovation_times =", value, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesNotEqualTo(Integer value) {
            addCriterion("supplier_innovation_times <>", value, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesGreaterThan(Integer value) {
            addCriterion("supplier_innovation_times >", value, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("supplier_innovation_times >=", value, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesLessThan(Integer value) {
            addCriterion("supplier_innovation_times <", value, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesLessThanOrEqualTo(Integer value) {
            addCriterion("supplier_innovation_times <=", value, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesIn(List<Integer> values) {
            addCriterion("supplier_innovation_times in", values, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesNotIn(List<Integer> values) {
            addCriterion("supplier_innovation_times not in", values, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesBetween(Integer value1, Integer value2) {
            addCriterion("supplier_innovation_times between", value1, value2, "supplierInnovationTimes");
            return (Criteria) this;
        }

        public Criteria andSupplierInnovationTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("supplier_innovation_times not between", value1, value2, "supplierInnovationTimes");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}