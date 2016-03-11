package com.rssreader.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import com.rssreader.MainApplication;
import com.rssreader.provider.FeedData;
import com.rssreader.utils.HtmlUtils;
import com.rssreader.utils.ImageUtils;
import com.rssreader.utils.TimeUtils;

public class RSSAtomParser {

    private static final Context context = MainApplication.getContext();
    private static final RSSParser parser = new RSSParser();

    public static void parseRSSFile(String rss) throws IOException, SAXException {
        Xml.parse(rss, parser);
    }

    public static class RSSParser extends DefaultHandler {

        private static final String TAG_TITLE = "title";
        private static final String TAG_LINK = "link";
        private static final String TAG_IMAGE = "image";
        private static final String TAG_DESCRIPTION = "description";
        private static final String TAG_ENCODED_CONTENT = "encoded";
        private static final String TAG_URL = "url";
        private static final String TAG_ENCLOSURE = "enclosure";
        private static final String TAG_PUBDATE = "pubDate";
        private static final String TAG_UPDATED = "updated";
        private static final String TAG_CONTENT = "content";
        private static final String TAG_ITEM = "item";
        private static final String TAG_ENTRY = "entry";

        private static final String ATTRIBUTE_HREF = "href";
        private static final String URL_ATTRIBUTE = "url";

        private String feedTitle;
        private String feedLink;
        private Date feedDate;
        private String feedImage;

        private StringBuilder mImageUrl;
        private StringBuilder mTitle;
        private StringBuilder mDescription;
        private StringBuilder mLink;
        private StringBuilder mDate;

        private boolean titleTag;
        private boolean feedLinkTag;
        private boolean imageUrlTag;
        private boolean descriptionTag;
        private boolean pubDateTag;

        private static int feedId;

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            switch (localName) {
                case TAG_TITLE:
                    titleTag = true;
                    mTitle = new StringBuilder();
                    break;
                case TAG_LINK: {
                    String href = attributes.getValue("", ATTRIBUTE_HREF);
                    mLink = new StringBuilder();
                    if (!TextUtils.isEmpty(href)) {
                        mLink.append(href);
                        feedLinkTag = false;
                    } else {
                        feedLinkTag = true;
                    }
                    feedLinkTag = true;
                    break;
                }
                case TAG_IMAGE: {
                    String href = attributes.getValue("", ATTRIBUTE_HREF);
                    if (mImageUrl == null && href != null) {
                        mImageUrl = new StringBuilder();
                        if (ImageUtils.isCorrectImage(href)) {
                            mImageUrl.append(href);
                        }
                    }
                    break;
                }
                case TAG_DESCRIPTION:
                case TAG_CONTENT:
                case TAG_ENCODED_CONTENT:
                    descriptionTag = true;
                    mDescription = new StringBuilder();
                    break;
                case TAG_URL:
                    mImageUrl = new StringBuilder();
                    imageUrlTag = true;
                    break;
                case TAG_ENCLOSURE:
                    String url = attributes.getValue("", URL_ATTRIBUTE);
                    if (url != null && ImageUtils.isCorrectImage(url)) {
                        mImageUrl = new StringBuilder();
                        mImageUrl.append(attributes.getValue("", URL_ATTRIBUTE));
                    }
                    break;
                case TAG_PUBDATE:
                    pubDateTag = true;
                    mDate = new StringBuilder();
                    break;
                case TAG_UPDATED:
                    pubDateTag = true;
                    mDate = new StringBuilder();
                    break;
                case TAG_ITEM:
                case TAG_ENTRY:
                    if (feedTitle == null) {
                        feedTitle = mTitle.toString();
                        mTitle = null;
                    }
                    if (feedLink == null) {
                        feedLink = mLink.toString();
                        mLink = null;
                    }
                    if (feedDate == null && mDate != null) {
                        feedDate = TimeUtils.parseUpdateDate(mDate.toString(), true);
                        mDate = null;
                    }
                    if (feedImage == null && mImageUrl != null) {
                        feedImage = mImageUrl.toString();
                        mImageUrl = null;
                    }
                    break;
            }
            super.startElement(uri, localName, qName, attributes);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            switch (localName) {
                case TAG_TITLE:
                    titleTag = false;
                    break;
                case TAG_LINK:
                    feedLinkTag = false;
                    break;
                case TAG_DESCRIPTION:
                case TAG_CONTENT:
                case TAG_ENCODED_CONTENT:
                    if (mDescription != null) {
                        String improvedContent = HtmlUtils.improveHtmlContent(mDescription.toString());
                        List<String> imagesUrls = HtmlUtils.getImageURLs(improvedContent);
                        if (!imagesUrls.isEmpty() && TextUtils.isEmpty(mImageUrl)) {
                            String mainImage = HtmlUtils.getMainImageURL(imagesUrls);
                            if (mainImage != null && ImageUtils.isCorrectImage(mainImage)) {
                                mImageUrl = new StringBuilder();
                                mImageUrl.append(HtmlUtils.getMainImageURL(imagesUrls));
                            }
                        }
                    }
                    descriptionTag = false;
                    break;
                case TAG_URL:
                    imageUrlTag = false;
                    break;
                case TAG_PUBDATE:
                    pubDateTag = false;
                    break;
                case TAG_UPDATED:
                    pubDateTag = false;
                    break;
                case TAG_ITEM:
                case TAG_ENTRY:
                    Date entryTime = TimeUtils.parsePubdateDate(mDate != null ? mDate.toString() : null, true);
                    ContentValues values = new ContentValues();
                    values.put(FeedData.FeedEntries.ID_REF, feedId);
                    values.put(FeedData.FeedEntries.NAME, mTitle != null ? mTitle.toString() : null);
                    values.put(FeedData.FeedEntries.URL, mLink != null ? mLink.toString() : null);
                    values.put(FeedData.FeedEntries.DESCRIPTION, mDescription != null ? mDescription.toString() : null);
                    values.put(FeedData.FeedEntries.PUB_DATE, entryTime != null ? entryTime.getTime() : 0);
                    values.put(FeedData.FeedEntries.IMAGE_URL, mImageUrl != null ? mImageUrl.toString() : null);

                    ContentResolver cr = context.getContentResolver();
                    cr.insert(FeedData.FeedEntries.CONTENT_URI, values);

                    clearEntryTags();
                    break;
            }

            super.endElement(uri, localName, qName);
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            clearFeedTags();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (titleTag) {
                mTitle.append(ch, start, length);
            } else if (feedLinkTag) {
                mLink.append(ch, start, length);
            } else if (imageUrlTag) {
                mImageUrl.append(ch, start, length);
            } else if (descriptionTag) {
                mDescription.append(ch, start, length);
            } else if (pubDateTag) {
                mDate.append(ch, start, length);
            }
            super.characters(ch, start, length);
        }

        private void clearFeedTags() {
            feedId = 0;
            feedTitle = null;
            feedLink = null;
            mImageUrl = null;

            feedImage = null;
            feedLink = null;
            feedDate = null;
            feedTitle = null;
        }

        private void clearEntryTags() {
            mLink = null;
            mDate = null;
            mTitle = null;
            mImageUrl = null;
            mDescription = null;
        }

        public static void setFeedId(int id) {
            feedId = id;
        }
    }
}


