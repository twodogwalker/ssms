/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;

/**
 *
 * @author Frank
 */
public class Utilities {

  public static int countNumberOfMessages(Context context, String smsBox, boolean unread) {
    if (smsBox == null) {
      return 0;
    }
    final Uri boxUri = Uri.parse("content://sms/" + smsBox.toLowerCase());
    String UNREAD_CONDITION = null;
    if (unread) {
      UNREAD_CONDITION = "read=0";
    }
    int count = 0;
    Cursor cursor = context.getContentResolver().query(boxUri,
            new String[]{"_id"}, UNREAD_CONDITION, null, null);
    if (cursor != null) {
      try {
        count = cursor.getCount();
      } finally {
        cursor.close();
      }
    }
    return count;
  }

  public static String getPersonNameFromNumber(Context context, String box,
          String address) {
    if (address == null) {
      return "unknown";
    }
    if (!box.equalsIgnoreCase("draft")) {
      Cursor cursor = context.getContentResolver().query(
              Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)),
              new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
      if (cursor != null) {
        try {
          if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String name = cursor.getString(0);
            return name;
          }
        } finally {
          cursor.close();
        }
      }
    }
    if (address != null) {
      return PhoneNumberUtils.formatNumber(address);
    }
    return "unknown";
  }
}
