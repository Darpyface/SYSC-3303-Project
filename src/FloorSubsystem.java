import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalTime;

public class FloorSubsystem implements Runnable {

    private ArrayList<Floor> listOfFloors;
    private ArrayList<Request> listOfRequests;
    private Request currRequest;

    FloorSubsystem(int numberOfFloors, Request buffer) {
        listOfFloors = new ArrayList<>();
        for (int i = 0; i < numberOfFloors; i++) {
            listOfFloors.add(new Floor(i+1));
        }
        listOfRequests = readCSV("Input.csv");
        currRequest = null;

        currRequest = buffer;
    }

    public static ArrayList<Request> readCSV(String csvName) {
        //SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.S");
        ArrayList<Request> toReturn = new ArrayList<Request>();
        try {
            FileReader file = new FileReader(csvName);
            BufferedReader input = new BufferedReader(file);
            String line = input.readLine();

            while(line != null){
                line = input.readLine();
                String[] values = line.split(" ");
                Request newRequest = new Request(LocalTime.parse(values[0]),
                        Integer.parseInt(values[1]),
                        values[2],
                        Integer.parseInt(values[3]));
                toReturn.add(newRequest);
            }
            input.close();
        } catch(Exception e) { e.getStackTrace(); }
        return toReturn;
    }

    private boolean checkTime(LocalTime reqTime, LocalTime currTime){
        int result = reqTime.compareTo(currTime);
        notifyAll();
        return result >= 0;
    }

    private synchronized void sendRequest(Request request) {
        while(currRequest != null){};
        currRequest = request;
        notifyAll();
    }

    public void run() {
        while(true){
            for (Request r : listOfRequests) {
                boolean requestNow = checkTime(r.getTime(), LocalTime.now());
                if (requestNow){
                   sendRequest(r);
                }
            }

        }
    }
}
