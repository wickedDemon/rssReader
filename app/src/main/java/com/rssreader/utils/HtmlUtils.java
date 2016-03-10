package com.rssreader.utils;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtils {

    private static final Pattern IMG_PATTERN = Pattern.compile("<img\\s+[^>]*src=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ADS_PATTERN = Pattern.compile("<div class=('|\")mf-viral('|\")><table border=('|\")0('|\")>.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern LAZY_LOADING_PATTERN = Pattern.compile("\\s+src=[^>]+\\s+original[-]*src=(\"|')", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY_IMAGE_PATTERN = Pattern.compile("<img\\s+(height=['\"]1['\"]\\s+width=['\"]1['\"]|width=['\"]1['\"]\\s+height=['\"]1['\"])\\s+[^>]*src=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern NON_HTTP_IMAGE_PATTERN = Pattern.compile("\\s+(href|src)=(\"|')//", Pattern.CASE_INSENSITIVE);
    private static final Pattern BAD_IMAGE_PATTERN = Pattern.compile("<img\\s+[^>]*src=\\s*['\"]([^'\"]+)\\.img['\"][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern START_BR_PATTERN = Pattern.compile("^(\\s*<br\\s*[/]*>\\s*)*", Pattern.CASE_INSENSITIVE);
    private static final Pattern END_BR_PATTERN = Pattern.compile("(\\s*<br\\s*[/]*>\\s*)*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTIPLE_BR_PATTERN = Pattern.compile("(\\s*<br\\s*[/]*>\\s*){3,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY_LINK_PATTERN = Pattern.compile("<a\\s+[^>]*></a>", Pattern.CASE_INSENSITIVE);


    private static final String URL_SPACE = "%20";

    public static String improveHtmlContent(String content) {
        content = ADS_PATTERN.matcher(content).replaceAll("");

        if (content != null) {
            // remove some ads
            content = ADS_PATTERN.matcher(content).replaceAll("");
            // remove lazy loading images stuff
            content = LAZY_LOADING_PATTERN.matcher(content).replaceAll(" src=$1");

            // remove empty or bad images
            content = EMPTY_IMAGE_PATTERN.matcher(content).replaceAll("");
            content = BAD_IMAGE_PATTERN.matcher(content).replaceAll("");
            // remove empty links
            content = EMPTY_LINK_PATTERN.matcher(content).replaceAll("");
            // fix non http image paths
            content = NON_HTTP_IMAGE_PATTERN.matcher(content).replaceAll(" $1=$2http://");
            // remove trailing BR & too much BR
            content = START_BR_PATTERN.matcher(content).replaceAll("");
            content = END_BR_PATTERN.matcher(content).replaceAll("");
            content = MULTIPLE_BR_PATTERN.matcher(content).replaceAll("<br><br>");
        }

        return content;
    }

    public static ArrayList<String> getImageURLs(String content) {
        ArrayList<String> images = new ArrayList<>();

        if (!TextUtils.isEmpty(content)) {
            Matcher matcher = IMG_PATTERN.matcher(content);

            while (matcher.find()) {
                images.add(matcher.group(1).replace(" ", URL_SPACE));
            }
        }

        return images;
    }

    public static String getMainImageURL(ArrayList<String> imgUrls) {
        for (String imgUrl : imgUrls) {
            if (ImageUtils.isCorrectImage(imgUrl)) {
                return imgUrl;
            }
        }

        return null;
    }
}
