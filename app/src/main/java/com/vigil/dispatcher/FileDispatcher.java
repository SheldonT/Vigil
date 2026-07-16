package com.vigil.dispatcher;

import java.util.Map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.logging.Logger;

import com.vigil.alarm.AlarmResult;

public class FileDispatcher extends Dispatcher{

    public record Configuration(String fileName, long maxLineCount) implements DispatcherConfig{

        public static Configuration fromMap(Map<String, Object> map){
            Configuration config = new Configuration(
                (String)map.get("fileName"),
                (long)map.get("maxLineCount")
            );

            return config;
        }

        @Override
        public String getType(){
            return "File";
        }
    }

    private static final Logger logger =
            Logger.getLogger(FileDispatcher.class.getName());
    private Path currentFile;
    private long lineCount = 0;
    private final Configuration config;

    public FileDispatcher(Configuration config) {

        this.config = config;

        this.currentFile = this.createFilePath();
    }
    @Override
    public void send(AlarmResult result){

        String eventString = result.timestampNow + " - " + result.name + " " + result.status + " : " + result.value;

        if (this.lineCount >= config.maxLineCount()){
            this.rotateFile();
        }
        
        try (var writer = Files.newBufferedWriter(
            this.currentFile,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        )) {

            writer.write(eventString);
            writer.newLine();
            writer.flush();

            this.lineCount++;

        } catch(IOException e) {
            logger.severe("Error writing to output file" + e);
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