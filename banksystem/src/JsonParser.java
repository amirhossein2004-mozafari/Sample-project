import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
    // JSON Element Types ======================================================
    public static class JSONObject {
        private final Map<String, Object> map = new HashMap<>();

        public void put(String key, Object value) {
            map.put(key, value);
        }

        public Object get(String key) {
            return map.get(key);
        }

        public Map<String, Object> getMap() {
            return new HashMap<>(map);
        }
    }

    public static class JSONArray {
        private final List<Object> list = new ArrayList<>();

        public void add(Object value) {
            list.add(value);
        }

        public Object get(int index) {
            return list.get(index);
        }

        public List<Object> getList() {
            return new ArrayList<>(list);
        }
    }

    // Parser Core ============================================================
    private int index;
    private String json;

    public Object parse(String jsonString) throws JsonParseException {
        this.json = jsonString.trim();
        this.index = 0;
        return parseValue();
    }

    public Object parseFile(String filePath) throws IOException, JsonParseException {
        try {
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            String content = new String(fileBytes, StandardCharsets.UTF_8);
            return parse(content);
        } catch (IOException e) {
            throw new IOException("Error reading file: " + filePath, e);
        }
    }

    private Object parseValue() throws JsonParseException {
        skipWhitespace();
        if (index >= json.length()) {
            throw new JsonParseException("Unexpected end of input");
        }

        char c = json.charAt(index);
        if (c == '{') return parseObject();
        if (c == '[') return parseArray();
        if (c == '"') return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n') return parseNull();
        if (Character.isDigit(c) || c == '-' || c == '.') return parseNumber();

        throw new JsonParseException("Unexpected character: '" + c + "' at position " + index);
    }

    private JSONObject parseObject() throws JsonParseException {
        JSONObject obj = new JSONObject();
        index++; // Skip '{'

        while (true) {
            skipWhitespace();
            if (index >= json.length()) {
                throw new JsonParseException("Unterminated object");
            }

            if (currentChar() == '}') {
                index++;
                return obj;
            }

            String key = parseString();
            skipWhitespace();

            if (currentChar() != ':') {
                throw new JsonParseException("Expected ':' after key");
            }
            index++;

            Object value = parseValue();
            obj.put(key, value);

            skipWhitespace();
            if (index >= json.length()) {
                throw new JsonParseException("Unterminated object");
            }

            if (currentChar() == ',') {
                index++;
            } else if (currentChar() == '}') {
                index++;
                return obj;
            } else {
                throw new JsonParseException("Expected ',' or '}' after value");
            }
        }
    }

    private JSONArray parseArray() throws JsonParseException {
        JSONArray arr = new JSONArray();
        index++; // Skip '['

        while (true) {
            skipWhitespace();
            if (index >= json.length()) {
                throw new JsonParseException("Unterminated array");
            }

            if (currentChar() == ']') {
                index++;
                return arr;
            }

            Object value = parseValue();
            arr.add(value);

            skipWhitespace();
            if (index >= json.length()) {
                throw new JsonParseException("Unterminated array");
            }

            if (currentChar() == ',') {
                index++;
            } else if (currentChar() == ']') {
                index++;
                return arr;
            } else {
                throw new JsonParseException("Expected ',' or ']' after value");
            }
        }
    }

    private String parseString() throws JsonParseException {
        StringBuilder sb = new StringBuilder();
        index++; // Skip '"'

        while (index < json.length()) {
            char c = currentChar();

            if (c == '"') {
                index++;
                return sb.toString();
            }

            if (c == '\\') {
                index++;
                if (index >= json.length()) {
                    throw new JsonParseException("Unterminated escape sequence");
                }
                c = currentChar();
                switch (c) {
                    case '"':
                        sb.append('"');
                        break;
                    case '\\':
                        sb.append('\\');
                        break;
                    default:
                        throw new JsonParseException("Invalid escape sequence: \\" + c);
                }
            } else {
                sb.append(c);
            }

            index++;
        }

        throw new JsonParseException("Unterminated string");
    }

    private Object parseNumber() throws JsonParseException {
        int start = index;
        boolean isDouble = false;
        boolean hasExponent = false;

        while (index < json.length()) {
            char c = currentChar();

            if (c == '-' && index != start) {
                throw new JsonParseException("Invalid minus sign position");
            }

            if (c == '.') {
                if (isDouble || hasExponent) {
                    throw new JsonParseException("Invalid decimal point");
                }
                isDouble = true;
            }

            if (c == 'e' || c == 'E') {
                if (hasExponent) {
                    throw new JsonParseException("Multiple exponents");
                }
                hasExponent = true;
                isDouble = true;
            }

            if (!Character.isDigit(c) && c != '-' && c != '+' && c != '.' && c != 'e' && c != 'E') {
                break;
            }

            index++;
        }

        String numStr = json.substring(start, index);
        try {
            if (isDouble || hasExponent) {
                return Double.parseDouble(numStr);
            } else {
                return Long.parseLong(numStr);
            }
        } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid number format: " + numStr);
        }
    }

    private Boolean parseBoolean() throws JsonParseException {
        if (json.startsWith("true", index)) {
            index += 4;
            return true;
        } else if (json.startsWith("false", index)) {
            index += 5;
            return false;
        }
        throw new JsonParseException("Invalid boolean value");
    }

    private Object parseNull() throws JsonParseException {
        if (json.startsWith("null", index)) {
            index += 4;
            return null;
        }
        throw new JsonParseException("Invalid null value");
    }

    // Helper Methods =========================================================
    private char currentChar() {
        return json.charAt(index);
    }

    private void skipWhitespace() {
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
    }

    // Serialization ==========================================================
    public String stringify(Object value) throws JsonParseException {
        StringBuilder sb = new StringBuilder();
        serialize(value, sb);
        return sb.toString();
    }

    private void serialize(Object value, StringBuilder sb) throws JsonParseException {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof JSONObject) {
            serializeObject((JSONObject) value, sb);
        } else if (value instanceof JSONArray) {
            serializeArray((JSONArray) value, sb);
        } else if (value instanceof String) {
            serializeString((String) value, sb);
        } else if (value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Number) {
            serializeNumber((Number) value, sb);
        } else if (value instanceof Map) {
            serializeMap((Map<?, ?>) value, sb);
        } else if (value instanceof List) {
            serializeList((List<?>) value, sb);
        } else {
            throw new JsonParseException("Unsupported type: " + value.getClass().getName());
        }
    }

    private void serializeObject(JSONObject obj, StringBuilder sb) throws JsonParseException {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : obj.getMap().entrySet()) {
            if (!first) sb.append(',');
            serializeString(entry.getKey(), sb);
            sb.append(':');
            serialize(entry.getValue(), sb);
            first = false;
        }
        sb.append('}');
    }

    private void serializeArray(JSONArray arr, StringBuilder sb) throws JsonParseException {
        sb.append('[');
        boolean first = true;
        for (Object item : arr.getList()) {
            if (!first) sb.append(',');
            serialize(item, sb);
            first = false;
        }
        sb.append(']');
    }

    private void serializeString(String str, StringBuilder sb) {
        sb.append('"');
        for (char c : str.toCharArray()) {
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append('"');
    }

    private void serializeNumber(Number num, StringBuilder sb) {
        if (num instanceof Double || num instanceof Float) {
            sb.append(num.doubleValue());
        } else {
            sb.append(num.longValue());
        }
    }

    private void serializeMap(Map<?, ?> map, StringBuilder sb) throws JsonParseException {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            serializeString(entry.getKey().toString(), sb);
            sb.append(':');
            serialize(entry.getValue(), sb);
            first = false;
        }
        sb.append('}');
    }

    private void serializeList(List<?> list, StringBuilder sb) throws JsonParseException {
        sb.append('[');
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(',');
            serialize(item, sb);
            first = false;
        }
        sb.append(']');
    }

    // File Operations ========================================================
    public void writeToFile(String filePath, Object data) throws IOException {
        try {
            String jsonString = stringify(data);
            Files.write(
                    Paths.get(filePath),
                    jsonString.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (JsonParseException e) {
            throw new IOException("خطا در تولید JSON: " + e.getMessage(), e);
        }
    }

    // Exception ==============================================================
    public static class JsonParseException extends Exception {
        public JsonParseException(String message) {
            super(message);
        }
    }

    // User Data Handling =====================================================
    public List<User> loadUsers(String filePath) throws IOException, JsonParseException {
        List<User> users = new ArrayList<>();
        try {
            Object data = parseFile(filePath);
            if (data instanceof JSONArray) {
                for (Object userObj : ((JSONArray) data).getList()) {
                    if (userObj instanceof JSONObject) {
                        JSONObject userJson = (JSONObject) userObj;


                        String username = getStringSafe(userJson, "username");
                        String hashedPassword = getStringSafe(userJson, "hashedPassword");
                        String accountNumber = getStringSafe(userJson, "accountNumber");


                        User user = new User(username, "dummy");
                        user.setHashedPassword(hashedPassword);
                        user.setAccountNumber(accountNumber);

                        JSONArray accounts = (JSONArray) userJson.get("accounts");
                        if (accounts != null) {
                            for (Object accObj : accounts.getList()) {
                                if (accObj instanceof JSONObject) {
                                    JSONObject accJson = (JSONObject) accObj;
                                    Account account = loadAccount(accJson);
                                    user.getAccounts().clear();
                                    user.getAccounts().add(account);
                                }
                            }
                        }
                        users.add(user);
                    }
                }
            }
            return users;
        } catch (Exception e) {
            throw new JsonParseException("خطا در بارگذاری کاربران: " + e.getMessage());
        }
    }
    private String getStringSafe(JSONObject json, String key) {
        Object value = json.get(key);
        return (value != null) ? value.toString() : "";
    }
    private double getDoubleSafe(JSONObject json, String key) {
        Object value = json.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }


    private Account loadAccount(JSONObject accJson) {
        Account account = new Account();


        Object balanceObj = accJson.get("balance");
        if (balanceObj instanceof Number) {
            double balance = ((Number) balanceObj).doubleValue();
            account.deposit(balance, false);
        }


        JSONArray transactions = (JSONArray) accJson.get("transactions");
        if (transactions != null) {
            for (Object txnObj : transactions.getList()) {
                if (txnObj instanceof JSONObject) {
                    JSONObject txnJson = (JSONObject) txnObj;
                    Transaction transaction = parseTransaction(txnJson);
                    account.getTransactions().add(transaction);
                }
            }
        }
        return account;
    }
    private Transaction parseTransaction(JSONObject txnJson) {
        try {

            Transaction transaction = new Transaction(
                    Transaction.Type.valueOf(getStringSafe(txnJson, "type")),
                    getDoubleSafe(txnJson, "amount"),
                    getStringSafe(txnJson, "source"),
                    getStringSafe(txnJson, "destination")
            );


            String timestampStr = getStringSafe(txnJson, "timestamp");
            if (!timestampStr.isEmpty()) {
                LocalDateTime timestamp = LocalDateTime.parse(
                        timestampStr,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                );
                transaction.setTimestamp(timestamp);
            }

            return transaction;
        } catch (Exception e) {
            try {
                throw new JsonParseException("خطا در پردازش تراکنش: " + e.getMessage());
            } catch (JsonParseException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void saveUsers(List<User> users, String filePath) throws IOException {
        try {
            List<Map<String, Object>> usersData = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", user.getUsername());
                userData.put("hashedPassword", user.getHashedPassword());
                userData.put("accountNumber", user.getAccountNumber());

                List<Map<String, Object>> accountsData = new ArrayList<>();
                for (Account account : user.getAccounts()) {
                    Map<String, Object> accountData = new HashMap<>();
                    accountData.put("balance", account.getBalance());

                    List<Map<String, Object>> transactionsData = new ArrayList<>();
                    for (Transaction t : account.getTransactions()) {
                        Map<String, Object> txnData = new HashMap<>();
                        txnData.put("type", t.getType().name());
                        txnData.put("amount", t.getAmount());
                        txnData.put("source", t.getSource());
                        txnData.put("destination", t.getDestination());
                        txnData.put("timestamp", t.getFormattedTimestamp());
                        transactionsData.add(txnData);
                    }
                    accountData.put("transactions", transactionsData);
                    accountsData.add(accountData);
                }
                userData.put("accounts", accountsData);
                usersData.add(userData);
            }
            writeToFile(filePath, usersData);
        } catch (Exception e) {
            throw new IOException("خطا در تولید JSON");
        }
    }
}
