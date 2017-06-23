package com.example.administrator.wangyi_music.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by Administrator on 2017/6/15.
 */

public class SongPlayCount {
    // how many weeks worth of playback to track
    private static final int NUM_WEEKS = 52;
    private static SongPlayCount sInstance = null;
    // interpolator curve applied for measuring the curve
    private static Interpolator sInterpolator = new AccelerateInterpolator(1.5f);
    // how high to multiply the interpolation curve
    private static int INTERPOLATOR_HEIGHT = 50;
    // how high the base value is. The ratio of the Height to Base is what really matters
    private static int INTERPOLATOR_BASE = 25;
    private static int ONE_WEEK_IN_MS = 1000 * 60 * 60 * 24 * 7;
    private static String WHERE_ID_EQUALS = SongPlayCountColumns.ID + "=?";
    private MusicDB mMusicDatabase = null;
    // number of weeks since epoch time
    private int mNumberOfWeeksSinceEpoch;

    // used to track if we've walkd through the db and updated all the rows
    private boolean mDatabaseUpdated;

    public SongPlayCount(final Context context) {
        mMusicDatabase = MusicDB.getsInstance(context);
        long msSinceEpoch = System.currentTimeMillis();
        mNumberOfWeeksSinceEpoch = (int) (msSinceEpoch / ONE_WEEK_IN_MS);
        mDatabaseUpdated = false;
    }

