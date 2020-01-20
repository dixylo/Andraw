package com.unitec.xiaofenglin.andraw;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    CanvasView canvasView; // creates the view where drawing happens
    SeekBar sbBrushSize; // the seek bar to adjust the brush size with
    Switch swtColorMode; // the switch that sets the color mode
    TextView tvBrushSize; // the label to show the current brush size
    Button btnUndo; // undo button
    Button btnRedo; // redo button
    Button btnClear; // canvas-clearing button
    AlertDialog alertUndo; // creates a warning when no more undos can be done
    AlertDialog alertRedo; // creates a warning when no more redos can be done
    AlertDialog alertClear; // creates a warning when no content can be cleared
    AlertDialog alertConfirm; // creates a prompt asking for a confirmation on clearance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        canvasView = (CanvasView) findViewById(R.id.canvasView); // Must be instantiated after setContentView!
        btnUndo = (Button) findViewById(R.id.btnUndo);
        btnRedo = (Button) findViewById(R.id.btnRedo);
        btnClear = (Button) findViewById(R.id.btnClear);
        seekBar();
        switchColorMode();
        createAlert();

    }

    // Create a seek bar and a listener
    public void seekBar()
    {
        sbBrushSize = (SeekBar) findViewById(R.id.sbBrushSize);
        tvBrushSize = (TextView) findViewById(R.id.tvBrushSize);
        tvBrushSize.setText("Brush Size: " + sbBrushSize.getProgress() + " dp"); // sets the default label text

        // Create a listener on the change of seek bar progress
        sbBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int barValue;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                barValue = i; // reads the progress
                tvBrushSize.setText("Brush Size: " + barValue + " dp");
                canvasView.setBrushSize(barValue); // sets the brush size

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    // Create a switch for toggling the color mode
    public void switchColorMode()
    {
        swtColorMode = (Switch) findViewById(R.id.swtColorMode);
        // Create a listener on the change of the switch
        swtColorMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                canvasView.setColorRandom(isChecked); // sets the color mode
                // Show the current mode
                if (isChecked)
                {
                    swtColorMode.setText("Random Color");
                } else
                {
                    swtColorMode.setText("Single Color");
                }
            }
        });
    }

    // When the undo button is tapped
    public void undo(View view)
    {
        if (!canvasView.undoable())
        {
            alertUndo.show();
        }
    }

    // When the redo button is tapped
    public void redo(View view)
    {
        if (!canvasView.redoable())
        {
            alertRedo.show();
        }
    }

    // When the clear button is tapped
    public void clear(View view)
    {
        if (canvasView.clearable())
        {
            alertConfirm.show();
        }
        else
        {
            alertClear.show();
        }
    }

    // Create all alerts that will be used
    public void createAlert()
    {
        // Create an undo alert
        AlertDialog.Builder builderUndo = new AlertDialog.Builder(this);
        builderUndo.setTitle("Warning");
        builderUndo.setMessage("No stroke can be undone!");
        builderUndo.setCancelable(true);

        builderUndo.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertUndo = builderUndo.create();

        // Create a redo alert
        AlertDialog.Builder builderRedo = new AlertDialog.Builder(this);
        builderRedo.setTitle("Warning");
        builderRedo.setMessage("No stroke can be redone!");
        builderRedo.setCancelable(true);

        builderRedo.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertRedo = builderRedo.create();

        // Create a clear alert
        AlertDialog.Builder builderClear = new AlertDialog.Builder(this);
        builderClear.setTitle("Warning");
        builderClear.setMessage("No content can be cleared!");
        builderClear.setCancelable(true);

        builderClear.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertClear = builderClear.create();

        // Create a clear confirmation alert
        AlertDialog.Builder builderConfirm = new AlertDialog.Builder(this);
        builderConfirm.setTitle("Warning");
        builderConfirm.setMessage("Are you sure you want to clear the canvas?");
        builderConfirm.setCancelable(true);

        builderConfirm.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        canvasView.clear();
                        dialog.cancel();
                    }
                });

        builderConfirm.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        alertConfirm = builderConfirm.create();
    }
}
