package com.vigil.dispatcher;

import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.vigil.alarm.AlarmAcknowledge;
import com.vigil.alarm.AlarmMessage;
import com.vigil.config.ConfigValidator;
import com.vigil.monitor.MonitorReading;

public class FileDispatcher extends Dispatcher{

    public record Configuration(String fileName, long maxLineCount) implements DispatcherConfig{

        public static Configuration fromMap(Map<String, Object> map){

            Map<String, Object> validMap = ConfigValidator.requireMap(map, "File Dispatcher Map");
            Configuration config = new Configuration(

                ConfigValidator.requireString(validMap, "File Dispatcher", "fileName"),
                ConfigValidator.requireLong(validMap, "File Dispatcher", "maxLineCount")
            );

            return config;
        }

        @Override
        public String getType(){
            return "File";
        }
    }

    private Path currentFile;
    private long lineCount = 0;
    private final Configuration config;

    public FileDispatcher(Configuration config) {

        this.config = config;

        this.currentFile = this.createFilePath();
    }
    
    @Override
    public void sendAlarm(AlarmMessage<?> result){

        String eventString = "STATUS => " + result.lastUpdated() + " - " + result.name() + " - " + result.alarmId() + " " + result.status() + " : " + result.value();
        
        this.writeLine(eventString);
    }

    @Override
    public void sendValue(MonitorReading<?> result){
        String telemetryString = "TELEMETRY => " + result.timestamp() + " - " + result.name() + " : " + result.value();

        this.writeLine(telemetryString);
    }

    @Override
    public void sendAlarmAcknowledgement(AlarmAcknowledge acknowledgement){
        String ackString = "ALARM ACK => " + acknowledgement.acknowledgedAt()+ " - " + acknowledgement.source() + " - " + acknowledgement.alarmId() + " - " + "ACK";

        this.writeLine(ackString);
    }

    private void writeLine(String line){
        if (this.lineCount >= config.maxLineCount()){
            this.rotateFile();
        }
        
        try (var writer = Files.newBufferedWriter(
            this.currentFile,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )) {

            writer.write(line);
            writer.newLine();
            writer.flush();

            this.lineCount++;

        } catch(IOException e) {
            this.logger.severe("Error writing to output file" + e);
        }
    }

    private void rotateFile() {
        this.lineCount = 0;
        this.currentFile = createFilePath();
    }

    private Path createFilePath() {

        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        return Path.of(
            config.fileName() + "-" + timestamp
        );
    }
}