    public synchronized static final SongPlayCount getsInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SongPlayCount(context.getApplicationContext());
        }
        return sInstance;
    }

    private static float calculateScore(final int[] playCounts) {
        if (playCounts == null) {
            return 0;
        }
        float score = 0;
        for (int i = 0; i < Math.min(playCounts.length, NUM_WEEKS); i++)
            score += playCounts[i] * getScoreMultiplierForWeek(i);
        return score;
    }

    private static float getScoreMultiplierForWeek(final int week) {
        return sInterpolator.getInterpolation(1 - (week / (float) NUM_WEEKS)) * INTERPOLATOR_HEIGHT + INTERPOLATOR_BASE;
    }

    private static String getColumnNameForWeek(final int week) {
        return SongPlayCountColumns.WEEK_PLAY_COUNT + String.valueOf(week);
    }

    private static int getColumnIndexForWeek(final int week) {
        return 1 + week;
    }

    public void onCreate(final SQLiteDatabase db) {
        // create the play count table
        // WARNING: If you change the order of these columns
        // please update getColumnIndexForWeek
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(SongPlayCountColumns.NAME);
        builder.append("(");
        builder.append(SongPlayCountColumns.ID);
        builder.append(" INT UNIQUE,");

        for (int i = 0; i < NUM_WEEKS; i++) {
            builder.append(getColumnNameForWeek(i));
            builder.append(" INT DEFAULT 0,");
        }

        builder.append(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX);
        builder.append(" INT NOT NULL,");

        builder.append(SongPlayCountColumns.PLAYCOUNTSCORE);
        builder.append(" REAL DEFAULT 0);");

        db.execSQL(builder.toString());
    }

    public void bumpSongCount(final long songId) {
        if (songId < 0) return;
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        updateExistingRow(database, songId, true);
    }

    private void createNewPalyedEntry(SQLiteDatabase database, long songId) {
        float newScore = getScoreMultiplierForWeek(0);
        int newPlayCount = 1;
        ContentValues values = new ContentValues(4);
        values.put(SongPlayCountColumns.ID, songId);
        values.put(SongPlayCountColumns.PLAYCOUNTSCORE, newScore);
        values.put(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX, mNumberOfWeeksSinceEpoch);
        values.put(getColumnNameForWeek(0), newPlayCount);
        database.insert(SongPlayCountColumns.NAME, null, values);
    }

    private void updateExistingRow(final SQLiteDatabase database, final long id, boolean bumpCount) {
        String stringId = String.valueOf(id);
        database.beginTransaction();
        final Cursor cursor = database.query(SongPlayCountColumns.NAME, null, WHERE_ID_EQUALS, new String[]{stringId}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int lastUpdatedIndex = cursor.getColumnIndex(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX);
            int lastUpdatedWeek = cursor.getInt(lastUpdatedIndex);
            int weekDiff = mNumberOfWeeksSinceEpoch - lastUpdatedWeek;
            if (Math.abs(weekDiff) >= NUM_WEEKS) {
                deleteEntry(database, stringId);
                if (bumpCount) createNewPalyedEntry(database, id);
            } else if (weekDiff != 0) {
                int[] playCounts = new int[NUM_WEEKS];

                if (weekDiff > 0) {
                    for (int i = 0; i < NUM_WEEKS - weekDiff; i++) {
                        playCounts[i + weekDiff] = cursor.getInt(getColumnIndexForWeek(i));
                    }
                } else if (weekDiff < 0) {
                    for (int i = 0; i < NUM_WEEKS + weekDiff; i++) {
                        playCounts[i] = cursor.getInt(getColumnIndexForWeek(i - weekDiff));
                    }
                }

                if (bumpCount) playCounts[0]++;

                float score = calculateScore(playCounts);
                // if the score is non-existant, then delete it
                if (score < .01f) {
                    deleteEntry(database, stringId);
                } else {
                    // create the content values
                    ContentValues values = new ContentValues(NUM_WEEKS + 2);
                    values.put(SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX, mNumberOfWeeksSinceEpoch);
                    values.put(SongPlayCountColumns.PLAYCOUNTSCORE, score);

                    for (int i = 0; i < NUM_WEEKS; i++) {
                        values.put(getColumnNameForWeek(i), playCounts[i]);
                    }

                    // update the entry
                    database.update(SongPlayCountColumns.NAME, values, WHERE_ID_EQUALS,
                            new String[]{stringId});
                }
            } else if (bumpCount) {
                // else no shifting, just update the scores
                ContentValues values = new ContentValues(2);

                // increase the score by a single score amount
                int scoreIndex = cursor.getColumnIndex(SongPlayCountColumns.PLAYCOUNTSCORE);
                float score = cursor.getFloat(scoreIndex) + getScoreMultiplierForWeek(0);
                values.put(SongPlayCountColumns.PLAYCOUNTSCORE, score);

                // increase the play count by 1
                values.put(getColumnNameForWeek(0), cursor.getInt(getColumnIndexForWeek(0)) + 1);

                // update the entry
                database.update(SongPlayCountColumns.NAME, values, WHERE_ID_EQUALS,
                        new String[]{stringId});
            }

            cursor.close();
        } else if (bumpCount) {
            createNewPalyedEntry(database, id);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void deleteAll() {
        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();
        database.delete(SongPlayCountColumns.NAME, null, null);
    }

    private void deleteEntry(SQLiteDatabase database, String stringId) {
        database.delete(SongPlayCountColumns.NAME, WHERE_ID_EQUALS, new String[]{stringId});
    }
    /**
     * Gets a cursor containing the top songs played.  Note this only returns songs that have been
     * played at least once in the past NUM_WEEKS
     *
     * @param numResults number of results to limit by.  If <= 0 it returns all results
     * @return the top tracks
     */
    public Cursor getTopPlayedResults(int numResults) {
        uupdateResults();

        SQLiteDatabase database = mMusicDatabase.getReadableDatabase();

        return database.query(SongPlayCountColumns.NAME, new String[]{SongPlayCountColumns.ID}, null, null, null, null,
                SongPlayCountColumns.PLAYCOUNTSCORE + "DESC", numResults < 0 ? null : String.valueOf(numResults));
    }

    private synchronized void uupdateResults() {
        if (mDatabaseUpdated) return;

        SQLiteDatabase database = mMusicDatabase.getWritableDatabase();

        database.beginTransaction();

        int oldestWeekWeCareAbout = mNumberOfWeeksSinceEpoch - NUM_WEEKS + 1;

        database.delete(SongPlayCountColumns.NAME, SongPlayCountColumns.LAST_UPDATED_WEEK_INDEX + "<" + oldestWeekWeCareAbout, null);

        Cursor cursor = database.query(SongPlayCountColumns.NAME, new String[]{SongPlayCountColumns.ID}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                updateExistingRow(database, cursor.getLong(0), false);
            } while (cursor.moveToNext());
            cursor.close();
            cursor = null;
        }

        mDatabaseUpdated = true;

        database.setTransactionSuccessful();

        database.endTransaction();
    }

    public void removeItem(final long songId){
        SQLiteDatabase database=mMusicDatabase.getWritableDatabase();

        DeleteEntry(database,String.valueOf(songId));
    }

    private void DeleteEntry(SQLiteDatabase database,String songId){
        database.delete(SongPlayCountColumns.NAME,WHERE_ID_EQUALS,new String[]{songId});
    }

    public interface SongPlayCountColumns {

        /* Table name */
        String NAME = "songplaycount";

        /* Song IDs column */
        String ID = "songid";

        /* Week Play Count */
        String WEEK_PLAY_COUNT = "week";

        /* Weeks since Epoch */
        String LAST_UPDATED_WEEK_INDEX = "weekindex";

        /* Play count */
        String PLAYCOUNTSCORE = "playcountscore";
    }
}
