package cn.edu.zju.kpaperproject.pojo;

public class TbRelationMatrix {
    private Integer id;

    private Integer experimentsNumber;

    private Integer cycleTimes;

    private String engineFactoryId;

    private String supplierId;

    private String mapKey;

    private Double relationScore;

    private Double initialRelationalDegree;

    private Integer accumulativeTotalScore;

    private Integer transactionNumber;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getExperimentsNumber() {
        return experimentsNumber;
    }

    public void setExperimentsNumber(Integer experimentsNumber) {
        this.experimentsNumber = experimentsNumber;
    }

    public Integer getCycleTimes() {
        return cycleTimes;
    }

    public void setCycleTimes(Integer cycleTimes) {
        this.cycleTimes = cycleTimes;
    }

    public String getEngineFactoryId() {
        return engineFactoryId;
    }

    public void setEngineFactoryId(String engineFactoryId) {
        this.engineFactoryId = engineFactoryId == null ? null : engineFactoryId.trim();
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId == null ? null : supplierId.trim();
    }

    public String getMapKey() {
        return mapKey;
    }

    public void setMapKey(String mapKey) {
        this.mapKey = mapKey == null ? null : mapKey.trim();
    }

    public Double getRelationScore() {
        return relationScore;
    }

    public void setRelationScore(Double relationScore) {
        this.relationScore = relationScore;
    }

    public Double getInitialRelationalDegree() {
        return initialRelationalDegree;
    }

    public void setInitialRelationalDegree(Double initialRelationalDegree) {
        this.initialRelationalDegree = initialRelationalDegree;
    }

    public Integer getAccumulativeTotalScore() {
        return accumulativeTotalScore;
    }

    public void setAccumulativeTotalScore(Integer accumulativeTotalScore) {
        this.accumulativeTotalScore = accumulativeTotalScore;
    }

    public Integer getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(Integer transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
}