package theoretical;

import java.util.ArrayList;
import java.util.List;

public class DHeap {
    int d;
    List<Integer> array;

    public static void main(String[] args) {
        DHeap heap = new DHeap(5);

        for (int i = 10; i > 0; i--)
            heap.insert(i);

        for (int i = 0; i < 10; i++)
            System.out.println(heap.extractMin());
    }

    public DHeap(int d) {
        array = new ArrayList<>();
        this.d = d;
    }

    public int getParentIndex(int childIndex) {
        return (childIndex - 1) / d;
    }

    public int getChildIndex(int parentIndex, int childNumber) {
        return d * parentIndex + childNumber;
    }

    public void insert(int key) {
        array.add(key);                  // Add item in the end of heap
        int index = array.size() - 1;

        // while there is parent and parent of this item is greater that item itself and then swaps item and its parent
        while (index > 0 && array.get(getParentIndex(index)) > array.get(index)) {
            swap(array, index, getParentIndex(index));
            index = getParentIndex(index);
        }
    }

    public Integer extractMin() {
        if (array.size() == 0)  // check if there are elements
            return null;

        int result = array.get(0);                  // remove first element and puts last element as first
        array.set(0, array.get(array.size() - 1));
        array.remove(array.size() - 1);

        int index = 0;
        int min = 0;
        while (index < array.size()) {      // checks items children if any of them is lesser
            for (int i = 0; i < d; i++)     // finds the smallest
                if (getChildIndex(index, i) < array.size() && array.get(min) > array.get(getChildIndex(index, i)))
                    min = getChildIndex(index, i);
            if (index != min) {             // swaps current item and smallest child
                swap(array, index, min);
                index = min;
            } else
                break;
        }
        return result;
    }

    public void swap(List<Integer> array, int firstPos, int secondPos) {
        int temp = array.get(firstPos);
        array.set(firstPos, array.get(secondPos));
        array.set(secondPos, temp);
    }
}
