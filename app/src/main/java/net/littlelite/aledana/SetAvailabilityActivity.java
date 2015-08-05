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
        Spinner spinner1 = (Spinner)findViewById(R.id.spinnerAvailMode);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.availability_mode, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);

        Spinner spinner2 = (Spinner)findViewById(R.id.spinnerHowManyHours);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.availability_hours, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
    }

    public void setAvailability(View view)
    {
        Spinner spinner1 = (Spinner)findViewById(R.id.spinnerAvailMode);
        Spinner spinner2 = (Spinner)findViewById(R.id.spinnerHowManyHours);

        String availType = spinner1.getSelectedItem().toString();
        String availTime = spinner2.getSelectedItem().toString();

        Context context = getApplicationContext();
        CharSequence text = availType + " per " + availTime;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void backHome(View view)
    {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }
}
