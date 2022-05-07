package weather;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class WeatherIBM866 {
    public static void main(String[] args) throws IOException {
        System.out.println("\n" + "...Weather by SINOPTIK.UA...");

        makeRequest();
    }
    private static void makeRequest() throws IOException {
        System.out.println("""
                Для поиска введите название города:
                (для выхода нажмите - 0)""");
        Scanner sc = new Scanner(System.in, "IBM866");

        String city;
        city = sc.nextLine().toLowerCase(Locale.ROOT);

        String url = "https://sinoptik.ua/погода-" + city + "/10-дней";

        String[] userAgents = {"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36"};
        String userAgent = userAgents[new Random().nextInt(3)];

        System.out.println("Подключение к странице..." + url + "\n" +
                "С помощью User-Agent: " + userAgent);
        System.out.println("Идет обработка информации...");

        tryConnection(url, userAgent);
    }
    private static void tryConnection(String http, String userAgent) throws IOException {
        Connection.Response response = Jsoup.connect(http).userAgent(userAgent).
                timeout(10000).ignoreHttpErrors(true).execute();
        int responseCode = response.statusCode();

        if (responseCode == 200) {
            getWeatherSimple(http, userAgent);
        } else if (responseCode >=400 & responseCode <500) {
            System.out.println("Ошибка доступа №"+ responseCode + "." + "\n"
                    + "Страница повреждена либо не найдена..." + "\n"
                    + "Проверьте правильность написания указанного города.");
            System.out.println();
            makeRequest();
        } else if (responseCode>= 500) {
            System.out.println("Ошибка сервера №" + responseCode + "." + "\n"+ "Попробуйте позже...");
        }
    }
    private static void getWeatherSimple(String http, String userAgent) throws IOException {
        Document doc = Jsoup.connect(http).userAgent(userAgent).get();
        System.out.println("Вывод обобщённой информации...");

        Elements links = doc.getElementsByClass("tabs");
        ArrayList<String> linksAr = getLinksArray(links);

        Elements cities = doc.getElementsByAttributeValue("property", "og:title");
        String city = cities.attr("content");
        System.out.println("\n" + "Город: " + city);

        Elements regions = doc.getElementsByClass("currentRegion");
        String region = regions.text();
        System.out.println("Регион: " + region + "\n");

        for (int i = 1; i<=10; i++) {
            System.out.println("#" + i);
            Element weekInfo =  doc.getElementById("bd" + i);
            String dayInfo = Objects.requireNonNull(weekInfo).tagName("p").text();
            System.out.println(dayInfo);

            Elements types = weekInfo.getElementsByTag("div");
            String type = types.attr("title");
            System.out.println(type);

            System.out.println("-----------------------------------------");
        }
        getQuestion(linksAr, userAgent);
    }
    private static void getQuestion(ArrayList<String> linksAr, String userAgent) throws IOException {
        System.out.println("""
                Если хотите получить детальную информацию, введите порядковый номер выбранного дня.
                Для завершения нажмите - 0.
                Для смены города нажмите - 11.""");
        Scanner sc = new Scanner(System.in);
        int day = sc.nextInt();
        if (day > 0 & day < 11) {
            getWeatherDetails(linksAr, userAgent, day);
        } else if (day == 11) {
            makeRequest();
        } else if (day > 11) {
            System.out.println("Недопустимая команда, попробуйте еще раз...");
            getQuestion(linksAr, userAgent);
        } else {
            System.out.println("Работы программы завершена.");
        }
    }
    private static void getWeatherDetails(ArrayList<String> linksAr, String userAgent, int day) throws IOException {
        Document doc = Jsoup.connect(linksAr.get(day-1)).userAgent(userAgent).get();

        Element tab = doc.getElementById("bd" + day + "c");
        Elements titles = Objects.requireNonNull(tab).getElementsByClass("titles");
        Elements weatherDetails = Objects.requireNonNull(tab).getElementsByClass("weatherDetails").tagName("thead").tagName("td");
        Element imgBlock = tab.getElementsByClass("imgBlock").first();

        ArrayList<String> titlesAr = getTitlesArray(titles);
        ArrayList<String> partDays = getPartDaysArray(weatherDetails);
        ArrayList<String> times = getTimesArray(weatherDetails);
        ArrayList<String> types = getTypesArray(weatherDetails);
        ArrayList<String> temperatures = getTemperaturesArray(weatherDetails);
        ArrayList<String> temperatureSens = getTemperatureSensArray(weatherDetails);
        ArrayList<String> pressures = getPressuresArray(weatherDetails);
        ArrayList<String> wet = getWetArray(weatherDetails);
        ArrayList<String> wind = getWindArray(weatherDetails);
        ArrayList<String> probability = getProbabilityArray(weatherDetails);
        String wDescription = getWDescriptionStr(tab);
        String oDescription = getODescriptionStr(tab);
        String infoDayLight = tab.getElementsByClass("infoDaylight").text();
        String infoHistory = Objects.requireNonNull(tab.getElementsByClass("oDescription clearfix").first()).getElementsByClass("lside").text();
        String today;
        String todayTemp = "";
        String todayWater = "";
        String todayExplain = "";
        if (day == 1) {
            today = Objects.requireNonNull(imgBlock).getElementsByClass("today-time").text();
            todayTemp = Objects.requireNonNull(imgBlock).getElementsByClass("today-temp").text();
            todayWater = Objects.requireNonNull(imgBlock).getElementsByClass("today-water").text();
            todayExplain = Objects.requireNonNull(imgBlock.getElementsByAttribute("alt").first()).attr("alt");
        } else {
            today = tab.getElementsByClass("calendBlock").text();
        }

        if (day == 1) {
            printTableFirst(today, todayTemp, todayWater, todayExplain, infoDayLight,
                    titlesAr, partDays, times,temperatures,temperatureSens, pressures,
                    wet, wind, probability, infoHistory, wDescription, oDescription);
        } else if (day == 2) {
            printTableSecond(today, infoDayLight, titlesAr, partDays, times,temperatures,temperatureSens,
                    pressures, wet, wind, probability, infoHistory, wDescription, oDescription);
        } else {
            printTableOther(today, infoDayLight, titlesAr, partDays, times,temperatures,temperatureSens,
                    pressures, wet, wind, probability, infoHistory, wDescription, oDescription);
        }

        getQuestion(linksAr, userAgent);
    }
    private static ArrayList<String> getLinksArray(Elements links) {
        ArrayList<String> linksAr = new ArrayList<>();
        Elements ls = Objects.requireNonNull(links.first()).getElementsByAttribute("data-link");
        for (Element l : ls) {
            linksAr.add("https:" + l.attr("data-link"));
        }
        return linksAr;
    }
    private static ArrayList<String> getTitlesArray(Elements titles) {
        ArrayList<String> titlesAr = new ArrayList<>();
        for (int t = 0; t <= 5; t++) {
            Element title = Objects.requireNonNull(titles.tagName("p").first()).child(t);
            titlesAr.add(title.text());
        }
        return titlesAr;
    }
    private static ArrayList<String> getPartDaysArray(Elements weatherDetails) {
        ArrayList<String> partDays = new ArrayList<>();
        int sizePartDay = Objects.requireNonNull(weatherDetails.first()).child(0).tagName("td").child(0).tagName("td").childrenSize();
        for (int pd = 0; pd < sizePartDay; pd++) {
            partDays.add(Objects.requireNonNull(weatherDetails.first()).child(0).tagName("td").child(0).tagName("td").child(pd).text());
        }
        return partDays;
    }
    private static ArrayList<String> getTimesArray(Elements weatherDetails) {
        ArrayList<String> times = new ArrayList<>();
        int sizeTime = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(0).tagName("td").childrenSize();
        for (int t = 0; t < sizeTime; t++) {
            times.add(Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(0).tagName("td").child(t).text());
        }
        return times;
    }
    private static ArrayList<String> getTypesArray(Elements weatherDetails) {
        ArrayList<String> types = new ArrayList<>();
        Elements w = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(1).getElementsByAttribute("title");
        for (Element el : w) {
            types.add(el.attr("title"));
        }
        return types;
    }
    private static ArrayList<String> getTemperaturesArray(Elements weatherDetails) {
        ArrayList<String> temperatures = new ArrayList<>();
        int sizeTemperatures = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(2).tagName("td").childrenSize();
        for (int t = 0; t < sizeTemperatures; t++) {
            temperatures.add(Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(2).tagName("td").child(t).text());
        }
        return temperatures;
    }
    private static ArrayList<String> getTemperatureSensArray(Elements weatherDetails){
        ArrayList<String> temperatureSens = new ArrayList<>();
        int sizeTemperatureSens = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(3).tagName("td").childrenSize();
        for (int ts = 0; ts < sizeTemperatureSens; ts++) {
            temperatureSens.add(Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(3).tagName("td").child(ts).text());
        }
        return temperatureSens;
    }
    private static ArrayList<String> getPressuresArray(Elements weatherDetails){
        ArrayList<String> pressures = new ArrayList<>();
        int sizePressures = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(4).tagName("td").childrenSize();
        for (int p = 0; p <sizePressures; p++) {
            pressures.add(Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(4).tagName("td").child(p).text());
        }
        return pressures;
    }
    private static ArrayList<String> getWetArray(Elements weatherDetails) {
        ArrayList<String> wet = new ArrayList<>();
        int sizePressures = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(5).tagName("td").childrenSize();
        for (int p = 0; p <sizePressures; p++) {
            wet.add(Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(5).tagName("td").child(p).text());
        }
        return wet;
    }
    private static ArrayList<String> getWindArray(Elements weatherDetails) {
        ArrayList<String> wind = new ArrayList<>();
        Elements winds = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(6).getElementsByAttribute("data-tooltip");
        for (Element elm : winds) {
            wind.add(elm.attr("data-tooltip"));
        }
        return wind;
    }
    private static ArrayList<String> getProbabilityArray(Elements weatherDetails) {
        ArrayList<String> probability = new ArrayList<>();
        int sizePressures = Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(7).tagName("td").childrenSize();
        for (int p = 0; p <sizePressures; p++) {
            probability.add(Objects.requireNonNull(weatherDetails.first()).child(1).tagName("td").child(7).tagName("td").child(p).text());
        }
        return probability;
    }
    private static String getWDescriptionStr(Element tab) {
        char[] wdChar = Objects.requireNonNull(tab.getElementsByClass("wDescription clearfix").first()).getElementsByClass("description").text().toCharArray();
        int count = 0;
        for (int i = 0; i < wdChar.length; i++) {
            if (wdChar[i] == ' ') count++;
            if (count > 10 & wdChar[i] == ' ') {
                wdChar[i] = '\n';
                count = 0;
            }
        }
        return new String(wdChar);
    }
    private static String getODescriptionStr(Element tab) {
        char[] odChar = Objects.requireNonNull(tab.getElementsByClass("oDescription clearfix").first()).getElementsByClass("description").text().toCharArray();
        int count = 0;
        for (int i = 0; i < odChar.length; i++) {
            if (odChar[i] == ' ') count++;
            if (count > 10 & odChar[i] == ' ') {
                odChar[i] = '\n';
                count = 0;
            }
        }
        return new String(odChar);
    }
    private static void printTableFirst(String today, String todayTemp, String todayWater, String todayExplain,
                                        String infoDayLight, ArrayList<String> titlesAr,
                                        ArrayList<String> partDays, ArrayList<String> times, ArrayList<String> temperatures,
                                        ArrayList<String> temperatureSens, ArrayList<String> pressures,
                                        ArrayList<String> wet, ArrayList<String> wind, ArrayList<String> probability,
                                        String infoHistory, String wDescription, String oDescription) {

        System.out.println();
        System.out.printf("%5s%n", today + ":");
        System.out.printf("%5s%n", todayTemp);
        System.out.printf("%5s%n", todayExplain);
        System.out.printf("%5s%n", infoDayLight);
        System.out.printf("%5s%n", todayWater);
        System.out.printf("%46s%-57s%-1s%n","", partDays.get(0), partDays.get(1));
        System.out.printf("%32s%-28s%-28s%-28s%-28s%n", "", times.get(0), times.get(1), times.get(2), times.get(3));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(0), "", temperatures.get(0), temperatures.get(1), temperatures.get(2), temperatures.get(3));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(1), "", temperatureSens.get(0), temperatureSens.get(1), temperatureSens.get(2), temperatureSens.get(3));
        System.out.printf("%s%-21s%-28s%-28s%-28s%-28s%n", titlesAr.get(2), "", pressures.get(0), pressures.get(1), pressures.get(2), pressures.get(3));
        System.out.printf("%s%-22s%-28s%-28s%-28s%-28s%n", titlesAr.get(3), "", wet.get(0), wet.get(1), wet.get(2), wet.get(3));
        System.out.printf("%s%-15s%-28s%-28s%-28s%-28s%n", titlesAr.get(4), "", wind.get(0), wind.get(1), wind.get(2), wind.get(3));
        System.out.printf("%s%-13s%-28s%-28s%-28s%-28s%n", titlesAr.get(5), "", probability.get(0), probability.get(1), probability.get(2), probability.get(3));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%46s%-57s%-1s%n","", partDays.get(2), partDays.get(3));
        System.out.printf("%32s%-28s%-28s%-28s%-28s%n", "", times.get(4), times.get(5), times.get(6), times.get(7));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(0), "", temperatures.get(4), temperatures.get(5), temperatures.get(6), temperatures.get(7));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(1), "", temperatureSens.get(4), temperatureSens.get(5), temperatureSens.get(6), temperatureSens.get(7));
        System.out.printf("%s%-21s%-28s%-28s%-28s%-28s%n", titlesAr.get(2), "", pressures.get(4), pressures.get(5), pressures.get(6), pressures.get(7));
        System.out.printf("%s%-22s%-28s%-28s%-28s%-28s%n", titlesAr.get(3), "", wet.get(4), wet.get(5), wet.get(6), wet.get(7));
        System.out.printf("%s%-15s%-28s%-28s%-28s%-28s%n", titlesAr.get(4), "", wind.get(4), wind.get(5), wind.get(6), wind.get(7));
        System.out.printf("%s%-13s%-28s%-28s%-28s%-28s%n", titlesAr.get(5), "", probability.get(4), probability.get(5), probability.get(6), probability.get(7));
        System.out.printf("\n" + ' ' + infoHistory + "\n");
        System.out.println("\n" + ' ' + wDescription + "\n");
        System.out.println(' ' + oDescription + "\n");
        System.out.println("**************************************************************************************************************************************");
    }
    private static void printTableSecond(String today, String infoDayLight, ArrayList<String> titlesAr, ArrayList<String> partDays,
                                         ArrayList<String> times, ArrayList<String> temperatures, ArrayList<String> temperatureSens,
                                         ArrayList<String> pressures, ArrayList<String> wet, ArrayList<String> wind,
                                         ArrayList<String> probability, String infoHistory, String wDescription, String oDescription){
        System.out.println();
        System.out.printf("%5s%n", today);
        System.out.printf("%5s%n", infoDayLight + ".");
        System.out.printf("%46s%-57s%-1s%n","", partDays.get(0), partDays.get(1));
        System.out.printf("%32s%-28s%-28s%-28s%-28s%n", "", times.get(0), times.get(1), times.get(2), times.get(3));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(0), "", temperatures.get(0), temperatures.get(1), temperatures.get(2), temperatures.get(3));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(1), "", temperatureSens.get(0), temperatureSens.get(1), temperatureSens.get(2), temperatureSens.get(3));
        System.out.printf("%s%-21s%-28s%-28s%-28s%-28s%n", titlesAr.get(2), "", pressures.get(0), pressures.get(1), pressures.get(2), pressures.get(3));
        System.out.printf("%s%-22s%-28s%-28s%-28s%-28s%n", titlesAr.get(3), "", wet.get(0), wet.get(1), wet.get(2), wet.get(3));
        System.out.printf("%s%-15s%-28s%-28s%-28s%-28s%n", titlesAr.get(4), "", wind.get(0), wind.get(1), wind.get(2), wind.get(3));
        System.out.printf("%s%-13s%-28s%-28s%-28s%-28s%n", titlesAr.get(5), "", probability.get(0), probability.get(1), probability.get(2), probability.get(3));
        System.out.println("-----------------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%46s%-57s%-1s%n","", partDays.get(2), partDays.get(3));
        System.out.printf("%32s%-28s%-28s%-28s%-28s%n", "", times.get(4), times.get(5), times.get(6), times.get(7));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(0), "", temperatures.get(4), temperatures.get(5), temperatures.get(6), temperatures.get(7));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(1), "", temperatureSens.get(4), temperatureSens.get(5), temperatureSens.get(6), temperatureSens.get(7));
        System.out.printf("%s%-21s%-28s%-28s%-28s%-28s%n", titlesAr.get(2), "", pressures.get(4), pressures.get(5), pressures.get(6), pressures.get(7));
        System.out.printf("%s%-22s%-28s%-28s%-28s%-28s%n", titlesAr.get(3), "", wet.get(4), wet.get(5), wet.get(6), wet.get(7));
        System.out.printf("%s%-15s%-28s%-28s%-28s%-28s%n", titlesAr.get(4), "", wind.get(4), wind.get(5), wind.get(6), wind.get(7));
        System.out.printf("%s%-13s%-28s%-28s%-28s%-28s%n", titlesAr.get(5), "", probability.get(4), probability.get(5), probability.get(6), probability.get(7));
        System.out.printf("\n" + ' ' + infoHistory + "\n");
        System.out.println("\n" + ' ' + wDescription + "\n");
        System.out.println(' ' + oDescription + "\n");
        System.out.println("**************************************************************************************************************************************");
    }
    private static void printTableOther(String today, String infoDayLight, ArrayList<String> titlesAr, ArrayList<String> partDays,
                                        ArrayList<String> times, ArrayList<String> temperatures, ArrayList<String> temperatureSens,
                                        ArrayList<String> pressures, ArrayList<String> wet, ArrayList<String> wind,
                                        ArrayList<String> probability, String infoHistory, String wDescription, String oDescription) {
        System.out.println();
        System.out.printf("%5s%n", today);
        System.out.printf("%5s%n", infoDayLight + ".");
        System.out.printf("%33s%-28s%-28s%-27s%-28s%n", "", partDays.get(0), partDays.get(1), partDays.get(2), partDays.get(3));
        System.out.printf("%32s%-28s%-28s%-28s%-28s%n", "", times.get(0), times.get(1), times.get(2), times.get(3));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(0), "", temperatures.get(0), temperatures.get(1), temperatures.get(2), temperatures.get(3));
        System.out.printf("%s%-18s%-28s%-28s%-28s%-28s%n", titlesAr.get(1), "", temperatureSens.get(0), temperatureSens.get(1), temperatureSens.get(2), temperatureSens.get(3));
        System.out.printf("%s%-21s%-28s%-28s%-28s%-28s%n", titlesAr.get(2), "", pressures.get(0), pressures.get(1), pressures.get(2), pressures.get(3));
        System.out.printf("%s%-22s%-28s%-28s%-28s%-28s%n", titlesAr.get(3), "", wet.get(0), wet.get(1), wet.get(2), wet.get(3));
        System.out.printf("%s%-15s%-28s%-28s%-28s%-28s%n", titlesAr.get(4), "", wind.get(0), wind.get(1), wind.get(2), wind.get(3));
        System.out.printf("%s%-13s%-28s%-28s%-28s%-28s%n", titlesAr.get(5), "", probability.get(0), probability.get(1), probability.get(2), probability.get(3));
        System.out.printf("\n" + ' ' + infoHistory + "\n");
        System.out.println("\n" + ' ' + wDescription + "\n");
        System.out.println(' ' + oDescription + "\n");
        System.out.println("**************************************************************************************************************************************");
    }
}