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
import java.util.Arrays;

public class Logger {

    public static final String HELICOPTER = "Helicopter";
    public static final String HOSPITAL = "Hospital";
    public static final String PATIENT = "Patient";

    private static final SimpleDateFormat filenameDateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
    private static final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final String fileExtension = ".aiad";

    public static boolean testOnly;         // will prevent logging in test-only runs

    private static Path outputHelicopters;  // path to file where helicopter messages will be logged
    private static Path outputHospitals;    // path to file where hospital messages will be logged
    private static Path outputPatients;     // path to file where patient messages will be logged

    private static String nowString(SimpleDateFormat format) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return format.format(timestamp);
    }

    private static String makeFileName(String directory, String category, String now) {
        return directory + now + "-" + category + fileExtension;
    }
    
    public static void init(boolean test) {

        testOnly = test;
        if (testOnly)
            return;

        String now = nowString(filenameDateFormat);
        String logsDirectory = "logs/";     // exit utils package and exit src directory to get to project root
                                            // then create logs directory in project root
        String helicoptersFile = makeFileName(logsDirectory, "helicopters", now);
        String hospitalsFile = makeFileName(logsDirectory, "hospitals", now);
        String patientsFile = makeFileName(logsDirectory, "patients", now);

        File directory = new File(logsDirectory);
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                System.out.println("Error occurred when trying to create " + logsDirectory + " directory.");
                testOnly = true;
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
            case HELICOPTER:
                path = outputHelicopters;
                break;
            case HOSPITAL:
                path = outputHospitals;
                break;
            case PATIENT:
                path = outputPatients;
                break;
            default:
                return;
        }

        message = nowString(logDateFormat) + " - " + message;
        try {
            Files.write(path, Arrays.asList(message), StandardCharsets.UTF_8,
                    Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
