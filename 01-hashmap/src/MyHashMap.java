import java.util.Objects;

public class MyHashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] table;
    private int size;
    private int threshold;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.table = (Node<K, V>[]) new Node[DEFAULT_CAPACITY];
        this.threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
    }

    public V put(K key, V value) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        for (Node<K, V> n = table[index]; n != null; n = n.next) {
            if (n.hash == hash && Objects.equals(n.key, key)) {
                V old = n.value;
                n.value = value;
                return old;
            }
        }

        table[index] = new Node<>(hash, key, value, table[index]);
        size++;

        if (size > threshold) {
            resize();
        }
        return null;
    }

    public V get(Object key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        for (Node<K, V> n = table[index]; n != null; n = n.next) {
            if (n.hash == hash && Objects.equals(n.key, key)) {
                return n.value;
            }
        }
        return null;
    }

    public V remove(Object key) {
        int hash = hash(key);
        int index = indexFor(hash, table.length);

        Node<K, V> prev = null;
        Node<K, V> curr = table[index];

        while (curr != null) {
            if (curr.hash == hash && Objects.equals(curr.key, key)) {
                if (prev == null) {
                    table[index] = curr.next;
                } else {
                    prev.next = curr.next;
                }
                size--;
                return curr.value;
            }
            prev = curr;
            curr = curr.next;
        }
        return null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Node<K, V> n : table) {
            for (; n != null; n = n.next) {
                if (!first) sb.append(", ");
                sb.append(n.key).append('=').append(n.value);
                first = false;
            }
        }
        return sb.append('}').toString();
    }

    //Spread-функция: перемешивает старшие биты hashCode в младшие.
    private static int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    //index = hash % capacity, но быстрее, т.к. capacity — степень двойки.
    private static int indexFor(int hash, int length) {
        return hash & (length - 1);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int oldCap = oldTable.length;
        int newCap = oldCap << 1;

        if (oldCap >= (1 << 30)) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCap];
        threshold = (int) (newCap * LOAD_FACTOR);

        for (int i = 0; i < oldCap; i++) {
            Node<K, V> node = oldTable[i];
            if (node == null) continue;

            // Разбиваем цепочку на "low" (остаётся на i) и "high" (переезжает на i+oldCap)
            Node<K, V> loHead = null, loTail = null;
            Node<K, V> hiHead = null, hiTail = null;

            while (node != null) {
                if ((node.hash & oldCap) == 0) {
                    if (loTail == null) loHead = node; else loTail.next = node;
                    loTail = node;
                } else {
                    if (hiTail == null) hiHead = node; else hiTail.next = node;
                    hiTail = node;
                }
                node = node.next;
            }

            if (loTail != null) { loTail.next = null; newTable[i] = loHead; }
            if (hiTail != null) { hiTail.next = null; newTable[i + oldCap] = hiHead; }
        }

        table = newTable;
    }
}