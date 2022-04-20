package penalty_checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class PenaltyChecker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int n = scanner.nextInt();
        scanner.nextLine();

        FibonacciHeap<Integer, String> heap = new FibonacciHeap<>();
        String[] input;

        for (int i = 0; i < n; i++) {
            input = scanner.nextLine().split(" ");
            if (input[0].equals("ADD"))
                heap.insert(new HeapNode<>(Integer.parseInt(input[2]), input[1]));
            else if (input[0].equals("PRINT_MIN")) {
                System.out.println(heap.extractMin().value);
            }
        }
    }
}

class FibonacciHeap<K extends Number & Comparable<? super K>, V extends Comparable<V>> implements PriorityQueue<K, V>{

    int nodesCounter;
    HeapNode<K, V> min;

    public FibonacciHeap() {
        nodesCounter = 0;
        min = null;
    }

    @Override
    public void insert(HeapNode<K, V> item) {
        insertWithoutIncrement(item);
        nodesCounter++;
    }

    private void insertWithoutIncrement(HeapNode<K, V> item) {
        if (min == null)
            min = item;
        else {
            item.left = min;
            item.right = min.right;
            min.right.left = item;
            min.right = item;
            if (item.compareTo(min) < 0)
                min = item;
        }
    }

    @Override
    public HeapNode<K, V> findMin() {
        return min;
    }

    @Override
    public HeapNode<K, V> extractMin() {
        if (min == null)
            return null;

        HeapNode<K, V> toExtract = min;
        HeapNode<K, V> currentChild = toExtract.child;
        HeapNode<K, V> nextChild;
        if (currentChild != null) {
            do {
                nextChild = currentChild.left;
                insertWithoutIncrement(currentChild);
                currentChild.parent = null;
                currentChild = nextChild;
            } while (currentChild != toExtract.child);
        }

        min.left.right = min.right;
        min.right.left = min.left;

        if (toExtract == toExtract.right)
            min = null;
        else {
            min = toExtract.right;
            consolidate();
        }
        nodesCounter--;

        return toExtract;
    }

    private void consolidate() {
        List<HeapNode<K, V>> table = new ArrayList<>();
        List<HeapNode<K, V>> order = new ArrayList<>();

        HeapNode<K, V> current = min;

        do {
            order.add(current);
            current = current.right;
        } while (current != order.get(0));

        for (HeapNode<K, V> currentNode : order) {
            while (true) {
                while (currentNode.degree >= table.size())
                    table.add(null);

                if (table.get(currentNode.degree) == null) {
                    table.set(currentNode.degree, currentNode);
                    break;
                }

                HeapNode<K, V> other = table.get(currentNode.degree);
                table.set(currentNode.degree, null);

                HeapNode<K, V> tempMin = currentNode.compareTo(other) < 0 ? currentNode : other;
                HeapNode<K, V> tempMax = currentNode.compareTo(other) > 0 ? currentNode : other;

                tempMax.right.left = tempMax.left;
                tempMax.left.right = tempMax.right;

                if (tempMin.child == null) {
                    tempMin.child = tempMax;
                    tempMax.left = tempMax;
                    tempMax.right = tempMax;
                } else {
                    tempMin.child.right.left = tempMax;
                    tempMax.right = tempMin.child.right;
                    tempMin.child.right = tempMax;
                    tempMax.left = tempMin.child;
                }

                tempMax.parent = tempMin;
                tempMax.mark = false;
                tempMin.degree++;
                currentNode = tempMin;
            }

            if (currentNode.compareTo(min) < 0)
                min = currentNode;
        }
    }

    @Override
    public void decreaseKey(HeapNode<K, V> item, K newKey) throws IllegalStateException {
        if (newKey.compareTo(item.key) > 0)
            throw new IllegalStateException();
        item.key = newKey;
        HeapNode<K, V> parent = item.parent;
        if (parent != null && item.compareTo(parent) < 0) {
            cut(item, parent);
            cascadingCut(parent);
        }
    }

    private void cut(HeapNode<K, V> item, HeapNode<K, V> parent) {
        if (item.right == item)
            parent.child = null;
        else {
            item.left.right = item.right;
            item.right.left = item.left;
        }
        insertWithoutIncrement(item);
        item.parent = null;
        item.mark = false;
    }

    private void cascadingCut(HeapNode<K, V> item) {
        HeapNode<K, V> parent = item.parent;
        if (parent != null) {
            if (!item.mark)
                item.mark = true;
            else {
                cut(item, parent);
                cascadingCut(parent);
            }
        }
    }

    @Override
    public void delete(HeapNode<K, V> item) {
        item.isMinusInfinity = true;
        decreaseKey(item, item.key);
        extractMin();
    }

    @Override
    public void union(PriorityQueue<K, V> anotherQueue) throws IllegalStateException {
        if (!(anotherQueue instanceof FibonacciHeap))
            throw new IllegalStateException();
        FibonacciHeap<K, V> anotherHeap = (FibonacciHeap<K, V>) anotherQueue;

        if (min == null) {
            min = anotherHeap.min;
            return;
        }
        if (anotherHeap.min == null)
            return;

        HeapNode<K, V> anotherHeapLast = anotherHeap.min.right;
        anotherHeapLast.left = min;
        anotherHeap.min.right = min.right;
        min.right.left = anotherHeap.min;
        min.right = anotherHeapLast;

        if (min.compareTo(anotherHeap.min) > 0)
            min = anotherHeap.min;

        nodesCounter += anotherHeap.nodesCounter;
    }
}


interface PriorityQueue<K extends Number & Comparable<? super K>, V extends Comparable<V>> {
    void insert(HeapNode<K, V> item);
    HeapNode<K, V> findMin();
    HeapNode<K, V> extractMin();
    void decreaseKey(HeapNode<K, V> item, K newKey);
    void delete(HeapNode<K, V> item);
    void union(PriorityQueue<K, V> anotherQueue);
}


class HeapNode<K extends Number & Comparable<? super K>, V extends Comparable<V>> {
    int degree = 0;
    HeapNode<K, V> parent = null;
    HeapNode<K, V> child = null;
    HeapNode<K, V> left = this;
    HeapNode<K, V> right = this;
    boolean mark = false;
    K key;
    V value;
    boolean isMinusInfinity = false;

    public HeapNode(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public int compareTo(HeapNode<K, V> other) {
        if (isMinusInfinity)
            return -1;
        int res = key.compareTo(other.key);
        if (res == 0)
            res = value.compareTo(other.value);
        return res;
    }
}
