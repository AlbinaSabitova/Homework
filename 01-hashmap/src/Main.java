public class Main {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();

        expect(map.put("a", 1) == null, "put новой пары → null");
        expect(map.get("a") == 1,       "get возвращает значение");
        expect(map.get("x") == null,    "get отсутствующего ключа → null");
        expect(map.put("a", 2) == 1,    "put перезаписи → старое значение");
        expect(map.get("a") == 2,       "значение перезаписалось");
        expect(map.size() == 1,         "size не растёт при перезаписи");
        expect(map.remove("a") == 2,    "remove → значение");
        expect(map.get("a") == null,    "после remove ключ отсутствует");
        expect(map.remove("a") == null, "повторный remove → null");

        map.put(null, 42);
        expect(map.get(null) == 42, "null-ключ работает");
        expect(map.remove(null) == 42, "null-ключ удаляется");

        MyHashMap<CollidingKey, Integer> coll = new MyHashMap<>();
        CollidingKey k1 = new CollidingKey(1);
        CollidingKey k2 = new CollidingKey(2);
        CollidingKey k3 = new CollidingKey(3);

        coll.put(k1, 10);
        coll.put(k2, 20);
        coll.put(k3, 30);

        expect(coll.get(k1) == 10, "k1 найден в цепочке");
        expect(coll.get(k2) == 20, "k2 найден в цепочке");
        expect(coll.get(k3) == 30, "k3 найден в цепочке");
        expect(coll.remove(k2) == 20, "удаление из середины цепочки");
        expect(coll.get(k1) == 10, "k1 цел после удаления k2");
        expect(coll.get(k3) == 30, "k3 цел после удаления k2");
        expect(coll.get(k2) == null, "k2 удалён");

        MyHashMap<String, Integer> big = new MyHashMap<>();
        for (int i = 0; i < 1000; i++) big.put("k" + i, i);

        expect(big.size() == 1000, "1000 элементов добавлено");

        boolean allFound = true;
        for (int i = 0; i < 1000; i++) {
            if (big.get("k" + i) != i) { allFound = false; break; }
        }
        expect(allFound, "все 1000 элементов найдены после ресайза");

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    private static void expect(boolean ok, String message) {
        if (ok) {
            passed++;
            System.out.println("✅ " + message);
        } else {
            failed++;
            System.err.println("❌ " + message);
        }
    }

    record CollidingKey(int id) {
        @Override public int hashCode() { return 1; }
    }
}