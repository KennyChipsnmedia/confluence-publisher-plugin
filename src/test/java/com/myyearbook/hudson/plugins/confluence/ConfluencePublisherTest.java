package com.myyearbook.hudson.plugins.confluence;

import com.sun.jndi.toolkit.url.Uri;
import hudson.model.BuildListener;
//import junit.framework.TestCase;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.plugins.confluence.soap.v1.RemoteException;
import jenkins.plugins.confluence.soap.v1.RemotePage;
import jenkins.plugins.confluence.soap.v1.RemotePageSummary;
import jenkins.plugins.confluence.soap.v1.RemoteSpace;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.*;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.Url;
import org.mockito.Mock;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.*;

import com.myyearbook.hudson.plugins.confluence.wiki.editors.MarkupEditor.TokenNotFoundException;
import com.myyearbook.hudson.plugins.confluence.wiki.generators.MarkupGenerator;
import org.mockito.Mockito;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import java.util.stream.Collectors;

public class ConfluencePublisherTest {
    private static Logger logger = Logger.getLogger(ConfluencePublisherTest.class);

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    @Before
    public void setup() {
        PrintStream ps = new PrintStream(new FileOutputStream(FileDescriptor.out));
        System.setOut(ps);

        logger.addAppender(new ConsoleAppender(new PatternLayout("%r [%t] %p %c %x - %m%n")));
    }

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();




    private RemotePage createPage(ConfluenceSession confluence, String spaceName, String pageName, long parentId) throws java.rmi.RemoteException {
        RemotePage newPage = new RemotePage();
        newPage.setTitle(pageName);
        newPage.setSpace(spaceName);
        newPage.setParentId(parentId);
        newPage.setContent("");
        return confluence.storePage(newPage);
    }

    @Test
    public void testPage() throws Exception {
        String spaceKey = "MAR";
        URL url = new URL("http://localhost:8090/");
        ConfluenceSite site = new ConfluenceSite(url, "admin", "!!KK1891kk");
        ConfluenceSession session = site.createSession();
        String pages = "TX/T2-2/T2-2-2";
        List<String> pageList = Arrays.stream(pages.split("/")).filter(s -> s.length() > 0).collect(Collectors.toList());

        RemoteSpace space = session.getSpace(spaceKey);
        long parentId = space.getHomePage();
        System.out.println("HomePage id:" + space.getHomePage());
        for(String page:  pageList) {

            try {
                RemotePageSummary summary = session.getPageSummary(spaceKey, page);

                if(summary != null) {
                    System.out.println("page:" + page + " parentId:" + summary.getParentId() +" summary:" + summary.toString());
                    parentId = summary.getId();
                }
                else {
                    System.out.println("page:" + page + " summary null");
                }
            }
            catch (RemoteException e) {
                if(parentId != space.getHomePage()) {
                    System.out.println("page:" + page + " not found creating...");
                    RemotePage remotePage = createPage(session, spaceKey, page, parentId);
                    parentId = remotePage.getId();
                }
                else {
                    System.out.println("page:" + page + " not found, but cannot create first child, so create page with full page.");
                    createPage(session, spaceKey, pages, space.getHomePage());
                }
            }

        }
    }


    /**
     * T1/T2/T3/T4
     */
}
