package net.littlelite.aledana;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SetAvailabilityActivity extends Activity
{

    private static final int MIN_HOURS = 1;
    private static final int MAX_HOURS = 5;

    private int howManyHours;
    private String availMode;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);
        this.initUI();
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

    private void initUI()
    {
        // Init members
        this.availMode = "Al telefono";
        this.howManyHours = 1;

        // Radio group: availability mode
        RadioGroup radGroup = (RadioGroup) findViewById(R.id.radGroup);
        radGroup.check(R.id.rad_item_altelefono);
        radGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.rad_item_altelefono:
                        availMode = "Al telefono";
                        break;
                    case R.id.rad_item_whatsapp:
                        availMode = "SMS e WhatsApp";
                        break;
                    case R.id.rad_item_nondisponibile:
                        availMode = "Non sono disponibile";
                        break;
                }
            }

        });

        // Seekbar: how many hours
        SeekBar seekbar = (SeekBar)this.findViewById(R.id.seekHowManyHours);
        seekbar.setMax((this.MAX_HOURS - this.MIN_HOURS) / 1);
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)
                    {
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar)
                    {
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser)
                    {
                        TextView perLeProssime = (TextView) findViewById(R.id.textProssime);
                        // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3
                        double value = MIN_HOURS + (progress * 1);
                        howManyHours = ((int) value);
                        StringBuilder sb = new StringBuilder();

                        if (howManyHours > 1)
                        {
                            sb.append("per le prossime ");
                            sb.append(howManyHours);
                            sb.append(" ore.");
                        }
                        else
                        {
                            sb.append("per la prossima ");
                            sb.append("ora.");
                        }

                        perLeProssime.setText(sb.toString());
                    }
                }
        );
    }


    public void setAvailability(View view)
    {
        String availTime = Integer.valueOf(howManyHours).toString();
        availTime = availTime.substring(0, 1);

        EditText messageText = (EditText) findViewById(R.id.editMessaggioOpzionale);
        String lastMessage = messageText.getText().toString();

        Log.d(Logic.TAG, "Avail mode = " + this.availMode);
        Log.d(Logic.TAG, "For hours = " + availTime);

        if (this.availMode != null)
        {
            this.setActivityResult(this.availMode.toLowerCase(), availTime, lastMessage);
            this.finish();
        }
        else
        {
            Log.e(Logic.TAG, "ERROR: Incorrect service params!!!");
        }


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

}
