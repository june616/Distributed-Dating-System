package DataModel;

public class Record {
    private Long startTime;
    private String requestType = "POST";
    private Long latency;
    private Integer responseCode;

    public Record(Long startTime, String requestType, Long latency, Integer responseCode) {
        this.startTime = startTime;
        this.requestType = requestType;
        this.latency = latency;
        this.responseCode = responseCode;
    }

    public Record(Long startTime, Long latency, Integer responseCode) {
        this.startTime = startTime;
        this.latency = latency;
        this.responseCode = responseCode;
    }

    public Long getStartTime() {
        return startTime;
    }

    public String getRequestType() {
        return requestType;
    }

    public Long getLatency() {
        return latency;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setLatency(Long latency) {
        this.latency = latency;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "Record{" +
                "startTime=" + startTime +
                ", requestType='" + requestType + '\'' +
                ", latency=" + latency +
                ", responseCode=" + responseCode +
                '}';
    }
}


