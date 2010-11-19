/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.frankprins.ssms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.DialogInterface;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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
  EditText numberInput, messageBody;
  TextView counter;
  int charCounter = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_sms_form);
    numberInput = (EditText) findViewById(R.id.entry);
    // if this is a reply, get the number to reply to
    Bundle extras = getIntent().getExtras();
    String numberToReplyTo = "";
    if (extras != null) {
      numberToReplyTo = extras.getString("nl.frankprins.ssms.replynumber");
      if (numberToReplyTo != null) {
        numberInput.setText(numberToReplyTo, TextView.BufferType.NORMAL);
      }
    }
    Button btnContacts = (Button) findViewById(R.id.select);

    btnContacts.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
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
    Button btnSend = (Button) findViewById(R.id.sendButton);
    btnSend.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        int result = 0;
        if (messageBody.getText().length() < 160) {
          result = sendMessage(numberInput.getText().toString(), messageBody.getText().toString());
        } else {
          // TODO: localize message to user
          // TODO: handle splitting of message
          new AlertDialog.Builder(NewSmsMessage.this).setMessage("message is too long for one SMS").show();
        }
        if (result == 1) {
          // TODO: save message in outbox here
          // TODO: localize message to user
          Toast.makeText(NewSmsMessage.this, "SMS message sent", Toast.LENGTH_LONG).show();
          finish();
        } else {
          new AlertDialog.Builder(
                  // TODO: localize message to user
                  NewSmsMessage.this).setMessage("Error while sending SMS message!").show();
          finish();
        }
      }
    });
  }

  private int sendMessage(String recipient, String bodyText) {
    PendingIntent pi = PendingIntent.getActivity(
            NewSmsMessage.this,
            0,
            new Intent(NewSmsMessage.this, NewSmsMessage.class),
            0);
    SmsManager sms = SmsManager.getDefault();
    Log.v(TAG, "preparing message for recipient: " + recipient);
    //sms.sendTextMessage(recipient, null, bodyText, pi, null);
    return 1;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      new AlertDialog.Builder(
              // TODO: create a possibility to save as draft message
              NewSmsMessage.this).setMessage("Are you sure you want to leave this message?").show();
    }
    return super.onKeyDown(keyCode, event);
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
      // TODO: localize message to user
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
