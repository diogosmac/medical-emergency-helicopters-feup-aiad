package utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;

public class Logger {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
    private static final String fileExtension = ".aiad";

    public static boolean testOnly;         // will prevent logging in test-only runs

    private static Path outputHelicopters;  // path to file where helicopter messages will be logged
    private static Path outputHospitals;    // path to file where hospital messages will be logged
    private static Path outputPatients;     // path to file where patient messages will be logged
    
    private static String makeFileName(String directory, String category, String now) {
        return directory + category + "-" + now + fileExtension;
    }
    
    public static void init(boolean test) {

        testOnly = test;
        if (testOnly)
            return;

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String now = dateFormat.format(timestamp);
        
        String logsDirectory = "../../logs";    // exit utils package and exit src directory to get to project root
                                                // then create logs directory in project root
        String helicoptersFile = makeFileName(logsDirectory, "helicopters", now);
        String hospitalsFile = makeFileName(logsDirectory, "hospitals", now);
        String patientsFile = makeFileName(logsDirectory, "patients", now);

        File directory = new File(logsDirectory);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.out.println("Error occurred when trying to log execution of the program.");
                return;
            }
        }

        outputHelicopters = Paths.get(helicoptersFile);
        outputHospitals = Paths.get(hospitalsFile);
        outputPatients = Paths.get(patientsFile);
        
    }

    public static void writeLog(String message, String logSource) {

        if (testOnly) {
            System.out.println(logSource + " log - " + message);
            return;
        }

        Path path = null;

        switch (logSource) {
            case "Helicopter":
                path = outputHelicopters;
                break;
            case "Hospital":
                path = outputHospitals;
                break;
            case "Patient":
                path = outputPatients;
                break;
            default:
                return;
        }

        try {
            Files.write(path, Collections.singletonList(message), StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
