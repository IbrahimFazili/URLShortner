import java.util.HashMap;
import java.util.Map;

public class LRUCache {
    private final int capacity;
    private final Map<String, LinkedListNode> map;
    private final DoublyLinkedList doublyLinkedList;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        map = new HashMap<>();
        doublyLinkedList = new DoublyLinkedList();
    }

    public synchronized String get(String key) {
        LinkedListNode node = map.get(key);
        if (node == null) return null;
        doublyLinkedList.moveToHead(node);
        return node.value;
    }

    public synchronized void put(String key, String value) {
        LinkedListNode node = map.get(key);
        if (node != null) {
            node.value = value;
            doublyLinkedList.moveToHead(node);
        }
        else {
            if (map.size() == capacity) {
                LinkedListNode last = doublyLinkedList.getLast(); // LRU element
                doublyLinkedList.removeNode(last);
                map.remove(last.key);
            }
            node = new LinkedListNode(key, value);
            doublyLinkedList.addNode(node);
            map.put(key, node);
        }
    }

    public String toString() {
        String result = "";
        for (String key: map.keySet()){
            result += String.format("[%s]: [%s]\n", key, this.map.get(key).value);
        }
        return result;
    }
}

class DoublyLinkedList {
    LinkedListNode head, tail;

    void addNode(LinkedListNode node) {
        if (head == null) head = tail = node;
        else {
            node.next = head;
            head.prev = node;
            head = node;
        }
    }

    void removeNode(LinkedListNode node) {
        if (node == head && node == tail) head = tail = null;
        else if (node == head) head = head.next;
        else if (node == tail) tail = tail.prev;
        else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        node.next = node.prev = null;
    }

    void moveToHead(LinkedListNode node) {
        removeNode(node);
        addNode(node);
    }

    LinkedListNode getLast() {
        return tail;
    }
}

class LinkedListNode {
    String key;
    String value;
    LinkedListNode next;
    LinkedListNode prev;

    LinkedListNode(String key, String value) {
        this.key = key;
        this.value = value;
    }
}