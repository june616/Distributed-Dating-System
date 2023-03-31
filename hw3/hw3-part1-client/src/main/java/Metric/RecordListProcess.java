package Metric;

import DataModel.Record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecordListProcess {
    private List<Record> recordList;

    public RecordListProcess(List<Record> recordList) {
        this.recordList = recordList;
    }

    public void execute() {
        List<Integer> responseTimeList = createResponseTimeList(this.recordList);
        processList(responseTimeList);
    }

    public static List<Integer> createResponseTimeList(List<Record> recordList) {
        List<Integer> responseTimeList = new ArrayList<>();
        int length = recordList.size();
        for (int i = 0; i < length; i++) {
            Long currentResponseTime = recordList.get(i).getLatency();
            Integer currentInsertValue = (int)(long) currentResponseTime;
            responseTimeList.add(currentInsertValue);
        }
        // sort the responseTimeList
        Collections.sort(responseTimeList);
        return responseTimeList;
    }

    public static void processList(List<Integer> responseTimeList) {
        // get the mean response time
        Integer totalResponseTime = 0;
        int length = responseTimeList.size();
        for (int i = 0; i < length; i++) {
            totalResponseTime += responseTimeList.get(i);
        }
        Integer meanResponseTime = totalResponseTime / length;
        System.out.println("The mean response time of POST requests (millisecs): " + meanResponseTime);

        // get the median response time
        Integer medianResponseTime = getMedian(responseTimeList);
        System.out.println("The median response time of POST requests (millisecs): " + medianResponseTime);

        // get the p99 (99th percentile) response time
        Integer p99ResponseTime = getPercentile(responseTimeList, 99);
        System.out.println("The p99 response time of POST requests (millisecs): " + p99ResponseTime);

        // get the min and max response time
        Integer minResponseTime = responseTimeList.get(0);
        Integer maxResponseTime = responseTimeList.get(length - 1);
        System.out.println("The min response time of POST (millisecs): " + minResponseTime);
        System.out.println("The max response time of POST (millisecs): " + maxResponseTime);
    }

    public static Integer getMedian(List<Integer> list) {
        int length = list.size();
        int mid = length / 2;
        if (length % 2 == 0) {
            return (list.get(mid - 1) + list.get(mid)) / 2;
        }
        return list.get(mid);
    }

    public static Integer getPercentile(List<Integer> list, double percentile) {
        int length = list.size();
        int index = (int) Math.ceil(percentile / 100.0 * length);
        return list.get(index - 1);
    }
}


