import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteCsv {
    private String filePath;
    private List<Record> recordList;

    public WriteCsv(String filePath, List<Record> recordList) {
        this.filePath = filePath;
        this.recordList = recordList;
    }

    public void execute() throws IOException {
        generateFile(this.filePath, this.recordList);
    }

    public static void generateFile(String filePath, List<Record> recordList) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(filePath));
        String header[] = {"start time", "request type", "latency", "response code"};
        csvWriter.writeNext(header);

        for (int i = 0; i < recordList.size(); i++) {
            Record curRecord = recordList.get(i);
            String curStartTime = Long.toString(curRecord.getStartTime());
            String curRequestType = curRecord.getRequestType();
            String curLatency = Long.toString(curRecord.getLatency());
            String curResponseCode = Integer.toString(curRecord.getResponseCode());
            String line[] = {curStartTime, curRequestType, curLatency, curResponseCode};
            csvWriter.writeNext(line);
        }
        csvWriter.flush();
//        System.out.println("csv file is created!");
    }
}

