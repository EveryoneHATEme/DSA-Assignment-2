import java.util.*;

public class CarRental {
    public static void main(String[] args) {
        Graph<String, Integer> graph = new Graph<>();

        Vertex<String, Integer> vertexA = graph.insertVertex("A", 1);
        Vertex<String, Integer> vertexB = graph.insertVertex("B", 1);
        Vertex<String, Integer> vertexC = graph.insertVertex("C", 1);
        Vertex<String, Integer> vertexD = graph.insertVertex("D", 1);
        Vertex<String, Integer> vertexE = graph.insertVertex("E", 1);
        Vertex<String, Integer> vertexF = graph.insertVertex("F", 1);
        Vertex<String, Integer> vertexG = graph.insertVertex("G", 1);

        graph.insertEdge(vertexA, vertexB, 7);
        graph.insertEdge(vertexA, vertexD, 5);
        graph.insertEdge(vertexB, vertexD, 9);
        graph.insertEdge(vertexB, vertexE, 7);
        graph.insertEdge(vertexB, vertexC, 8);
        graph.insertEdge(vertexC, vertexE, 5);
        graph.insertEdge(vertexD, vertexE, 15);
        graph.insertEdge(vertexD, vertexF, 6);
        graph.insertEdge(vertexE, vertexF, 8);
        graph.insertEdge(vertexE, vertexG, 9);
        graph.insertEdge(vertexF, vertexG, 11);

        Graph<String, Integer> spanningTree = graph.minimumSpanningTree();
    }
}


interface IGraph<K, V extends Number> {
    Vertex<K, V> insertVertex(K key, V value);
    Edge<K, V> insertEdge(Vertex<K, V> from, Vertex<K, V> to, V weight);
    void removeVertex(Vertex<K, V> v);
    void removeEdge(Edge<K, V> e);
    boolean areAdjacent(Vertex<K, V> v, Vertex<K, V> u);
    int degree(Vertex<K, V> v);
}


class Vertex<K, V extends Number> implements Comparable<Vertex<K, V>> {
    K key;
    V value;
    int index;

    int primIndex;

    public Vertex(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public Vertex(K key, V value, int index) {
        this(key, value);
        this.index = index;
    }

    @Override
    public int compareTo(Vertex<K, V> other) {
        return 1;
    }
}


class Edge<K, V extends Number> implements Comparable<Edge<K, V>> {
    Vertex<K, V> first;
    Vertex<K, V> second;
    double distance;

    public Edge(Vertex<K, V> first, Vertex<K, V> second) {
        this.first = first;
        this.second = second;
    }

    public Edge(Vertex<K, V> first, Vertex<K, V> second, V weight) {
        this(first, second);
        this.distance = weight.doubleValue() / (first.value.doubleValue() + second.value.doubleValue());
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public int compareTo(Edge<K, V> o) {
        return (int) (distance - o.distance);
    }
}


class Graph<K, V extends Number> implements IGraph<K, V> {
    private final List<Vertex<K, V>> vertices;
    private final List<Edge<K, V>> edges;
    private final List<List<Edge<K, V>>> adjacencyMatrix;

    public Graph() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        adjacencyMatrix = new ArrayList<>();
    }

    @Override
    public Vertex<K, V> insertVertex(K key, V value) {
        Vertex<K, V> toInsert = new Vertex<>(key, value, vertices.size());
        vertices.add(toInsert);

        List<Edge<K, V>> newLine = new ArrayList<>();
        for (int i = 0; i < vertices.size() - 1; i++)
            newLine.add(null);
        adjacencyMatrix.add(newLine);

        for (List<Edge<K, V>> matrix : adjacencyMatrix)
            matrix.add(null);

        for (int i = 0; i < vertices.size() - 1; i++)
            adjacencyMatrix.get(adjacencyMatrix.size() - 1).set(i, null);

        return toInsert;
    }

    @Override
    public Edge<K, V> insertEdge(Vertex<K, V> from, Vertex<K, V> to, V weight) {
        Edge<K, V> toInsert = new Edge<>(from, to, weight);
        edges.add(toInsert);

        adjacencyMatrix.get(from.index).set(to.index, toInsert);
        adjacencyMatrix.get(to.index).set(from.index, toInsert);

        return toInsert;
    }

    public Edge<K, V> insertEdge(Vertex<K, V> from, Vertex<K, V> to, double weight) {
        Edge<K, V> toInsert = new Edge<>(from, to);
        toInsert.setDistance(weight);
        edges.add(toInsert);

        adjacencyMatrix.get(from.index).set(to.index, toInsert);
        adjacencyMatrix.get(to.index).set(from.index, toInsert);

        return toInsert;
    }

