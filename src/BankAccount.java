import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankAccount {

    public static void main(String[] args) throws ParseException {
        BTree history = new BTree(3);

        Queue<Command> commandQueue = readInput();
//        normalize(commandQueue);

        int i = 1;
        for (Command command : commandQueue) {
//            if (i == commandQueue.size())
//                System.out.println("");
            switch (command.commandType) {
                case DEPOSIT:
                    history.add(command.dates[0], command.amount);
                    break;
                case WITHDRAW:
                    history.add(command.dates[0], -command.amount);
                    break;
                case REPORT:
                    List<Integer> operations = history.lookupRange(command.dates[0], command.dates[1]);
                    System.out.println(sum(operations));
            }
            i++;
        }
    }

    public static Queue<Command> readInput() throws ParseException {
        Scanner scanner = new Scanner(System.in);

        int lines = scanner.nextInt();
        scanner.nextLine();

        Queue<Command> commandQueue = new LinkedList<>();

        for (int i = 0; i < lines; i++)
            commandQueue.add(new Command(scanner.nextLine()));

        return commandQueue;
    }

    public static int sum(List<Integer> integerList) {
        int sum = 0;
        for (Integer num : integerList)
            sum += num;
        return sum;
    }

    public static void normalize(Queue<Command> commands) {
        long min = -1L;
        int size = commands.size();
        Command current;

        for (int i = 0; i < size; i++) {
            current = commands.remove();
            if (min == -1L)
                min = current.dates[0];
            else if (min > current.dates[0])
                min = current.dates[0];
            commands.add(current);
        }

        for (int i = 0; i < size; i++) {
            current = commands.remove();
            for (int j = 0; j < current.dates.length; j++)
                current.dates[j] -= min;
            commands.add(current);
        }
    }
}


class Command {
    static SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");

    enum CommandType {
        DEPOSIT,
        WITHDRAW,
        REPORT
    }

    Long[] dates;
    CommandType commandType;
    int amount;

    public Command(String commandString) throws ParseException {
        String[] parts = commandString.split(" ");

        if (parts.length == 3) {
            dates = new Long[] { parser.parse(parts[0]).getTime() / 86400000L };
            commandType = parts[1].equals("DEPOSIT") ? CommandType.DEPOSIT : CommandType.WITHDRAW;
            amount = Integer.parseInt(parts[2]);
        } else {
            dates = new Long[] { parser.parse(parts[2]).getTime() / 86400000L, parser.parse(parts[4]).getTime() / 86400000L };
            commandType = CommandType.REPORT;
            amount = 0;
        }
    }
}


class BTree implements RangeMap<Long, Integer>{

    final int minDegree;
    int depth;
    TreeNode head;

    public BTree(int minDegree) {
        this.minDegree = minDegree;
        depth = 0;
        head = null;
    }

    @Override
    public int size() {
        return depth;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public void add(Long key, Integer value) {
        if (head == null) {
            head = new TreeNode(minDegree, true);
            head.addItem(key, value);
            depth = 1;
        } else {
            if (head.isFull()) {
                head.split();
                depth++;
            }
            head.addItem(key, value);
        }
    }

    @Override
    public boolean contains(Long key) {
        if (head == null)
            return false;
        else
            return head.contains(key);
    }

    @Override
    public Integer lookup(Long key) {
        if (head == null)
            return null;
        else
            return head.lookup(key);
    }

    @Override
    public List<Integer> lookupRange(Long from, Long to) {
        if (head == null)
            return new LinkedList<>();
        else
            return head.lookupRange(from, to);
    }

    @Override
    public Integer remove(Long key) {
        return null;
    }

}


class TreeNode {

    List<TreeNode> children;
    List<TreeItem> items;
    private final int minDegree;
    private boolean isLeaf;

    public TreeNode(int minDegree, boolean isLeaf) {
        this.minDegree = minDegree;
        this.isLeaf = isLeaf;
        items = new ArrayList<>();
        children = new ArrayList<>();
    }

    public boolean isFull() {
        return items.size() == 2 * minDegree - 1;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void addItem(TreeItem item) {
        if (isLeaf()) {
            if (isFull()) {
                split();
                isLeaf = false;
            } else {
                int index = 0;
                while (index < items.size() && items.get(index).key < item.key)
                    index++;
                items.add(index, item);
            }
        }

        if (!isLeaf()) {
            if (items.size() == 0) {
                items.add(item);
                return;
            }

            int index = 0;
            while (index < items.size() && items.get(index).key < item.key)
                index++;
            TreeNode child = children.get(index);

            if (child.isFull()) {
                children.remove(index);
                child.split();

                items.addAll(index, child.items);
                children.addAll(index, child.children);

                while (index < items.size() && items.get(index).key < item.key)
                    index++;
                child = children.get(index);
            }

            child.addItem(item);
        }
    }

    public void addItem(Long key, Integer value) {
        addItem(new TreeItem(key, value));
    }

    public void addChild(TreeNode child) {
        isLeaf = false;
        children.add(child);
    }

    public void split() {
        TreeNode left = new TreeNode(minDegree, true);

        for (int i = 0; i < minDegree - 1; i++)
            left.addItem(items.remove(0));

        if (!isLeaf())
            for (int i = 0; i < minDegree; i++)
                left.addChild(children.remove(0));

        TreeNode right = new TreeNode(minDegree, true);

        for (int i = 0; i < minDegree - 1; i++)
            right.addItem(items.remove(1));

        if (!isLeaf())
            for (int i = 0; i < minDegree; i++)
                right.addChild(children.remove(0));

        addChild(left);
        addChild(right);
        isLeaf = false;
    }

    public boolean contains(Long key) {
        return lookup(key) != null;
    }

    public Integer lookup(Long key) {
        int index = 0;
        while (index < items.size() && items.get(index).key < key)
            index++;

        if (index < items.size() && items.get(index).key.equals(key))
            return items.get(index).value;
        else if (isLeaf())
            return null;
        else
            return children.get(index).lookup(key);
    }

    public List<Integer> lookupRange(Long from, Long to) {
        List<Integer> result = new ArrayList<>();

        int i;
        for (i = 0; i < items.size(); i++) {
            if (from <= items.get(i).key) {
                if (items.get(i).key > to)
                    break;

                if (!isLeaf())
                    result.addAll(children.get(i).lookupRange(from, to));
                result.add(items.get(i).value);
            }
        }
        if (!isLeaf())
            result.addAll(children.get(i).lookupRange(from, to));

        return result;
    }
}


class TreeItem {
    public Long key;
    public Integer value;

    public TreeItem(Long key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public TreeItem(Date date, Integer value) {
        this(date.getTime(), value);
    }

    public TreeItem() {
        this(0L, 0);
    }
}


interface RangeMap<K, V> {
    int size();
    boolean isEmpty();
    void add(K key, V value);               // insert new item into the map
    boolean contains(K key);                // check if a key is present
    V lookup(K key);                        // lookup a value by the key
    List<V> lookupRange(K from, K to);      // lookup values for a range of keys
    Integer remove(K key);                  // remove an item from a map (+1% extra credit)
}
