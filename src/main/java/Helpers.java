import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {
    static String BASE_PATH = "/home/misha/";

    public static Document getDocument(String urlToRssFeed) {
        if (urlToRssFeed.equals("")) {
            return null;
        }
        System.out.println(urlToRssFeed);
        Document doc = null;
        try {
            doc = Jsoup.connect(urlToRssFeed)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .ignoreContentType(true)
                    .validateTLSCertificates(false)
                    .followRedirects(true)
                    .timeout(30000)
                    .get();

        } catch (IOException e) {
            System.out.println(e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
        }
        return doc;
    }

    public static Document postDocument(String urlToRssFeed, Map<String, String> options) {
        System.out.println(urlToRssFeed);
        Document doc = null;
        try {
            Connection connection = Jsoup.connect(urlToRssFeed)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .timeout(30000);
            Iterator it = options.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
                connection.data((String) pair.getKey(), (String) pair.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
            doc = connection.post();

        } catch (IOException e) {
            System.out.println("fail: " + e.getMessage());  //To change body of catch statement use File | Settings | File Templates.
        }
        return doc;
    }

    public static String getLastPart(String url) {
        Pattern pattern = Pattern.compile(".+/(.+/.+)/?$");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static String findEmails(Document document) {
        String emailsStr = "";
        if (document != null) {
            Elements emails = document.select("a[href^=mailto]");
            Set<String> emailsAr = new HashSet<>();
            for (int i = 0; i < emails.size(); i++) {
                emailsAr.add(emails.get(i).attr("href").replace("mailto:", ""));
            }
            emailsStr = String.join(",", emailsAr);
        }
        return emailsStr;
    }

    public static void saveDocument(String text, String source) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(BASE_PATH + source + ".csv"), "utf-8"))) {
            writer.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