    @Override
    public void removeVertex(Vertex<K, V> vertex) {
        for (List<Edge<K, V>> line : adjacencyMatrix)
            line.remove(vertex.index);

        adjacencyMatrix.remove(vertex.index);

        int i = 0;
        while (i < edges.size()) {
            if (edges.get(i).first.equals(vertex) || edges.get(i).second.equals(vertex))
                edges.remove(i);
            else
                i++;
        }

        vertices.remove(vertex);
    }

    @Override
    public void removeEdge(Edge<K, V> edge) {
        adjacencyMatrix.get(edge.first.index).set(edge.second.index, null);
        adjacencyMatrix.get(edge.second.index).set(edge.first.index, null);

        edges.remove(edge);
    }

    @Override
    public boolean areAdjacent(Vertex<K, V> first, Vertex<K, V> second) {
        return adjacencyMatrix.get(first.index).get(second.index) != null;
    }

    @Override
    public int degree(Vertex<K, V> vertex) {
        int degree = 0;

        for (List<Edge<K, V>> line : adjacencyMatrix)
            if (line.get(vertex.index) != null)
                degree++;

        return degree;
    }

    public Graph<K, V> minimumSpanningTree() {
        Graph<K, V> spanningTree = new Graph<>();
        List<Double> distances = new ArrayList<>(Collections.singletonList(0.0));
        for (int i = 1; i < vertices.size(); i++)
            distances.add(Double.POSITIVE_INFINITY);

        FibonacciHeap<Double, GraphPair<K, V>> heap = new FibonacciHeap<>();
        List<HeapNode<Double, GraphPair<K, V>>> heapNodes = new ArrayList<>();

        for (int i = 0; i < vertices.size(); i++) {
            HeapNode<Double, GraphPair<K, V>> node = new HeapNode<>(
                    distances.get(i),
                    new GraphPair<>(vertices.get(i), null, i)
            );
            heap.insert(node);
            heapNodes.add(node);
        }

        HeapNode<Double, GraphPair<K, V>> currentHeapNode;
        GraphPair<K, V> currentPair;
        Vertex<K, V> currentVertex;
        Edge<K, V> currentEdge;
        Vertex<K, V> insertedVertex;
        Vertex<K, V> anotherVertex;

        while (heap.findMin() != null) {
            currentHeapNode = heap.extractMin();
            currentPair = currentHeapNode.value;
            currentVertex = currentPair.vertex;
            currentEdge = currentPair.edge;

            insertedVertex = spanningTree.insertVertex(currentVertex.key, currentVertex.value);
            if (currentEdge != null)
                spanningTree.insertEdge(currentEdge.first, insertedVertex, currentEdge.distance);

            for (int i = 0; i < adjacencyMatrix.get(currentVertex.index).size(); i++) {
                currentEdge = adjacencyMatrix.get(currentVertex.index).get(i);
                if (currentEdge == null)
                    continue;

                if (currentEdge.first == currentVertex)
                    anotherVertex = currentEdge.second;
                else
                    anotherVertex = currentEdge.first;

                if (currentEdge.distance < distances.get(anotherVertex.index)) {
                    distances.set(anotherVertex.index, currentEdge.distance);
                    heap.decreaseKey(heapNodes.get(anotherVertex.index), currentEdge.distance);
                    heapNodes.get(anotherVertex.index).value.edge = new Edge<>(insertedVertex, anotherVertex);
                    heapNodes.get(anotherVertex.index).value.edge.setDistance(distances.get(anotherVertex.index));
                }
            }

        }

        return spanningTree;
    }
}


class GraphPair<K, V extends Number> implements Comparable<GraphPair<K, V>> {
    Vertex<K, V> vertex;
    Edge<K, V> edge;
    int index;

    public GraphPair(Vertex<K, V> vertex, Edge<K, V> edge, int index) {
        this.vertex = vertex;
        this.edge = edge;
        this.index = index;
    }

    @Override
    public int compareTo(GraphPair<K, V> other) {
        int res = vertex.compareTo(other.vertex);
        if (res == 0)
            res = edge.compareTo(other.edge);
        return res;
    }
}


interface PriorityQueue<K extends Comparable<? super K>, V extends Comparable<V>> {
    void insert(HeapNode<K, V> item);
    HeapNode<K, V> findMin();
    HeapNode<K, V> extractMin();
    void decreaseKey(HeapNode<K, V> item, K newKey);
    void delete(HeapNode<K, V> item);
    void union(PriorityQueue<K, V> anotherQueue);
}


class HeapNode<K extends Comparable<? super K>, V extends Comparable<V>> {
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


class FibonacciHeap<K extends Comparable<K>, V extends Comparable<V>> implements PriorityQueue<K, V>{

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

    public void insert(K key, V value) {
        insert(new HeapNode<>(key, value));
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
