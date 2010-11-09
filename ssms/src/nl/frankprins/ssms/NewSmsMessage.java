/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Frank
 */
public class NewSmsMessage extends Activity {

  public static final int PICK_CONTACT = 1;
  private static final String TAG = "SSMS-Debug";
  EditText numberInput, messageBody;
  TextView counter;
  int charCounter = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_sms_form);
    Button btnContacts = (Button) findViewById(R.id.select);
    numberInput = (EditText) findViewById(R.id.entry);
    btnContacts.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View arg0) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
      }
    });
    messageBody = (EditText) findViewById(R.id.msgBody);
    counter = (TextView) findViewById(R.id.msgCharCount);
    messageBody.addTextChangedListener(new TextWatcher() {

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        // I left empty
      }

      public void beforeTextChanged(CharSequence s, int start, int count,
              int after) {
        // I left empty
      }

      public void afterTextChanged(Editable s) {
        counter.setText(String.valueOf(s.length()));
      }
    });

  }

  @Override
  public void onActivityResult(int reqCode, int resultCode, Intent data) {
    super.onActivityResult(reqCode, resultCode, data);
    List<String> numberList = new ArrayList();
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
                numberList.add(number);
                Log.v(TAG, "number: " + number);
              }
              pCur.close();
            }
          }
        }
        break;
    }
    if (numberList.size() > 1) {
      final String[] numberArray = numberList.toArray(new String[numberList.size()]);
      Builder builder = new AlertDialog.Builder(NewSmsMessage.this);
      builder.setTitle("Found " + numberList.size() + " numbers, pick the correct one!");
      builder.setSingleChoiceItems(numberArray, -1, new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int item) {
          numberInput.setText(numberArray[item]);
          dialog.dismiss();
        }
      });

      builder.show();
    }

  }
}
