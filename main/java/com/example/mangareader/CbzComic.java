package com.example.mangareader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CbzComic implements IComic {
    /*
     * The zip archive
     */
    private ZipFile mZip;
    private String mFileName;

    /*
     * Ordered list of the files in the zip archive
     */
    private ArrayList<ZipEntry> mPages;

    /*
     * Constructor
     * @param fileName the filename of the Zip archive file
     */

    public CbzComic(String fileName) {
        mFileName = fileName;
        try {
            // populate mPages with the names of all the ZipEntries
            mPages = new ArrayList<ZipEntry>();
            mZip = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> entries = mZip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (isImageFile(entry)) {
                    mPages.add(entry);
                }
            }
        } catch (IOException e) {
            Log.e(Globals.TAG, "Error opening file", e);
        }
    }

    private boolean isImageFile(ZipEntry entry) {
        // TODO: additional filtering, to check that entry
        // is actually for an image file
        return !entry.isDirectory();
    }
    /*
     * @return Identifier of the comic book
     */
    public String getName() {
        return mFileName;
    }

    /*
     * Close the zip file when we're done.
     */
    @Override
    public void close() {
        if (mZip != null) {
            try {
                mZip.close();
            } catch (IOException e) {
                Log.e(Globals.TAG, "Error closing file", e);
            }
        }
    }

    /*
     * @return number of pages in comic
     */
    @Override
    public int numPages() {
        return mPages.size();
    }

    /*
     *  Retrieve a page's image, full size
     *
     *  @param pageNum (zero based) index of page to fetch
     *  @return the page (or null if unable to fetch page)
     */

    @Override
    public Bitmap getPage(int pageNum) {
        Bitmap bitmap = null;
        try {
            InputStream in = null;
            try {
                in = mZip.getInputStream(mPages.get(pageNum));
                bitmap = BitmapFactory.decodeStream(in);
            }  finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (IOException e) {
            Log.e(Globals.TAG, "Error loading bitmap", e);
        }
        return bitmap;
    }
    /*
     * Retrieve a page's image, scaled down
     * Strictly speaking, this is scope creep, as we could just
     * Fetch the full size image and then scale it down.
     * This method just does it using less memory
     *
     *  @param pageNum (zero based) index of page to fetch
     *  @param maxLength Scale image so that height and width are no longer than this
     *  @return the page (or null if unable to fetch page)
     */
    @Override
    public Bitmap getPageAsThumbnail(int pageNum, int maxLength) {
        Bitmap bitmap = null;
        try {
            InputStream in = null;
            BitmapFactory.Options opt = new BitmapFactory.Options();
            try {
                // get bitmap's dimension
                opt.inJustDecodeBounds = true;
                in = mZip.getInputStream(mPages.get(pageNum));
                BitmapFactory.decodeStream(in, null, opt);
            }  finally {
                if (in != null) {
                    in.close();
                }
            }
            in = null;

            // calculate scaling to get bitmap to desired size
            int scale = (maxLength <= 0) ? 1 : Math.max(opt.outWidth, opt.outHeight) / maxLength;
            opt = new BitmapFactory.Options();
            opt.inSampleSize = (1 < scale) ? scale : 1;

            // get resized bitmap
            try {
                in = mZip.getInputStream(mPages.get(pageNum));
                bitmap = BitmapFactory.decodeStream(in, null, opt);
            }  finally {
                if (in!= null) {
                    in.close();
                }
            }
        } catch (IOException e) {
            Log.e(Globals.TAG, "Error loading bitmap", e);
        }
        return bitmap;
    }
}