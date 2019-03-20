package com.ahmed;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;


public class Main {
    /*https://www.animefreak.tv/watch/yu-gi-oh-duel-monsters/episode/episode-3*/

    static WebClient webClient;
    public static void main(String[] args) {
        if (args.length != 3) {
            print("required args: [url] [begin episode] [end episode]");
            System.exit(1);
        }
        int beginEpisode = Integer.parseInt(args[1]);
        int endEpisode = Integer.parseInt(args[2]);
        String urlString = args[0] + "/episode";

        print("url = " + urlString);
        print("episodes = " + (endEpisode - beginEpisode + 1));

        webClient = new WebClient();

        String animeFolder =  urlString.substring(urlString.indexOf("watch/")+6, urlString.indexOf("/episode")-1).toUpperCase();
        File file = new File("/media/ahmed/New Volume/Movies/Animation", animeFolder);
        print("animeFolder = " + animeFolder);
        boolean mkdir = file.mkdir();
        if(!mkdir && !file.exists()){
            print("Error making directory");
            System.exit(-1);
        }

        for (int episode = beginEpisode; episode <= endEpisode; episode++) {
            URL url = null;
            try {
                url = new URL(urlString + "/episode-" + episode);
                print("url = " + url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            try {
                String videoUrl = getVideoUrl(webClient, url.toString());
                print("video link = " + videoUrl);
                if(videoUrl == null) System.exit(2);

                URL url1 = new URL(videoUrl);
                HttpURLConnection httpsURLConnection = (HttpURLConnection) url1.openConnection();
                int status = httpsURLConnection.getResponseCode();
                print("status = " + status);
                if (status != HttpsURLConnection.HTTP_OK) {
                    print("Error downloading episode " + episode);
                    continue;
                }

                InputStream inputStream = url1.openStream();
                String fileName = videoUrl.substring(videoUrl.indexOf(" "), videoUrl.indexOf("mp4") + 3);

                File animeFile = new File(file, fileName);
                if (animeFile.exists() && getFileSize(animeFile) > 23) {
                    print(fileName + " exists");
                    continue;
                }
                print("Downloading: " + fileName);
                print(animeFile.getAbsolutePath());
                FileOutputStream outputStream = new FileOutputStream(animeFile);

                int bytesRead = -1, line = 0;
                byte[] buffer = new byte[6144];
                int count = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    count++;
                    System.out.print('.');
                    outputStream.write(buffer, 0, bytesRead);
                    if(count>100){
                        System.out.println();
                        System.out.print(++line);
                        count = 0;
                    }
                }
                System.out.println();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
    private static String getVideoUrl(WebClient webClient, String url) {
        try {
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = webClient.getPage(url);
//            print("page = " + page.asXml());
            List<HtmlElement> parent = page.getBody().getElementsByAttribute("div", "class", "container");
            if(parent.size() == 0) {
                print("parent = 0");
                System.exit(1);
            }
//            print("parent = " + parent.get(0).asXml());
            List<HtmlElement> parent1 = parent.get(0).getElementsByAttribute("div", "class", "centerAligner");
            if(parent1.size()==0){
                print("parent1 = 0");
                System.exit(1);
            }
            List<HtmlElement> videoTag = parent.get(0).getElementsByAttribute("div", "class", "video-main-new watchFull");
            if (videoTag.size() == 0) {
                print("videoTag = 0");
                System.exit(1);
            }
            List<HtmlElement> tag = videoTag.get(0).getElementsByAttribute("div", "class", "vmn-ct");
            if (tag.size() == 0) {
                print("tag = 0");
                System.exit(1);
            }
            List<HtmlElement> tag1 = tag.get(0).getElementsByAttribute("div", "class", "vmn-player");
            if (tag1.size() == 0) {
                print("tag1 = 0");
                System.exit(1);
            }
            List<HtmlElement> parentV = tag1.get(0).getElementsByAttribute("div", "class", "vmn-video");
            if (parentV.size() == 0) {
                print("parentV = 0");
                System.exit(1);
            }
            for (HtmlElement element : parentV.get(0).getHtmlElementsByTagName("script")) {
                String text = element.asXml();
                String textL = text.toLowerCase();
                String videoUrl;
                boolean cont = textL.contains("anime1.com");
                if (cont) {
                    String httpText = text.substring(text.indexOf("http"));
                    videoUrl = httpText.substring(0, httpText.indexOf("\""));
                    return videoUrl;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void print(String message) {
        System.out.println(message);
    }
    private static long getFileSize(File file){
        return file.length()/1024L/1024L;
    }
}
