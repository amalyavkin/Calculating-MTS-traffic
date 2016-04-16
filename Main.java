import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by amalyavkin on 16.04.16.
 */
public class Main {
    private static final int AM = 0;
    private static final int PM = 1;

    public static void printUsedTraffic(String path) {
        try {
            File info = new File(path);
            Map<Date, Double> history = new TreeMap<>();
            DateFormat dateTimeFormatter = new SimpleDateFormat();
            DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(info);
            NodeList items = document.getElementsByTagName("i");

            Double trafficKbDay = 0.0;
            Double trafficKbNight = 0.0;
            for (int i = 0; i < items.getLength(); i++) {
                Node item = items.item(i);

                String itemDate = String.valueOf(item.getAttributes().getNamedItem("d"));
                itemDate = itemDate.substring(3, itemDate.length() - 9);

                Date date = dateFormatter.parse(itemDate);
                String duAttr = String.valueOf(item.getAttributes().getNamedItem("du"));
                duAttr = duAttr.substring(4, duAttr.length() - 3);

                Double traffic = history.get(date);
                if (traffic == null) {
                    traffic = 0.0;
                }
                traffic += Double.valueOf(duAttr);
                history.put(date, traffic);

                String dAttr = String.valueOf(item.getAttributes().getNamedItem("d"));
                dAttr = dAttr.substring(3, dAttr.length() - 1);
                Date dateTime = dateTimeFormatter.parse(dAttr);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(dateTime);

                int hours = calendar.get(Calendar.HOUR);
                int partOfDay = calendar.get(Calendar.AM_PM);

                if (hours > 7 && partOfDay == AM || hours >= 0 && partOfDay == PM) {
                    trafficKbDay += Double.valueOf(duAttr);
                } else {
                    trafficKbNight += Double.valueOf(duAttr);
                }

            }

            for (Map.Entry<Date, Double> entry : history.entrySet()) {
                Double gbDouble = entry.getValue() / 1024 / 1024;
                Integer gb = gbDouble.intValue();
                Double mbs = gbDouble - gb;
                System.out.println(dateFormatter.format(entry.getKey()) + " потрачено :" + gb + "Gb " + new BigDecimal(mbs * 1024).setScale(0, BigDecimal.ROUND_UP) + "Mb");

            }

            System.out.println("===============================================================");
            System.out.println("Потрачено гигабайт днем: " + new BigDecimal(trafficKbDay / 1024 / 1024).setScale(1, BigDecimal.ROUND_UP) + "Gb");
            System.out.println("Потрачено гигабайт ночью: " + new BigDecimal(trafficKbNight / 1024 / 1024).setScale(1, BigDecimal.ROUND_UP) + "Gb");
            System.out.println("Потрачено гигабайт всего: " + new BigDecimal((trafficKbDay + trafficKbNight) / 1024 / 1024).setScale(1, BigDecimal.ROUND_UP) + "Gb");
        } catch (Exception e) {
            System.out.println("Операция неудалась, скорее всего файл поврежден: " + e.toString());
        }
    }
    public static void main(String[] args) {
        if (args.length == 1) {
            printUsedTraffic(args[0]);
        } else {
            System.out.println("Не указан путь");
        }

    }
}
