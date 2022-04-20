import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SimpleFraudDetection {
    List<String> rawDates;
    List<Integer> dates;
    List<Double> costs;
    int trailingDays;

    public static void main(String[] args) throws ParseException {
        new SimpleFraudDetection();
    }

    SimpleFraudDetection() throws ParseException {
        getInput();
        dates = handleDates(rawDates);
        Utils.countingSort(dates, costs);
        System.out.println(countNotifications());
    }

    private void getInput() {
        Scanner scanner = new Scanner(System.in);

        int lines = scanner.nextInt();
        trailingDays = scanner.nextInt();
        scanner.nextLine();

        rawDates = new ArrayList<>(lines);
        costs = new ArrayList<>(lines);
        String[] inputLine;

        for (int i = 0; i < lines; i++) {
            inputLine = scanner.nextLine().split(" \\$");
            rawDates.add(inputLine[0]);
            costs.add(Double.parseDouble(inputLine[1]));
        }

        scanner.close();
    }

    private static List<Integer> handleDates(List<String> dates) throws ParseException {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        List<Integer> result = new ArrayList<>();

        for (String dateString : dates)
            result.add((int) (parser.parse(dateString).getTime() / 3600000L / 24L));

        int firstDay = Utils.min(result);

        for (int i = 0; i < result.size(); i++)
            result.set(i, result.get(i) - firstDay);

        return result;
    }

    private int countNotifications() {
        Queue<Double> trackedDays = new LinkedList<>();

        int currentRecord = 1;
        double todaySpent = costs.get(0);

        for (int day = 0; day < trailingDays; day++) {
            while (dates.get(currentRecord) == day) {
                todaySpent += costs.get(currentRecord);
                currentRecord++;
            }
            trackedDays.add(todaySpent);
            todaySpent = 0.0;
        }

        int lastDay = dates.get(dates.size() - 1);
        double currentThreshold;
        int notificationsAmount = 0;

        for (int day = trailingDays; day <= lastDay; day++) {
            currentThreshold = 2 * Utils.median(new ArrayList<>(trackedDays));

            while (dates.size() > currentRecord && dates.get(currentRecord) == day) {
                todaySpent += costs.get(currentRecord);
                currentRecord++;

                if (todaySpent >= currentThreshold)
                    notificationsAmount++;
            }
            trackedDays.poll();
            trackedDays.add(todaySpent);
            todaySpent = 0.0;
        }

        return notificationsAmount;
    }
}


class Utils {
    public static int min(List<Integer> array) {
        int min = array.get(0);

        for (int i = 1; i < array.size(); i++)
            if (array.get(i) < min)
                min = array.get(i);

        return min;
    }

    public static int max(List<Integer> array) {
        int max = array.get(0);

        for (int i = 1; i < array.size(); i++)
            if (array.get(i) > max)
                max = array.get(i);

        return max;
    }

    public static void countingSort(List<Integer> array, List<Double> satelliteData) {
        int lastDay = max(array);

        List<Integer> countArray = new ArrayList<>(lastDay + 1);
        for (int i = 0; i < lastDay + 1; i++)
            countArray.add(0);

        for (int number : array)
            countArray.set(number, countArray.get(number) + 1);

        for (int i = 1; i < countArray.size(); i++)
            countArray.set(i, countArray.get(i - 1) + countArray.get(i));

        List<Integer> newArray = new ArrayList<>(array.size());
        List<Double> newSatellite = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            newArray.add(0);
            newSatellite.add(0.0);
        }

        for (int i = array.size() - 1; i >= 0; i--) {
            newArray.set(countArray.get(array.get(i)) - 1, array.get(i));
            newSatellite.set(countArray.get(array.get(i)) - 1, satelliteData.get(i));
            countArray.set(array.get(i), countArray.get(array.get(i)) - 1);
        }

        for (int i = 0; i < array.size(); i++) {
            array.set(i, newArray.get(i));
            satelliteData.set(i, newSatellite.get(i));
        }
    }

    public static void heapSort(List<Double> array) {
        for (int i = array.size() / 2 - 1; i >= 0; i--)
            heapify(array, array.size(), i);

        for (int i = array.size() - 1; i >= 0; i--) {
            double temp = array.get(0);
            array.set(0, array.get(i));
            array.set(i, temp);

            heapify(array, i, 0);
        }
    }

    private static void heapify(List<Double> array, int size, int root) {
        int largest = root;
        int left = root * 2 + 1;
        int right = root * 2 + 2;

        if (left < size && array.get(left) > array.get(largest))
            largest = left;
        if (right < size && array.get(right) > array.get(largest))
            largest = right;

        if (largest != root) {
            double temp = array.get(root);
            array.set(root, array.get(largest));
            array.set(largest, temp);

            heapify(array, size, largest);
        }
    }

    public static double median(List<Double> array) {
        List<Double> sorted = new ArrayList<>(array);
        heapSort(sorted);

        if (sorted.size() % 2 == 0)
            return (sorted.get(sorted.size() / 2 - 1) + sorted.get(sorted.size() / 2)) / 2;
        else
            return sorted.get(sorted.size() / 2);
    }
}
