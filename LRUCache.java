import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class LRUCache {
    private int capacity;
    private Map<Long, Node> cache;
    private LinkedList<Node> order;

    /**
     * Constructor
     * @param capacity for cache
     * */
    public LRUCache(int capacity) {
        this.capacity = capacity;
        cache = new HashMap<>();
        order=new LinkedList<>();
    }

    /**
     * Read the record from the cache and change the order of the records.
     * @param key of the record
     * @return record
     * */
    public String get(long key) {
        Node node = cache.get(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);
        return node.value;
    }

    /**
     * Store records in the cache.
     * @param key of the record
     * @param value of the record
     * */
    public void put(long key, String value) {
        Node node = cache.get(key);
        if (node == null) {
            Node newNode = new Node(key, value);
            cache.put(key, newNode);
            order.addFirst(newNode);

            if (cache.size() > capacity) {
                Node tail = order.removeLast();
                cache.remove(tail.key);
            }
        } else {
            node.value = value;
            moveToHead(node);
        }
    }

    /**
     * Moves the node in the cache to the header
     * @param node that needs to be moved
     * */
    private void moveToHead(Node node) {
        order.remove(node);
        order.addFirst(node);
    }

    /**
     * Output the cache structure to a file
     * @param fileWriter Handle to the output file
     * */
    public void display(FileWriter fileWriter) throws IOException {
        fileWriter.write("MRU\n");
        for(Node node:order){
            fileWriter.write(String.format("   %d:  %s\n",node.key,node.value));
        }
        fileWriter.write("LRU\n");
    }

    /**
     * Cache unit
     * */
    class Node {
        long key;
        String value;

        /**
         * Constructor
         * @param _key of record
         * @param _value of record
         * */
        public Node(long _key, String _value) {
            key = _key;
            value = _value;
        }
    }
}

