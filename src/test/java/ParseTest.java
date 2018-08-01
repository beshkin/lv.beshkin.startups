import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;

public class ParseTest {
    @Test
    public void AngelLsitParseTest() {
        AngelList angelList = new AngelList();
        try {
            angelList.parse();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}
