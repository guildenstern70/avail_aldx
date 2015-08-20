package net.littlelite.aledana;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SetAvailabilityActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);
        this.initSpinners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initSpinners()
    {
        Spinner spinner1 = (Spinner) findViewById(R.id.spinnerAvailMode);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.availability_mode, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        Spinner spinner2 = (Spinner) findViewById(R.id.spinnerHowManyHours);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.availability_hours, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
    }

    public void setAvailability(View view)
    {
        Spinner spinner1 = (Spinner) findViewById(R.id.spinnerAvailMode);
        Spinner spinner2 = (Spinner) findViewById(R.id.spinnerHowManyHours);

        String availType = spinner1.getSelectedItem().toString();
        String availTime = spinner2.getSelectedItem().toString();
        availTime = availTime.substring(0, 1);

        EditText messageText = (EditText) findViewById(R.id.editMessaggioOpzionale);
        String lastMessage = messageText.getText().toString();

        this.setActivityResult(availType.toLowerCase(), availTime, lastMessage);
        this.finish();
    }

    private void setActivityResult(String availType, String hours, String message)
    {
        if (message == null)
            message = "";
        Intent output = new Intent();
        output.putExtra("AVAIL_TYPE", availType);
        output.putExtra("AVAIL_HOURS", hours);
        output.putExtra("AVAIL_MESSAGE", message);
        this.setResult(RESULT_OK, output);
    }

    public void backHome(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }
}
