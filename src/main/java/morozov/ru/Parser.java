package morozov.ru;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * void writeItUp(String outputFile, int count) - главный метод парсинга\записи
 * List<String> getProducts(int count) - список нужного количества товаров.
 * String getPartOfProducts(int limit, int offset) - список товаров за лдин запрос.
 * String parsingOSingleString(String input) - парсинг строки, полученной из метода выше.
 * String getFields(JSONArray jsonArray, List<String> keys) - выдёргивание значений по ключам.
 * runOverFields(StringBuilder builder, JSONObject o, List<String> keys) - что б избавиться от повторяющегося кода.
 */
public class Parser {

    private static final Logger LOG = Logger.getLogger("Parser");

    private static final String ROOT_KEY = "results";
    private static final String TRACE_KEY = "trace";
    private static final String ALL_KEY = "all";
    /**
     * Ключи, содержащиеся в разбираемом JSON
     */
    private final List<String> generalKeyList = new ArrayList<>(Arrays.asList(
            "phase", "oriMaxPrice", "productId", "discount",
            "itemEvalTotalNum", "gmtCreate", "soldout", "promotionId",
            "productTitle", "productDetailUrl", "sellerId", "productImage",
            "oriMinPrice", "minPrice", "totalStock", "startTime",
            "orders", "endTime", "maxPrice", "productPositiveRate",
            "productAverageStar", "stock", "totalTranpro3"
    ));
    private final List<String> traceKeyList = new ArrayList<>(Arrays.asList("pvid", "scm-cnt", "gps-id"));

    /**
     * Метод, возвращающий готовую для записи строку-результат похода на Aliexpress.
     *
     * @param count
     * @throws IOException
     * @throws ParseException
     */
    public String getReadyStringForWriting(int count) throws IOException, ParseException {
        LOG.info(String.format("> > > Entered in getReadyStringForWriting( %d )", count));
        List<String> forParsing = this.getProducts(count);
        StringBuilder result = new StringBuilder();
        for (String s : forParsing) {
            result.append(this.parsingOSingleString(s));
        }
        LOG.info(String.format("> > > Left getReadyStringForWriting( %d )", count));
        return result.toString();
    }

    /**
     * Т.к. по заданию нужно распарсить 100 товаров-
     * введён этот метод(позволяет распарсить и другое количество,
     * но туит нет проверки на кратность шагу,
     * так что в итоге может достать меньше товаров.)
     *
     * @param count
     * @return
     * @throws IOException
     */
    private List<String> getProducts(int count) throws IOException {
        List<String> result = new ArrayList<>();
        String buffer = null;
        int border = count;
        //Количество товаров для парсинга не меньше 10-ти
        if (count < 10) {
            border = 10;
        }
        for (int i = 10; i <= border; i += 10) {
            result.add(this.getPartOfProducts(10, i));
        }
        return result;
    }

    /**
     * Метод, получающий json от Aliexpress
     * и перегоняющий его в строку.
     *
     * @param limit  - количество товаров во входящем json
     * @param offset - "страница", которая запрашивается
     * @return
     * @throws IOException
     */
    private String getPartOfProducts(int limit, int offset) throws IOException {
        StringBuilder builder = new StringBuilder();
        //URL не доступен напрямую- надо смотреть в отладчике.
        builder.append("https://gpsfront.aliexpress.com/getRecommendingResults.do")
                .append("?widget_id=5547572&platform=pc")
                .append("&limit=")
                .append(limit)
                .append("&offset=")
                .append(offset)
                .append("&phase=1")
                .append("&productIds2Top=&postback=4afd0300-e0ec-4fc9-b3af-bebd67792ac8&_=1605781948043");
        URL url = new URL(builder.toString());
        URLConnection conn = url.openConnection();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine = null;
        builder = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            builder.append(inputLine);
        }
        br.close();
        return builder.toString();
    }

    /**
     * Непосредственно сам парсинг JSON строки в обычную "человеческую" строку.
     *
     * @param input
     * @return
     * @throws ParseException
     */
    private String parsingOSingleString(String input) throws ParseException {
        StringBuilder result = new StringBuilder();
        JSONObject jsonObject = (JSONObject) JSONValue.parseWithException(input);
        JSONArray jsonArray = (JSONArray) jsonObject.get(ROOT_KEY);
        result.append(getFields(jsonArray));
        return result.toString();
    }

    /**
     * Выдёргивает из JSONArray значения полей.
     *
     * @param jsonArray
     * @return
     */
    private String getFields(JSONArray jsonArray) {
        StringBuilder result = new StringBuilder();
        jsonArray.forEach(x -> {
            JSONObject o = (JSONObject) x;
            result.append(" * * *\n");
            this.runOverFields(result, o, generalKeyList);
            //Просто с (JSONObject) o.get(TRACE_KEY) работать не хочет.
            JSONObject trace = (JSONObject) JSONValue.parse(String.valueOf(o.get(TRACE_KEY)));
            JSONObject all = (JSONObject) JSONValue.parse(String.valueOf(trace.get(ALL_KEY)));
            result.append("trace:\n");
            this.runOverFields(result, trace, traceKeyList);
            result.append("all:\n");
            this.runOverFields(result, all, traceKeyList);
        });
        return result.toString();
    }

    /**
     * Используя списки ключей записывает значения из JSONObject.
     *
     * @param builder
     * @param o
     * @param keys
     */
    private void runOverFields(StringBuilder builder, JSONObject o, List<String> keys) {
        for (String k : keys) {
            builder
                    .append(k)
                    .append(" : ")
                    .append(o.get(k))
                    .append("\n");
        }
    }
}