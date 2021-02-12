package fr.xenonspace.rocketmotortestor;

import jssc.SerialPort;
import jssc.SerialPortException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public final class Main {

    private static SerialPort serialPort;

    public static void main(String[] args) {
        System.out.println("Hey, you are on a XenonSpace StaticMotorTester getaway.");
        Scanner scanner = new Scanner(System.in);
        String scanned;
        Map<Long, String> data = new TreeMap<>();

        String testName;
        System.out.println("Please enter the name of the FireTest.");
        testName = scanner.next();
        System.out.println("Ok the name of the fire test is: " + testName);

        File dataFile = new File(testName + ".xenonspacedata");


        System.out.println("Please enter the name of the serial.");
        scanned = scanner.next();
        System.out.println("Serial connecting.");
        try {
            serialPortConnect(scanned);
        } catch (SerialPortException exception) {
            System.err.println("Sorry serial can't connect. " + exception.getMessage());
            return;
        }
        System.out.println("Ok " + serialPort.getPortName() + " is connected.");


        System.out.println("Data-writer initializing.");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(dataFile);
        } catch (IOException exception) {
            System.err.println("Sorry can't initialize data-writer. " + exception.getMessage());
            return;
        }
        System.out.println("Data-writer initialized.");

        String startMessage = "Enter 's' or 'start' for starting fire test. Be careful, this motor is not a Toy!";
        System.out.println(startMessage);
        while (scanner.hasNext()) {
            scanned = scanner.next();

            if (scanned.equalsIgnoreCase("s") || scanned.equalsIgnoreCase("start")) break;
            System.out.println(startMessage);
        }
        System.out.println("Fire-Test starting.");

        System.out.println("Serial preparing.");
        try {

            serialPort.readString();
            Thread.sleep(2500);
            serialPort.readString();
        } catch (SerialPortException | InterruptedException exception) {
            System.err.println("Sorry can't prepare serial. " + exception.getMessage());
            return;
        }
        System.out.println("Serial prepared.");

        Thread processThread = new Thread(() -> {
            System.out.println("Process started.\" \n Fire-Test started. \n Ok, you can get out. \n Enter 's' or 'stop' for stopping the fire test. Wait for the motor to shutdown for your safety and not to lose data.");

            String arevage = "";
            long time = System.currentTimeMillis();
            long timeNow;
            boolean firstError = true;

            while (true) {

                try {
                    arevage = serialPort.readString();
                } catch (SerialPortException exception) {

                    if (firstError) {
                        firstError = false;
                        System.err.println("Omg error. " + exception.getMessage());
                        System.out.println("Try reconnecting.");
                    }
                    try {
                        serialPortConnect(serialPort.getPortName());
                        System.out.println("Ouf reconnected.");
                        firstError = true;
                    } catch (SerialPortException e) {
                        //TODO
                    }
                }

                if (arevage != null) {
                    timeNow = (System.currentTimeMillis() - time);
                    data.put(timeNow, arevage);
                    try {
                        fileWriter.write(timeNow + " " + arevage);
                        fileWriter.flush();
                    } catch (IOException exception) {
                        //TODO
                    }
                }
            }
        });
        System.out.println("Process starting.");
        processThread.start();

        do {
            scanned = scanner.next();
        } while (!scanned.equalsIgnoreCase("s") && !scanned.equalsIgnoreCase("stop"));

        System.out.println("Process stopping.");
        processThread.stop();
        System.out.println("Process stopped.");

        System.out.println("Serial disconnecting.");
        try {
            serialPort.closePort();
            System.out.println("Ok " + serialPort.getPortName() + " is disconnected.");
        } catch (SerialPortException exception) {
            System.out.println("Sorry can't disconnect serial. " + exception.getMessage());
        }

        System.out.println("Security-Saving.");
        try {
            fileWriter.write(" \n-----------------------[Security Saving Separator]----------------------- \n");
            fileWriter.flush();
        } catch (IOException exception) {
            System.out.println("Sorry, can't write to file a separator for security-saving. " + exception.getMessage());
        }
        for (Map.Entry<Long, String> entry : data.entrySet()) {
            scanned = entry.getKey() + " " + entry.getValue();
            System.out.println(scanned);
            try {
                fileWriter.write(scanned);
                fileWriter.flush();
            } catch (IOException exception) {
                //TODO
            }
        }

        System.out.println("Sort-Saving.");
        try {
            fileWriter.write("\n -----------------------[Sort-Saving]----------------------- \n");
            fileWriter.flush();
        } catch (IOException exception) {
            System.out.println("Sorry, can't write to file a separator for sort-saving. " + exception.getMessage());
        }

        boolean isStart = false;
        long startTime = 0L;
        for (Map.Entry<Long, String> entry : data.entrySet()) {

            if (Double.parseDouble(entry.getValue()) > 0.02D && !isStart) {
                startTime = entry.getKey();
                isStart = true;
            }

            if (isStart) {
                scanned = (entry.getKey() - startTime) + " " + entry.getValue();
                System.out.println(scanned);

                try {
                    fileWriter.write(scanned);
                    fileWriter.flush();
                } catch (IOException exception) {
                    //TODO
                }
            }
        }

        System.out.println("Data-writer ending.");
        try {
            fileWriter.close();
            System.out.println("Data-writer ended.");
        } catch (IOException exception) {
            System.out.println("Sorry, can't end data-writer." + exception.getMessage());
        }

        System.out.println("All data is associated with a ms and a thrust expressed on Kg. \n Bye and have a luck.");
        System.exit(1);
    }

    private static void serialPortConnect(String serialPortAddress) throws SerialPortException {
        serialPort = new SerialPort(serialPortAddress);
        serialPort.openPort();
        serialPort.setParams(
                SerialPort.BAUDRATE_115200,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
    }
}
