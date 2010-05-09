/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Frank
 */
public class NewSmsMessage extends Activity {

  public static final int PICK_CONTACT = 1;
  private static final String TAG = "SSMS-Debug";
  Spinner txtContacts;
  ArrayAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_sms_form);
    Button btnContacts = (Button) findViewById(R.id.select);
    txtContacts = (Spinner) findViewById(R.id.entry);
    adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    txtContacts.setAdapter(adapter);
    btnContacts.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
      }
    });
  }

  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
    super.onActivityResult(reqCode, resultCode, data);
    List<String> numbers = new ArrayList();
    switch (reqCode) {
      case (PICK_CONTACT):
        if (resultCode == Activity.RESULT_OK) {
          Uri contactData = data.getData();
          Cursor c = managedQuery(contactData, null, null, null, null);
          if (c.moveToFirst()) {
            String ctcId = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
              Cursor pCur = getContentResolver().query(
                      ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                      null,
                      ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                      new String[]{ctcId}, null);
              while (pCur.moveToNext()) {
                String number = pCur.getString(
                        pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                numbers.add(number);
                Log.v(TAG, "number: " + number);
              }
              pCur.close();
            }
          }
        }
        break;
    }
    if (adapter.getCount() > 1) {
      final CharSequence[] items = (CharSequence[]) numbers.toArray();

      AlertDialog.Builder builder = new AlertDialog.Builder(NewSmsMessage.this);
      builder.setTitle("Found " + adapter.getCount() + " numbers, pick the correct one!");
      builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int item) {
          Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_SHORT).show();
        }
      });
      AlertDialog alert = builder.create();
    }

  }
}
