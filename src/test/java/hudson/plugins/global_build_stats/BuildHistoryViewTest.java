package hudson.plugins.global_build_stats;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildHistoryViewTest {

    @Test
    void populatesNativeDateTimeInputsFromEpochParameters(JenkinsRule r) throws Exception {
        JenkinsRule.WebClient webClient = r.createWebClient();
        webClient.setJavaScriptEnabled(true);
        HtmlPage page = webClient.goTo("plugin/global-build-stats/buildHistory?start=1704067200000&end=1704153600000"
                + "&jobFilter=ALL&nodeFilter=ALL&launcherFilter=ALL");

        HtmlInput startPicker = page.getHtmlElementById("timeStartPicker");
        HtmlInput endPicker = page.getHtmlElementById("timeEndPicker");

        assertEquals("datetime-local", startPicker.getTypeAttribute());
        assertEquals("datetime-local", endPicker.getTypeAttribute());

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        ZoneId browserTimeZone =
                webClient.getBrowserVersion().getSystemTimezone().toZoneId();
        assertEquals(
                inputFormat.format(Instant.ofEpochMilli(1704067200000L).atZone(browserTimeZone)),
                startPicker.getValue());
        assertEquals(
                inputFormat.format(Instant.ofEpochMilli(1704153600000L).atZone(browserTimeZone)), endPicker.getValue());
    }
}
