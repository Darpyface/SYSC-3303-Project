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

    /**
     * Reads the input csv file and parses into our desired format for request calls
     * @param csvName input csv file
     * @return the list of requests in the desired format
     */
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

    /**
     * Checks if the request time is the current time
     * @param reqTime the time of the request
     * @param currTime the current clock time
     * @return if the time is exactly the same or if the currTime has passed the reqTime
     */
    private boolean checkTime(LocalTime reqTime, LocalTime currTime){
        int result = reqTime.compareTo(currTime);
        notifyAll();
        return result >= 0;
    }

    /**
     * Sets shared variable to request to be sent and removes request from list so that the request isn't repeated
     * @param request the request to check the time of
     */
    private synchronized void sendRequest(Request request) {
        while(currRequest != null){
            try {
                wait();
            } catch (InterruptedException e) {};
        }
        currRequest = request;
        listOfRequests.remove(request);
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
