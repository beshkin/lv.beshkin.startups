import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class AngelList {
    public void parse() throws JSONException, IOException {
        Document doc = Jsoup.connect("https://angel.co/company_filters/search_data")
                .header("X-Requested-With", "XMLHttpRequest")
                .ignoreContentType(true)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("https://angel.co/companies")
                .data("filter_data[raised][min]", "0")
                .data("filter_data[raised][max]", "0")
                .data("filter_data[locations][]", "1830-Poland")
                .data("filter_data[locations][]", "1741-Lithuania")
                .data("filter_data[locations][]", "2337-Belarus")
                .data("sort", "signal")
                .post();

        JSONObject result = null;
        try {
            result = new JSONObject(doc.text());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (result != null) {
            JSONArray ids = result.getJSONArray("ids");
            String[] idsAr = new String[ids.length()];
            for (int i = 0; i < ids.length(); i++) {
                idsAr[i] = ids.getString(i);
            }
            String url = String.format("%s&total=%s&page=%s&sort=signal&new=false&hexdigest=%s",
                    String.join("&ids%5B%5D=", idsAr),
                    result.getString("total"),
                    result.getString("page"),
                    result.getString("hexdigest")
            );
            doc = Jsoup.connect("https://angel.co/companies/startups?ids%5B%5D=" + url)
                    .header("X-Requested-With", "XMLHttpRequest")
                    .ignoreContentType(true)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("https://angel.co/companies")
                    .get();
            
            JSONObject companiesObj = new JSONObject(doc.text());
            if (companiesObj.has("html")) {
                String htmlObj = companiesObj.getString("html");
                Document document = Jsoup.parse(StringEscapeUtils.unescapeHtml4(htmlObj));
                if (document != null) {
                    StringBuilder text = new StringBuilder("Name;Desc;Joined;Location;Market;Url;Size;Emails\n");
                    Elements companies = document.select(".results div[data-_tn=companies/row]");
                    for (Element company : companies) {
                        String companyAngelUrl = company.select(".name a").attr("href");
                        String companyName = company.select(".name").text();
                        String companyDesc = company.select(".pitch").text();
                        String companyJoined = company.select(".joined .value").text();
                        String companyLocation = company.select(".location .value").text();
                        String companyMarket = company.select(".market .value").text();
                        String companyUrl = company.select(".website a").attr("href").replaceAll("(.*)/?$", "$1");
                        String companySize = company.select(".company_size .value").html();

                        if (companyName.equals("")) {
                            continue;
                        }
                        String emailsStr = "";
                        document = Helpers.getDocument(companyUrl);
                        if (document != null) {
                            emailsStr = Helpers.findEmails(document);
                            if (emailsStr.equals("")) {
                                Element contactPage = document.selectFirst("a:matches(.*Contact.*)");
                                if (contactPage != null) {
                                    String contactUrl = contactPage.attr("href");
                                    contactUrl = contactUrl.matches("^http.*") ? contactUrl : companyUrl + "/" + contactUrl;
                                    document = Helpers.getDocument(contactUrl);
                                    emailsStr = Helpers.findEmails(document);
                                }
                            }
                        }

                        text.append(companyName).append(";").append(companyDesc).append(";").append(companyJoined).append(";").append(companyLocation).append(";").append(companyMarket).append(";").append(companyUrl).append(";").append(companySize).append(";").append(emailsStr).append("\n");
                    }
                    Helpers.saveDocument(text.toString(), "angellist");
                }

            }
        }
    }
}

